package nu.mine.mosher.gedcom;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import nu.mine.mosher.*;
import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.*;
import nu.mine.mosher.logging.Jul;
import spark.*;
import nu.mine.mosher.template.TemplAtEngine;

import java.io.IOException;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;
import static nu.mine.mosher.logging.Jul.log;
import static spark.Spark.*;

/**
 * @author Chris Mosher
 * Created 2006-09-24.
 */
public class GedcomWebView {
    private static String googleClientID() {
        return System.getenv("CLIENT_ID");
    }

    private static final NetHttpTransport TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JACKSON = JacksonFactory.getDefaultInstance();

    public static void main(final String... args) {
        Jul.setLevel(Level.INFO);
        log().entering("Main", "main");

        try {
            new GedcomWebView().run();
        } catch (final Throwable e) {
            Jul.thrown(e);
        }

        log().exiting("Main", "main");
        System.out.flush();
        System.err.flush();
    }



    private final GedcomFilesHandler files;

    private GedcomWebView() throws IOException, InvalidLevel {
        this.files = new GedcomFilesHandler();
    }



    private void run() {
        staticFiles.location("/public");
        staticFiles.expireTime(600);


        before(this::backwardCompatibility);

        redirect.get("", "/");
        get("/", (req, res) -> index());

        get("/favicon.ico", (req, res) -> null);

        path("/persons", () -> {
            redirect.get("", "persons/");
            get("/:id", (req, res) -> findGedcom(res, Util.uuidFromString(req.params(":id"))));
        });

        path("/:ged", () -> {
            path("/persons", () -> {
                redirect.get("", "persons/");
                get("/", (req, res) -> personIndex(res, auth(req), req.params(":ged")));
                get("/:id", (req, res) -> person(res, auth(req), req.params(":ged"), Util.uuidFromString(req.params(":id"))));
            });
        });
    }

    private String findGedcom(final Response res, final UUID uuid) {
        final Optional<Loader> loader= this.files.findLoaderForPerson(uuid);
        if (loader.isPresent()) {
            res.redirect("../"+loader.get().getName() + "/persons/" + uuid);
        } else {
            res.status(SC_NOT_FOUND);
        }
        return "";
    }

    private static RbacRole auth(final Request req) {
        try {
            final String idStringOrNull = req.cookie("idtoken");
            if (idStringOrNull == null || idStringOrNull.isEmpty()) {
                throw new GeneralSecurityException("error");
            }
            final GoogleIdToken idTokenOrNull = tokenVerifier().verify(idStringOrNull);
            if (idTokenOrNull == null) {
                throw new GeneralSecurityException("error");
            }
            final String email = idTokenOrNull.getPayload().getEmail();
            return new RbacRole(true, emailIsAuthorized(email));
        } catch (Throwable e) {
            return new RbacRole(false, false);
        }
    }

    private static GoogleIdTokenVerifier tokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(TRANSPORT, JACKSON).setAudience(Collections.singleton(googleClientID())).build();
    }

    private static boolean emailIsAuthorized(final String email) {
        if (Objects.isNull(email) || email.isEmpty()) {
            return false;
        }

        final boolean authorized = emailIsInFile(email);
        if (authorized) {
            log().warning("Authorizing user: " + email);
        }
        return authorized;
    }

    private static boolean emailIsInFile(String email) {
        try {
            return Files.lines(Paths.get("gedcom/SERVE_PUBLIC_GED_FILES")).collect(Collectors.toSet()).contains(email);
        } catch (final Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private void backwardCompatibility(final Request req, final Response res) {
        final String sUuidPerson = getOldFormatPersonUuidOrEmpty(req);
        if (!sUuidPerson.isEmpty()) {
            try {
                final UUID uuidPerson = UUID.fromString(sUuidPerson);
                log().info("Searching for old uuid: "+uuidPerson);
                final Optional<Loader> loader= this.files.findLoaderForPerson(uuidPerson);
                if (loader.isPresent()) {
                    log().info("found in "+loader.get().getName());
                    res.redirect(loader.get().getName() + "/persons/" + uuidPerson, SC_MOVED_PERMANENTLY);
                } else {
                    log().info("not found; ignoring");
                }
            } catch (final Throwable bad) {
                // bad UUID format, OK just ignore it
                log().info("Invalid uuid value on person_uuid or personfam_uuid query parameter; ignoring.");
            }
        }
    }

    private static String getOldFormatPersonUuidOrEmpty(final Request req) {
        String uuidOrEmpty = req.queryParamOrDefault("person_uuid", "");
        if (uuidOrEmpty.isEmpty()) {
            uuidOrEmpty = req.queryParamOrDefault("personfam_uuid", "");
        }
        return uuidOrEmpty;
    }



    private String index() {
        return render("index.tat", this.files.getFiles(), ".", googleClientID());
    }

    private String personIndex(final Response res, final RbacRole auth, final String gedcomName) {
        final List<Person> people = this.files.getAllPeople(gedcomName);
        if (people.isEmpty()) {
            res.status(SC_NOT_FOUND);
            return "";
        }
        final String copyright = this.files.getCopyright(gedcomName);
        return render("personIndex.tat", people, gedcomName, copyright, "../..", auth, googleClientID());
    }

    private String person(final Response res, final RbacRole auth, String gedcomName, final UUID uuid) {
        final Optional<Person> person = this.files.getPerson(gedcomName, uuid);
        if (!person.isPresent() || Util.privatize(person.get(), auth)) {
            res.status(SC_NOT_FOUND);
            return "";
        }
        final List<String> otherFiles = this.files.getXrefs(gedcomName, uuid);
        final NoteList footnotes = GedcomFilesHandler.getFootnotesFor(person.get());
        return render("person.tat", person, gedcomName, otherFiles, footnotes, "../..", auth, googleClientID());
    }



    private static String render(final String view, final Object... args) {
        return new TemplAtEngine().render(new ModelAndView(args, view));
    }
}
