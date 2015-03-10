package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Util {

    private static final String KEY = Resource.class.toString();

    public static Resource getResource(InternalContextAdapter ctx) {
        Resource resource = (Resource)ctx.get(KEY);

        if (resource == null) {
            resource = new Resource();
            ctx.put(KEY, resource);
        }

        resource.refs++;

        return resource;
    }

    public static class ExtendInfo {
        private Node node;

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        public ExtendInfo(Node node) {
            this.node = node;
        }

        private ExtendInfo parent;

        public ExtendInfo getParent() {
            return parent;
        }

        public void setParent(ExtendInfo parent) {
            this.parent = parent;
        }

        private Map<String, Node> blocks = new HashMap<String, Node>();

        public void putBlock(String id, Node block) {
            blocks.put(id, block);
        }

        public Node getBlock(String id) {
            return blocks.get(id);
        }

//        final static String key = ExtendInfo.class.getName();
    }

    final static String EXTEND_KEY = Util.class.getName() + ".extend.";
    public static ExtendInfo currentExtendInfo(InternalContextAdapter ctx, String tplName) {
        return (ExtendInfo)ctx.get(EXTEND_KEY + tplName);
    }

    public static void assignExtendInfo(InternalContextAdapter ctx, String tplName, ExtendInfo info) {
        ctx.put(EXTEND_KEY + tplName, info);
    }


    final static String PARENT_BLOCK_KEY = Util.class.getName() + ".parent.block.";
    public static void setBlockParent(InternalContextAdapter ctx, Node node, Node parent) {
        ctx.put(PARENT_BLOCK_KEY + node.hashCode(), parent);
    }

    public static Node getBlockParent(InternalContextAdapter ctx, Node node) {
        return (Node)ctx.get(PARENT_BLOCK_KEY + node.hashCode());
    }

    public static Node findAncestorDirectiveWithName(Node node, String directive) {
        while (node != null) {
            if (node instanceof ASTDirective &&
                    ((ASTDirective)node).getDirectiveName().equals(directive)) {
                return node;
            }

            node = node.jjtGetParent();
        }

        return null;
    }
}
