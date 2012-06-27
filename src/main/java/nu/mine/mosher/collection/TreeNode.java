package nu.mine.mosher.collection;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Represents a node in a tree, and the (sub-)tree rooted at that node.
 * @author Chris Mosher
 * @param <T> type of object in each node
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>
{
    private T object;

    private TreeNode<T> parent;
    private final List<TreeNode<T>> children = new ArrayList<>();

    /**
     * Constructs a node, with no children, and no wrapped object.
     */
    public TreeNode()
    {
        this(null);
    }

    /**
     * Constructs a node, with no children, and the given wrapped object.
     * @param object the object that this node will wrap
     */
    public TreeNode(final T object)
    {
        this.object = object;
    }

    /**
     * Returns the object this node wraps, as passed in to the constructor.
     * Returns <code>null</code> if this node does not wrap an object.
     * @return the wrapped object
     */
    public T getObject()
    {
        return this.object;
    }

    /**
     * Sets (or resets) the object this node wraps.
     * @param object the object that this node will wrap
     */
    public void setObject(final T object)
    {
        this.object = object;
    }

    /**
     * Gets the (immediate) children of this node, as an iterator.
     * @return iterator of immediate children
     */
    public Iterator<TreeNode<T>> children()
    {
        return this.children.iterator();
    }

    /**
     * Gets the count of (immediate) children of this node.
     * @return the count of immediate children
     */
    public int getChildCount()
    {
        return this.children.size();
    }

    /**
     * Adds a child to this node. The given child will be removed from any
     * parent it may have.
     * @param child the child to add
     */
    public void addChild(final TreeNode<T> child)
    {
        if (child.parent != null)
        {
            child.removeFromParent();
        }

        this.children.add(child);
        child.parent = this;
    }

    /**
     * Indicates the attempt to remove a child of a node, when that child is not
     * actually a child of the node.
     * @author Chris Mosher
     */
    public static class NotChild extends Exception
    {
        private NotChild(final TreeNode<?> node, final TreeNode<?> child)
        {
            super("given TreeNode (" + child + ") is not a child of this TreeNode (" + node + ")");
        }
    }

    /**
     * Removes the given child from this node.
     * @param child the child to remove
     * @throws NotChild if this given <code>child</code> is not a child of this node
     */
    @SuppressWarnings("synthetic-access")
    public void removeChild(final TreeNode<T> child) throws NotChild
    {
        if (child.parent != this)
        {
            throw new NotChild(this, child);
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

    /**
     * Gets this node's parent, if any.
     * @return this node's parent, or <code>null</code> if this node has no
     *         parent.
     */
    public TreeNode<T> parent()
    {
        return this.parent;
    }

    /**
     * Removes this node from any parent it may have. If this node does not have
     * a parent, then this method just returns without doing anything.
     */
    public void removeFromParent()
    {
        if (this.parent == null)
        {
            return;
        }

        try
        {
            this.parent.removeChild(this);
        }
        catch (final NotChild e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns what this node's wrapped object's <code>toString</code> method
     * returns, or "[null]" if this node does not have a parent
     * @return same as wrapped object's <code>toString</code>, or "[null]"
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        try
        {
            appendStringShallow(sb);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }
        return sb.toString();
    }

    /**
     * Builds an outline-style string representation of the tree rooted at this
     * node, and appends it to the given <code>Appendable</code>
     * @param appendTo <code>Appendable</code> to append the tree to
     * @throws IOException if an I/O exception occurs while appending to
     *             <code>appendTo</code>
     */
    public void appendStringDeep(final Appendable appendTo) throws IOException
    {
        appendStringDeep(appendTo, 0);
    }

    private void appendStringDeep(final Appendable appendTo, final int level) throws IOException
    {
        for (int i = 0; i < level; ++i)
        {
            appendTo.append("    ");
        }

        appendStringShallow(appendTo);
        appendTo.append("\n");

        for (final TreeNode<T> child : this.children)
        {
            child.appendStringDeep(appendTo, level + 1);
        }
    }

    /**
     * Builds the same string as {@link TreeNode#toString}, and appends it to
     * the given <code>Appendable</code>
     * @param appendTo <code>Appendable</code> to append the string to
     * @throws IOException if an I/O exception occurs while appending to
     *             <code>appendTo</code>
     */
    public void appendStringShallow(final Appendable appendTo) throws IOException
    {
        if (this.object != null)
        {
            appendTo.append(this.object.toString());
        }
        else
        {
            appendTo.append("[null]");
        }
    }

    /**
     * Same as {@link TreeNode#children()}
     * @return iterator of immediate children
     */
    @Override
    public Iterator<TreeNode<T>> iterator()
    {
        return children();
    }
}
