package nu.mine.mosher.gedcom;

import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.GedcomFile;
import nu.mine.mosher.gedcom.model.Loader;
import nu.mine.mosher.gedcom.model.Person;
import nu.mine.mosher.gedcom.model.Source;
import nu.mine.mosher.logging.Jul;
import spark.ModelAndView;
import template.TemplAtEngine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.*;

import static nu.mine.mosher.logging.Jul.log;
import static spark.Spark.*;

/**
 * @author Chris Mosher
 * Created 2006-09-24.
 */
public class GedcomWebView {
    public static void main(final String... args) {
//        Jul.verbose(true);
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



    private final Map<String, Loader> mapLoader = new TreeMap<>();
    private final Map<UUID, Set<Loader>> mapPersonCrossRef = new HashMap<>(32);



    private GedcomWebView() {
    }



    private void run() throws IOException, InvalidLevel {
        initGedcom();

        staticFiles.location("/public");
        staticFiles.expireTime(600);

        get("/favicon.ico", (req, res) -> null);

        get("", (req, res) -> {
            res.redirect("/");
            return null;
        });
        get("/", (req, res) -> {
            final List<GedcomFile> rFile = buildGedcomFilesList(new ArrayList<>(this.mapLoader.size()));
            res.type("text/html");
            return new ModelAndView(new Object[]{ rFile }, "index.tat");
        }, new TemplAtEngine());



        path("/:ged/persons", () -> {
            get("", (req, res) -> {
                res.redirect("persons/");
                return null;
            });

            get("/", (req, res) -> {
                final String gedcomName = req.params(":ged");
                final Loader loader = this.mapLoader.get(gedcomName);
                res.type("text/html");
                return new ModelAndView(new Object[]{ loader.getAllPeople(), 0 }, "personIndex.tat");
            }, new TemplAtEngine());

            get("/:id", (req, res) -> {
                final String gedcomName = req.params(":ged");
                final Loader loader = this.mapLoader.get(gedcomName);
                final UUID uuid = uuidFromString(req.params(":id"));
                final Person person = loader.lookUpPerson(uuid);
//                TODO
//                if (person == null) {
//                    showErrorPage(response);
//                    return;
//                }
                final List<String> otherFiles = new ArrayList<>();
                if (this.mapPersonCrossRef.containsKey(uuid)) {
                    for (final Loader gedcom : this.mapPersonCrossRef.get(uuid)) {
                        if (gedcom != loader) {
                            otherFiles.add(gedcom.getName());
                        }
                    }
                }
                res.type("text/html");
                return new ModelAndView(new Object[]{ person, gedcomName, otherFiles }, "person.tat");
            }, new TemplAtEngine());
        });

        path("/:ged/sources", () -> {
            get("/:id", (req, res) -> {
                final String gedcomName = req.params(":ged");
                final Loader loader = this.mapLoader.get(gedcomName);
                final UUID uuid = uuidFromString(req.params(":id"));
                final Source source = loader.lookUpSource(uuid);
                //                TODO
                //                if (source == null) {
                //                    showErrorPage(response);
                //                    return;
                //                }
                res.type("text/html");
                return new ModelAndView(new Object[]{ source, gedcomName }, "source.tat");
            }, new TemplAtEngine());
        });
    }



    private void initGedcom() throws IOException, InvalidLevel {
        final List<File> rFileGedcom = new ArrayList<>(16);
        getGedcomFiles(rFileGedcom);

        final Map<UUID, Loader> mapMasterUuidToLoader = new HashMap<>(1024);
        for (final File fileGedcom : rFileGedcom) {
            final GedcomTree gt = Gedcom.readFile(new BufferedInputStream(new FileInputStream(fileGedcom)));
            final Loader loader = new Loader(gt, fileGedcom.getName());
            new GedcomConcatenator(gt).concatenate();
            loader.parse();
            this.mapLoader.put(loader.getName(), loader);

            buildPersonCrossReferences(loader, mapMasterUuidToLoader);
        }
    }

    private void buildPersonCrossReferences(
        final Loader loader, final Map<UUID, Loader> mapMasterUuidToLoader) {
        final Set<UUID> uuids = new HashSet<>(256);
        loader.appendAllUuids(uuids);
        for (final UUID uuid : uuids) {
            if (mapMasterUuidToLoader.containsKey(uuid)) {
                Set<Loader> loaders = this.mapPersonCrossRef.get(uuid);
                if (loaders == null) {
                    loaders = new HashSet<>(2);
                    this.mapPersonCrossRef.put(uuid, loaders);

                    loaders.add(mapMasterUuidToLoader.get(uuid));
                }
                loaders.add(loader);
            } else {
                mapMasterUuidToLoader.put(uuid, loader);
            }
        }
    }

    private static void getGedcomFiles(final Collection<File> rFileGedcom) throws IOException {
        final File dirGedcom = new File("gedcom").getCanonicalFile();

        // @formatter:off
        final File[] rFile = dirGedcom.listFiles(
            file -> file.isFile() && file.canRead() && (file.getName().endsWith(".ged") || file.getName().endsWith(".GED")));
        // @formatter:on

        if (rFile == null || rFile.length == 0) {
            throw new IOException("Cannot find any readable files in " + dirGedcom);
        }

        rFileGedcom.addAll(Arrays.asList(rFile));
    }

    //    private void writeSourcePage(final String gedcomName, final String paramSourceUUID, final HttpServletResponse response) throws IOException, TemplateLexingException, TemplateParsingException {
    //        final Source source = findSourceByUuid(gedcomName, paramSourceUUID);
    //        if (source == null) {
    //            showErrorPage(response);
    //            return;
    //        }
    //        final Writer out = openPage(response);
    //        showSourcePage(source, out);
    //        closePage(out);
    //    }

    //    private static void showSourcePage(final Source source, final Writer out) throws TemplateLexingException, TemplateParsingException, IOException {
    //        final StringBuilder sb = new StringBuilder(256);
    //        final Templat tat = new Templat(GedcomServlet.class.getResource("template/source.tat"));
    //        tat.render(sb, source);
    //        out.write(sb.toString());
    //    }



    private List<GedcomFile> buildGedcomFilesList(final List<GedcomFile> rFile) {
        for (final Map.Entry<String, Loader> entry : this.mapLoader.entrySet()) {
            final Person first = entry.getValue().getFirstPerson();
            if (first != null) {
                final String descrip = entry.getValue().getDescription();
                final GedcomFile file = new GedcomFile(entry.getKey(), first, HtmlUtil.escapeHtml(descrip == null ? "" : descrip));
                rFile.add(file);
            }
        }

        final Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        rFile.sort((f1, f2) -> collator.compare(f1.getFile(), f2.getFile()));
        return rFile;
    }


//    private Person findPersonByUuid(final String gedcomName, final String uuid) {
//        if (uuid == null || uuid.length() == 0) {
//            return null;
//        }
//        if (gedcomName == null || gedcomName.length() == 0) {
//            return null;
//        }
//        final Loader loader = this.mapLoader.get(gedcomName);
//        if (loader == null) {
//            return null;
//        }
//        return loader.lookUpPerson(uuidFromString(uuid));
//    }
//
//    private Source findSourceByUuid(final String pathInfo, final String uuid) {
//        if (uuid == null || uuid.length() == 0) {
//            return null;
//        }
//        if (pathInfo == null || pathInfo.length() == 0) {
//            return null;
//        }
//        final Loader loader = this.mapLoader.get(pathInfo);
//        if (loader == null) {
//            return null;
//        }
//        return loader.lookUpSource(uuidFromString(uuid));
//    }

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
