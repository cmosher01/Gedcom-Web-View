package nu.mine.mosher.gedcom;

import nu.mine.mosher.Util;
import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.*;

import java.io.*;
import java.nio.file.*;
import java.text.Collator;
import java.util.*;

import static java.util.Optional.ofNullable;
import static nu.mine.mosher.logging.Jul.log;


@SuppressWarnings("WeakerAccess")
public class GedcomFilesHandler {
    /*
     *  This is for the list of gedcom files
     *  on the first page (index.tat)
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
    private final Map<UUID, Loader> mapMasterUuidToLoader = new HashMap<>(1024);
    private final Map<UUID, Set<Loader>> mapPersonCrossRef = new HashMap<>(32);

    public GedcomFilesHandler() throws IOException, InvalidLevel {
        final List<GedcomFile> files = new ArrayList<>(32);
        for (final File fileGedcom : getGedcomFiles()) {
            log().info("Reading GEDCOM file: "+fileGedcom.getCanonicalPath());
            final GedcomTree gt = parseGedcom(fileGedcom);
            final Loader loader = new Loader(gt, fileGedcom.getName());
            loader.parse();

            this.mapLoader.put(loader.getName(), loader);
            files.add(new GedcomFile(loader.getName(), loader.getDescription()));

            buildPersonCrossReferences(loader);
        }

        final Collator collator = Util.createCollator();
        files.sort((f1, f2) -> collator.compare(f1.getName(), f2.getName()));
        this.rFile = Collections.unmodifiableList(files);
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

    public List<GedcomFile> getFiles() {
        return this.rFile;
    }

    public String getCopyright(String gedcomName) {
        final Loader loader = this.mapLoader.get(gedcomName);
        if (loader == null) {
            log().info("Request for non-existent gedcom (all people).");
            return "";
        }
        return loader.getCopyright();
    }

    public List<Person> getAllPeople(final String gedcomName) {
        final Loader loader = this.mapLoader.get(gedcomName);
        if (loader == null) {
            log().info("Request for non-existent gedcom (all people).");
            return null;
        }
        return loader.getAllPeople();
    }

    public Person getPerson(final String gedcomName, final UUID uuid) {
        final Loader loader = this.mapLoader.get(gedcomName);
        if (loader == null) {
            log().info("Request for non-existent gedcom (person).");
            return null;
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

    /**
     * Gets all readable *.ged files in the "gedcom" sub-directory (non-recursively)
     * of the current default directory.
     *
     * @return list of files
     * @throws IOException if no files found
     */
    private static List<File> getGedcomFiles() throws IOException {
        final Path pathGedcom = Paths.get(GEDCOM_DIR_NAME);
        int timesChecked = 0;
        while (!pathGedcom.toFile().exists()) {
            if (++timesChecked < 10) {
                log().warning("Cannot find './gedcom' directory. Will try again in 3 seconds...");
                try {
                    Thread.sleep(3000);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                throw new IOException("Cannot find directory " + pathGedcom.toFile().getCanonicalPath());
            }
        }
        // wait a little longer, just for good measure
        try {
            Thread.sleep(2000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        final File[] rFile = pathGedcom.toFile().listFiles(file -> file.isFile() && file.canRead() && (file.getName().endsWith(".ged") || file.getName().endsWith(".GED")));

        if (rFile == null || rFile.length == 0) {
            throw new IOException("Cannot find any readable files in " + pathGedcom.toFile().getCanonicalPath());
        }

        return List.of(rFile);
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

    public static NoteList getFootnotesFor(final Person person) {
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
