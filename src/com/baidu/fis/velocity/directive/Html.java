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

public class Html extends Block {
    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {

        super.init(rs, context, node);

        // 初始化 fis 的 Resource 模块。
        ResourceSingleton.init(rs);
    }

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

        String str = buffer.toString();

        str = str.replace(Resource.STYLE_PLACEHOLDER, ResourceSingleton.renderCSS());
        str = str.replace(Resource.SCRIPT_PLACEHOLDER, ResourceSingleton.renderJS());

        writer.write(str);

        // 重要：必须得重置，否则会出现资源重复。
        ResourceSingleton.reset();
        return true;
    }
}
