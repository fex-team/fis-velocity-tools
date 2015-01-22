package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;
import com.baidu.fis.util.Settings;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ExtendsTag extends SimpleTagSupport implements DynamicAttributes {
    private String name = null;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private Map<String, Object> attrs = new HashMap<String, Object>();

    @Override
    public void setDynamicAttribute(String s, String s2, Object o) throws JspException {
        attrs.put(s2, o);
    }

    private Map<String, BlockTag> blocks = new HashMap<String, BlockTag>();

    public void putBlock(String id, BlockTag tag) {
        blocks.put(id, tag);
    }

    public BlockTag getBlock(String id) {
        return blocks.get(id);
    }

    private ExtendsTag parentExtendsTag;

    public ExtendsTag getParentExtendsTag() {
        return parentExtendsTag;
    }

    public void setParentExtendsTag(ExtendsTag parentExtendsTag) {
        this.parentExtendsTag = parentExtendsTag;
    }

    @Override
    public void doTag() throws JspException, IOException {
        getJspBody().invoke(getJspContext().getOut());

        Resource resource = Util.getResource(getJspContext());
        String path = name;

        if (resource.exists(name)) {
            path = resource.addResource(name);
        }

        try {
            ServletContext servletContext = ((PageContext)getJspContext()).getServletContext();

            if (servletContext.getResource(path) == null) {

                path = Settings.getString("jspDir", "/WEB-INF/views/") + path;

                if (servletContext.getResource(path) == null) {
                    throw new IOException("Cannot resolve resource " + path);
                }
            }

            if (Util.currentExtendTag(getJspContext()) != null) {
                this.setParentExtendsTag(Util.currentExtendTag(getJspContext()));
            }

            JspContext context = getJspContext();
            Util.pushExtendsTag(context, this);

            try {
                for (String key:attrs.keySet()) {
                    Object value = attrs.get(key);

                    if (key.equals("with")) {
                        Map map = (Map)value;

                        if (map != null) {
                            for (Object key2:map.keySet()) {
                                context.setAttribute(key2.toString(), map.get(key2), PageContext.REQUEST_SCOPE);
                            }
                        }

                    } else {
                        context.setAttribute(key, value, PageContext.REQUEST_SCOPE);
                    }
                }

                ((PageContext)context).include(path);
            } finally {

                for (String key:attrs.keySet()) {

                    if (key.equals("with")) {
                        Map map = (Map)attrs.get(key);

                        if (map != null) {
                            for (Object key2:map.keySet()) {
                                context.removeAttribute(key2.toString(), PageContext.REQUEST_SCOPE);
                            }
                        }

                    } else {
                        context.removeAttribute(key, PageContext.REQUEST_SCOPE);
                    }
                }

                Util.popExtendTag(context);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
