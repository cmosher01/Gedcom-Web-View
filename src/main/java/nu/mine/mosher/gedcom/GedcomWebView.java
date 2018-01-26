package nu.mine.mosher.gedcom;

import nu.mine.mosher.Util;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.Person;
import nu.mine.mosher.logging.Jul;
import spark.ModelAndView;
import template.TemplAtEngine;

import java.io.IOException;
import java.util.*;

import static nu.mine.mosher.logging.Jul.log;
import static spark.Spark.*;

/**
 * @author Chris Mosher
 * Created 2006-09-24.
 */
public class GedcomWebView {
    private static final boolean VERBOSE = false;

    public static void main(final String... args) {
        Jul.verbose(VERBOSE);
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



    @SuppressWarnings("CodeBlock2Expr")
    private void run() throws IOException {
        staticFiles.location("/public");
        staticFiles.expireTime(600);



        redirect.get("", "/");
        get("/", (req, res) -> index());

        get("/favicon.ico", (req, res) -> null);

        path("/:ged", () -> {
            path("/persons", () -> {
                redirect.get("", "persons/");
                get("/", (req, res) -> personIndex(req.params(":ged")));
                get("/:id", (req, res) -> person(req.params(":ged"), Util.uuidFromString(req.params(":id"))));
            });
        });
    }



    private String index() throws IOException {
        final Object[] rArgs = { this.files.getFiles(), "." };
        return new TemplAtEngine().render(new ModelAndView(rArgs, "index.tat"));
    }

    private String personIndex(final String gedcomName) throws IOException {
        final List<Person> people = this.files.getAllPeople(gedcomName);
        final Object[] rArgs = { people, gedcomName, 0, "../.." };
        return new TemplAtEngine().render(new ModelAndView(rArgs, "personIndex.tat"));
    }

    private String person(final String gedcomName, final UUID uuid) throws IOException {
        final Person person = this.files.getPerson(gedcomName, uuid);
        final List<String> otherFiles = this.files.getXrefs(gedcomName, uuid);
        final Object[] rArgs = { person, gedcomName, otherFiles, "../.." };
        return new TemplAtEngine().render(new ModelAndView(rArgs, "person.tat"));
    }
}
