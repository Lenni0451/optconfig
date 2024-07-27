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
            List<CommentLine> unrelatedComments = getUnrelatedComments(previousTuple.getKeyNode());
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

        List<CommentLine> unrelatedComments = getUnrelatedComments(oldNodes.getKeyNode());
        if (!unrelatedComments.isEmpty()) {
            List<CommentLine> blockComments = makeCommentsMutable(newNodes.getKeyNode());
            blockComments.addAll(0, unrelatedComments);
        }
    }

    public static void remove(final MappingNode mappingNode, final String key) {
        NodeTuple tuple = get(mappingNode, key);
        if (tuple == null) return;
        int index = mappingNode.getValue().indexOf(tuple);
        mappingNode.getValue().remove(tuple);

        if (index == 0 && !mappingNode.getValue().isEmpty()) {
            //If the first element is removed, copy all leading comments to the next element
            //Make sure to filter out unrelated comments that are not related to the removed element
            List<CommentLine> unrelatedComments = getUnrelatedComments(tuple.getKeyNode());
            if (!unrelatedComments.isEmpty()) {
                NodeTuple nextTuple = mappingNode.getValue().get(0);
                removeLeadingBlankLines(nextTuple.getKeyNode()); //First remove all leading blank lines
                List<CommentLine> blockComments = makeCommentsMutable(nextTuple.getKeyNode());
                blockComments.addAll(0, unrelatedComments); //And then add the unrelated comments including the blank lines
            }
        }
    }

    public static List<CommentLine> getUnrelatedComments(final Node node) {
        //Try to find all unrelated comments
        //This is done by finding the last blank line before the node and taking all comments before that
        //This is only an assumption, but it should be better than not taking any comments
        List<CommentLine> comments = makeMutable(node.getBlockComments());
        int cut = comments.size();
        for (int i = comments.size() - 1; i >= 0; i--) {
            //Find the last blank line
            CommentLine comment = comments.get(i);
            if (comment.getCommentType().equals(CommentType.BLANK_LINE)) {
                cut = i + 1; //Include the blank line
                break;
            }
        }
        comments = comments.subList(0, cut);
        if (comments.stream().allMatch(comment -> comment.getCommentType().equals(CommentType.BLANK_LINE))) return new ArrayList<>();
        return comments;
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

    public static void appendComment(final NodeTuple node, final String... comments) {
        if (comments.length == 0) return;
        List<CommentLine> blockComments = makeCommentsMutable(node.getKeyNode());
        for (String comment : comments) {
            if (comment.equals("\n")) {
                blockComments.add(new CommentLine(null, null, "\n", CommentType.BLANK_LINE));
            } else {
                blockComments.add(new CommentLine(null, null, comment, CommentType.BLOCK));
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
