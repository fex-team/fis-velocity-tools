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

import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.resource.*;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoaderFactory;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Class to manage the text resource for the Velocity Runtime.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:henning@apache.org">Henning P. Schmiedehausen</a>
 * @version $Id: ResourceManagerImpl.java 745757 2009-02-19 06:48:10Z nbubna $
 */
public class ResourceManagerImpl
        implements ResourceManager {

    /**
     * A template resources.
     */
    public static final int RESOURCE_TEMPLATE = 1;

    /**
     * A static content resource.
     */
    public static final int RESOURCE_CONTENT = 2;

    /**
     * token used to identify the loader internally.
     */
    private static final String RESOURCE_LOADER_IDENTIFIER = "_RESOURCE_LOADER_IDENTIFIER_";

    /**
     * Object implementing ResourceCache to be our resource manager's Resource cache.
     */
    protected ResourceCache globalCache = null;

    /**
     * The List of templateLoaders that the Runtime will use to locate the InputStream source of a template.
     */
    protected final List resourceLoaders = new ArrayList();

    /**
     * This is a list of the template input stream source initializers, basically properties for a particular template stream
     * source. The order in this list reflects numbering of the properties i.e.
     * <p>
     * <p>&lt;loader-id&gt;.resource.loader.&lt;property&gt; = &lt;value&gt;</p>
     */
    private final List sourceInitializerList = new ArrayList();

    /**
     * Has this Manager been initialized?
     */
    private boolean isInit = false;

    /**
     * switch to turn off log notice when a resource is found for the first time.
     */
    private boolean logWhenFound = true;

    /**
     * The internal RuntimeServices object.
     */
    protected RuntimeServices rsvc = null;

    /**
     * Logging.
     */
    protected Log log = null;


    public synchronized RouterMapManager loadProperties(ExtendedProperties props){

        RouterMapManager rmm = RouterMapManager.getInstance();

        // 是否开启该路由功能
        boolean configEnabled = props.getBoolean("resource.manager.class.config.enabled");
        rmm.setEnabled(configEnabled);

        // 获取Server要跑起来的端口
        Integer configPort = props.getInteger("resource.manager.class.config.port", null);

        if(configPort != null){
            // 设置配置的端口
            rmm.setPort(configPort);
            System.out.println("WEB Server is running on port: " + configPort);
        }else{
            // 获取当前WEB服务器运行的HTTP端口
            List<Integer> serverPortList = new ArrayList<Integer>();

            // 1. Tomcat 6.0 Below的获取API.
            Server server = ServerFactory.getServer();
            Service[] services = server.findServices();
            for (Service service : services) {
                for (Connector connector : service.findConnectors()) {
                    ProtocolHandler protocolHandler = connector.getProtocolHandler();
                    if (protocolHandler instanceof Http11Protocol || protocolHandler instanceof Http11AprProtocol || protocolHandler instanceof Http11NioProtocol) {
                        int serverPort = connector.getPort();
                        System.out.println("Loop each HTTP WEB Server Port: " + connector.getPort());
                        serverPortList.add(serverPort);
                    }
                }
            }
            // 2. 其他WEB服务器


            // 判断端口
            if(serverPortList.size() != 1){
                rmm.setEnabled(false);
                System.err.println("ResourceManagerImpl could not detect current web server port, so disable it.");
                log.error("ResourceManagerImpl could not detect current web server port, so disable it.");
            }else{
                rmm.setPort(serverPortList.get(0));
                System.out.println("WEB Server is running on port: " + serverPortList.get(0));
            }
        }

        return rmm;
    }

    /**
     * Initialize the ResourceManager.
     *
     * @param rsvc The Runtime Services object which is associated with this Resource Manager.
     */
    public synchronized void initialize(final RuntimeServices rsvc) {
        if (isInit) {
            log.debug("Re-initialization of ResourceLoader attempted and ignored.");
            return;
        }

        ResourceLoader resourceLoader = null;

        this.rsvc = rsvc;
        log = rsvc.getLog();

        log.trace("ResourceManager initializing: %s" + this.getClass());

        // 路由平台设置
        ExtendedProperties props = rsvc.getConfiguration();

        // 加载到全局变量中
        RouterMapManager rmm = this.loadProperties(props);

        // 当前WEB项目的名称
        String configLabel = props.getString("resource.manager.class.config.label", "Default name");
        // 路由提供平台对应的网址
        String configPlatform = props.getString("resource.manager.class.config.registry");
        // 路由平台的超时时间
        int timeoutSeconds = props.getInt("resource.manager.class.config.timeout", 10);

        if (rmm.isEnabled()) {
            // 使用平台路由功能，request可以被路由到其它server。
            String encodedLabel =configLabel;
            try{
                // encodeURIComponent
                encodedLabel = URLEncoder.encode(configLabel, "UTF-8");
            }catch (Exception e){
                e.printStackTrace();
            }
            String data = "name=" + encodedLabel + "&port=" + rmm.getPort();
            String sURL = configPlatform + "/server/rdserver";

            // 1. 设置Timeout
            Method[] timeoutMethods = null;
            if (timeoutSeconds > 0) {
                try {
                    Class[] types = new Class[]{Integer.TYPE};
                    Method conn = URLConnection.class.getMethod("setConnectTimeout", types);
                    Method read = URLConnection.class.getMethod("setReadTimeout", types);
                    timeoutMethods = new Method[]{conn, read};
                    log.debug("ResourceManager : timeout set to " + timeoutSeconds);
                } catch (NoSuchMethodException nsme) {
                    log.debug("ResourceManager : Java 1.5+ is required to customize timeout!", nsme);
                    timeoutSeconds = 101;
                }
            }

            // 2. 注册当前Server
            try {
                URL u = new URL(sURL);
                URLConnection conn = u.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection)conn;

                // POST
                httpConn.setDoOutput(true);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Accept-Charset", "utf-8");
                httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpConn.setRequestProperty("Content-Length", String.valueOf(data.length()));

                // Append Param data.
                OutputStream outputStream = httpConn.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(data.toString());
                outputStreamWriter.flush();

                tryToSetTimeout(conn, timeoutSeconds, timeoutMethods);
                InputStream inputStream = conn.getInputStream();
                StringBuffer responseBuffer = new StringBuffer();

                if (inputStream != null) {
                    if (log.isDebugEnabled()) log.debug("ResourceManager: Succeed to register to: " + sURL);

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String tempLine;
                    while ((tempLine = reader.readLine()) != null) {
                        responseBuffer.append(tempLine);
                    }
                    System.out.format("HTTP URL[%s] Response: " + responseBuffer.toString(), sURL);
                }
            } catch (IOException ioe) {
                String msg = "ResourceManager: Exception when register to: " + sURL + " with: " + ioe.getMessage();
                System.err.println(msg);
                if (log.isDebugEnabled()) log.debug(msg);
            }

        }

        assembleResourceLoaderInitializers();

        for (Iterator it = sourceInitializerList.iterator(); it.hasNext(); ) {
            /**
             * Resource loader can be loaded either via class name or be passed
             * in as an instance.
             */
            ExtendedProperties configuration = (ExtendedProperties) it.next();

            String loaderClass = StringUtils.nullTrim(configuration.getString("class"));
            ResourceLoader loaderInstance = (ResourceLoader) configuration.get("instance");

            if (loaderInstance != null) {
                resourceLoader = loaderInstance;
            } else if (loaderClass != null) {
                resourceLoader = ResourceLoaderFactory.getLoader(rsvc, loaderClass);
            } else {
                String msg = "Unable to find '" +
                        configuration.getString(RESOURCE_LOADER_IDENTIFIER) +
                        ".resource.loader.class' specification in configuration." +
                        " This is a critical value.  Please adjust configuration.";
                log.error(msg);
                throw new VelocityException(msg);
            }

            resourceLoader.commonInit(rsvc, configuration);
            resourceLoader.init(configuration);
            resourceLoaders.add(resourceLoader);
        }

        /*
         * now see if this is overridden by configuration
         */

        logWhenFound = rsvc.getBoolean(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, true);

        /*
         *  now, is a global cache specified?
         */

        String cacheClassName = rsvc.getString(RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS);

        Object cacheObject = null;

        if (org.apache.commons.lang.StringUtils.isNotEmpty(cacheClassName)) {
            try {
                cacheObject = ClassUtils.getNewInstance(cacheClassName);
            } catch (ClassNotFoundException cnfe) {
                String msg = "The specified class for ResourceCache (" + cacheClassName +
                        ") does not exist or is not accessible to the current classloader.";
                log.error(msg, cnfe);
                throw new VelocityException(msg, cnfe);
            } catch (IllegalAccessException ae) {
                throw new VelocityException("Could not access class '"
                        + cacheClassName + "'", ae);
            } catch (InstantiationException ie) {
                throw new VelocityException("Could not instantiate class '"
                        + cacheClassName + "'", ie);
            }

            if (!(cacheObject instanceof ResourceCache)) {
                String msg = "The specified resource cache class (" + cacheClassName +
                        ") must implement " + ResourceCache.class.getName();
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }

        /*
         *  if we didn't get through that, just use the default.
         */
        if (cacheObject == null) {
            cacheObject = new ResourceCacheImpl();
        }

        globalCache = (ResourceCache) cacheObject;

        globalCache.initialize(rsvc);

        log.trace("Default ResourceManager initialization complete.");
    }

    /**
     * This will produce a List of Hashtables, each hashtable contains the intialization info for a particular resource loader. This
     * Hashtable will be passed in when initializing the the template loader.
     */
    private void assembleResourceLoaderInitializers() {
        Vector resourceLoaderNames = rsvc.getConfiguration().getVector(RuntimeConstants.RESOURCE_LOADER);
        StringUtils.trimStrings(resourceLoaderNames);

        for (Iterator it = resourceLoaderNames.iterator(); it.hasNext(); ) {

            /*
             * The loader id might look something like the following:
             *
             * file.resource.loader
             *
             * The loader id is the prefix used for all properties
             * pertaining to a particular loader.
             */
            String loaderName = (String) it.next();
            StringBuffer loaderID = new StringBuffer(loaderName);
            loaderID.append(".").append(RuntimeConstants.RESOURCE_LOADER);

            ExtendedProperties loaderConfiguration =
                    rsvc.getConfiguration().subset(loaderID.toString());

            /*
             *  we can't really count on ExtendedProperties to give us an empty set
             */
            if (loaderConfiguration == null) {
                log.debug("ResourceManager : No configuration information found " +
                        "for resource loader named '" + loaderName +
                        "' (id is " + loaderID + "). Skipping it...");
                continue;
            }

            /*
             *  add the loader name token to the initializer if we need it
             *  for reference later. We can't count on the user to fill
             *  in the 'name' field
             */

            loaderConfiguration.setProperty(RESOURCE_LOADER_IDENTIFIER, loaderName);

            /*
             * Add resources to the list of resource loader
             * initializers.
             */
            sourceInitializerList.add(loaderConfiguration);
        }
    }

    /**
     * Key组成：resourceType, resourceName, feServerId
     * @param resourceType
     * @param resourceName
     * @return
     */
    public String getResourceKey(int resourceType, String resourceName){
        // 当前配置的Loader中的server配置
        String feServerId = ThreadRequestUtil.getThreadFeServerId();

        return resourceType + "|" + resourceName + "|" + feServerId != null ? feServerId : "";
    }

    /**
     * Gets the named resource. Returned class type corresponds to specified type (i.e. <code>Template</code> to <code>
     * RESOURCE_TEMPLATE</code>).
     * <p>
     * This method is now unsynchronized which requires that ResourceCache
     * implementations be thread safe (as the default is).
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     * @param encoding     The character encoding to use.
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found from any available source.
     * @throws ParseErrorException       if template cannot be parsed due to syntax (or other) error.
     */
    public Resource getResource(final String resourceName, final int resourceType, final String encoding)
            throws ResourceNotFoundException,
            ParseErrorException {
        /*
         * Check to see if the resource was placed in the cache.
         * If it was placed in the cache then we will use
         * the cached version of the resource. If not we
         * will load it.
         *
         * Note: the type is included in the key to differentiate ContentResource
         * (static content from #include) with a Template.
         */

        String resourceKey = this.getResourceKey(resourceType, resourceName);
        Resource resource = globalCache.get(resourceKey);

        if (resource != null) {
            try {
                // avoids additional method call to refreshResource
                if (resource.requiresChecking()) {
                    resource = refreshResource(resource, encoding);
                }
            } catch (ResourceNotFoundException rnfe) {
                globalCache.remove(resourceKey);
                return getResource(resourceName, resourceType, encoding);
            } catch (ParseErrorException pee) {
                log.error("ResourceManager.getResource() exception", pee);
                throw pee;
            } catch (RuntimeException re) {
                log.error("ResourceManager.getResource() exception", re);
                throw re;
            }
        } else {
            try {
                /*
                 *  it's not in the cache, so load it.
                 */
                resource = loadResource(resourceName, resourceType, encoding);

                if (resource.getResourceLoader().isCachingOn()) {
                    globalCache.put(resourceKey, resource);
                }
            } catch (ResourceNotFoundException rnfe) {
                rnfe.printStackTrace();
                log.error("ResourceManager : unable to find resource '" + resourceName + "' in any resource loader.");
                throw rnfe;
            } catch (ParseErrorException pee) {
                pee.printStackTrace();
                log.error("ResourceManager.getResource() parse exception", pee);
                throw pee;
            } catch (RuntimeException re) {
                re.printStackTrace();
                log.error("ResourceManager.getResource() load exception", re);
                throw re;
            }
        }

        return resource;
    }

    /**
     * Create a new Resource of the specified type.
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     * @return new instance of appropriate resource type
     * @since 1.6
     */
    protected Resource createResource(String resourceName, int resourceType) {
        return ResourceFactory.getResource(resourceName, resourceType);
    }

    /**
     * Loads a resource from the current set of resource loaders.
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     * @param encoding     The character encoding to use.
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found from any available source.
     * @throws ParseErrorException       if template cannot be parsed due to syntax (or other) error.
     */
    protected Resource loadResource(String resourceName, int resourceType, String encoding) throws ResourceNotFoundException, ParseErrorException {
        Resource resource = createResource(resourceName, resourceType);
        resource.setRuntimeServices(rsvc);
        resource.setName(resourceName);
        resource.setEncoding(encoding);

        /*
         * Now we have to try to find the appropriate
         * loader for this resource. We have to cycle through
         * the list of available resource loaders and see
         * which one gives us a stream that we can use to
         * make a resource with.
         */

        long howOldItWas = 0;

        for (Iterator it = resourceLoaders.iterator(); it.hasNext(); ) {
            ResourceLoader resourceLoader = (ResourceLoader) it.next();
            resource.setResourceLoader(resourceLoader);

            /*
             *  catch the ResourceNotFound exception
             *  as that is ok in our new multi-loader environment
             */

            try {

                if (resource.process()) {
                    /*
                     *  FIXME  (gmj)
                     *  moved in here - technically still
                     *  a problem - but the resource needs to be
                     *  processed before the loader can figure
                     *  it out due to to the new
                     *  multi-path support - will revisit and fix
                     */

                    if (logWhenFound && log.isDebugEnabled()) {
                        log.debug("ResourceManager : found " + resourceName +
                                " with loader " +
                                resourceLoader.getClassName());
                    }

                    howOldItWas = resourceLoader.getLastModified(resource);

                    break;
                }
            } catch (ResourceNotFoundException rnfe) {
                /*
                 *  that's ok - it's possible to fail in
                 *  multi-loader environment
                 */
                if(resourceLoader instanceof RoutableResourceLoader){
                    // 如果是RoutableResourceLoader，则放弃查找，直接抛错
                    if(((RoutableResourceLoader) resourceLoader).throwResourceNotException(resourceName)){
                       // 需要报错，不再向下查找
                        break;
                    }
                }
            }
        }

        /*
         * Return null if we can't find a resource.
         */
        if (resource.getData() == null) {
            throw new ResourceNotFoundException("Unable to find resource '" + resourceName + "'");
        }

        /*
         *  some final cleanup
         */

        resource.setLastModified(howOldItWas);
        resource.setModificationCheckInterval(resource.getResourceLoader().getModificationCheckInterval());

        resource.touch();

        return resource;
    }

    /**
     * Takes an existing resource, and 'refreshes' it. This generally means that the source of the resource is checked for changes
     * according to some cache/check algorithm and if the resource changed, then the resource data is reloaded and re-parsed.
     *
     * @param resource resource to refresh
     * @param encoding character encoding of the resource to refresh.
     * @throws ResourceNotFoundException if template not found from current source for this Resource
     * @throws ParseErrorException       if template cannot be parsed due to syntax (or other) error.
     */
    protected Resource refreshResource(Resource resource, final String encoding)
            throws ResourceNotFoundException, ParseErrorException {
        /*
         * The resource knows whether it needs to be checked
         * or not, and the resource's loader can check to
         * see if the source has been modified. If both
         * these conditions are true then we must reload
         * the input stream and parse it to make a new
         * AST for the resource.
         */

        /*
         *  touch() the resource to reset the counters
         */
        resource.touch();

        /* check whether this can now be found in a higher priority
         * resource loader.  if so, pass the request off to loadResource.
         */
        ResourceLoader loader = resource.getResourceLoader();
        if (resourceLoaders.size() > 0 && resourceLoaders.indexOf(loader) > 0) {
            String name = resource.getName();
            if (loader != getLoaderForResource(name)) {
                return loadResource(name, resource.getType(), encoding);
            }
        }

        if (resource.isSourceModified()) {
            /*
             *  now check encoding info.  It's possible that the newly declared
             *  encoding is different than the encoding already in the resource
             *  this strikes me as bad...
             */

            if (!org.apache.commons.lang.StringUtils.equals(resource.getEncoding(), encoding)) {
                log.warn("Declared encoding for template '" +
                        resource.getName() +
                        "' is different on reload. Old = '" +
                        resource.getEncoding() + "' New = '" + encoding);

                resource.setEncoding(encoding);
            }

            /*
             *  read how old the resource is _before_
             *  processing (=>reading) it
             */
            long howOldItWas = loader.getLastModified(resource);

            String resourceKey = this.getResourceKey(resource.getType(), resource.getName());

            /* 
             * we create a copy to avoid partially overwriting a
             * template which may be in use in another thread
             */

            Resource newResource =
                    ResourceFactory.getResource(resource.getName(), resource.getType());

            newResource.setRuntimeServices(rsvc);
            newResource.setName(resource.getName());
            newResource.setEncoding(resource.getEncoding());
            newResource.setResourceLoader(loader);
            newResource.setModificationCheckInterval(loader.getModificationCheckInterval());

            newResource.process();
            newResource.setLastModified(howOldItWas);
            resource = newResource;

            globalCache.put(resourceKey, newResource);
        }
        return resource;
    }

    /**
     * Gets the named resource. Returned class type corresponds to specified type (i.e. <code>Template</code> to <code>
     * RESOURCE_TEMPLATE</code>).
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>, <code>RESOURCE_CONTENT</code>, etc.).
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found from any available source.
     * @throws ParseErrorException       if template cannot be parsed due to syntax (or other) error.
     * @throws Exception                 if a problem in parse
     * @deprecated Use {@link #getResource(String resourceName, int resourceType, String encoding)}
     */
    public Resource getResource(String resourceName, int resourceType)
            throws ResourceNotFoundException,
            ParseErrorException,
            Exception {
        return getResource(resourceName, resourceType, RuntimeConstants.ENCODING_DEFAULT);
    }

    /**
     * Determines if a template exists, and returns name of the loader that provides it. This is a slightly less hokey way to
     * support the Velocity.templateExists() utility method, which was broken when per-template encoding was introduced. We can
     * revisit this.
     *
     * @param resourceName Name of template or content resource
     * @return class name of loader than can provide it
     */
    public String getLoaderNameForResource(String resourceName) {
        ResourceLoader loader = getLoaderForResource(resourceName);
        if (loader == null) {
            return null;
        }
        return loader.getClass().toString();
    }

    /**
     * Returns the first {@link ResourceLoader} in which the specified
     * resource exists.
     */
    private ResourceLoader getLoaderForResource(String resourceName) {
        for (Iterator i = resourceLoaders.iterator(); i.hasNext(); ) {
            ResourceLoader loader = (ResourceLoader) i.next();
            if (loader.resourceExists(resourceName)) {
                return loader;
            }
        }
        return null;
    }

    private void tryToSetTimeout(URLConnection conn, int timeoutSeconds, Method[] timeoutMethods) {
        if (timeoutSeconds > 0) {
            Object[] arg = new Object[]{new Integer(timeoutSeconds * 1000)};
            try {
                timeoutMethods[0].invoke(conn, arg);
                timeoutMethods[1].invoke(conn, arg);
            } catch (Exception e) {
                String msg = "Unexpected exception while setting connection timeout for " + conn;
                log.error(msg, e);
                throw new VelocityException(msg, e);
            }
        }
    }

}
