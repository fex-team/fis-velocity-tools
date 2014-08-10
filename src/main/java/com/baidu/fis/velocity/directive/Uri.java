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
public class Uri extends AbstractInline {
    @Override
    public String getName() {
        return "uri";
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#require(): argument missing at " +
                    Log.formatFileString(this));
        }

        connectFis(context);

        try {
            writer.write(fisResource.getUri(node.jjtGetChild(0).value(context).toString()));
        } catch (Exception err) {
            throw new VelocityException(err.getMessage() +
                    Log.formatFileString(this));
        }

        disConnectFis(context);

        return true;
    }
}
