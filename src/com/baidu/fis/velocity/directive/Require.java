package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.ResourceSingleton;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by 2betop on 5/4/14.
 */
public class Require extends Directive {
    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);

        // 初始化 fis 的 Resource 模块。
        ResourceSingleton.init(rs);
    }

    @Override
    public String getName() {
        return "require";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#require(): argument missing at " +
                    Log.formatFileString(this));
        }

        // 只需要把依赖加载上就可以了。
        ResourceSingleton.addResource(node.jjtGetChild(0).value(context).toString());

        return true;
    }
}
