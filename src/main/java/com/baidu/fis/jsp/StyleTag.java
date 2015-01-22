package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by 2betop on 15/1/16.
 */
public class StyleTag extends SimpleTagSupport {
    private String href = null;


    private String prefix;
    private String affix;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setAffix(String affix) {
        this.affix = affix;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public void doTag() throws JspException, IOException {
        Resource resource = Util.getResource(getJspContext());

        if (href == null) {
            Writer buffer = new StringWriter();
            getJspBody().invoke(buffer);

            resource.addCSSEmbed(buffer.toString(), prefix, affix);
        } else {
            resource.addCSS(href, prefix, affix);
        }
    }
}


