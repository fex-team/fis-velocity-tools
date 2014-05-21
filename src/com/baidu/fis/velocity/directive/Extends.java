package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * Created by 2betop on 5/7/14.
 */
public class Extends extends AbstractInclude {

    protected Map<String, Stack<Node>> map;

    @Override
    public String getName() {
        return "extends";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        Boolean isTopNode = false;
        Writer buffer = writer;

        Node parent = node.jjtGetParent();
        if ((parent == null || parent instanceof ASTprocess) &&
                context.getTemplateNameStack().length < 2) {
            isTopNode = true;
        }

        if (isTopNode) {
            buffer = new StringWriter();
        }

        this.doRender(context, buffer, node);

        // 只有当它为顶级 node 的时候才这么做，当然不能是被extends时。
        if (isTopNode) {
            writer.write(fisResource.filterContent(buffer.toString()));
            fisResource.reset();
        }

        return true;
    }

    @Override
    protected void preRender(InternalContextAdapter context) {

        if (this.map != null) {
            List macroLibraries = context.getMacroLibraries();
            String templateName = macroLibraries.get(macroLibraries.size() - 1).toString();

            Block.registerBlocks(templateName, this.map);
        }

        super.preRender(context);
    }

    @Override
    protected void postRender(InternalContextAdapter context) {
        Block.unRegisterBlocks(context.getCurrentTemplateName());

        this.map.clear();
        this.map = null;

        super.postRender(context);
    }

    protected void doRender(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        /*
         *  did we get an argument?
         */
        if ( node.jjtGetNumChildren() == 1 )
        {
            throw new VelocityException("#extends(): argument missing at " +
                    Log.formatFileString(this));
        }

        String target = node.jjtGetChild(0).value(context).toString();

        if (target.isEmpty()) {
            throw  new VelocityException("#extends(): the first argument is empty.");
        }


        // 先将 block 与基模板中的block绑定。
        Node block = node.jjtGetChild(node.jjtGetNumChildren() - 1);
        Map<String, Stack<Node>> map = new HashMap<String, Stack<Node>>();
        Stack<Node> list;
        Node child;
        String blockId;
        ArrayList<Node> rest = new ArrayList<Node>();

        for (int i = 0, len = block.jjtGetNumChildren(); i < len; i++) {
            child = block.jjtGetChild(i);

            // 找出 block 节点
            if (child instanceof ASTDirective &&
                    ((ASTDirective)child).getDirectiveName().equals("block")) {
                blockId = child.jjtGetChild(0).value(context).toString();

                list = map.get(blockId);
                if (list == null) {
                    list = new Stack<Node>();
                    map.put(blockId, list);
                }

                list.add(child);
            } else {
                rest.add(child);
            }
        }

        if (!map.isEmpty()) {
            this.map = map;
        }

        super.render(context, writer, node);

        // 把 rest 的 Node 渲染了
        for (Node _rest:rest) {
            _rest.render(context, writer);
        }
    }
}
