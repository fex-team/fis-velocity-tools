package com.baidu.fis.jsp;

import com.baidu.fis.util.Resource;
import com.baidu.fis.util.Settings;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import java.util.Stack;

/**
 * Created by 2betop on 15/1/15.
 */
public class Util {
    final static String RESOURCE_KEY = Resource.class.getName();

    public static Resource getResource(JspContext context) {
        Resource resource = (Resource)context.getAttribute(RESOURCE_KEY, PageContext.REQUEST_SCOPE);

        if (resource == null) {
            initFis(context);

            resource = new Resource(((PageContext)context).getRequest());
            context.setAttribute(RESOURCE_KEY, resource, PageContext.REQUEST_SCOPE);
        }

        return resource;
    }

    final static String SEVLETCONTEXT_KEY = ServletContext.class.getName();
    static Boolean flag = false;
    public static void initFis(JspContext context) {
        if (!flag && Settings.getApplicationAttribute(SEVLETCONTEXT_KEY) == null) {
            ServletContext servletContext = ((PageContext)context).getServletContext();
            Settings.setApplicationAttribute(SEVLETCONTEXT_KEY, servletContext);
            Settings.load(servletContext.getResourceAsStream(Settings.DEFAULT_PATH));
        }

        flag = true;
    }

//    final static String TEMPLATES_STACK_KEY = Util.class.getName() + "_TEMPLATES_STACK";
//    public static void pushTemplateFile(JspContext context, String tplName) {
//        Stack<String> files = (Stack<String>)context.getAttribute(TEMPLATES_STACK_KEY);
//
//        if (files == null) {
//            files = new Stack<String>();
//            context.setAttribute(TEMPLATES_STACK_KEY, files);
//        }
//
//        files.push(tplName);
//    }
//
//    public static String popTemplateFile(JspContext context) {
//        Stack<String> files = (Stack<String>)context.getAttribute(TEMPLATES_STACK_KEY);
//
//        if (files != null) {
//            return files.pop();
//        }
//
//        return null;
//    }
//
//    public static String getCurrentTemplateFile(JspContext context) {
//        Stack<String> files = (Stack<String>)context.getAttribute(TEMPLATES_STACK_KEY);
//
//        return files == null ? null : files.peek();
//    }

//    public static void assignBlockTag(JspContext context, String filename, String id, BlockTag tag) {
//
//    }

    final static String EXTENDS_KEY = ExtendsTag.class.getName();
    @SuppressWarnings("unchecked")
    public static void pushExtendsTag(JspContext context, ExtendsTag tag) {
        Stack<ExtendsTag> tags = (Stack<ExtendsTag>)context.getAttribute(EXTENDS_KEY, PageContext.REQUEST_SCOPE);

        if (tags == null) {
            tags = new Stack<ExtendsTag>();
            context.setAttribute(EXTENDS_KEY, tags, PageContext.REQUEST_SCOPE);
        }

        tags.push(tag);
    }

    @SuppressWarnings("unchecked")
    public static void popExtendTag(JspContext context) {
        Stack<ExtendsTag> tags = (Stack<ExtendsTag>)context.getAttribute(EXTENDS_KEY, PageContext.REQUEST_SCOPE);

        if (tags != null) {
            tags.pop();
        }
    }

    @SuppressWarnings("unchecked")
    public static ExtendsTag currentExtendTag(JspContext context) {
        Stack<ExtendsTag> tags = (Stack<ExtendsTag>)context.getAttribute(EXTENDS_KEY, PageContext.REQUEST_SCOPE);

        if (tags != null) {
            return tags.peek();
        }

        return null;
    }
}
