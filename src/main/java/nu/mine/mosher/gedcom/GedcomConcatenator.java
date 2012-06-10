package nu.mine.mosher.gedcom;



import java.util.ArrayList;
import java.util.List;

import nu.mine.mosher.collection.TreeNode;



/**
 * Handles CONT and CONC tags in a given <code>GedcomTree</code> by appending
 * their values to the previous <code>GedcomLine</code>.
 * @author Chris Mosher
 */
class GedcomConcatenator
{
    private final GedcomTree tree;

    /**
     * @param tree
     */
    public GedcomConcatenator(final GedcomTree tree)
    {
        this.tree = tree;
    }

    /**
	 * 
	 */
    public void concatenate()
    {
        concatenateHelper(this.tree.getRoot());
    }

    private static void concatenateHelper(final TreeNode<GedcomLine> nodeParent)
    {
        final List<TreeNode<GedcomLine>> rToBeRemoved = new ArrayList<TreeNode<GedcomLine>>();

        for (final TreeNode<GedcomLine> nodeChild : nodeParent)
        {
            // TODO: remove recursion
            concatenateHelper(nodeChild);

            final GedcomLine lineChild = nodeChild.getObject();

            final GedcomTag tag = lineChild.getTag();

            switch (tag)
            {
                case CONT:
                {
                    nodeParent.setObject(nodeParent.getObject().contValue(
                        lineChild.getValue()));
                    rToBeRemoved.add(nodeChild);
                }
                break;

                case CONC:
                {
                    nodeParent.setObject(nodeParent.getObject().concValue(
                        lineChild.getValue()));
                    rToBeRemoved.add(nodeChild);
                }
                break;

                default:
                    // we don't do anything with tags other than CONT or CONC
            }
        }

        for (final TreeNode<GedcomLine> nodeRemove : rToBeRemoved)
        {
            nodeRemove.removeFromParent();
        }
    }
}
