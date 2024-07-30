package net.lenni0451.optconfig.utils;

import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class YamlNodeUtils {

    @Nullable
    public static NodeTuple get(final MappingNode mappingNode, final String key) {
        for (NodeTuple tuple : mappingNode.getValue()) {
            ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
            if (keyNode.getValue().equals(key)) return tuple;
        }
        return null;
    }

    public static void insert(final MappingNode mappingNode, final NodeTuple tuple, final int index) {
        if (index == 0 && !mappingNode.getValue().isEmpty()) {
            //Move all unrelated comments from the previous element to the new element
            //The previous element should also get a blank line prepended
            NodeTuple previousTuple = mappingNode.getValue().get(0);
            List<CommentLine> unrelatedComments = getUnrelatedComments(previousTuple.getKeyNode(), true);
            List<CommentLine> previousComments = makeCommentsMutable(previousTuple.getKeyNode());
            if (!unrelatedComments.isEmpty()) {
                List<CommentLine> blockComments = makeCommentsMutable(tuple.getKeyNode());
                blockComments.addAll(0, unrelatedComments);

                //Remove the unrelated comments from the previous element
                previousComments.removeAll(unrelatedComments);
            }
            //Prepend a blank line to the previous element
            previousComments.add(0, new CommentLine(null, null, "\n", CommentType.BLANK_LINE));
        }
        mappingNode.getValue().add(index, tuple);
    }

    public static void replace(final MappingNode mappingNode, final NodeTuple oldNodes, final NodeTuple newNodes) {
        int index = mappingNode.getValue().indexOf(oldNodes);
        mappingNode.getValue().set(index, newNodes);

        //Copy over all unrelated comments from the old node to the new node
        List<CommentLine> unrelatedComments = getUnrelatedComments(oldNodes.getKeyNode(), true);
        if (!unrelatedComments.isEmpty()) {
            removeLeadingBlankLines(newNodes.getKeyNode()); //Remove leading blank lines if there are comments to copy over
            List<CommentLine> blockComments = makeCommentsMutable(newNodes.getKeyNode());
            blockComments.addAll(0, unrelatedComments);
        }
    }

    public static void remove(final MappingNode mappingNode, final String key) {
        NodeTuple tuple = get(mappingNode, key);
        if (tuple == null) return;
        int index = mappingNode.getValue().indexOf(tuple);
        mappingNode.getValue().remove(tuple);

        List<CommentLine> unrelatedComments = getUnrelatedComments(tuple.getKeyNode(), false);
        if ((index == 0 && !unrelatedComments.isEmpty()) || unrelatedComments.size() > 1) {
            //If the first element has unrelated comments, or another element has more than just a blank line, copy all comments to the next element
            List<CommentLine> comments;
            if (index < mappingNode.getValue().size()) {
                NodeTuple nextTuple = mappingNode.getValue().get(index);
                comments = makeCommentsMutable(nextTuple.getKeyNode());
            } else {
                comments = makeMutable(mappingNode.getEndComments());
                mappingNode.setEndComments(comments);
            }
            comments.addAll(0, unrelatedComments);
        }
    }

    public static List<CommentLine> getUnrelatedComments(final Node node, final boolean includeBlank) {
        //Try to find all unrelated comments
        //This is done by finding the last blank line before the node and taking all comments before that
        //This is only an assumption, but it should be better than not taking any comments
        List<CommentLine> comments = makeMutable(node.getBlockComments());
        int cut = 0;
        for (int i = comments.size() - 1; i >= 0; i--) {
            //Find the last blank line
            CommentLine comment = comments.get(i);
            if (comment.getCommentType().equals(CommentType.BLANK_LINE)) {
                cut = i + (includeBlank ? 1 : 0); //Include the blank line if requested
                break;
            }
        }
        return comments.subList(0, cut);
    }

    public static void removeLeadingBlankLines(final Node node) {
        List<CommentLine> comments = node.getBlockComments();
        if (comments == null || comments.isEmpty()) return;
        Iterator<CommentLine> it = comments.iterator();
        while (it.hasNext()) {
            CommentLine comment = it.next();
            if (comment.getCommentType().equals(CommentType.BLANK_LINE)) it.remove();
            else break;
        }
    }

    public static void appendComment(final NodeTuple node, final int commentSpacing, final String... comments) {
        appendComment(node.getKeyNode(), commentSpacing, comments);
    }

    public static void appendComment(final Node node, final int commentSpacing, final String... comments) {
        if (comments.length == 0) return;
        List<CommentLine> blockComments = makeCommentsMutable(node);
        String commentPrefix = "";
        for (int i = 0; i < commentSpacing; i++) commentPrefix += " ";
        for (String comment : comments) {
            if (comment.equals("\n")) {
                blockComments.add(new CommentLine(null, null, "\n", CommentType.BLANK_LINE));
            } else {
                blockComments.add(new CommentLine(null, null, commentPrefix + comment, CommentType.BLOCK));
            }
        }
    }

    public static void recurse(final Node node, final Consumer<Node> consumer) {
        consumer.accept(node);
        if (node instanceof SequenceNode) {
            SequenceNode sequenceNode = (SequenceNode) node;
            for (Node child : sequenceNode.getValue()) recurse(child, consumer);
        } else if (node instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) node;
            for (NodeTuple tuple : mappingNode.getValue()) {
                recurse(tuple.getKeyNode(), consumer);
                recurse(tuple.getValueNode(), consumer);
            }
        }
    }

    public static void copyComments(final MappingNode from, final MappingNode to) {
        to.setBlockComments(from.getBlockComments());
        to.setEndComments(from.getEndComments());
        to.setInLineComments(from.getInLineComments());
        for (NodeTuple tuple : to.getValue()) {
            NodeTuple fromTuple = get(from, ((ScalarNode) tuple.getKeyNode()).getValue());
            if (fromTuple == null) continue;

            tuple.getKeyNode().setBlockComments(fromTuple.getKeyNode().getBlockComments());
            tuple.getKeyNode().setEndComments(fromTuple.getKeyNode().getEndComments());
            tuple.getKeyNode().setInLineComments(fromTuple.getKeyNode().getInLineComments());
            tuple.getValueNode().setBlockComments(fromTuple.getValueNode().getBlockComments());
            tuple.getValueNode().setEndComments(fromTuple.getValueNode().getEndComments());
            tuple.getValueNode().setInLineComments(fromTuple.getValueNode().getInLineComments());

            if (tuple.getValueNode() instanceof MappingNode && fromTuple.getValueNode() instanceof MappingNode) {
                copyComments((MappingNode) fromTuple.getValueNode(), (MappingNode) tuple.getValueNode());
            }
        }
    }

    public static void copyValues(final MappingNode from, final MappingNode to) {
        for (NodeTuple tuple : from.getValue()) {
            NodeTuple toTuple = get(to, ((ScalarNode) tuple.getKeyNode()).getValue());
            if (toTuple == null) {
                //If the node does not exist in the target, add it
                int index = from.getValue().indexOf(tuple);
                insert(to, tuple, index);
            } else {
                //If the node exists, check if it's a mapping node and copy the values
                if (tuple.getValueNode() instanceof MappingNode && toTuple.getValueNode() instanceof MappingNode) {
                    copyValues((MappingNode) tuple.getValueNode(), (MappingNode) toTuple.getValueNode());
                } else {
                    //If it's not a mapping node, just copy the value
                    NodeTuple newTuple = new NodeTuple(toTuple.getKeyNode(), tuple.getValueNode());
                    replace(to, toTuple, newTuple);
                }
            }
        }
    }

    public static List<CommentLine> makeCommentsMutable(final Node node) {
        //Makes the comments of a node mutable
        //This also initializes the comments if they are null
        node.setBlockComments(makeMutable(node.getBlockComments()));
        return node.getBlockComments();
    }

    private static <T> List<T> makeMutable(final List<T> list) {
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

}
