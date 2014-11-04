package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Block extends AbstractBlock {
    private static final String TEMPLATE_KEY = AbstractBlock.class.getName() + "templates-stack";
    private static final String BLOCK_KEY = AbstractBlock.class.getName() + "blocks-stack";

    @SuppressWarnings("unchecked")
    public static void pushTemplate(InternalContextAdapter ctx, String template) {
        Stack<String> templates = (Stack<String>)ctx.get(TEMPLATE_KEY);

        if (templates == null) {
            templates = new Stack<String>();
            ctx.put(TEMPLATE_KEY, templates);
        }

        templates.push(template);
    }

    @SuppressWarnings("unchecked")
    public static String popTemplate(InternalContextAdapter ctx) {
        Stack<String> templates = (Stack<String>)ctx.get(TEMPLATE_KEY);
        String template = "";

        if (templates == null || templates.isEmpty()) {
            return template;
        }

        template = templates.pop();

        // self clean up.
        if (templates.isEmpty()) {
            ctx.remove(TEMPLATE_KEY);
        }

        return template;
    }

    @SuppressWarnings("unchecked")
    public static Stack<String> getTempplateStack(InternalContextAdapter ctx) {
        return (Stack<String>)ctx.get(TEMPLATE_KEY);
    }

    @SuppressWarnings("unchecked")
    public static void registerBlocks(InternalContextAdapter ctx, String templateName, Map<String, Node> map) {
        Map<String, Map<String, Node>> blocks = (Map<String, Map<String, Node>>)ctx.get(BLOCK_KEY);

        if (blocks == null) {
            blocks = new HashMap<String, Map<String, Node>>();
            ctx.put(BLOCK_KEY, blocks);
        }

        blocks.put(templateName, map);
    }

    @SuppressWarnings("unchecked")
    public static void unRegisterBlocks(InternalContextAdapter ctx, String templateName) {

        Map<String, Map<String, Node>> blocks = (Map<String, Map<String, Node>>)ctx.get(BLOCK_KEY);

        if (blocks == null) {
            return;
        }

        blocks.remove(templateName);

        // self clean up.
        if (blocks.isEmpty()) {
            ctx.remove(BLOCK_KEY);
        }
    }

    @Override
    public String getName() {
        return "block";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        if ( node.jjtGetNumChildren() == 0 )
        {
            throw new VelocityException("#block(): argument missing at " +
                    Log.formatFileString(this));
        }

        String id = node.jjtGetChild(0).value(context).toString();

        if ( id.isEmpty() ) {
            throw new VelocityException("#block(): the first argument is empty ");
        }



        // 是否被覆盖
        Boolean overrated = false;
        Stack<String> templates = getTempplateStack(context);

        if (templates!=null) {
            Stack<String> buffer = new Stack<String>();
            Node extend = null;

            while (!templates.isEmpty()) {
                String templateName = templates.pop();
                buffer.push(templateName);

                Map<String, Map<String, Node>> blocks = (Map<String, Map<String, Node>>)context.get(BLOCK_KEY);
                if (blocks == null) {
                    continue;
                }

                Map<String, Node> map = blocks.get(templateName);
                if (map != null) {

                    if (map.get(id) != null && map.get(id) != node) {
                        overrated = true;
                        extend = map.get(id);
                        break;
                    }
                }
            }

            if (extend != null) {
                try {
                    extend.render(context, writer);
                } catch (Exception err) {
                    // todo
                }
            }

            while (!buffer.isEmpty()) {
                templates.push(buffer.pop());
            }
        }

        if (!overrated) {
            Node block = node.jjtGetChild(node.jjtGetNumChildren() - 1);
            block.render(context, writer);
        }

        return true;
    }
}
