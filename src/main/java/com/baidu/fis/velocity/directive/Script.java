package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Script extends AbstractBlock {
    @Override
    public String getName() {
        return "script";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        this.avoidEmbedSelf(node);
        Resource fisResource = Util.getResource(context);

        String prefix = null;
        String affix = null;

        // js embed
        if (node.jjtGetChild(node.jjtGetNumChildren() -1).jjtGetNumChildren() > 0) {
            if (node.jjtGetNumChildren() > 1) {
                prefix = node.jjtGetChild(0).value(context).toString();
            }

            if (node.jjtGetNumChildren() > 2) {
                affix = node.jjtGetChild(1).value(context).toString();
            }

            StringWriter embed = new StringWriter();

            // 让父级去渲染 block body。
            super.render(context, embed);

            fisResource.addJSEmbed(embed.toString(), prefix, affix);
        } else {
            String uri = node.jjtGetChild(0).value(context).toString();

            if (node.jjtGetNumChildren() > 2) {
                prefix = node.jjtGetChild(1).value(context).toString();
            }

            if (node.jjtGetNumChildren() > 3) {
                affix = node.jjtGetChild(2).value(context).toString();
            }

            fisResource.addJS(uri, prefix, affix);
        }

//        ResourceManager.unRef(context);
        return true;
    }
}
