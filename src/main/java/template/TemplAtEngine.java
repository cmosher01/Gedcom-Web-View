package template;

import net.sourceforge.templat.Templat;
import spark.*;

import java.net.URL;

public class TemplAtEngine extends TemplateEngine {
    @Override
    public String render(final ModelAndView modelAndView) {
        final URL nameView = getClass().getResource(modelAndView.getViewName());
        return render(new Templat(nameView), (Object[])modelAndView.getModel());
    }

    private static String render(final Templat tat, final Object[] varargs) {
        final StringBuilder sb = new StringBuilder(1024);
        render(tat, varargs, sb);
        return sb.toString();
    }

    private static void render(final Templat tat, final Object[] varargs, final StringBuilder sb) {
        try {
            tat.render(sb, varargs);
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
