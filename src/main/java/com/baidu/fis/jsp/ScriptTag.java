package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ScriptTag extends SimpleTagSupport {
    private String src = null;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
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

        if (src == null) {
            Writer buffer = new StringWriter();
            getJspBody().invoke(buffer);

            resource.addJSEmbed(buffer.toString(), prefix, affix);
        } else {
            resource.addJS(src, prefix, affix);
        }
    }
}
