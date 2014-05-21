package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.Resource;
import com.baidu.fis.velocity.event.IncludeFisSource;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by 2betop on 5/21/14.
 */
abstract public class AbstractInclude extends Parse {
    protected Resource fisResource = null;

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        fisResource = Resource.getByVelocityRS(rs);
        super.init(rs, context, node);
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        //  支持 include fis id.
        EventCartridge ec = new EventCartridge();
        IncludeFisSource eh = new IncludeFisSource();
        eh.setFisResource(fisResource);

        ec.addEventHandler(eh);
        context.attachEventCartridge(ec);


        Boolean result = super.render(context, writer, node);

        context.attachEventCartridge(null);

        return result;
    }
}
