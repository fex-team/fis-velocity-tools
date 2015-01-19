package com.baidu.fis.velocity.directive;


import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Html extends AbstractBlock {
    @Override
    public String getName() {
        return "html";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Resource fisResource = Util.getResource(context);

        String framework;
        Boolean isTopNode = false;
        Writer buffer = writer;

        this.avoidEmbedSelf(node);

        Node parent = node.jjtGetParent();
        if ((parent == null || parent instanceof ASTprocess) &&
                context.getTemplateNameStack().length < 2) {
            isTopNode = true;
        }

        if (isTopNode) {
            buffer = new StringWriter();
        }

        buffer.write("<html");

        // 如果指定了 framework （通过第一个参数指定）
        // 如: #html( "static/js/mod.js")#end
        if (node.jjtGetNumChildren() > 1) {
            framework = node.jjtGetChild(0).value(context).toString();
            fisResource.setFramework(framework);

            // 生成attributes
            writer.write(this.buildAttrs(node, context, 1));
        }


        buffer.write(">");

        // 让父级去渲染 block body。
        super.render(context, buffer);

        buffer.write("</html>");

        // 只有当它为顶级 node 的时候才这么做，当然不能是被extends时。
        if (isTopNode) {
            writer.write(fisResource.filterContent(buffer.toString()));
        }

//        ResourceManager.unRef(context);

        return true;
    }
}
