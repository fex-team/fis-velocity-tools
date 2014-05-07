package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.ResourceSingleton;
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
        StringWriter embed = new StringWriter();

        // embed.write("<style type=\"text/css\"");

        // 生成attributes
        // embed.write(this.buildAttrs(node, context, 0));

        // embed.write(">");

        // 让父级去渲染 block body。
        super.render(context, embed);

        // embed.write("</style>");

        ResourceSingleton.addCSSEmbed(embed.toString());

        return true;
    }
}
