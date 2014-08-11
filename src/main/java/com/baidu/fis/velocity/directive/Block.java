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

/**
 * Created by 2betop on 5/7/14.
 */
public class Block extends AbstractBlock {
    private static final String TEMPLATE_KEY = "templates-stack";
    private static final String BLOCK_KEY = "blocks-stack";

    public static void pushTemplate(InternalContextAdapter ctx, String template) {
        Stack<String> templates = (Stack<String>)ctx.get(TEMPLATE_KEY);

        if (templates == null) {
            templates = new Stack<String>();
            ctx.put(TEMPLATE_KEY, templates);
        }

        templates.push(template);
    }

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

    public static String getCurrentTemplate(InternalContextAdapter ctx) {
        Stack<String> templates = (Stack<String>)ctx.get(TEMPLATE_KEY);

        if (templates == null || templates.isEmpty()) {
            return "";
        }

        return templates.peek();
    }

    public static void registerBlocks(InternalContextAdapter ctx, String templateName, Map<String, Node> map) {
        Map<String, Map<String, Node>> blocks = (Map<String, Map<String, Node>>)ctx.get(BLOCK_KEY);

        if (blocks == null) {
            blocks = new HashMap<String, Map<String, Node>>();
            ctx.put(BLOCK_KEY, blocks);
        }

        blocks.put(templateName, map);
    }

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


        String templateName = getCurrentTemplate(context);
        Map<String, Map<String, Node>> blocks = (Map<String, Map<String, Node>>)context.get(BLOCK_KEY);

        if (blocks == null) {
            return true;
        }

        Map<String, Node> map = blocks.get(templateName);
        Boolean overrated = false;
        if (map != null) {

            Node extend = map.get(id);
            if (extend != null) {
                overrated = true;
                map.remove(id);
                popTemplate(context);
                extend.render(context, writer);
                pushTemplate(context, templateName);
            }
        }

        if (!overrated) {
            Node block = node.jjtGetChild(node.jjtGetNumChildren() - 1);
            block.render(context, writer);
        }

        return true;
    }
}
