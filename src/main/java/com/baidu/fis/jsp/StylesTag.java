package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by 2betop on 15/1/21.
 */
public class StylesTag extends SimpleTagSupport {

    @Override
    public void doTag() throws JspException, IOException {
        Writer out = getJspContext().getOut();

        try {
            out.write(Resource.STYLE_PLACEHOLDER);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}