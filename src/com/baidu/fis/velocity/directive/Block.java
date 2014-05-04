package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * 提取一些公共的方法
 * Created by 2betop on 5/4/14.
 */
public abstract class Block extends org.apache.velocity.runtime.directive.Block {

    protected String buildAttrs(Node node, InternalContextAdapter context, int start) {
        return this.buildAttrs(node, context, start, -1);
    }

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
