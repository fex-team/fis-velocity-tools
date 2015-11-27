package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by 2betop on 15/1/20.
 */
public class Filter extends AbstractBlock {
    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Resource fisResource = Util.getResource(context);

        Writer buffer = new StringWriter();
        String framework;
        // 如果指定了 framework （通过第一个参数指定）
        // 如: #html( "static/js/mod.js")#end
        if (node.jjtGetNumChildren() > 1) {
            framework = node.jjtGetChild(0).value(context).toString();
            fisResource.setFramework(framework);
        }

        // 让父级去渲染 block body。
        super.render(context, buffer);

        writer.write(fisResource.filterContent(buffer.toString()));

        return true;
    }
}
