package nu.mine.mosher.gedcom;

import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.Person;
import nu.mine.mosher.gedcom.model.Source;
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



    private void run() throws IOException, InvalidLevel {
        staticFiles.location("/public");
        staticFiles.expireTime(600);

        get("/favicon.ico", (req, res) -> null);

        get("", (req, res) -> {
            res.redirect("/");
            return null;
        });
        get("/", (req, res) -> {
            res.type("text/html");
            final Object[] rArgs = { this.files.getFiles() };
            return new ModelAndView(rArgs, "index.tat");
        }, new TemplAtEngine());



        path("/:ged/persons", () -> {
            get("", (req, res) -> {
                res.redirect("persons/");
                return null;
            });

            get("/", (req, res) -> {
                final String gedcomName = req.params(":ged");

                final List<Person> people = this.files.getAllPeople(gedcomName);

                final Object[] rArgs = { people, 0 };
                res.type("text/html");
                return new ModelAndView(rArgs, "personIndex.tat");
            }, new TemplAtEngine());

            get("/:id", (req, res) -> {
                final String gedcomName = req.params(":ged");
                final UUID uuid = uuidFromString(req.params(":id"));

                final Person person = this.files.getPerson(gedcomName, uuid);
                final List<String> otherFiles = this.files.getXrefs(gedcomName, uuid);

                final Object[] rArgs = { person, gedcomName, otherFiles };
                res.type("text/html");
                return new ModelAndView(rArgs, "person.tat");
            }, new TemplAtEngine());
        });

        path("/:ged/sources", () -> {
            get("/:id", (req, res) -> {
                final String gedcomName = req.params(":ged");
                final UUID uuid = uuidFromString(req.params(":id"));

                final Source source = this.files.getSource(gedcomName, uuid);

                final Object[] rArgs = { source, gedcomName };
                res.type("text/html");
                return new ModelAndView(rArgs, "source.tat");
            }, new TemplAtEngine());
        });
    }




    private static UUID uuidFromString(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (final Throwable e) {
            return null;
        }
    }

    public static String styleCitation(final String citation) {
        return citation.replaceAll("\\b_(.+?)_\\b", "<span class=\"published\">$1</span>").replaceAll("\\b(\\w+?://\\S+?)\\s", "<a href=\"$1\">$1</a> ");
    }
}
