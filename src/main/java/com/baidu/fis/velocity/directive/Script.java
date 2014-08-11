package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.util.Resource;
import com.baidu.fis.velocity.util.ResourceManager;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by 2betop on 5/4/14.
 */
public class Script extends AbstractBlock {
    @Override
    public String getName() {
        return "script";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        this.avoidEmbedSelf(node);
        Resource fisResource = ResourceManager.ref(context);

        // 指定了 url
        if (node.jjtGetNumChildren() > 1) {
            String uri = node.jjtGetChild(0).value(context).toString();
            fisResource.addJS(uri);
        } else {
            StringWriter embed = new StringWriter();

            // 让父级去渲染 block body。
            super.render(context, embed);

            fisResource.addJSEmbed(embed.toString());
        }

        ResourceManager.unRef(context);
        return true;
    }
}
