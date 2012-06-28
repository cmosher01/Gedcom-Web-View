/**
 * Created March 25, 2012
 */
package nu.mine.mosher.gedcom.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This servlet filter allows this one webapp to serve
 * default (static) pages as well as a servlet.
 * 
 * Any URL that starts with "/genealogy/default" will
 * get handled by the default servlet. Any thing else
 * will go to our servlet. The two types of incoming URLs
 * are handled as follows:
 * 
 * <ol>
 * <li>
 * <code>/genealogy/mosher.ged</code><br>
 * <code>/genealogy</code> is the context path.<br>
 * <code>mosher.ged</code> does not start with <code>/default</code>, so is for our servlet.<br>
 * Change to <code>/GedcomServlet/mosher.ged</code>.<br>
 * Forward to GedcomServlet (which web.xml sets up to handle <code>/GedcomServlet/*</code>).
 * </li>
 * <li>
 * <code>/genealogy/default/css/foo.css</code><br>
 * <code>/genealogy</code> is the context path.<br>
 * <code>/default/css/foo.css</code> does start with <code>/default</code>, so is for the default servlet.<br>
 * Change to <code>/css/foo.css</code><br>
 * Pass to the default servlet, which looks for it relative to the
 * context root (maven webapp directory; war file root directory).
 * </li>
 * </ol>
 * Note that we do not intend to receive an incoming URL that starts with <code>/genealogy/GedcomServlet</code>;
 * we only define it in web.xml that way so that this filter can get a reference to the servlet via the path name.
 * 
 * @author Chris Mosher
 */
public class Filter implements javax.servlet.Filter {
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, @SuppressWarnings("unused") FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpreq = (HttpServletRequest) req;

		final String uri = httpreq.getRequestURI();
		String path = uri.substring(httpreq.getContextPath().length());

		/*
		 * If path (after "/genealogy/") is
		 * "/default/[res]" then pass "/[res]"
		 * to the default servlet.
		 */
		if (path.startsWith("/default")) {
			final String pathChopped = path.substring("/default".length());
			req.getRequestDispatcher(pathChopped).forward(req, resp);
		} else {
			req.getRequestDispatcher("/GedcomServlet" + path).forward(req, resp);
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void init(@SuppressWarnings("unused") FilterConfig config) {
		// do nothing
	}
}
