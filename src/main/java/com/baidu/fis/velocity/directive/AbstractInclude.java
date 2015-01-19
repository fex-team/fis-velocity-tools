package com.baidu.fis.velocity.directive;

import com.alibaba.fastjson.JSONObject;
import com.baidu.fis.util.Resource;
import com.baidu.fis.velocity.event.IncludeFisSource;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.ChainedInternalContextAdapter;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract public class AbstractInclude extends Parse {
    protected static class FisLocalContext extends ChainedInternalContextAdapter {
        private Context localContext;
        private EventCartridge eventCartridge = null;

        public FisLocalContext(InternalContextAdapter inner) {
            super(inner);
            localContext = new VelocityContext();
        }

        /**
         *  Put method also stores values in local scope
         *
         *  @param key name of item to set
         *  @param value object to set to key
         *  @return old stored object
         */
        public Object put(String key, Object value)
        {
            if (localContext != null)
            {
                return localContext.put(key, value);
            }
            return super.put(key, value);
        }

        /**
         *  Retrieves from local or global context.
         *
         *  @param key name of item to get
         *  @return  stored object or null
         */
        public Object get( String key )
        {
        /*
         *  always try the local context then innerContext
         */
            Object o = null;
            if (localContext != null)
            {
                o = localContext.get(key);
            }
            if (o == null)
            {
                o = super.get( key );
            }
            return o;
        }

        /**
         * @see org.apache.velocity.context.Context#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key)
        {
            return (localContext != null && localContext.containsKey(key)) ||
                    super.containsKey(key);
        }

        /**
         * @see org.apache.velocity.context.Context#getKeys()
         */
        @SuppressWarnings("unchecked")
        public Object[] getKeys()
        {
            if (localContext != null)
            {
                Set keys = new HashSet();
                Object[] localKeys = localContext.getKeys();
                for (int i=0; i < localKeys.length; i++)
                {
                    keys.add(localKeys[i]);
                }

                Object[] innerKeys = super.getKeys();
                for (int i=0; i < innerKeys.length; i++)
                {
                    keys.add(innerKeys[i]);
                }
                return keys.toArray();
            }
            return super.getKeys();
        }

        /**
         * @see org.apache.velocity.context.Context#remove(java.lang.Object)
         */
        public Object remove(Object key)
        {
            if (localContext != null)
            {
                return localContext.remove(key);
            }
            return super.remove(key);
        }

        /**
         * Allows callers to explicitly put objects in the local context.
         * Objects added to the context through this method always end up
         * in the top-level context of possible wrapped contexts.
         *
         *  @param key name of item to set.
         *  @param value object to set to key.
         *  @return old stored object
         */
        public Object localPut(final String key, final Object value)
        {
            if (localContext != null)
            {
                return localContext.put(key, value);
            }
            return super.localPut(key, value);
        }

        @Override
        public EventCartridge attachEventCartridge(EventCartridge ec) {
            Context context = this.innerContext.getInternalUserContext();

            if ( !(context instanceof InternalEventContext) ) {
                EventCartridge tmp = eventCartridge;
                eventCartridge = ec;
                return tmp;
            }

            return super.attachEventCartridge(ec);
        }

        @Override
        public EventCartridge getEventCartridge() {
            if (eventCartridge!=null) {
                return eventCartridge;
            }

            return super.getEventCartridge();
        }
    }

    protected Log log;

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        log = rs.getLog();
        super.init(rs, context, node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        Resource fisResource = Util.getResource(context);

        try {
            // 添加资源 Like Require
            fisResource.addResource(node.jjtGetChild(0).value(context).toString());
        } catch (Exception err) {
            // do nothings
        }

        FisLocalContext ctx = new FisLocalContext(context);
        //  支持 include fis id.
        IncludeFisSource eh = new IncludeFisSource();
        eh.setFisResource(fisResource);

        EventCartridge ec = new EventCartridge();
        ec.addIncludeEventHandler(eh);
        ctx.attachEventCartridge(ec);

        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            Node arg = node.jjtGetChild(i);

            if (arg.getType() !=  ParserTreeConstants.JJTSTRINGLITERAL) {
                continue;
            }

            String value = arg.literal();
            value = value.substring(1, value.length() - 1);

            if (value.startsWith("with:")) {
                value = value.substring(5);

                if (!value.startsWith("$")) {
                    value = "$" + value;
                }

                Object obj = this.wrapValue(value, context);

                if (obj != null && obj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    Set<String> keys = map.keySet();

                    for(String key:keys) {
                        ctx.localPut(key, map.get(key));
                    }
                }
            } else if (value.startsWith("var:") && value.contains("=")) {
                value = value.substring(4);
                int idx = value.indexOf("=");
                String key = value.substring(0, idx);
                value = value.substring(idx + 1);
                Object ret = this.wrapValue(value, context);

                if (ret != null ) {
                    ctx.localPut(key, ret);
                }
            }
        }

        Boolean result = super.render(ctx, writer, node);

        eh.setFisResource(null);
//        ResourceManager.unRef(context);

        return result;
    }

    protected Object wrapValue(String value, InternalContextAdapter ctx) {
        Object ret = null;

        if (value.startsWith("{") && value.endsWith("}")) {
            try {
                ret = JSONObject.parse(value);
            } catch (Exception e) {
                System.out.print(e.getMessage());
            }
        } else {
            /*
             * The new string needs to be parsed since the text has been dynamically generated.
             */
            String templateName = ctx.getCurrentTemplateName();

            try {
                SimpleNode nodeTree = rsvc.parse(new StringReader(value), templateName, false);
                nodeTree.init(ctx, rsvc);

                // 如果只有一个节点，且是 reference, 则尝试去获取这个变量的值。
                if (nodeTree.jjtGetNumChildren() == 1 && nodeTree.jjtGetChild(0).getType() == ParserTreeConstants.JJTREFERENCE) {
                    Node right = nodeTree.jjtGetChild(0);
                    ret = right.value(ctx);
                } else {
                    StringWriter buff = new StringWriter();

                    try {
                        nodeTree.render(ctx, buff);
                    } catch (Exception err) {
                        System.out.println(err.getMessage());
                    }

                    ret = buff.toString();
                }
            } catch (Exception err) {
                System.out.println(err.getMessage());
            }
        }

        return ret;
    }
}
