package nu.mine.mosher.collection;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class TreeNode<T> implements Iterable<TreeNode<T>>
{
    private T object;

    private TreeNode<T> parent;
    private final List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();

    public TreeNode()
    {
        this(null);
    }

    public TreeNode(final T object)
    {
        this.object = object;
    }

    public T getObject()
    {
        return this.object;
    }

    public void setObject(final T object)
    {
        this.object = object;
    }

    public Iterator<TreeNode<T>> children()
    {
        return this.children.iterator();
    }

    public int getChildCount()
    {
        return this.children.size();
    }

    public void addChild(final TreeNode<T> child)
    {
        if (child.parent != null)
        {
            child.removeFromParent();
        }

        this.children.add(child);
        child.parent = this;
    }

    public void removeChild(final TreeNode<T> child)
    {
        if (child.parent != this)
        {
            throw new IllegalArgumentException(
                "given TreeNode is not a child of this TreeNode");
        }

        for (final Iterator<TreeNode<T>> i = children(); i.hasNext();)
        {
            final TreeNode<T> childN = i.next();
            if (childN == child)
            {
                i.remove();
                child.parent = null;
            }
        }
    }

    public TreeNode<T> parent()
    {
        return this.parent;
    }

    public void removeFromParent()
    {
        if (this.parent == null)
        {
            return;
        }

        this.parent.removeChild(this);
    }

    @Override
    public String toString()
    {
        // final StringBuilder sb = new StringBuilder();
        // appendStringDeep(sb);
        // return sb.toString();
        return toStringShallow();
    }

    public void appendStringDeep(final StringBuilder sb)
    {
        appendStringDeep(sb, 0);
    }

    private void appendStringDeep(final StringBuilder sb, final int level)
    {
        for (int i = 0; i < level; ++i)
        {
            sb.append("    ");
        }

        appendStringShallow(sb);
        sb.append("\n");

        for (final TreeNode<T> child : this.children)
        {
            child.appendStringDeep(sb, level + 1);
        }
    }

    public String toStringShallow()
    {
        final StringBuilder sb = new StringBuilder();
        appendStringShallow(sb);
        return sb.toString();
    }

    public void appendStringShallow(final StringBuilder sb)
    {
        if (this.object != null)
        {
            sb.append(this.object.toString());
        }
        else
        {
            sb.append("[null]");
        }
    }

    @Override
    public Iterator<TreeNode<T>> iterator()
    {
        return children();
    }
}
