package net.lenni0451.optconfig.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class YamlUtils {

    public static Yaml createYaml(final Consumer<LoaderOptions> loaderOptionsConsumer, final Consumer<DumperOptions> dumperOptionsConsumer) {
        LoaderOptions loaderOptions = new LoaderOptions();
        DumperOptions dumperOptions = new DumperOptions();
        applyDefaultYamlOptions(loaderOptions, dumperOptions, loaderOptionsConsumer, dumperOptionsConsumer);
        return new Yaml(new SafeConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions); //Use safe constructor to prevent code execution
    }

    public static void applyDefaultYamlOptions(final LoaderOptions loaderOptions, final DumperOptions dumperOptions) {
        applyDefaultYamlOptions(loaderOptions, dumperOptions, o -> {}, o -> {});
    }

    public static void applyDefaultYamlOptions(final LoaderOptions loaderOptions, final DumperOptions dumperOptions, final Consumer<LoaderOptions> loaderOptionsConsumer, final Consumer<DumperOptions> dumperOptionsConsumer) {
        loaderOptionsConsumer.accept(loaderOptions);
        loaderOptions.setProcessComments(true); //Enable comment parsing

        dumperOptions.setWidth(Integer.MAX_VALUE); //Disable line wrapping
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); //Set the default flow style to block
        dumperOptions.setIndentWithIndicator(true); //Allow lists and maps to be indented
        dumperOptions.setIndicatorIndent(2); //Set the list indent to 2
        dumperOptionsConsumer.accept(dumperOptions);
        dumperOptions.setProcessComments(true); //Enable comment writing
    }

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

    public static boolean copyValues(final Node from, final Node to) {
        if (from instanceof MappingNode && to instanceof MappingNode) {
            MappingNode fromMap = (MappingNode) from;
            MappingNode toMap = (MappingNode) to;
            for (NodeTuple fromTuple : fromMap.getValue()) {
                NodeTuple toTuple = get(toMap, ((ScalarNode) fromTuple.getKeyNode()).getValue());
                if (toTuple == null) {
                    //If the node does not exist in the target, add it
                    int index = fromMap.getValue().indexOf(fromTuple);
                    insert(toMap, fromTuple, index);
                } else if (!copyValues(fromTuple.getValueNode(), toTuple.getValueNode())) {
                    //If the node was not merged, replace the value
                    NodeTuple newTuple = new NodeTuple(toTuple.getKeyNode(), fromTuple.getValueNode());
                    toMap.getValue().set(toMap.getValue().indexOf(toTuple), newTuple);

                    //Copy the comments of the old value to the new value
                    newTuple.getValueNode().setBlockComments(toTuple.getValueNode().getBlockComments());
                    newTuple.getValueNode().setEndComments(toTuple.getValueNode().getEndComments());
                    newTuple.getValueNode().setInLineComments(toTuple.getValueNode().getInLineComments());
                }
            }
            return true;
        } else if (from instanceof SequenceNode && to instanceof SequenceNode) {
            //Go through all elements of the sequences and merge them
            SequenceNode fromSeq = (SequenceNode) from;
            SequenceNode toSeq = (SequenceNode) to;
            List<Node> newNodes = new ArrayList<>();
            FROM_LOOP:
            for (Node fromNode : fromSeq.getValue()) {
                for (Node toNode : toSeq.getValue()) {
                    if (equals(fromNode, toNode)) {
                        newNodes.add(toNode);
                        continue FROM_LOOP;
                    }
                }
                newNodes.add(fromNode);
            }
            toSeq.getValue().clear();
            toSeq.getValue().addAll(newNodes);
            return true;
        }
        return false;
    }

    public static boolean equals(final Node node1, final Node node2) {
        if (node1 instanceof MappingNode && node2 instanceof MappingNode) {
            MappingNode map1 = (MappingNode) node1;
            MappingNode map2 = (MappingNode) node2;
            if (map1.getValue().size() != map2.getValue().size()) return false;
            for (NodeTuple tuple : map1.getValue()) {
                NodeTuple otherTuple = get(map2, ((ScalarNode) tuple.getKeyNode()).getValue());
                if (otherTuple == null || !equals(tuple.getValueNode(), otherTuple.getValueNode())) return false;
            }
            return true;
        } else if (node1 instanceof SequenceNode && node2 instanceof SequenceNode) {
            SequenceNode seq1 = (SequenceNode) node1;
            SequenceNode seq2 = (SequenceNode) node2;
            if (seq1.getValue().size() != seq2.getValue().size()) return false;
            for (int i = 0; i < seq1.getValue().size(); i++) {
                if (!equals(seq1.getValue().get(i), seq2.getValue().get(i))) return false;
            }
            return true;
        } else if (node1 instanceof ScalarNode && node2 instanceof ScalarNode) {
            ScalarNode scalar1 = (ScalarNode) node1;
            ScalarNode scalar2 = (ScalarNode) node2;
            return Objects.equals(scalar1.getValue(), scalar2.getValue());
        }
        return false;
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
