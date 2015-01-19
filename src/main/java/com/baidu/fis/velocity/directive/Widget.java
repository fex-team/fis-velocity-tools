package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

public class Widget extends AbstractInclude {

    @Override
    public String getName() {
        return "widget";
    }

    @Override
    public String getScopeName() {
        return "widget";
    }


    @Override
    @SuppressWarnings("unchecked")
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        Resource fisResource = Util.getResource(context);

        try {
            // 添加资源 Like Require
            fisResource.addResource(node.jjtGetChild(0).value(context).toString());

        } catch (Exception err) {
            writer.write(err.getMessage() + " " + Log.formatFileString(this));
            log.warn(err.getStackTrace());
        }

        super.render(context, writer, node);
//        ResourceManager.unRef(context);
        return true;
    }


}
