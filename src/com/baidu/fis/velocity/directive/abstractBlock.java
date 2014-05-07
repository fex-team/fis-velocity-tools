package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.ResourceSingleton;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * 提取一些公共的方法
 * Created by 2betop on 5/4/14.
 */
public abstract class abstractBlock extends org.apache.velocity.runtime.directive.Block {
    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);

        // 初始化 fis 的 Resource 模块。
        ResourceSingleton.init(rs);
    }

    protected String buildAttrs(Node node, InternalContextAdapter context, int start) {
        return this.buildAttrs(node, context, start, -1);
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

        if (end == -1) {
            end = node.jjtGetNumChildren() - 2;
        }

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
}
