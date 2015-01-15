package com.baidu.fis.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class ParentTag extends SimpleTagSupport {
    @Override
    public void doTag() throws JspException, IOException {
        BlockTag blockTag = (BlockTag) findAncestorWithClass(this, BlockTag.class);

        if (blockTag == null) {
            throw new RuntimeException("Parent tag must be called in a block tag.");
        }

        if (blockTag.getParentBlock() != null) {
            blockTag.getParentBlock().render(getJspContext().getOut());
        }
    }
}
