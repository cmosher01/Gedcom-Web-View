package nu.mine.mosher.gedcom;

import nu.mine.mosher.*;
import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.*;
import nu.mine.mosher.logging.Jul;
import spark.*;
import template.TemplAtEngine;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static javax.servlet.http.HttpServletResponse.*;
import static nu.mine.mosher.logging.Jul.log;
import static spark.Spark.*;

/**
 * @author Chris Mosher
 * Created 2006-09-24.
 */
public class GedcomWebView {
    private static final Credentials.Store credentialsStore = GuestStoreImpl.instance();

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

        get("/login", this::logIn);

        path("/:ged", () -> {
            path("/persons", () -> {
                redirect.get("", "persons/");
                get("/", (req, res) -> personIndex(res, auth(req), req.params(":ged")));
                get("/:id", (req, res) -> person(res, auth(req), req.params(":ged"), Util.uuidFromString(req.params(":id"))));
            });
            //TODO: privatize dropline chart:
//            path("/chart", () -> {
//                redirect.get("", "chart/");
//                get("/", (req, res) -> personChart(auth(req), req.params(":ged")));
//                get("/data", (req, res) -> personChartData(auth(req), req.params(":ged"), res));
//                redirect.get("/dropline.css", "/genealogy/css/dropline.css");
//            });
        });
    }

    private String logIn(final Request req, final Response res) {
        if (auth(req)) {
            res.redirect(req.headers("Referer"), SC_MOVED_TEMPORARILY);
        } else {
            res.status(SC_UNAUTHORIZED);
            res.header("WWW-Authenticate", "Basic realm=\"web site\"");
        }
        return "Unauthorized. Please quit your browser.";
    }

    private static boolean auth(final Request req) {
        return Credentials.fromSession(req, credentialsStore).valid();
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
        final Object[] args = { this.files.getFiles(), "." };
        return render("index.tat", args);
    }

    private String personIndex(final Response res, final boolean auth, final String gedcomName) {
        final List<Person> people = this.files.getAllPeople(gedcomName);
        if (Objects.isNull(people)) {
            res.status(SC_NOT_FOUND);
            return "";
        }
        final Object[] args = { people, gedcomName, 0, "../..", auth };
        return render("personIndex.tat", args);
    }

//    private String personChart(final boolean auth, final String gedcomName) {
//        final Object[] args = { gedcomName, "../.." };
//        return render("personChart.tat", args);
//    }
//
//    private String personChartData(final boolean auth, final String gedcomName, Response res) {
//        res.type("image/svg+xml");
//        return this.files.getChartData(gedcomName);
//    }

    private String person(final Response res, final boolean auth, final String gedcomName, final UUID uuid) {
        final Person person = this.files.getPerson(gedcomName, uuid);
        if (Objects.isNull(person) || Util.privatize(person, auth)) {
            res.status(SC_NOT_FOUND);
            return "";
        }
        final List<String> otherFiles = this.files.getXrefs(gedcomName, uuid);
        final NoteList footnotes = GedcomFilesHandler.getFootnotesFor(person);
        final Object[] args = { person, gedcomName, otherFiles, footnotes, "../..", auth };
        return render("person.tat", args);
    }



    private static String render(final String view, final Object[] args) {
        return new TemplAtEngine().render(new ModelAndView(args, view));
    }
}
