package com.baidu.fis.jsp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.Writer;

public class BlockTag extends SimpleTagSupport {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private BlockTag parentBlock;

    public BlockTag getParentBlock() {
        return parentBlock;
    }

    public void setParentBlock(BlockTag parentBlock) {
        this.parentBlock = parentBlock;
    }

    @Override
    public void doTag() throws JspException, IOException {
        ExtendsTag extendsTag = (ExtendsTag) findAncestorWithClass(this, ExtendsTag.class);
        Writer out = getJspContext().getOut();

        // 说明不是在 extend 中，则要看是否有覆盖。
        if (extendsTag == null || (findAncestorWithClass(this, BlockTag.class)) != null ) {

            BlockTag blockTag = this;

            extendsTag = Util.currentExtendTag(getJspContext());

            while (extendsTag != null) {
                if (extendsTag.getBlock(name) != null) {
                    extendsTag.getBlock(name).setParentBlock(blockTag);
                    blockTag = extendsTag.getBlock(name);
                }

                extendsTag = extendsTag.getParentExtendsTag();
            }

            blockTag.render(out);
        } else {
            extendsTag.putBlock(name, this);
        }
    }

    public void render(Writer writer) throws IOException, JspException {
        if (getJspBody() != null) {
            getJspBody().invoke(writer);
        }
    }

    @Override
    public String toString() {
        return "BlockTag{" +
                "name='" + name + '\'' +
                '}';
    }
}
