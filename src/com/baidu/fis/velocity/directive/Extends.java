package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.ResourceSingleton;
import com.baidu.fis.velocity.event.IncludeFisSource;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created by 2betop on 5/7/14.
 */
public class Extends extends Parse {

    protected Log log;

    protected Map<String, Stack<Node>> map;

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);

        log = rs.getLog();

        // 初始化 fis 的 Resource 模块。
        ResourceSingleton.init(rs);
    }

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

        for (int i = 0, len = block.jjtGetNumChildren(); i < len; i++) {
            child = block.jjtGetChild(i);

            if (child instanceof ASTDirective &&
                    ((ASTDirective)child).getDirectiveName().equals("block")) {
                blockId = child.jjtGetChild(0).value(context).toString();

                list = map.get(blockId);
                if (list == null) {
                    list = new Stack<Node>();
                    map.put(blockId, list);
                }

                list.add(child);
            }
        }

        if (!map.isEmpty()) {
            this.map = map;
        }

        // 开始渲染 extends 对象
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(new IncludeFisSource());
        context.attachEventCartridge(ec);
        super.render(context, writer, node);
        context.attachEventCartridge(null);

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
}
