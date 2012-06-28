package nu.mine.mosher.gedcom.servlet.struct;
import java.util.HashMap;
import java.util.Map;
import nu.mine.mosher.gedcom.GedcomTag;

/*
 * Created on 2006-10-09.
 */
public final class EventNames
{
	private static final Map<GedcomTag,String> map = new HashMap<GedcomTag,String>();
	static
	{
		map.put(GedcomTag.EVEN,"[unknown]");
		map.put(GedcomTag.CENS,"census");
		map.put(GedcomTag.BIRT,"birth");
		map.put(GedcomTag.DEAT,"death");
		map.put(GedcomTag.CHR ,"christening");
		map.put(GedcomTag.BURI,"reposition");
		map.put(GedcomTag.CREM,"cremation");
		map.put(GedcomTag.ADOP,"adoption");
		map.put(GedcomTag.BAPM,"baptism");
		map.put(GedcomTag.BARM,"bar mitzvah");
		map.put(GedcomTag.BASM,"bas mitzvah");
		map.put(GedcomTag.BLES,"blessing");
		map.put(GedcomTag.CHRA,"adult christening");
		map.put(GedcomTag.CONF,"confirmation");
		map.put(GedcomTag.FCOM,"first communion");
		map.put(GedcomTag.ORDN,"ordination");
		map.put(GedcomTag.NATU,"naturalization");
		map.put(GedcomTag.EMIG,"emigration");
		map.put(GedcomTag.IMMI,"immigration");
		map.put(GedcomTag.PROB,"will probated");
		map.put(GedcomTag.WILL,"signed will");
		map.put(GedcomTag.GRAD,"graduated");
		map.put(GedcomTag.RETI,"retirement");
		map.put(GedcomTag.RESI,"residence");
		map.put(GedcomTag.MARR,"marriage");
		map.put(GedcomTag.ANUL,"annulment");
		map.put(GedcomTag.DIV ,"divorce");
		map.put(GedcomTag.DIVF,"divorce filed");
		map.put(GedcomTag.ENGA,"engagement");
		map.put(GedcomTag.MARB,"marriage bann");
		map.put(GedcomTag.MARC,"marriage contract");
		map.put(GedcomTag.MARL,"marriage license");
		map.put(GedcomTag.MARS,"marriage settlement");
		map.put(GedcomTag.CAST,"caste");
		map.put(GedcomTag.DSCR,"description");
		map.put(GedcomTag.EDUC,"education");
		map.put(GedcomTag.IDNO,"national ID");
		map.put(GedcomTag.NATI,"national origin");
		map.put(GedcomTag.NCHI,"count of children");
		map.put(GedcomTag.NMR,"count of marriages");
		map.put(GedcomTag.OCCU,"occupation");
		map.put(GedcomTag.PROP,"posession");
		map.put(GedcomTag.RELI,"religion");
		map.put(GedcomTag.SSN ,"US Social Security number");
		map.put(GedcomTag.TITL,"title");
	}

	private EventNames()
	{
		assert false : "not instantiated";
	}

	public static String getName(final GedcomTag tag)
	{
		return map.get(tag);
	}
}
