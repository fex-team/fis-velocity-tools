<%@ page import="java.util.Date" %>
<%
    // 输出动态 json
    response.addHeader("Content-Type", "application/json");
%>{
    "age" : "9",
    "some" : {
        "a" : "a1",
        "b" : "a2",
        "c" : "a3"
    },
    "dynamc": "<%= new Date().toString()%>"
}