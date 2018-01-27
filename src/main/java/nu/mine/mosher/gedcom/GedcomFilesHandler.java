package nu.mine.mosher.gedcom;

import nu.mine.mosher.Util;
import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.exception.InvalidLevel;
import nu.mine.mosher.gedcom.model.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Collator;
import java.util.*;



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
    private final Map<UUID, Set<Loader>> mapPersonCrossRef = new HashMap<>(32);

    public GedcomFilesHandler() throws IOException, InvalidLevel {
        final Map<UUID, Loader> mapMasterUuidToLoader = new HashMap<>(1024);
        final List<GedcomFile> files = new ArrayList<>(32);
        for (final File fileGedcom : getGedcomFiles()) {
            final Loader loader = parseGedcomFile(fileGedcom);

            this.mapLoader.put(loader.getName(), loader);
            files.add(new GedcomFile(loader.getName(), loader.getDescription()));

            buildPersonCrossReferences(loader, mapMasterUuidToLoader);
        }

        final Collator collator = Util.createCollator();
        files.sort((f1, f2) -> collator.compare(f1.getName(), f2.getName()));
        this.rFile = Collections.unmodifiableList(files);
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
        final Person person = loader.lookUpPerson(uuid);
        if (person == null) {
            throw new IOException();
        }
        return person;
    }

    public Source getSource(final String gedcomName, final UUID uuid) throws IOException {
        final Loader loader = this.mapLoader.get(gedcomName);
        if (loader == null) {
            throw new IOException();
        }
        final Source source = loader.lookUpSource(uuid);
        if (source == null) {
            throw new IOException();
        }
        return source;
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
     * Parses the given GEDCOM file.
     *
     * @param fileGedcom GEDCOM file to read
     * @return Loader representing the GEDCOM file (and model)
     * @throws IOException if I/O error
     * @throws InvalidLevel if GEDCOM level number is invalid
     */
    private static Loader parseGedcomFile(final File fileGedcom) throws IOException, InvalidLevel {
        final GedcomTree gt = Gedcom.readFile(new BufferedInputStream(new FileInputStream(fileGedcom)));
        new GedcomConcatenator(gt).concatenate();
        final Loader loader = new Loader(gt, fileGedcom.getName());
        loader.parse();
        return loader;
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

    private void buildPersonCrossReferences(final Loader loader, final Map<UUID, Loader> mapMasterUuidToLoader) {
        final Set<UUID> uuids = new HashSet<>(256);
        loader.appendAllUuids(uuids);
        uuids.forEach(uuid -> {
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
        });
    }

    public NoteList getFootnotesFor(final Person person) {
        final NoteList notes = new NoteList();
        person.getEvents().forEach(e -> e.getCitations().forEach(notes::note));
        person.getPartnerships().forEach(p -> p.getEvents().forEach(e -> e.getCitations().forEach(notes::note)));
        return notes;
    }
}
