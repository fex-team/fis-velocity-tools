package com.baidu.fis.velocity.dest;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is a simple URL-based loader.
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id: URLResourceLoader.java 191743 2005-06-21 23:22:20Z dlr $
 * @since 1.5
 */
public class RoutableResourceLoader extends ResourceLoader
{
    private String[] roots = null;
    protected HashMap templateRoots = null;
    private int timeout = -1;
    private Method[] timeoutMethods;

    protected Boolean searchFromNextLoader = false;

    /**
     * VM请求时，忽略的系统性资源文件
     */
    private List<String> ignoredVelocityList = new ArrayList<String>();

    /**
     * @see ResourceLoader#init(ExtendedProperties)
     */
    public void init(ExtendedProperties configuration)
    {
        log.trace("RoutableResourceLoader : initialization starting.");

        timeout = configuration.getInt("timeout", -1);
        if (timeout > 0)
        {
            try
            {
                Class[] types = new Class[] { Integer.TYPE };
                Method conn = URLConnection.class.getMethod("setConnectTimeout", types);
                Method read = URLConnection.class.getMethod("setReadTimeout", types);
                timeoutMethods = new Method[] { conn, read };
                log.debug("RoutableResourceLoader : timeout set to "+timeout);
            }
            catch (NoSuchMethodException nsme)
            {
                log.debug("RoutableResourceLoader : Java 1.5+ is required to customize timeout!", nsme);
                timeout = -1;
            }
        }

        // 如果找不到，是否继续向下查找
        boolean searchFromNextLoader = configuration.getBoolean("searchfromnextloader", false);
        RouterMapManager.getInstance().setRoute(this);
        RouterMapManager.getInstance().setSearchFromNextLoader(searchFromNextLoader);

        // init the template paths map
        templateRoots = new HashMap();


        // 默认忽略的VM文件
        this.ignoredVelocityList.add("org/springframework/web");

        log.trace("RoutableResourceLoader : initialization complete.");
    }

    /**
     * 判断请求的VM是否在忽略名单中
     * @param vm
     * @return
     */
    protected boolean isInIgnoredVelocityList(String vm){
        for(String path : this.ignoredVelocityList){
            if(vm.startsWith(path)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否抛资源找不到的异常
     * @param resource
     * @return
     */
    public boolean throwResourceNotException(String resource){

        // 是否启用
        boolean configEnabled = RouterMapManager.getInstance().isEnabled();

        // 根据IP请求获取VM服务器的地址
        String feServerId = ThreadRequestUtil.getThreadFeServerId();

        // 加载资源
        if(configEnabled == false || feServerId == null) {
            // 没有启用，或者没有配置，则不抛错
            return false;
        }else if(isInIgnoredVelocityList(resource)) {
            return false;
        }else if(searchFromNextLoader){
            // 允许继续向下查找，则不抛错
            return false;
        }else{
            return true;
        }
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template to fetch bytestream of
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found
     *         in the file template path.
     */
    public synchronized InputStream getResourceStream(String name) throws ResourceNotFoundException
    {
        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException("RoutableResourceLoader : No template name provided");
        }

        InputStream inputStream = null;
        Exception exception = null;
        String msg = null;

        // 是否启用
        boolean configEnabled = RouterMapManager.getInstance().isEnabled();

        // 根据IP请求获取VM服务器的地址
        String feServerId = ThreadRequestUtil.getThreadFeServerId();

        // 加载资源
        if(configEnabled && feServerId != null){
            // 启用，并且该HTTP Request有映射到前端服务器时
            try
            {
                    // 发送请求
                    String remoteURL = "http://$0$1".replace("$0", feServerId).replace("$1", name);
                    URL u = new URL(remoteURL);
                    URLConnection conn = u.openConnection();
                    tryToSetTimeout(conn);
                    inputStream = conn.getInputStream();

                    if (inputStream != null)
                    {
                        if (log.isDebugEnabled()) log.debug("RoutableResourceLoader: Found '"+name+"' at '" + feServerId + "'");

                        // save this root for later re-use
                        templateRoots.put(name, feServerId);
                    }
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
                msg = String.format("RoutableResourceLoader: %s when looking for resource[%s] on feserver[%s].", ioe.getClass().getName(), name, feServerId);

                if (log.isDebugEnabled()) log.debug(msg);
                System.out.println(msg);

                if(exception == null)
                {
                    // only save the first one for later throwing
                    exception = ioe;
                }
            }
            catch(IllegalStateException ise)
            {
                ise.printStackTrace();
                msg = String.format("RoutableResourceLoader: %s when looking for resource[%s] on feserver[%s].", ise.getClass().getName(), name, feServerId);

                if (log.isDebugEnabled()) log.debug(msg);
                System.out.println(msg);

                // only save the first one for later throwing
                if (exception == null)
                {
                    exception = ise;
                }
            }
        }

        // if we never found the template
        if (inputStream == null)
        {
            if(msg != null){
                // 已经设置了msg，则跳过
            }else if (exception == null)
            {
                msg = "RoutableResourceLoader : Resource '" + name + "' not found.";
            }else{
                msg = exception.getMessage();
            }
            // 抛出ResourceNotFoundException，会继续从其它Loader查找；
            // convert to a general Velocity ResourceNotFoundException
            throw new ResourceNotFoundException(msg);
        }

        return inputStream;
    }

    /**
     * Checks to see if a resource has been deleted, moved or modified.
     *
     * @param resource Resource  The resource to check for modification
     * @return boolean  True if the resource has been modified, moved, or unreachable
     */
    public boolean isSourceModified(Resource resource)
    {
        long fileLastModified = getLastModified(resource);
        // if the file is unreachable or otherwise changed
        if (fileLastModified == 0 ||
                fileLastModified != resource.getLastModified())
        {
            return true;
        }
        return false;
    }

    /**
     * Checks to see when a resource was last modified
     *
     * @param resource Resource the resource to check
     * @return long The time when the resource was last modified or 0 if the file can't be reached
     */
    public long getLastModified(Resource resource)
    {
        // get the previously used root
        String name = resource.getName();
        String root = (String)templateRoots.get(name);

        try
        {
            // get a connection to the URL
            String remoteURL = "http://$0$1".replace("$0", root).replace("$1", name);
            URL u = new URL(remoteURL);
            URLConnection conn = u.openConnection();
            tryToSetTimeout(conn);
            return conn.getLastModified();
        }
        catch (IOException ioe)
        {
            // the file is not reachable at its previous address
            String msg = "RoutableResourceLoader: '"+name+"' is no longer reachable at '"+root+"'";
            log.error(msg, ioe);
            throw new ResourceNotFoundException(msg, ioe);
        }
    }

    /**
     * Returns the current, custom timeout setting. If negative, there is no custom timeout.
     * @since 1.6
     */
    public int getTimeout()
    {
        return timeout;
    }

    private void tryToSetTimeout(URLConnection conn)
    {
        if (timeout > 0)
        {
            Object[] arg = new Object[] { new Integer(timeout) };
            try
            {
                timeoutMethods[0].invoke(conn, arg);
                timeoutMethods[1].invoke(conn, arg);
            }
            catch (Exception e)
            {
                String msg = "Unexpected exception while setting connection timeout for "+conn;
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }
        }
    }

    public Boolean getSearchFromNextLoader() {
        return searchFromNextLoader;
    }

    public void setSearchFromNextLoader(Boolean searchFromNextLoader) {
        this.searchFromNextLoader = searchFromNextLoader;
    }
}
