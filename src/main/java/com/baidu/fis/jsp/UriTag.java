package com.baidu.fis.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Created by 2betop on 15/1/15.
 */
public class UriTag extends SimpleTagSupport {
    private String name = null;

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Default processing of the tag does nothing.
     *
     * @throws javax.servlet.jsp.JspException      Subclasses can throw JspException to indicate
     *                                             an error occurred while processing this tag.
     * @throws javax.servlet.jsp.SkipPageException If the page that
     *                                             (either directly or indirectly) invoked this tag is to
     *                                             cease evaluation.  A Simple Tag Handler generated from a
     *                                             tag file must throw this exception if an invoked Classic
     *                                             Tag Handler returned SKIP_PAGE or if an invoked Simple
     *                                             Tag Handler threw SkipPageException or if an invoked Jsp Fragment
     *                                             threw a SkipPageException.
     * @throws java.io.IOException                 Subclasses can throw IOException if there was
     *
     */
    @Override
    public void doTag() throws JspException, IOException {
        String uri = Util.getResource(getJspContext()).getUri(name);
        getJspContext().getOut().write(uri == null ? name : uri);
    }
}
