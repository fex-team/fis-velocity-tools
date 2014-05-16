<%@ page import="org.apache.velocity.context.Context" %>

<%
    // 在这个 jsp 里面，除了给 context 添加数据其他你都干不了。
    // 当然还可以设置 header.
    // 这里所任何页面输出都是没有意义的，全部都会被抛弃。

    Context context = (Context)request.getAttribute("context");

    context.put("name", "peter!");
%>