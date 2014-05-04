package com.baidu.fis.velocity.directive;

import com.baidu.fis.ResourceSingleton;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by liaoxuezhi on 5/4/14.
 */
public class Widget extends Parse {
    @Override
    public String getName() {
        return "widget";
    }

    @Override
    public String getScopeName() {
        return "widget";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        super.render(context, writer, node);

        // 添加资源 Like Require
        ResourceSingleton.addResource(node.jjtGetChild(0).value(context).toString());

        return true;
    }
}
