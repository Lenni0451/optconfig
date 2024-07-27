package net.lenni0451.optconfig.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YamlNodeUtils {

    public static NodeTuple toNode(final Yaml yaml, final String key, final Object value) {
        return new NodeTuple(yaml.represent(key), yaml.represent(value));
    }

    public static void prependComment(final NodeTuple node, final String... comments) {
        if (comments.length == 0) return;
        node.getKeyNode().setBlockComments(makeMutable(node.getKeyNode().getBlockComments()));
        for (String comment : comments) {
            if (comment.equals("\n")) {
                node.getKeyNode().getBlockComments().add(new CommentLine(null, null, "\n", CommentType.BLANK_LINE));
            } else {
                node.getKeyNode().getBlockComments().add(new CommentLine(null, null, comment, CommentType.BLOCK));
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

    private static <T> List<T> makeMutable(final List<T> list) {
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

}
