package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Block extends AbstractBlock {

    @Override
    public String getName() {
        return "block";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#block(): argument missing at " +
                    Log.formatFileString(this));
        }

        String id = node.jjtGetChild(0).value(context).toString();

        if ( id.isEmpty() ) {
            throw new VelocityException("#block(): the first argument is empty ");
        }

        Node blockNode = node;

        Util.ExtendInfo extendInfo = Util.currentExtendInfo(context, node.getTemplateName());

        while (extendInfo != null) {
            if (extendInfo.getBlock(id) != null) {
                Util.setBlockParent(context, extendInfo.getBlock(id), blockNode);
                blockNode = extendInfo.getBlock(id);
            }

            extendInfo = extendInfo.getParent();
        }

        blockNode.jjtGetChild(blockNode.jjtGetNumChildren() - 1).render(context, writer);

        return true;
    }
}
