package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by liaoxuezhi on 5/4/14.
 */
public class Style extends AbstractBlock {
    @Override
    public String getName() {
        return "style";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        this.avoidEmbedSelf(node);
        connectFis(context);

        // 指定了 url
        if (node.jjtGetNumChildren() > 1) {
            String uri = node.jjtGetChild(0).value(context).toString();
            fisResource.addCSS(uri);
        } else {
            StringWriter embed = new StringWriter();

            // 让父级去渲染 block body。
            super.render(context, embed);

            fisResource.addCSSEmbed(embed.toString());
        }

        disConnectFis(context);

        return true;
    }
}
