package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class RequireTag extends SimpleTagSupport {

    private String name = null;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // alias

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String prefix;
    private String affix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAffix() {
        return affix;
    }

    public void setAffix(String affix) {
        this.affix = affix;
    }

    @Override
    public void doTag() throws JspException, IOException {
        Resource resource = Util.getResource(getJspContext());

        String res = name;

        if (res == null && id != null) {
            res = id;
        }

        if (resource.exists(res)) {
            resource.addResource(res, false, false, prefix, affix);
        } else {
            throw new RuntimeException("Resource not found: " + res);
        }
    }
}
