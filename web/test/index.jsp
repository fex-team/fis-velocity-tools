<%@ page import="org.apache.velocity.context.Context" %><%
    // 只允许执行java代码，
    // 不能有非 java 区域，否则报错。

    Context context = (Context)request.getAttribute("context");

    context.put("name", "peter!");
%>