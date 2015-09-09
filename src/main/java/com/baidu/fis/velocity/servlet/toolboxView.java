package com.baidu.fis.velocity.servlet;


import org.apache.velocity.context.Context;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.view.ViewToolContext;
import org.springframework.web.servlet.view.velocity.VelocityToolboxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by xuchenhui on 2015/7/8.
 * 用于在spring环境下配置toolbox.xml
 *
 * 例子:
 * <bean id="velocityViewResolver" class="org.springframework.web.servlet.view.velocity.VelocityViewResolver">
 *  // ....
 *  <property name="viewClass" value="com.baidu.fis.velocity.servlet.toolboxView"/>
 *  <property name="toolboxConfigLocation" value="/WEB-INF/tools.xml"/>
 * </bean>
 *
 */
public class toolboxView extends VelocityToolboxView  {
    @Override
    protected Context createVelocityContext(Map<String, Object> model, HttpServletRequest request,
                                            HttpServletResponse response) throws Exception {
        ViewToolContext ctx;
        ctx = new ViewToolContext(getVelocityEngine(), request, response, getServletContext());
        ctx.putAll(model);
        if (this.getToolboxConfigLocation() != null) {
            ToolManager tm = new ToolManager();
            tm.setVelocityEngine(getVelocityEngine());
            tm.configure(getServletContext().getRealPath(getToolboxConfigLocation()));
            if (tm.getToolboxFactory().hasTools(Scope.REQUEST)) {
                ctx.addToolbox(tm.getToolboxFactory().createToolbox(Scope.REQUEST));
            }
            if (tm.getToolboxFactory().hasTools(Scope.APPLICATION)) {
                ctx.addToolbox(tm.getToolboxFactory().createToolbox(Scope.APPLICATION));
            }
            if (tm.getToolboxFactory().hasTools(Scope.SESSION)) {
                ctx.addToolbox(tm.getToolboxFactory().createToolbox(Scope.SESSION));
            }
        }
        return ctx;
    }
}
