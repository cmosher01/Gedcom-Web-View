package nu.mine.mosher.collection;



import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;



@SuppressWarnings(
{ "static-method", "boxing" })
public class TreeNodeTest
{
    @Test
    public void nominalChild()
    {
        final String sa = "a";
        final TreeNode<String> a = new TreeNode<>(sa);
        final String sb = "b";
        final TreeNode<String> b = new TreeNode<>(sb);

        a.addChild(b);

        assertThat(a.getChildCount(), is(1));

        int c = 0;
        for (final TreeNode<String> i : a)
        {
            ++c;
            assertThat(i.getObject(), sameInstance(sb));
        }
        assertThat(c, is(1));

        assertThat(b.parent(), sameInstance(a));
    }

    @Test
    public void nominalRemoveChild()
    {
        final String sa = "a";
        final TreeNode<String> a = new TreeNode<>(sa);
        final String sb = "b";
        final TreeNode<String> b = new TreeNode<>(sb);

        a.addChild(b);
        a.removeChild(b);
        assertThat(a.getChildCount(), is(0));
        int c = 0;
        for (final TreeNode<String> i : a)
        {
            i.getObject();
            ++c;
        }
        assertThat(c, is(0));

        assertThat(b.parent(), nullValue());
    }

    @Test
    public void nominalRemoveFromParent()
    {
        final String sa = "a";
        final TreeNode<String> a = new TreeNode<>(sa);
        final String sb = "b";
        final TreeNode<String> b = new TreeNode<>(sb);

        a.addChild(b);
        b.removeFromParent();

        assertThat(a.getChildCount(), is(0));
        int c = 0;
        for (final TreeNode<String> i : a)
        {
            i.getObject();
            ++c;
        }
        assertThat(c, is(0));

        assertThat(b.parent(), nullValue());
    }

    @Test
    public void addChildOfOtherExistingParent()
    {
        final String sa = "a";
        final TreeNode<String> a = new TreeNode<>(sa);
        final String sb = "b";
        final TreeNode<String> b = new TreeNode<>(sb);

        a.addChild(b);
        assertThat(a.getChildCount(), is(1));

        final String sx = "x";
        final TreeNode<String> x = new TreeNode<>(sx);
        x.addChild(b);

        assertThat(a.getChildCount(), is(0));

        assertThat(x.getChildCount(), is(1));

        for (final TreeNode<String> i : x)
        {
            assertThat(i.getObject(), sameInstance(sb));
        }

        assertThat(b.parent(), sameInstance(x));
    }

    @Test
    public void threeChildren()
    {
        final TreeNode<String> p = new TreeNode<>("p");

        final TreeNode<String> c1 = new TreeNode<>("c1");
        p.addChild(c1);
        final TreeNode<String> c2 = new TreeNode<>("c2");
        p.addChild(c2);
        final TreeNode<String> c3 = new TreeNode<>("c3");
        p.addChild(c3);

        assertThat(p.getChildCount(), is(3));

        final List<String> actual = new ArrayList<>();
        for (final TreeNode<String> i : p)
        {
            actual.add(i.getObject());
            assertThat(i.parent(), sameInstance(p));
        }
        assertThat(actual.get(0), is("c1"));
        assertThat(actual.get(1), is("c2"));
        assertThat(actual.get(2), is("c3"));
    }

    @Test
    public void removeMiddleChild()
    {
        final TreeNode<String> p = new TreeNode<>("p");

        final TreeNode<String> c1 = new TreeNode<>("c1");
        p.addChild(c1);
        final TreeNode<String> c2 = new TreeNode<>("c2");
        p.addChild(c2);
        final TreeNode<String> c3 = new TreeNode<>("c3");
        p.addChild(c3);

        p.removeChild(c2);

        assertThat(p.getChildCount(), is(2));
        final List<String> actual = new ArrayList<>();
        for (final TreeNode<String> i : p)
        {
            actual.add(i.getObject());
            assertThat(i.parent(), sameInstance(p));
        }
        assertThat(actual.get(0), is("c1"));
        assertThat(actual.get(1), is("c3"));
    }
}
