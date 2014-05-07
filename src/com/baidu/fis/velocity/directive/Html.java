package com.baidu.fis.velocity.directive;


import com.baidu.fis.velocity.Resource;
import com.baidu.fis.velocity.ResourceSingleton;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Html extends abstractBlock {

    @Override
    public String getName() {
        return "html";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        String framework;
        StringWriter buffer = new StringWriter();

        buffer.write("<html");

        // 如果指定了 framework （通过第一个参数指定）
        // 如: #html( "static/js/mod.js")#end
        if (node.jjtGetNumChildren() > 1) {
            framework = node.jjtGetChild(0).value(context).toString();
            ResourceSingleton.setFramework(framework);

            // 生成attributes
            buffer.write(this.buildAttrs(node, context, 1));
        }

        buffer.write(">");

        // 让父级去渲染 block body。
        super.render(context, buffer);

        buffer.write("</html>");

        // todo filterContent 应该放在整个velocity输出的最后。
        // todo 建议添加个 tomcat filter 控制在内容输出到页面的时候来做替换。
        writer.write(ResourceSingleton.filterContent(buffer.toString()));

        return true;
    }
}
