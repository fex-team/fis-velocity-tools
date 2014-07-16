package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by 2betop on 5/4/14.
 */
public class Require extends AbstractInline {


    @Override
    public String getName() {
        return "require";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#require(): argument missing at " +
                    Log.formatFileString(this));
        }

        try {
            // 只需要把依赖加载上就可以了。
            fisResource.addResource(node.jjtGetChild(0).value(context).toString());
        } catch (Exception err) {
            writer.write(err.getMessage() + " " + Log.formatFileString(this));
            log.warn(err.getStackTrace());
        }

        return true;
    }
}
