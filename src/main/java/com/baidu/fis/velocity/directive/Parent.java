package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

public class Parent extends AbstractInline {
    @Override
    public String getName() {
        return "parent";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        Node blockNode = Util.findAncestorDirectiveWithName(node, "block");

        if (blockNode != null && (blockNode = Util.getBlockParent(context, blockNode)) != null) {
            blockNode.jjtGetChild(blockNode.jjtGetNumChildren()-1).render(context, writer);
        }

        return true;
    }
}
