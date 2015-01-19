package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

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

        Resource fisResource = Util.getResource(context);

        try {
            writer.write(fisResource.getUri(node.jjtGetChild(0).value(context).toString()));
        } catch (Exception err) {
            throw new VelocityException(err.getMessage() +
                    Log.formatFileString(this));
        }

//        ResourceManager.unRef(context);
        return true;
    }
}
