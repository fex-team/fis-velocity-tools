package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;
import com.baidu.fis.util.Settings;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

public class FilterTag extends BodyTagSupport {
    private String framework;

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    private String mapDir;

    public String getMapDir() {
        return mapDir;
    }

    public void setMapDir(String mapDir) {
        this.mapDir = mapDir;
    }

    /**
     * Default processing of the start tag returning EVAL_BODY_BUFFERED.
     *
     * @return EVAL_BODY_BUFFERED
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     */
    @Override
    public int doStartTag() throws JspException {
        if (this.framework != null) {
            Util.getResource(pageContext).setFramework(this.framework);
        }

        if (this.mapDir != null) {
            Settings.put("mapDir", this.mapDir);
        }

        return EVAL_BODY_BUFFERED;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     */
    @Override
    public int doEndTag() throws JspException {

        try {
            BodyContent body = this.getBodyContent();
            String html = body.getString();
            Resource resource = Util.getResource(pageContext);

            html = resource.filterContent(html);
            JspWriter out = pageContext.getOut();

            out.write(html);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return EVAL_PAGE;
    }
}