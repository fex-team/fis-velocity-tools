package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.Resource;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 提取一些公共的方法
 * Created by 2betop on 5/4/14.
 */
public abstract class AbstractBlock extends org.apache.velocity.runtime.directive.Block {

    protected Resource fisResource = null;

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        fisResource = Resource.getByVelocityRS(rs);
        super.init(rs, context, node);
    }

    protected String buildAttrs(Node node, InternalContextAdapter context, int start) {
        return this.buildAttrs(node, context, start, node.jjtGetNumChildren() - 2);
    }

    /**
     * 生成 html attributes
     *
     * Examples:
     * #body("id", "block_id")
     * #end
     *
     * Result:
     * <body id="block_id"></body>
     *
     * @param node
     * @param context
     * @param start 参数开始序号
     * @param end 参数结束位置
     * @return
     */
    protected String buildAttrs(Node node, InternalContextAdapter context, int start, int end) {
        StringBuilder sb;
        Node item;

        if (end < start) {
            return "";
        }

        sb = new StringBuilder();

        for (int i = 0; i + start <= end; i++) {
            item = node.jjtGetChild(i + start);

            if ((i % 2) > 0) {
                sb.append("=\"" + item.value(context).toString().replace("\"", "&quot;") + "\"");
            } else {
                sb.append(" " + item.value(context).toString());
            }
        }

        return sb.toString();
    }

    protected void avoidEmbedSelf(Node node) {
        // style 不能嵌套。
        Node parent = node.jjtGetParent();
        while (parent != null) {
            if (parent instanceof ASTDirective &&
                    ((ASTDirective)parent).getDirectiveName().equals(this.getName())) {
                throw new VelocityException("Can\'t embed #"+ this.getName() +" in #"+ this.getName() +" directive " +
                        Log.formatFileString(this));
            }

            parent = parent.jjtGetParent();
        }
    }
}
