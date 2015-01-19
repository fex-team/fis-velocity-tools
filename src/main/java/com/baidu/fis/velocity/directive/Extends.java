package com.baidu.fis.velocity.directive;

import com.baidu.fis.util.Resource;
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

public class Extends extends AbstractInclude {

    final static String KEY = Extends.class.getName();

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
        Resource fisResource = Util.getResource(context);

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
        }

        return true;
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


        Node content = node.jjtGetChild(node.jjtGetNumChildren() - 1);
        ArrayList<Node> children = new ArrayList<Node>();
        Util.ExtendInfo info = new Util.ExtendInfo(node);

        for (int i = 0, len = content.jjtGetNumChildren(); i < len; i++) {
            Node child = content.jjtGetChild(i);
            String blockId;

            // 找出 content 节点
            if (child instanceof ASTDirective &&
                    ((ASTDirective)child).getDirectiveName().equals("block") && child.jjtGetNumChildren() > 0) {
                blockId = child.jjtGetChild(0).value(context).toString();

                info.putBlock(blockId, child);
                continue;
            }

            children.add(child);
        }


        if (Util.currentExtendInfo(context, node.getTemplateName()) != null) {
            info.setParent(Util.currentExtendInfo(context, node.getTemplateName()));
        }

        context.put(KEY, info);
        super.render(context, writer, node);

        StringWriter useless = new StringWriter();

        // 把 rest 的 Node 渲染了
        for (Node child:children) {
            child.render(context, useless);
        }
    }

    /**
     * This creates and places the scope control for this directive
     * into the context (if scope provision is turned on).
     *
     * @param context
     */
    @Override
    protected void preRender(InternalContextAdapter context) {

        List macroLibraries = context.getMacroLibraries();
        String templateName = macroLibraries.get(macroLibraries.size() - 1).toString();

        Util.assignExtendInfo(context, templateName, (Util.ExtendInfo)context.get(KEY));

        super.preRender(context);
    }
}
