package nu.mine.mosher.gedcom;

import nu.mine.mosher.Util;
import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.dropline.Dropline;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.*;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;

import static java.util.Optional.*;



@SuppressWarnings("WeakerAccess")
public class GedcomFilesHandler {
    /*
            this is for the list of gedcom files
            on the first page (index.tat)
     */
    @SuppressWarnings("unused")  /* used in templates */
    public static class GedcomFile {
        private final String name;
        private final String description;

        private GedcomFile(String n, String d) {
            name = n;
            description = d == null ? "" : d;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }



    /* directory to read the gedcom files from */
    private static final String GEDCOM_DIR_NAME = "gedcom";



    private final List<GedcomFile> rFile;
    private final Map<String, Loader> mapLoader = new TreeMap<>();
    private final Map<String, String> mapChart = new TreeMap<>();
    private final Map<UUID, Loader> mapMasterUuidToLoader = new HashMap<>(1024);
    private final Map<UUID, Set<Loader>> mapPersonCrossRef = new HashMap<>(32);

    public GedcomFilesHandler() throws IOException, InvalidLevel {
        final List<GedcomFile> files = new ArrayList<>(32);
        for (final File fileGedcom : getGedcomFiles()) {
            final GedcomTree gt = parseGedcom(fileGedcom);
            final Loader loader = new Loader(gt, fileGedcom.getName());
            loader.parse();

            this.mapLoader.put(loader.getName(), loader);
            files.add(new GedcomFile(loader.getName(), loader.getDescription()));

            buildPersonCrossReferences(loader);

//            try {
//                this.mapChart.put(loader.getName(), docToString(Dropline.build(gt)));
//            } catch (final Exception e) {
//                e.printStackTrace();
//            }
        }

        final Collator collator = Util.createCollator();
        files.sort((f1, f2) -> collator.compare(f1.getName(), f2.getName()));
        this.rFile = Collections.unmodifiableList(files);
    }

    private String docToString(final Document doc) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        final StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }

    private GedcomTree parseGedcom(final File fileGedcom) throws IOException, InvalidLevel
    {
        final GedcomTree gt = Gedcom.readFile(new BufferedInputStream(new FileInputStream(fileGedcom)));
        new GedcomConcatenator(gt).concatenate();
        return gt;
    }

    public Optional<Loader> findLoaderForPerson(final UUID uuidPerson) {
        if (this.mapMasterUuidToLoader.containsKey(uuidPerson)) {
            return ofNullable(this.mapMasterUuidToLoader.get(uuidPerson));
        }
        return Optional.empty();
    }

    public Optional<Person> findPerson(final UUID uuidPerson) {
        final Optional<Loader> loader = findLoaderForPerson(uuidPerson);
        if (loader.isPresent()) {
            return ofNullable(loader.get().lookUpPerson(uuidPerson));
        }
        return Optional.empty();
    }

    public List<GedcomFile> getFiles() {
        return this.rFile;
    }

    public List<Person> getAllPeople(final String gedcomName) throws IOException {
        final Loader loader = this.mapLoader.get(gedcomName);
        if (loader == null) {
            throw new IOException();
        }
        return loader.getAllPeople();
    }

    public Person getPerson(final String gedcomName, final UUID uuid) throws IOException {
        final Loader loader = this.mapLoader.get(gedcomName);
        if (loader == null) {
            throw new IOException();
        }
        return loader.lookUpPerson(uuid);
    }

    public List<String> getXrefs(final String gedcomName, final UUID uuid) {
        final List<String> otherFiles = new ArrayList<>();
        if (this.mapPersonCrossRef.containsKey(uuid)) {
            for (final Loader gedcom : this.mapPersonCrossRef.get(uuid)) {
                if (!gedcom.getName().equals(gedcomName)) {
                    otherFiles.add(gedcom.getName());
                }
            }
        }
        return otherFiles;
    }

    public String getChartData(final String gedcomName) {
        return this.mapChart.get(gedcomName);
    }




    /**
     * Gets all readable *.ged files in the "gedcom" sub-directory
     * of the current default directory.
     *
     * @return list of files
     * @throws IOException if no files found
     */
    private static List<File> getGedcomFiles() throws IOException {
        final File dirGedcom = new File(GEDCOM_DIR_NAME).getCanonicalFile();

        final File[] rFile = dirGedcom.listFiles(file -> file.isFile() && file.canRead() && (file.getName().endsWith(".ged") || file.getName().endsWith(".GED")));

        if (rFile == null || rFile.length == 0) {
            throw new IOException("Cannot find any readable files in " + dirGedcom);
        }

        return Collections.unmodifiableList(Arrays.asList(rFile));
    }

    private void buildPersonCrossReferences(final Loader loader) {
        final Set<UUID> uuids = new HashSet<>(256);
        loader.appendAllUuids(uuids);
        uuids.forEach(uuid -> {
            if (this.mapMasterUuidToLoader.containsKey(uuid)) {
                Set<Loader> loaders = this.mapPersonCrossRef.get(uuid);
                if (loaders == null) {
                    loaders = new HashSet<>(2);
                    this.mapPersonCrossRef.put(uuid, loaders);

                    loaders.add(this.mapMasterUuidToLoader.get(uuid));
                }
                loaders.add(loader);
            } else {
                this.mapMasterUuidToLoader.put(uuid, loader);
            }
        });
    }

    public static NoteList getFootnotesFor(final Person person, final boolean auth) {
        final NoteList notes = new NoteList();
        person.getEvents().forEach(e -> {
            if (!e.getNote().isEmpty()) {
                notes.note(e.getNote());
            }
            e.getCitations().forEach(notes::note);
        });
        person.getPartnerships().forEach(p -> p.getEvents().forEach(e -> e.getCitations().forEach(notes::note)));
        return notes;
    }
}
