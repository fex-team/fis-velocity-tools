package com.baidu.fis.velocity.directive;

import com.baidu.fis.ResourceSingleton;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by liaoxuezhi on 5/4/14.
 */
public class Uri extends Directive {
    @Override
    public String getName() {
        return "uri";
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

        writer.write(ResourceSingleton.getUri(node.jjtGetChild(0).value(context).toString()));

        return true;
    }
}
