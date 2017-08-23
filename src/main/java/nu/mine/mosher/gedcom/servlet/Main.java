package nu.mine.mosher.gedcom.servlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.Servlet;


/**
 * Created by Chris Mosher on 11/11/16.
 */
public class Main extends Tomcat {
    public static void main(final String... args) throws LifecycleException {
        new Main().initContext().run();
    }


    private Main() {
    }


    private Main initContext() {
        final Context ctx = addContext("", "");
        initStaticServlet(ctx);
        initGedcomServlet(ctx);
        return this;
    }

    private void run() throws LifecycleException {
        start();
        getServer().await();
    }


    private static void initGedcomServlet(final Context ctx) {
        initServlet("/*", new GedcomServlet(), ctx);
    }

    private static void initStaticServlet(final Context ctx) {
        initServlet("/static/*", new DefaultServlet(), ctx);
    }

    private static void initServlet(final String pattern, final Servlet servlet, final Context ctx) {
        Tomcat.addServlet(ctx, servlet.getClass().getSimpleName(), servlet);
        ctx.addServletMappingDecoded(pattern, servlet.getClass().getSimpleName());
    }
}
