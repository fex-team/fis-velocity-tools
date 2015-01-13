package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

public class Head extends AbstractBlock {

    @Override
    public String getName() {
        return "head";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        this.avoidEmbedSelf(node);

        writer.write("<head");

        // 生成attributes
        writer.write(this.buildAttrs(node, context, 0));

        writer.write(">");

        // 让父级去渲染 block body。
        super.render(context, writer);

        // 在头部把所有的样式输出。
        writer.write(Resource.STYLE_PLACEHOLDER);

        writer.write("</head>");

        return true;
    }
}
