package nu.mine.mosher.gedcom.servlet;

public class HtmlUtil {

	public static String escapeHtml(final String s)
	{
		return s
		.replaceAll("&","&amp;")
		.replaceAll("<","&lt;")
		.replaceAll(">","&gt;")
		.replaceAll("\"","&quot;");
	}

	public static String smartEscapeHtml(final String string) {
		if (
			string.contains("<p>") ||
			string.contains("<td>") ||
			string.contains("<TD>") ||
			string.contains("<br") ||
			string.contains("<BR") ||
			string.contains("<li>") ||
			string.contains("<LI>") ||
			string.contains("<img") ||
			string.contains("<IMG") ||
			string.contains("href=") ||
			string.contains("HREF="))
		{
			/*
			 * if it looks like HTML, don't cleanse it, so if displayed in browser
			 * then browser will interpret it
			 */
			return string;
		}

		return escapeHtml(string);
	}
}
