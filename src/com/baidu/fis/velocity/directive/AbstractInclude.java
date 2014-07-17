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
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by 2betop on 5/21/14.
 */
abstract public class AbstractInclude extends Parse {
    protected Resource fisResource = null;
    protected Log log;

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        log = rs.getLog();
        super.init(rs, context, node);
    }

    protected Resource connectFis(InternalContextAdapter context) {
        if (fisResource == null) {
            fisResource = Resource.connect(context, rsvc);
            fisResource.init(rsvc);
        }
        return fisResource;
    }

    protected void disConnectFis(InternalContextAdapter context) {
        if (fisResource == null) {
            return;
        }

        fisResource.disConnect(context);
        fisResource = null;
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

        eh.setFisResource(null);
        context.attachEventCartridge(new EventCartridge());

        return result;
    }
}
