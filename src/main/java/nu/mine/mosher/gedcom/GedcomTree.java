package nu.mine.mosher.gedcom;



import java.util.HashMap;
import java.util.Map;

import nu.mine.mosher.collection.TreeNode;
import nu.mine.mosher.gedcom.exception.InvalidLevel;



/**
 * Represents a GEDCOM document. A GEDCOM document is a tree structure or
 * <code>GedcomLine</code> objects.
 * @author Chris Mosher
 */
public class GedcomTree
{
    private final TreeNode<GedcomLine> root;
    private final Map<String, TreeNode<GedcomLine>> mapIDtoNode = new HashMap<String, TreeNode<GedcomLine>>();

    private int prevLevel;
    private TreeNode<GedcomLine> prevNode;

    /**
     * Initializes a new <code>GedcomTree</code>.
     */
    public GedcomTree()
    {
        this.root = new TreeNode<GedcomLine>();
        this.prevNode = this.root;
        this.prevLevel = -1;
    }

    /**
     * Appends a <code>GedcomLine</code> to this tree. This method must be
     * called in the same sequence that GEDCOM lines appear in the file.
     * @param line
     * @throws InvalidLevel
     */
    void appendLine(final GedcomLine line) throws InvalidLevel
    {
        final int cPops = this.prevLevel + 1 - line.getLevel();
        if (cPops < 0)
        {
            throw new InvalidLevel(line);
        }

        TreeNode<GedcomLine> parent = this.prevNode;
        for (int i = 0; i < cPops; ++i)
        {
            parent = parent.parent();
        }

        this.prevLevel = line.getLevel();
        this.prevNode = new TreeNode<GedcomLine>();
        this.prevNode.setObject(line);
        parent.addChild(this.prevNode);

        if (line.hasID())
        {
            this.mapIDtoNode.put(line.getID(), this.prevNode);
        }
    }

    /**
     * Gets the node in this <code>GedcomTree</code> with the given ID.
     * @param id
     * @return the node with the given ID.
     */
    public TreeNode<GedcomLine> getNode(final String id)
    {
        return this.mapIDtoNode.get(id);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(1024);
        this.root.appendStringDeep(sb);

        sb.append("--------map-of-IDs-to-Nodes--------\n");
        for (final Map.Entry<String, TreeNode<GedcomLine>> entry : this.mapIDtoNode
            .entrySet())
        {
            sb.append(entry.getKey());
            sb.append(" --> ");
            entry.getValue().appendStringShallow(sb);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Gets the root of this tree.
     * @return root node
     */
    public TreeNode<GedcomLine> getRoot()
    {
        return this.root;
    }
}
