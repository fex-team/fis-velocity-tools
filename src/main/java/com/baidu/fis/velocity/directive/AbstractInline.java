package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Created by 2betop on 5/21/14.
 */
abstract public class AbstractInline extends Directive {
    protected Resource fisResource = null;
    protected Log log;

    @Override
    public int getType() {
        return LINE;
    }

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
}
