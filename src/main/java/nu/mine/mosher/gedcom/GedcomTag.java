/*
 * Created on Aug 7, 2005
 */
package nu.mine.mosher.gedcom;



import java.util.Collections;
import java.util.HashSet;
import java.util.Set;



/**
 * TODO
 * @author Chris Mosher
 */
public enum GedcomTag
{
    UNKNOWN, ABBR, ADDR, ADR1, ADR2, ADOP, AFN, AGE, AGNC, ALIA, ANCE, ANCI, ANUL, ASSO, AUTH, BAPL, BAPM, BARM, BASM, BIRT, BLES, BLOB, BURI, CALN, CAST, CAUS, CENS, CHAN, CHAR, CHIL, CHR, CHRA, CITY, CONC, CONF, CONL, CONT, COPR, CORP, CREM, CTRY, DATA, DATE, DEAT, DESC, DESI, DEST, DIV, DIVF, DSCR, EDUC, EMIG, ENDL, ENGA, EVEN, FAM, FAMC, FAMF, FAMS, FCOM, FILE, FORM, GEDC, GIVN, GRAD, HEAD, HUSB, IDNO, IMMI, INDI, LANG, LEGA, MARB, MARC, MARL, MARR, MARS, MEDI, NAME, NATI, NATU, NCHI, NICK, NMR, NOTE, NPFX, NSFX, OBJE, OCCU, ORDI, ORDN, PAGE, PEDI, PHON, PLAC, POST, PROB, PROP, PUBL, QUAY, REFN, RELA, RELI, REPO, RESI, RESN, RETI, RFN, RIN, ROLE, SEX, SLGC, SLGS, SOUR, SPFX, SSN, STAE, STAT, SUBM, SUBN, SURN, TEMP, TEXT, TIME, TITL, TRLR, TYPE, VERS, WIFE, WILL, _UID, ;

    public static final Set<GedcomTag> setIndividualEvent;
    static
    {
        final Set<GedcomTag> set = new HashSet<>();

        set.add(GedcomTag.BIRT);
        set.add(GedcomTag.CHR);
        set.add(GedcomTag.DEAT);
        set.add(GedcomTag.BURI);
        set.add(GedcomTag.CREM);
        set.add(GedcomTag.ADOP);
        set.add(GedcomTag.BAPM);
        set.add(GedcomTag.BARM);
        set.add(GedcomTag.BASM);
        set.add(GedcomTag.BLES);
        set.add(GedcomTag.CHRA);
        set.add(GedcomTag.CONF);
        set.add(GedcomTag.FCOM);
        set.add(GedcomTag.ORDN);
        set.add(GedcomTag.NATU);
        set.add(GedcomTag.EMIG);
        set.add(GedcomTag.IMMI);
        set.add(GedcomTag.CENS);
        set.add(GedcomTag.PROB);
        set.add(GedcomTag.WILL);
        set.add(GedcomTag.GRAD);
        set.add(GedcomTag.RETI);
        set.add(GedcomTag.EVEN);

        setIndividualEvent = Collections.<GedcomTag> unmodifiableSet(set);
    }

    public static final Set<GedcomTag> setIndividualAttribute;
    static
    {
        final Set<GedcomTag> set = new HashSet<>();

        set.add(GedcomTag.CAST);
        set.add(GedcomTag.DSCR);
        set.add(GedcomTag.EDUC);
        set.add(GedcomTag.IDNO);
        set.add(GedcomTag.NATI);
        set.add(GedcomTag.NCHI);
        set.add(GedcomTag.NMR);
        set.add(GedcomTag.OCCU);
        set.add(GedcomTag.PROP);
        set.add(GedcomTag.RELI);
        set.add(GedcomTag.RESI);
        set.add(GedcomTag.SSN);
        set.add(GedcomTag.TITL);

        setIndividualAttribute = Collections.<GedcomTag> unmodifiableSet(set);
    }

    public static final Set<GedcomTag> setFamilyEvent;
    static
    {
        final Set<GedcomTag> set = new HashSet<>();

        set.add(GedcomTag.ANUL);
        set.add(GedcomTag.CENS);
        set.add(GedcomTag.DIV);
        set.add(GedcomTag.DIVF);
        set.add(GedcomTag.ENGA);
        set.add(GedcomTag.MARR);
        set.add(GedcomTag.MARB);
        set.add(GedcomTag.MARC);
        set.add(GedcomTag.MARL);
        set.add(GedcomTag.MARS);
        set.add(GedcomTag.EVEN);

        setFamilyEvent = Collections.<GedcomTag> unmodifiableSet(set);
    }
}
