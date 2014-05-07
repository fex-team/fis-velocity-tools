package com.baidu.fis.velocity.directive;

import com.baidu.fis.velocity.ResourceSingleton;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by 2betop on 5/7/14.
 */
public class Extends extends Parse {

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);

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
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        return false;
    }
}
