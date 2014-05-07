package com.baidu.fis.velocity.directive;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by 2betop on 5/7/14.
 */
public class Block extends AbstractBlock {

    private static Stack<String> opStack = new Stack<String>();
    private static Map<String, StringWriter> blocks = new HashMap<String, StringWriter>();

    final public static String SET_MODE = "<set>";

    public static void pushOp(String op) {
        opStack.push(op);
    }

    public static String popOp() {
        return opStack.pop();
    }

    private Boolean isSetMode() {
        return !opStack.isEmpty() && opStack.peek().equals(SET_MODE);
    }

    private String getSetTarget() {
        if (isSetMode()) {
            for(int i = opStack.size() - 1; i >= 0; i--) {
                if (!opStack.get(i).equals(SET_MODE)) {
                    return opStack.get(i).toString();
                }
            }
        }

        return null;
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

        Node block = node.jjtGetChild(node.jjtGetNumChildren() - 1);

        String target = getSetTarget();
        StringWriter content = blocks.get(id);

        // 设置模式。
        if ( target != null) {

            if (content==null) {
                content = new StringWriter();
                blocks.put(id, content);
            }

            block.render(context, content);
        } else {
            block.render(context, writer);

            if ( content != null ) {
                writer.write(content.toString());
                blocks.remove(id);
            }
        }

        return true;
    }
}
