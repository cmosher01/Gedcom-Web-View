package nu.mine.mosher.gedcom.servlet.struct;
/*
 * Created on 2006-10-13.
 */
public class GedcomFile
{
	private final String fileName;
	private final Person first;
	private final String description;
	/**
	 * @param fileName
	 * @param first
	 * @param description
	 */
	public GedcomFile(final String fileName, final Person first, final String description)
	{
		this.fileName = fileName;
		this.first = first;
		this.description = description;
	}
	public String getFile()
	{
		return this.fileName;
	}
	public Person getFirstPerson()
	{
		return this.first;
	}
	public String getDescription()
	{
		return this.description;
	}
}
