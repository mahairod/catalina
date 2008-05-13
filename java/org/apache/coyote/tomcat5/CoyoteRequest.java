/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.coyote.tomcat5;


import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
// START SJSAS 6347215
import java.net.UnknownHostException;
// END SJSAS 6347215
// START GlassFish 898
import java.net.URLDecoder;
// END GlassFish 898
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sun.grizzly.util.buf.B2CConverter;
// START CR 6309511
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.grizzly.util.buf.CharChunk;
// END CR 6309511
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.http.Cookies;
import com.sun.grizzly.util.http.FastHttpDateFormat;
import com.sun.grizzly.util.http.Parameters;
import com.sun.grizzly.util.http.ServerCookie;
import com.sun.grizzly.util.http.mapper.MappingData;

import com.sun.grizzly.tcp.ActionCode;
import com.sun.grizzly.tcp.Request;

import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
// START SJSAS 6406580
import org.apache.catalina.session.PersistentManagerBase;
// END SJSAS 6406580
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.ValveContext;
import org.apache.catalina.Wrapper;

import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.core.ApplicationFilterFactory;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.StringParser;
import org.apache.catalina.security.SecurityUtil;
import com.sun.org.apache.commons.logging.Log;
// START CR 6309511
import com.sun.org.apache.commons.logging.LogFactory;
// END CR 6309511

// START S1AS 6170450
import com.sun.appserv.ProxyHandler;
// END S1AS 6170450

/**
 * Wrapper object for the Coyote request.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision: 1.67.2.9 $ $Date: 2008/04/17 18:37:34 $
 */

public class CoyoteRequest
    implements HttpRequest, HttpServletRequest {


    /**
     * Whether or not to enforce scope checking of this object.
     */
    private static boolean enforceScope = false;

    // ----------------------------------------------------------- Constructors


    public CoyoteRequest() {
         // START OF SJSAS 6231069
        formats = (SimpleDateFormat[]) staticDateFormats.get();
        formats[0].setTimeZone(TimeZone.getTimeZone("GMT"));
        formats[1].setTimeZone(TimeZone.getTimeZone("GMT"));
        formats[2].setTimeZone(TimeZone.getTimeZone("GMT"));
        // END OF SJSAS 6231069
    }


    // ------------------------------------------------------------- Properties


    /**
     * Coyote request.
     */
    protected Request coyoteRequest;

    /**
     * Set the Coyote request.
     * 
     * @param coyoteRequest The Coyote request
     */
    public void setCoyoteRequest(Request coyoteRequest) {
        this.coyoteRequest = coyoteRequest;
        inputBuffer.setRequest(coyoteRequest);
    }

    /**
     * Get the Coyote request.
     */
    public Request getCoyoteRequest() {
        return (this.coyoteRequest);
    }


    /**
     * Set whether or not to enforce scope checking of this object.
     */
    public static void setEnforceScope(boolean enforce) {

        enforceScope = enforce;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    // START CR 6309511
    private static final Log log = LogFactory.getLog(CoyoteRequest.class);
    // END CR 6309511

    /**
     * The set of cookies associated with this Request.
     */
    protected ArrayList<Cookie> cookies = new ArrayList<Cookie>();

    // START OF SJSAS 6231069
    /*
    protected SimpleDateFormat formats[] = {
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
        new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
    }*/

    /**
     * The set of SimpleDateFormat formats to use in getDateHeader().
     *
     * Notice that because SimpleDateFormat is not thread-safe, we can't
     * declare formats[] as a static variable.
     */
    private static ThreadLocal staticDateFormats = new ThreadLocal() {
        protected Object initialValue() {
            SimpleDateFormat[] f = new SimpleDateFormat[3];
            f[0] = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", 
                                        Locale.US);
            f[1] = new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", 
                                        Locale.US);
            f[2] = new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US);
            return f;
        }
    };
    protected SimpleDateFormat formats[];
    // END OF SJSAS 6231069    

    /**
     * The default Locale if none are specified.
     */
    protected static final Locale defaultLocale = Locale.getDefault();


    /**
     * The attributes associated with this Request, keyed by attribute name.
     */
    protected HashMap attributes = new HashMap();


    /**
     * The preferred Locales assocaited with this Request.
     */
    protected ArrayList locales = new ArrayList();


    /**
     * Internal notes associated with this request by Catalina components
     * and event listeners.
     */
    private HashMap notes = new HashMap();


    /**
     * Authentication type.
     */
    protected String authType = null;


    /**
     * The current dispatcher type.
     */
    protected Object dispatcherType = null;


    /**
     * The associated input buffer.
     */
    protected InputBuffer inputBuffer = new InputBuffer();


    /**
     * ServletInputStream.
     */
    protected CoyoteInputStream inputStream = 
        new CoyoteInputStream(inputBuffer);


    /**
     * Reader.
     */
    protected CoyoteReader reader = new CoyoteReader(inputBuffer);


    /**
     * Using stream flag.
     */
    protected boolean usingInputStream = false;


    /**
     * Using writer flag.
     */
    protected boolean usingReader = false;


    /**
     * User principal.
     */
    protected Principal userPrincipal = null;


    /**
     * Session parsed flag.
     */
    protected boolean sessionParsed = false;


    /**
     * Request parameters parsed flag.
     */
    protected boolean requestParametersParsed = false;


     /**
     * Cookies parsed flag.
     */
    protected boolean cookiesParsed = false;


    /**
     * Secure flag.
     */
    protected boolean secure = false;

    
    /**
     * The Subject associated with the current AccessControllerContext
     */
    protected Subject subject = null;


    /**
     * Post data buffer.
     */
    protected static final int CACHED_POST_LEN = 8192;
    protected byte[] postData = null;


    /**
     * Hash map used in the getParametersMap method.
     */
    protected ParameterMap parameterMap = new ParameterMap();


    /**
     * The currently active session for this request.
     */
    protected Session session = null;


    /**
     * The current request dispatcher path.
     */
    protected Object requestDispatcherPath = null;


    /**
     * Was the requested session ID received in a cookie?
     */
    protected boolean requestedSessionCookie = false;


    /**
     * The requested session ID (if any) for this request.
     */
    protected String requestedSessionId = null;


    /**
     * The requested session version (if any) for this request.
     */
    protected String requestedSessionVersion = null;


    /**
     * Was the requested session ID received in a URL?
     */
    protected boolean requestedSessionURL = false;


    /**
     * The socket through which this Request was received.
     */
    protected Socket socket = null;


    /**
     * Parse locales.
     */
    protected boolean localesParsed = false;


    /**
     * The string parser we will use for parsing request lines.
     */
    private StringParser parser = new StringParser();

    /**
     * Local port
     */
    protected int localPort = -1;

    /**
     * Remote address.
     */
    protected String remoteAddr = null;


    /**
     * Remote host.
     */
    protected String remoteHost = null;

    
    /**
     * Remote port
     */
    protected int remotePort = -1;
    
    /**
     * Local address
     */
    protected String localName = null;


    /**
     * Local address
     */
    protected String localAddr = null;
    
    /** After the request is mapped to a ServletContext, we can also
     * map it to a logger.
     */
    /* CR 6309511
    protected Log log=null;
    */

    
    // START CR 6415120
    /**
     * Whether or not access to resources in WEB-INF or META-INF needs to be
     * checked.
     */
    protected boolean checkRestrictedResources = true;
    // END CR 6415120


    /**
     * has findSession been called and returned null already
     */
    private boolean unsuccessfulSessionFind = false;

    /*
     * Are we supposed to honor the unsuccessfulSessionFind flag?
     * WS overrides this to false.
     */
    protected boolean checkUnsuccessfulSessionFind = true;

    // START S1AS 4703023
    /**
     * The current application dispatch depth.
     */
    private int dispatchDepth = 0;

    /**
     * The maximum allowed application dispatch depth.
     */
    private static int maxDispatchDepth = Constants.DEFAULT_MAX_DISPATCH_DEPTH;
    // END S1AS 4703023


    // START CR 6309511
    /**
     * The match string for identifying a session ID parameter.
     */
    private static final String match =
        ";" + Globals.SESSION_PARAMETER_NAME + "=";

    /**
     * The match string for identifying a session ID parameter.
     */
    private static final char[] SESSION_ID = match.toCharArray();
    // END CR 6309511

    // START SJSAS 6346226
    private String jrouteId;
    // END SJSAS 6346226

    // START GlassFish 896
    private SessionTracker sessionTracker = new SessionTracker();
    // END GlassFish 896

    // START GlassFish 1024
    private boolean isDefaultContext = false;
    // END GlassFish 1024

    private String requestURI = null;

    /**
     * Associated context.
     */
    protected Context context = null;
    protected ServletContext servletContext = null;


    // --------------------------------------------------------- Public Methods

    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    public void recycle() {
        
        context = null;
        servletContext = null;
        wrapper = null;

        dispatcherType = null;
        requestDispatcherPath = null;

        authType = null;
        requestURI = null;
        inputBuffer.recycle();
        usingInputStream = false;
        usingReader = false;
        userPrincipal = null;
        subject = null;
        sessionParsed = false;
        requestParametersParsed = false;
        cookiesParsed = false;
        locales.clear();
        localesParsed = false;
        secure = false;
        remoteAddr = null;
        remoteHost = null;
        remotePort = -1;
        localPort = -1;
        localAddr = null;
        localName = null;        

        attributes.clear();
        notes.clear();
        cookies.clear();
        
        unsuccessfulSessionFind = false;

        if (session != null) {
            session.endAccess();
        }
        session = null;
        requestedSessionCookie = false;
        requestedSessionId = null;
        requestedSessionVersion = null;
        requestedSessionURL = false;

        // START GlassFish 896
        sessionTracker.reset();
        // END GlassFish 896

        /* CR 6309511
        log = null;
        */
        dispatchDepth = 0; // S1AS 4703023

        parameterMap.setLocked(false);
        parameterMap.clear();

        mappingData.recycle();

        if (enforceScope) {
            if (facade != null) {
                facade.clear();
                facade = null;
            }
            if (inputStream != null) {
                inputStream.clear();
                inputStream = null;
            }
            if (reader != null) {
                reader.clear();
                reader = null;
            }
        }

    }


    // -------------------------------------------------------- Request Methods


    /**
     * Return the authorization credentials sent with this request.
     */
    public String getAuthorization() {
        return (coyoteRequest.getHeader(Constants.AUTHORIZATION_HEADER));
    }

    /**
     * Set the authorization credentials sent with this request.
     *
     * @param authorization The new authorization credentials
     */
    public void setAuthorization(String authorization) {
        // Not used
    }


    /**
     * Associated Catalina connector.
     */
    protected Connector connector;

    /**
     * Return the Connector through which this Request was received.
     */
    public Connector getConnector() {
        return (this.connector);
    }

    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }


    /**
     * Return the Context within which this Request is being processed.
     */
    public Context getContext() {
        return (this.context);
    }


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    public void setContext(Context context) {
        this.context = context;
        if (context != null) {
            this.servletContext = context.getServletContext();
        }
        // START GlassFish 896
        initSessionTracker();
        // END GlassFish 896
    }


    // START GlassFish 1024
    /**
     * @param isDefaultContext true if this request was mapped to a context
     * with an empty context root that is backed by the vitual server's
     * default-web-module
     */
    public void setDefaultContext(boolean isDefaultContext) {
        this.isDefaultContext = isDefaultContext;
    }
    // END GlassFish 1024


    /**
     * Filter chain associated with the request.
     */
    protected FilterChain filterChain = null;

    /**
     * Get filter chain associated with the request.
     */
    public FilterChain getFilterChain() {
        return (this.filterChain);
    }

    /**
     * Set filter chain associated with the request.
     * 
     * @param filterChain new filter chain
     */
    public void setFilterChain(FilterChain filterChain) {
        this.filterChain = filterChain;
    }


    /**
     * Return the Host within which this Request is being processed.
     */
    public Host getHost() {
        return (Host) mappingData.host;
    }


    /**
     * Set the Host within which this Request is being processed.  This
     * must be called as soon as the appropriate Host is identified, and
     * before the Request is passed to a context.
     *
     * @param host The newly associated Host
     */
    public void setHost(Host host) {
        mappingData.host = host;
    }


    /**
     * Descriptive information about this Request implementation.
     */
    protected static final String info =
        "org.apache.coyote.catalina.CoyoteRequest/1.0";

    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }


    /**
     * Mapping data.
     */
    protected MappingData mappingData = new MappingData();

    /**
     * Return mapping data.
     */
    public MappingData getMappingData() {
        return (mappingData);
    }


    /**
     * The facade associated with this request.
     */
    protected CoyoteRequestFacade facade = null;

    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.  This method must be implemented by a subclass.
     */
    public HttpServletRequest getRequest() {
        if (facade == null) {
            facade = new CoyoteRequestFacade(this);
        } 
        return (facade);
    }


    /**
     * The response with which this request is associated.
     */
    protected org.apache.catalina.Response response = null;

    /**
     * Return the Response with which this Request is associated.
     */
    public org.apache.catalina.Response getResponse() {
        return (this.response);
    }

    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    public void setResponse(org.apache.catalina.Response response) {
        this.response = response;
        sessionTracker.setResponse((CoyoteResponse) response);
    }


    /**
     * Return the Socket (if any) through which this Request was received.
     * This should <strong>only</strong> be used to access underlying state
     * information about this Socket, such as the SSLSession associated with
     * an SSLSocket.
     */
    public Socket getSocket() {
        return (socket);
    }

    /**
     * Set the Socket (if any) through which this Request was received.
     *
     * @param socket The socket through which this request was received
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
        remoteHost = null;
        remoteAddr = null;
        remotePort = -1;
        localPort = -1;
        localAddr = null;
        localName = null;
    }


    /**
     * Return the input stream associated with this Request.
     */
    public InputStream getStream() {
        if (inputStream == null) {
            inputStream = new CoyoteInputStream(inputBuffer);
        }
        return inputStream;
    }

    /**
     * Set the input stream associated with this Request.
     *
     * @param stream The new input stream
     */
    public void setStream(InputStream stream) {
        // Ignore
    }


    /**
     * URI byte to char converter (not recycled).
     */
    protected B2CConverter URIConverter = null;

    /**
     * Return the URI converter.
     */
    protected B2CConverter getURIConverter() {
        return URIConverter;
    }

    /**
     * Set the URI converter.
     * 
     * @param URIConverter the new URI connverter
     */
    protected void setURIConverter(B2CConverter URIConverter) {
        this.URIConverter = URIConverter;
    }


    /**
     * The valve context associated with this request.
     */
    protected ValveContext valveContext = null;

    /**
     * Get valve context.
     */
    public ValveContext getValveContext() {
        return (this.valveContext);
    }

    /**
     * Set valve context.
     * 
     * @param valveContext New valve context object
     */
    public void setValveContext(ValveContext valveContext) {
        this.valveContext = valveContext;
    }


    /**
     * Associated wrapper.
     */
    protected Wrapper wrapper = null;

    /**
     * Return the Wrapper within which this Request is being processed.
     */
    public Wrapper getWrapper() {
        return (this.wrapper);
    }


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     * @param wrapper The newly associated Wrapper
     */
    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }


    // ------------------------------------------------- Request Public Methods


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream createInputStream() 
        throws IOException {
        if (inputStream == null) {
            inputStream = new CoyoteInputStream(inputBuffer);
        }
        return inputStream;
    }


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    public void finishRequest() throws IOException {
        // The reader and input stream don't need to be closed
    }


    /**
     * Return the object bound with the specified name to the internal notes
     * for this request, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    public Object getNote(String name) {
        return (notes.get(name));
    }


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this request.
     */
    public Iterator getNoteNames() {
        return (notes.keySet().iterator());
    }


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this request.
     *
     * @param name Name of the note to be removed
     */
    public void removeNote(String name) {
        notes.remove(name);
    }


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this request, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    public void setNote(String name, Object value) {
        notes.put(name, value);
    }


    /**
     * Set the content length associated with this Request.
     *
     * @param length The new content length
     */
    public void setContentLength(int length) {
        // Not used
    }


    /**
     * Set the content type (and optionally the character encoding)
     * associated with this Request.  For example,
     * <code>text/html; charset=ISO-8859-4</code>.
     *
     * @param type The new content type
     */
    public void setContentType(String type) {
        // Not used
    }


    /**
     * Set the protocol name and version associated with this Request.
     *
     * @param protocol Protocol name and version
     */
    public void setProtocol(String protocol) {
        // Not used
    }


    /**
     * Set the IP address of the remote client associated with this Request.
     *
     * @param remoteAddr The remote IP address
     */
    public void setRemoteAddr(String remoteAddr) {
        // Not used
    }


    /**
     * Set the fully qualified name of the remote client associated with this
     * Request.
     *
     * @param remoteHost The remote host name
     */
    public void setRemoteHost(String remoteHost) {
        // Not used
    }


    /**
     * Set the name of the scheme associated with this request.  Typical values
     * are <code>http</code>, <code>https</code>, and <code>ftp</code>.
     *
     * @param scheme The scheme
     */
    public void setScheme(String scheme) {
        // Not used
    }


    /**
     * Set the value to be returned by <code>isSecure()</code>
     * for this Request.
     *
     * @param secure The new isSecure value
     */
    public void setSecure(boolean secure) {
        this.secure = secure;

        if ( secure ){
            populateSSLAttributes();
        }
    }


    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name The server name
     */
    public void setServerName(String name) {
        coyoteRequest.serverName().setString(name);
    }


    /**
     * Set the port number of the server to process this request.
     *
     * @param port The server port
     */
    public void setServerPort(int port) {
        coyoteRequest.setServerPort(port);
    }


    // START CR 6415120
    /**
     * Set whether or not access to resources under WEB-INF or META-INF
     * needs to be checked.
     */
    public void setCheckRestrictedResources(boolean check) {

        this.checkRestrictedResources = check;

    }


    /**
     * Return whether or not access to resources under WEB-INF or META-INF
     * needs to be checked.
     */
    public boolean getCheckRestrictedResources() {

        return this.checkRestrictedResources;

    }
    // END CR 6415120


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Return the specified request attribute if it exists; otherwise, return
     * <code>null</code>.
     *
     * @param name Name of the request attribute to return
     */
    public Object getAttribute(String name) {

        if (name.equals(Globals.DISPATCHER_TYPE_ATTR)) {
            return (dispatcherType == null) 
                ? ApplicationFilterFactory.REQUEST_INTEGER
                : dispatcherType;
        } else if (name.equals(Globals.DISPATCHER_REQUEST_PATH_ATTR)) {
            return (requestDispatcherPath == null) 
                ? getRequestPathMB().toString()
                : requestDispatcherPath.toString();
        } else if (name.equals(Globals.CONSTRAINT_URI)) {
            return (getRequestPathMB() != null)
                ? getRequestPathMB().toString() : null;
        }        

        Object attr=attributes.get(name);

        if(attr!=null)
            return(attr);

        attr =  coyoteRequest.getAttribute(name);
        if(attr != null)
            return attr;
        // XXX Should move to Globals
        if(Constants.SSL_CERTIFICATE_ATTR.equals(name)) {
            coyoteRequest.action(ActionCode.ACTION_REQ_SSL_CERTIFICATE, null);
            attr = getAttribute(Globals.CERTIFICATES_ATTR);
            if(attr != null)
                attributes.put(name, attr);
        } else if( isSSLAttribute(name) ) {
            /* SJSAS 6419950
            coyoteRequest.action(ActionCode.ACTION_REQ_SSL_ATTRIBUTE, 
                                 coyoteRequest);
            attr = coyoteRequest.getAttribute(Globals.CERTIFICATES_ATTR);
            if( attr != null) {
                attributes.put(Globals.CERTIFICATES_ATTR, attr);
            }
            attr = coyoteRequest.getAttribute(Globals.CIPHER_SUITE_ATTR);
            if(attr != null) {
                attributes.put(Globals.CIPHER_SUITE_ATTR, attr);
            }
            attr = coyoteRequest.getAttribute(Globals.KEY_SIZE_ATTR);
            if(attr != null) {
                attributes.put(Globals.KEY_SIZE_ATTR, attr);
            }
            */
            // START SJSAS 6419950
            populateSSLAttributes();
            // END SJSAS 6419950
            attr = attributes.get(name);
        }
        return attr;
    }


    /**
     * Test if a given name is one of the special Servlet-spec SSL attributes.
     */
    static boolean isSSLAttribute(String name) {
        return Globals.CERTIFICATES_ATTR.equals(name) ||
            Globals.CIPHER_SUITE_ATTR.equals(name) ||
            Globals.KEY_SIZE_ATTR.equals(name);
    }

    /**
     * Return the names of all request attributes for this Request, or an
     * empty <code>Enumeration</code> if there are none.
     */
    public Enumeration getAttributeNames() {
        if (isSecure()) {
            populateSSLAttributes();
        }
        return new Enumerator(attributes.keySet(), true);
    }


    /**
     * Return the character encoding for this Request.
     */
    public String getCharacterEncoding() {
      return (coyoteRequest.getCharacterEncoding());
    }


    /**
     * Return the content length for this Request.
     */
    public int getContentLength() {
        return (coyoteRequest.getContentLength());
    }


    /**
     * Return the content type for this Request.
     */
    public String getContentType() {
        return (coyoteRequest.getContentType());
    }


    /**
     * Return the servlet input stream for this Request.  The default
     * implementation returns a servlet input stream created by
     * <code>createInputStream()</code>.
     *
     * @exception IllegalStateException if <code>getReader()</code> has
     *  already been called for this request
     * @exception IOException if an input/output error occurs
     */
    public ServletInputStream getInputStream() throws IOException {

        if (usingReader)
            throw new IllegalStateException
                (sm.getString("coyoteRequest.getInputStream.ise"));

        usingInputStream = true;
        if (inputStream == null) {
            inputStream = new CoyoteInputStream(inputBuffer);
        }
        return inputStream;

    }


    /**
     * Return the preferred Locale that the client will accept content in,
     * based on the value for the first <code>Accept-Language</code> header
     * that was encountered.  If the request did not specify a preferred
     * language, the server's default Locale is returned.
     */
    public Locale getLocale() {

        if (!localesParsed)
            parseLocales();

        if (locales.size() > 0) {
            return ((Locale) locales.get(0));
        } else {
            return (defaultLocale);
        }

    }


    /**
     * Return the set of preferred Locales that the client will accept
     * content in, based on the values for any <code>Accept-Language</code>
     * headers that were encountered.  If the request did not specify a
     * preferred language, the server's default Locale is returned.
     */
    public Enumeration getLocales() {

        if (!localesParsed)
            parseLocales();

        if (locales.size() > 0)
            return (new Enumerator(locales));
        ArrayList results = new ArrayList();
        results.add(defaultLocale);
        return (new Enumerator(results));

    }


    /**
     * Return the value of the specified request parameter, if any; otherwise,
     * return <code>null</code>.  If there is more than one value defined,
     * return only the first one.
     *
     * @param name Name of the desired request parameter
     */
    public String getParameter(String name) {

        if (!requestParametersParsed)
            parseRequestParameters();

        return coyoteRequest.getParameters().getParameter(name);

    }



    /**
     * Returns a <code>Map</code> of the parameters of this request.
     * Request parameters are extra information sent with the request.
     * For HTTP servlets, parameters are contained in the query string
     * or posted form data.
     *
     * @return A <code>Map</code> containing parameter names as keys
     *  and parameter values as map values.
     */
    public Map getParameterMap() {

        if (parameterMap.isLocked())
            return parameterMap;

        Enumeration e = getParameterNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement().toString();
            String[] values = getParameterValues(name);
            parameterMap.put(name, values);
        }

        parameterMap.setLocked(true);

        return parameterMap;

    }


    /**
     * Return the names of all defined request parameters for this request.
     */
    public Enumeration getParameterNames() {

        if (!requestParametersParsed)
            parseRequestParameters();

        return coyoteRequest.getParameters().getParameterNames();

    }


    /**
     * Return the defined values for the specified request parameter, if any;
     * otherwise, return <code>null</code>.
     *
     * @param name Name of the desired request parameter
     */
    public String[] getParameterValues(String name) {

        if (!requestParametersParsed)
            parseRequestParameters();

        return coyoteRequest.getParameters().getParameterValues(name);

    }


    /**
     * Return the protocol and version used to make this Request.
     */
    public String getProtocol() {
        return coyoteRequest.protocol().toString();
    }


    /**
     * Read the Reader wrapping the input stream for this Request.  The
     * default implementation wraps a <code>BufferedReader</code> around the
     * servlet input stream returned by <code>createInputStream()</code>.
     *
     * @exception IllegalStateException if <code>getInputStream()</code>
     *  has already been called for this request
     * @exception IOException if an input/output error occurs
     */
    public BufferedReader getReader() throws IOException {

        if (usingInputStream)
            throw new IllegalStateException
                (sm.getString("coyoteRequest.getReader.ise"));

        usingReader = true;
        inputBuffer.checkConverter();
        if (reader == null) {
            reader = new CoyoteReader(inputBuffer);
        }
        return reader;

    }


    /**
     * Return the real path of the specified virtual path.
     *
     * @param path Path to be translated
     *
     * @deprecated As of version 2.1 of the Java Servlet API, use
     *  <code>ServletContext.getRealPath()</code>.
     */
    public String getRealPath(String path) {

        if (servletContext == null) {
            return null;
        } else {
            try {
                return (servletContext.getRealPath(path));
            } catch (IllegalArgumentException e) {
                return (null);
            }
        }

    }


    /**
     * Return the remote IP address making this Request.
     */
    public String getRemoteAddr() {
        if (remoteAddr == null) {

            // START SJSAS 6347215
            if (connector.getAuthPassthroughEnabled()
                    && connector.getProxyHandler() != null) {
                remoteAddr = connector.getProxyHandler().getRemoteAddress(
                                            getRequest());
                if (remoteAddr == null) {
                    log.warn(sm.getString(
                        "coyoteRequest.nullRemoteAddressFromProxy"));
                }
                return remoteAddr;
            }
            // END SJSAS 6347215

            if (socket != null) {
                InetAddress inet = socket.getInetAddress();
                remoteAddr = inet.getHostAddress();
            } else {
                coyoteRequest.action
                    (ActionCode.ACTION_REQ_HOST_ADDR_ATTRIBUTE, coyoteRequest);
                remoteAddr = coyoteRequest.remoteAddr().toString();
            }
        }
        return remoteAddr;
    }


    /**
     * Return the remote host name making this Request.
     */
    public String getRemoteHost() {
        if (remoteHost == null) {
            if (!connector.getEnableLookups()) {
                remoteHost = getRemoteAddr();
            // START SJSAS 6347215
            } else if (connector.getAuthPassthroughEnabled()
                    && connector.getProxyHandler() != null) {
                String addr =
                    connector.getProxyHandler().getRemoteAddress(getRequest());
                if (addr != null) {
                    try {
                        remoteHost = InetAddress.getByName(addr).getHostName();
                    } catch (UnknownHostException e) {
                        log.warn(sm.getString(
                                    "coyoteRequest.unknownHost",
                                    addr),
                                 e);
                    }
                } else {
                    log.warn(sm.getString(
                        "coyoteRequest.nullRemoteAddressFromProxy"));
                }
            // END SJSAS 6347215
            } else if (socket != null) {
                InetAddress inet = socket.getInetAddress();
                remoteHost = inet.getHostName();
            } else {
                coyoteRequest.action
                    (ActionCode.ACTION_REQ_HOST_ATTRIBUTE, coyoteRequest);
                remoteHost = coyoteRequest.remoteHost().toString();
            }
        }
        return remoteHost;
    }

    /**
     * Returns the Internet Protocol (IP) source port of the client
     * or last proxy that sent the request.
     */    
    public int getRemotePort(){
        if (remotePort == -1) {
            if (socket != null) {
                remotePort = socket.getPort();
            } else {
                coyoteRequest.action
                    (ActionCode.ACTION_REQ_REMOTEPORT_ATTRIBUTE, coyoteRequest);
                remotePort = coyoteRequest.getRemotePort();
            }
        }
        return remotePort;    
    }

    /**
     * Returns the host name of the Internet Protocol (IP) interface on
     * which the request was received.
     */
    public String getLocalName(){
       if (localName == null) {
            if (socket != null) {
                InetAddress inet = socket.getLocalAddress();
                localName = inet.getHostName();
            } else {
                coyoteRequest.action
                    (ActionCode.ACTION_REQ_LOCAL_NAME_ATTRIBUTE, coyoteRequest);                
                localName = coyoteRequest.localName().toString();
            }
        }
        return localName;
    }

    /**
     * Returns the Internet Protocol (IP) address of the interface on
     * which the request  was received.
     */       
    public String getLocalAddr(){
        if (localAddr == null) {
            if (socket != null) {
                InetAddress inet = socket.getLocalAddress();
                localAddr = inet.getHostAddress();
            } else {
                coyoteRequest.action
                    (ActionCode.ACTION_REQ_LOCAL_ADDR_ATTRIBUTE, coyoteRequest);
                localAddr = coyoteRequest.localAddr().toString();
            }
        }
        return localAddr;    
    }


    /**
     * Returns the Internet Protocol (IP) port number of the interface
     * on which the request was received.
     */
    public int getLocalPort(){
        if (localPort == -1){
            if (socket != null) {
                localPort = socket.getLocalPort();
            } else {
                coyoteRequest.action
                    (ActionCode.ACTION_REQ_LOCALPORT_ATTRIBUTE, coyoteRequest);
                localPort = coyoteRequest.getLocalPort();
            }
        }
        return localPort;
    }
    
    /**
     * Return a RequestDispatcher that wraps the resource at the specified
     * path, which may be interpreted as relative to the current request path.
     *
     * @param path Path of the resource to be wrapped
     */
    public RequestDispatcher getRequestDispatcher(String path) {

        if (servletContext == null) {
            return null;
        }

        // If the path is already context-relative, just pass it through
        if (path == null)
            return (null);
        else if (path.startsWith("/"))
            return (servletContext.getRequestDispatcher(path));

        // Convert a request-relative path to a context-relative one
        String servletPath = (String) getAttribute(Globals.INCLUDE_SERVLET_PATH_ATTR);
        if (servletPath == null)
            servletPath = getServletPath();

        // Add the path info, if there is any
        String pathInfo = getPathInfo();
        String requestPath = null;

        if (pathInfo == null) {
            requestPath = servletPath;
        } else {
            requestPath = servletPath + pathInfo;
        }

        int pos = requestPath.lastIndexOf('/');
        String relative = null;
        if (pos >= 0) {
            relative = RequestUtil.normalize
                (requestPath.substring(0, pos + 1) + path);
        } else {
            relative = RequestUtil.normalize(requestPath + path);
        }

        return (servletContext.getRequestDispatcher(relative));

    }


    /**
     * Return the scheme used to make this Request.
     */
    public String getScheme() {
        // START S1AS 6170450
        if (getConnector() != null
                && getConnector().getAuthPassthroughEnabled()) {
            ProxyHandler proxyHandler = getConnector().getProxyHandler();
            if (proxyHandler != null
                    && proxyHandler.getSSLKeysize(getRequest()) > 0) {
                return "https";
            }
        }
        // END S1AS 6170450

        return (coyoteRequest.scheme().toString());
    }


    /**
     * Return the server name responding to this Request.
     */
    public String getServerName() {
        return (coyoteRequest.serverName().toString());
    }


    /**
     * Return the server port responding to this Request.
     */
    public int getServerPort() {
        /* SJSAS 6586658
        return (coyoteRequest.getServerPort());
        */
        // START SJSAS 6586658
        if (isSecure()) {
            String host = getHeader("host");
            if (host != null && host.indexOf(':') == -1) {
                // No port number provided with Host header, use default
                return 443;
            } else {
                return (coyoteRequest.getServerPort());
            }
        } else {
            return (coyoteRequest.getServerPort());
        }
        // END SJSAS 6586658
    }


    /**
     * Was this request received on a secure connection?
     */
    public boolean isSecure() {
        return (secure);
    }


    /**
     * Remove the specified request attribute if it exists.
     *
     * @param name Name of the request attribute to remove
     */
    public void removeAttribute(String name) {
        Object value = null;
        boolean found = attributes.containsKey(name);
        if (found) {
            value = attributes.get(name);
            attributes.remove(name);
        } else {
            return;
        }

        // Notify interested application event listeners
        Object listeners[] = context.getApplicationEventListeners();
        if ((listeners == null) || (listeners.length == 0))
            return;
        ServletRequestAttributeEvent event =
            new ServletRequestAttributeEvent(servletContext, getRequest(),
                                             name, value);
        for (int i = 0; i < listeners.length; i++) {
            if (!(listeners[i] instanceof ServletRequestAttributeListener))
                continue;
            ServletRequestAttributeListener listener =
                (ServletRequestAttributeListener) listeners[i];
            try {
                listener.attributeRemoved(event);
            } catch (Throwable t) {
                log(sm.getString("coyoteRequest.attributeEvent"), t);
                // Error valve will pick this execption up and display it to user
                attributes.put( Globals.EXCEPTION_ATTR, t );
            }
        }
    }


    /**
     * Set the specified request attribute to the specified value.
     *
     * @param name Name of the request attribute to set
     * @param value The associated value
     */
    public void setAttribute(String name, Object value) {

        // Name cannot be null
        if (name == null)
            throw new IllegalArgumentException
                (sm.getString("coyoteRequest.setAttribute.namenull"));

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }

        if (name.equals(Globals.DISPATCHER_TYPE_ATTR)) {
            dispatcherType = value;
            return;
        } else if (name.equals(Globals.DISPATCHER_REQUEST_PATH_ATTR)) {
            requestDispatcherPath = value;
            return;
        }

        boolean replaced = false;

        Object oldValue = attributes.put(name, value);
        if (oldValue != null) {
            replaced = true;
        }
        
        // START SJSAS 6231069
        // Pass special attributes to the ngrizzly layer
        if (name.startsWith("grizzly.")) {
            coyoteRequest.setAttribute(name, value);
        }
        // END SJSAS 6231069

        // Notify interested application event listeners
        Object listeners[] = context.getApplicationEventListeners();
        if ((listeners == null) || (listeners.length == 0))
            return;
        ServletRequestAttributeEvent event = null;
        if (replaced) {
            event = new ServletRequestAttributeEvent(servletContext,
                                                     getRequest(), name,
                                                     oldValue);
        } else {
            event = new ServletRequestAttributeEvent(servletContext,
                                                     getRequest(), name,
                                                     value);
        }

        for (int i = 0; i < listeners.length; i++) {
            if (!(listeners[i] instanceof ServletRequestAttributeListener))
                continue;
            ServletRequestAttributeListener listener =
                (ServletRequestAttributeListener) listeners[i];
            try {
                if (replaced) {
                    listener.attributeReplaced(event);
                } else {
                    listener.attributeAdded(event);
                }
            } catch (Throwable t) {
                log(sm.getString("coyoteRequest.attributeEvent"), t);
                // Error valve will pick this execption up and display it to user
                attributes.put( Globals.EXCEPTION_ATTR, t );
            }
        }
    }


    /**
     * Overrides the name of the character encoding used in the body of this
     * request.
     *
     * This method must be called prior to reading request parameters or
     * reading input using <code>getReader()</code>. Otherwise, it has no
     * effect.
     * 
     * @param env      <code>String</code> containing the name of
     *                 the character encoding.
     * @throws         java.io.UnsupportedEncodingException if this
     *                 ServletRequest is still in a state where a
     *                 character encoding may be set, but the specified
     *                 encoding is invalid
     *
     * @since Servlet 2.3
     */
    public void setCharacterEncoding(String enc)
        throws UnsupportedEncodingException {

        // START SJSAS 4936855
        if (requestParametersParsed || usingReader) {
            String contextName =
                (getContext() != null ? getContext().getName() : "UNKNOWN");
            log.warn(sm.getString("coyoteRequest.setCharacterEncoding.ise",
                                  enc, contextName));
            return;
        }
        // END SJSAS 4936855

        // Ensure that the specified encoding is valid
        byte buffer[] = new byte[1];
        buffer[0] = (byte) 'a';

        // START S1AS 6179607: Workaround for 6181598. Workaround should be
        // removed once the underlying issue in J2SE has been fixed.
        /*
         * String dummy = new String(buffer, enc);
         */
        // END S1AS 6179607
        // START S1AS 6179607
        final byte[] finalBuffer = buffer;
        final String finalEnc = enc;
        if (Globals.IS_SECURITY_ENABLED) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws UnsupportedEncodingException {
                        return new String(finalBuffer, finalEnc);
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw (UnsupportedEncodingException) pae.getCause();
            }
        } else {
            new String(buffer, enc);
        }
        // END S1AS 6179607

        // Save the validated encoding
        coyoteRequest.setCharacterEncoding(enc);

    }


    // START S1AS 4703023
    /**
     * Static setter method for the maximum dispatch depth
     */
    public static void setMaxDispatchDepth(int depth) {
        maxDispatchDepth = depth;
    }


    public static int getMaxDispatchDepth(){
        return maxDispatchDepth;
    }

    /**
     * Increment the depth of application dispatch
     */
    public int incrementDispatchDepth() {
        return ++dispatchDepth;
    }


    /**
     * Decrement the depth of application dispatch
     */
    public int decrementDispatchDepth() {
        return --dispatchDepth;
    }


    /**
     * Check if the application dispatching has reached the maximum
     */
    public boolean isMaxDispatchDepthReached() {
        return dispatchDepth > maxDispatchDepth;
    }
    // END S1AS 4703023


    // ---------------------------------------------------- HttpRequest Methods


    /**
     * Add a Cookie to the set of Cookies associated with this Request.
     *
     * @param cookie The new cookie
     */
    public void addCookie(Cookie cookie) {

        // For compatibility only
        if (!cookiesParsed)
            parseCookies();

        cookies.add(cookie);
    }


    /**
     * Add a Header to the set of Headers associated with this Request.
     *
     * @param name The new header name
     * @param value The new header value
     */
    public void addHeader(String name, String value) {
        // Not used
    }


    /**
     * Add a Locale to the set of preferred Locales for this Request.  The
     * first added Locale will be the first one returned by getLocales().
     *
     * @param locale The new preferred Locale
     */
    public void addLocale(Locale locale) {
        locales.add(locale);
    }


    /**
     * Add a parameter name and corresponding set of values to this Request.
     * (This is used when restoring the original request on a form based
     * login).
     *
     * @param name Name of this request parameter
     * @param values Corresponding values for this request parameter
     */
    public void addParameter(String name, String values[]) {
        coyoteRequest.getParameters().addParameterValues(name, values);
    }


    /**
     * Clear the collection of Cookies associated with this Request.
     */
    public void clearCookies() {
        cookiesParsed = true;
        cookies.clear();
    }


    /**
     * Clear the collection of Headers associated with this Request.
     */
    public void clearHeaders() {
        // Not used
    }


    /**
     * Clear the collection of Locales associated with this Request.
     */
    public void clearLocales() {
        locales.clear();
    }


    /**
     * Clear the collection of parameters associated with this Request.
     */
    public void clearParameters() {
        // Not used
    }


    /**
     * Set the authentication type used for this request, if any; otherwise
     * set the type to <code>null</code>.  Typical values are "BASIC",
     * "DIGEST", or "SSL".
     *
     * @param type The authentication type used
     */
    public void setAuthType(String type) {
        this.authType = type;
    }


    /**
     * Set the context path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The context path
     */
    public void setContextPath(String path) {

        if (path == null) {
            mappingData.contextPath.setString("");
        } else {
            mappingData.contextPath.setString(path);
        }

    }


    /**
     * Set the HTTP request method used for this Request.
     *
     * @param method The request method
     */
    public void setMethod(String method) {
        // Not used
    }


    /**
     * Set the query string for this Request.  This will normally be called
     * by the HTTP Connector, when it parses the request headers.
     *
     * @param query The query string
     */
    public void setQueryString(String query) {
        // Not used
    }


    /**
     * Set the path information for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The path information
     */
    public void setPathInfo(String path) {
        mappingData.pathInfo.setString(path);
    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a cookie.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionCookie(boolean flag) {

        this.requestedSessionCookie = flag;

    }


    /**
     * Set the requested session ID for this request.  This is normally called
     * by the HTTP Connector, when it parses the request headers.
     *
     * This method, which is called when the session id is sent as a cookie,
     * or when it is encoded in the request URL, removes a jvmRoute
     * (if present) from the given id.
     *
     * @param id The new session id
     */
    public void setRequestedSessionId(String id) {
        requestedSessionId = id;
        if (id != null && CoyoteAdapter.JVM_ROUTE != null) {
            // Remove jvmRoute. The assumption is that the first dot in the
            // passed in id is the separator between the session id and the
            // jvmRoute. Therefore, the session id, however generated, must
            // never have any dots in it if the jvmRoute mechanism has been
            // enabled. There is no choice to use a separator other than dot
            // because this is the semantics mandated by the mod_jk LB for it
            // to function properly.
            // We can't use StandardContext.getJvmRoute() to determine whether
            // jvmRoute has been enabled, because this CoyoteRequest may not
            // have been associated with any context yet.
            int index = id.indexOf(".");
            if (index > 0) {
                requestedSessionId = id.substring(0, index);
            }
        }
    }


    /**
     * Set a flag indicating whether or not the requested session ID for this
     * request came in through a URL.  This is normally called by the
     * HTTP Connector, when it parses the request headers.
     *
     * @param flag The new flag
     */
    public void setRequestedSessionURL(boolean flag) {

        this.requestedSessionURL = flag;

    }


    /**
     * Set the unparsed request URI for this Request.  This will normally be
     * called by the HTTP Connector, when it parses the request headers.
     *
     * @param uri The request URI
     */
    public void setRequestURI(String uri) {
        // Not used
    }


    /**
     * Set the decoded request URI.
     * 
     * @param uri The decoded request URI
     */
    public void setDecodedRequestURI(String uri) {
        // Not used
    }


    /**
     * Get the decoded request URI.
     * 
     * @return the URL decoded request URI
     */
    public String getDecodedRequestURI() {
        String ret = null;

        if (isDefaultContext) {
            ret = getContextPath() + coyoteRequest.decodedURI().toString();
        } else {
            ret = coyoteRequest.decodedURI().toString();
        }

        return ret;
    }


    /**
     * Get the decoded request URI.
     * 
     * @return the URL decoded request URI
     */
    public MessageBytes getDecodedRequestURIMB() {
        return (coyoteRequest.decodedURI());
    }


    /**
     * Set the servlet path for this Request.  This will normally be called
     * when the associated Context is mapping the Request to a particular
     * Wrapper.
     *
     * @param path The servlet path
     */
    public void setServletPath(String path) {
        if (path != null)
            mappingData.wrapperPath.setString(path);
    }


    /**
     * Set the Principal who has been authenticated for this Request.  This
     * value is also used to calculate the value to be returned by the
     * <code>getRemoteUser()</code> method.
     *
     * @param principal The user Principal
     */
    public void setUserPrincipal(Principal principal) {

        if (SecurityUtil.isPackageProtectionEnabled()){
            HttpSession session = getSession(false);
            if ( (subject != null) && 
                 (!subject.getPrincipals().contains(principal)) ){
                subject.getPrincipals().add(principal);         
            } else if (session != null && 
                            session.getAttribute(Globals.SUBJECT_ATTR) == null) {
                subject = new Subject();
                subject.getPrincipals().add(principal);         
            }
            if (session != null){
                session.setAttribute(Globals.SUBJECT_ATTR, subject);
            }
        } 

        this.userPrincipal = principal;
    }


    // --------------------------------------------- HttpServletRequest Methods


    /**
     * Return the authentication type used for this Request.
     */
    public String getAuthType() {
        return (authType);
    }


    /**
     * Return the portion of the request URI used to select the Context
     * of the Request.
     */
    public String getContextPath() {
        return (mappingData.contextPath.toString());
    }


    /**
     * Get the context path.
     * 
     * @return the context path
     */
    public MessageBytes getContextPathMB() {
        return (mappingData.contextPath);
    }


    /**
     * Return the set of Cookies received with this Request.
     */
    public Cookie[] getCookies() {

        if (!cookiesParsed)
            parseCookies();

        if (cookies.size() == 0) {
            return null;
        }

        return (cookies.toArray(new Cookie[cookies.size()]));
    }


    /**
     * Set the set of cookies recieved with this Request.
     */
    public void setCookies(Cookie[] cookies) {

        this.cookies.clear();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                this.cookies.add(cookies[i]);
        }
    }


    /**
     * Return the value of the specified date header, if any; otherwise
     * return -1.
     *
     * @param name Name of the requested date header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to a date
     */
    public long getDateHeader(String name) {

        String value = getHeader(name);
        if (value == null)
            return (-1L);

        // Attempt to convert the date header in a variety of formats
        long result = FastHttpDateFormat.parseDate(value, formats);
        if (result != (-1L)) {
            return result;
        }
        throw new IllegalArgumentException(value);

    }


    /**
     * Return the first value of the specified header, if any; otherwise,
     * return <code>null</code>
     *
     * @param name Name of the requested header
     */
    public String getHeader(String name) {
        return coyoteRequest.getHeader(name);
    }


    /**
     * Return all of the values of the specified header, if any; otherwise,
     * return an empty enumeration.
     *
     * @param name Name of the requested header
     */
    public Enumeration getHeaders(String name) {
        return coyoteRequest.getMimeHeaders().values(name);
    }


    /**
     * Return the names of all headers received with this request.
     */
    public Enumeration getHeaderNames() {
        return coyoteRequest.getMimeHeaders().names();
    }


    /**
     * Return the value of the specified header as an integer, or -1 if there
     * is no such header for this request.
     *
     * @param name Name of the requested header
     *
     * @exception IllegalArgumentException if the specified header value
     *  cannot be converted to an integer
     */
    public int getIntHeader(String name) {

        String value = getHeader(name);
        if (value == null) {
            return (-1);
        } else {
            return (Integer.parseInt(value));
        }

    }


    /**
     * Return the HTTP request method used in this Request.
     */
    public String getMethod() {
        return coyoteRequest.method().toString();
    }


    /**
     * Return the path information associated with this Request.
     */
    public String getPathInfo() {
        return (mappingData.pathInfo.toString());
    }


    /**
     * Get the path info.
     * 
     * @return the path info
     */
    public MessageBytes getPathInfoMB() {
        return (mappingData.pathInfo);
    }


    /**
     * Return the extra path information for this request, translated
     * to a real path.
     */
    public String getPathTranslated() {

        if (servletContext == null) {
            return (null);
        }

        if (getPathInfo() == null) {
            return (null);
        } else {
            return (servletContext.getRealPath(getPathInfo()));
        }

    }


    /**
     * Return the query string associated with this request.
     */
    public String getQueryString() {
        String queryString = coyoteRequest.queryString().toString();

        if (queryString == null || queryString.equals("")) {
            return (null);
        } else {
            return queryString;
        }
    }


    /**
     * Return the name of the remote user that has been authenticated
     * for this Request.
     */
    public String getRemoteUser() {

        if (userPrincipal != null) {
            return (userPrincipal.getName());
        } else {
            return (null);
        }

    }


    /**
     * Get the request path.
     * 
     * @return the request path
     */
    public MessageBytes getRequestPathMB() {
        return (mappingData.requestPath);
    }


    /**
     * Return the session identifier included in this request, if any.
     */
    public String getRequestedSessionId() {
        return (requestedSessionId);
    }


    /**
     * Return the request URI for this request.
     */
    public String getRequestURI() {

        if (requestURI == null) {
            // START GlassFish 1024
            if (isDefaultContext) {
                requestURI = getContextPath()
                    + coyoteRequest.requestURI().toString();
            } else {
                // END GlassFish 1024
                requestURI = coyoteRequest.requestURI().toString();
                // START GlassFish 1024
            }
            // END GlassFish 1024
        }

        return requestURI;
    }


    /**
     * Reconstructs the URL the client used to make the request.
     * The returned URL contains a protocol, server name, port
     * number, and server path, but it does not include query
     * string parameters.
     * <p>
     * Because this method returns a <code>StringBuffer</code>,
     * not a <code>String</code>, you can modify the URL easily,
     * for example, to append query parameters.
     * <p>
     * This method is useful for creating redirect messages and
     * for reporting errors.
     *
     * @return A <code>StringBuffer</code> object containing the
     *  reconstructed URL
     */
    public StringBuffer getRequestURL() {

        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0)
            port = 80; // Work around java.net.URL bug

        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        if ((scheme.equals("http") && (port != 80))
            || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());

        return (url);

    }


    /**
     * Return the portion of the request URI used to select the servlet
     * that will process this request.
     */
    public String getServletPath() {
        return (mappingData.wrapperPath.toString());
    }


    /**
     * Get the servlet path.
     * 
     * @return the servlet path
     */
    public MessageBytes getServletPathMB() {
        return (mappingData.wrapperPath);
    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary.
     */
    public HttpSession getSession() {
        Session session = doGetSession(true);
        if (session != null) {
            return session.getSession();
        } else {
            return null;
        }
    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create Create a new session if one does not exist
     */
    public HttpSession getSession(boolean create) {
        Session session = doGetSession(create);
        if (session != null) {
            return session.getSession();
        } else {
            return null;
        }
    }
    
    /**
     * set the session - this method is not for general use
     *
     * @param the new session
     */
    public void setSession(Session newSess) {
        session = newSess;
    }    


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from a cookie.
     */
    public boolean isRequestedSessionIdFromCookie() {

        if (requestedSessionId != null)
            return (requestedSessionCookie);
        else
            return (false);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     */
    public boolean isRequestedSessionIdFromURL() {

        if (requestedSessionId != null)
            return (requestedSessionURL);
        else
            return (false);

    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request came from the request URI.
     *
     * @deprecated As of Version 2.1 of the Java Servlet API, use
     *  <code>isRequestedSessionIdFromURL()</code> instead.
     */
    public boolean isRequestedSessionIdFromUrl() {
        return (isRequestedSessionIdFromURL());
    }


    /**
     * Return <code>true</code> if the session identifier included in this
     * request identifies a valid session.
     */
    public boolean isRequestedSessionIdValid() {

        if (requestedSessionId == null)
            return (false);
        if (context == null)
            return (false);

        if (session != null
                && requestedSessionId.equals(session.getIdInternal())) {
            return session.isValid();
        }

        Manager manager = context.getManager();
        if (manager == null)
            return (false);
        Session localSession = null;
        try {
            if (manager.isSessionVersioningSupported()) {
                localSession = manager.findSession(requestedSessionId,
                                                   requestedSessionVersion);
            } else {
                localSession = manager.findSession(requestedSessionId);
            }
        } catch (IOException e) {
            localSession = null;
        }
        if ((localSession != null) && localSession.isValid())
            return (true);
        else
            return (false);

    }


    /**
     * Return <code>true</code> if the authenticated user principal
     * possesses the specified role name.
     *
     * @param role Role name to be validated
     */
    public boolean isUserInRole(String role) {

        // BEGIN RIMOD 4949842
        /*
         * Must get userPrincipal through getUserPrincipal(), can't assume
         * it has already been set since it may be coming from core.
         */
        Principal userPrincipal = this.getUserPrincipal();
        // END RIMOD 4949842

        // Have we got an authenticated principal at all?
        if (userPrincipal == null)
            return (false);

        // Identify the Realm we will use for checking role assignmenets
        if (context == null)
            return (false);
        Realm realm = context.getRealm();
        if (realm == null)
            return (false);

        // Check for a role alias defined in a <security-role-ref> element
        if (wrapper != null) {
            String realRole = wrapper.findSecurityReference(role);

            //START SJSAS 6232464
            if ((realRole != null) &&
                //realm.hasRole(userPrincipal, realRole))
                realm.hasRole(this, (HttpResponse) response,
                    userPrincipal, realRole))
                return (true);
        }

        // Check for a role defined directly as a <security-role>

        //return (realm.hasRole(userPrincipal, role));
        return (realm.hasRole(this, (HttpResponse) response,
                    userPrincipal, role));
        //END SJSAS 6232464
    }


    /**
     * Return the principal that has been authenticated for this Request.
     */
    public Principal getUserPrincipal() {
        return (userPrincipal);
    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary.
     */
    public Session getSessionInternal() {
        return doGetSession(true);
    }


    /**
     * Return the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create Create a new session if one does not exist
     */
    public Session getSessionInternal(boolean create) {
        return doGetSession(create);
    }


    /**
     * This object does not implement a session ID generator. Provide
     * a dummy implementation so that the default one will be used.
     */
    public String generateSessionId() {
        return null;
    }


    /**
     * Gets the servlet context to which this servlet request was last
     * dispatched.
     *
     * @return the servlet context to which this servlet request was last
     * dispatched
     */
    public ServletContext getServletContext() {
        return servletContext;
    }


    /**
     * Gets the servlet response with which this servlet request has been
     * associated.
     *
     * @return the servlet response with which this servlet request has been
     * associated
     */
    public ServletResponse getServletResponse() {
        if (response != null) {
            // Return response facade
            return ((CoyoteResponse) response).getResponse();
        } else {
            return null;
        }
    }    


    // ------------------------------------------------------ Protected Methods


    protected Session doGetSession(boolean create) {

        // There cannot be a session if no context has been assigned yet
        if (context == null)
            return (null);

        // Return the current session if it exists and is valid
        if ((session != null) && !session.isValid()) {
            session = null;
            requestedSessionVersion = null;
        }
        if (session != null)
            return (session);

        // Return the requested session if it exists and is valid
        Manager manager = null;
        if (context != null)
            manager = context.getManager();
        if (manager == null)
            return (null);      // Sessions are not supported
        if (requestedSessionId != null) {
            if (!checkUnsuccessfulSessionFind || !unsuccessfulSessionFind) {
                try {
                    if (manager.isSessionVersioningSupported()) {
                        session = manager.findSession(requestedSessionId,
                                                      requestedSessionVersion);
                        incrementSessionVersion((StandardSession) session,
                                                context);
                    } else {
                        session = manager.findSession(requestedSessionId);
                    }
                    if(session == null) {
                        unsuccessfulSessionFind = true;
                    }
                } catch (IOException e) {
                    session = null;
                }
            }
            if ((session != null) && !session.isValid())
                session = null;
            if (session != null) {
                session.access();
                return (session);
            }
        }

        // Create a new session if requested and the response is not committed
        if (!create)
            return (null);
        if ((context != null) && (response != null) &&
            context.getCookies() &&
            response.getResponse().isCommitted()) {
            throw new IllegalStateException
              (sm.getString("coyoteRequest.sessionCreateCommitted"));
        }

        // START S1AS8PE 4817642
        if (requestedSessionId != null && context.getReuseSessionID()) {
            session = manager.createSession(requestedSessionId);
            if (manager instanceof PersistentManagerBase) {
                ((PersistentManagerBase) manager).
                    removeFromInvalidatedSessions(requestedSessionId);
            }
        // END S1AS8PE 4817642
        // START GlassFish 896
        } else if (sessionTracker.getActiveSessions() > 0) {
            synchronized (sessionTracker) {
                if (sessionTracker.getActiveSessions() > 0) {
                    String id = sessionTracker.getSessionId();
                    session = manager.createSession(id);
                    if (manager instanceof PersistentManagerBase) {
                        ((PersistentManagerBase) manager).
                            removeFromInvalidatedSessions(id);
                    }
                }
            }
        // END GlassFish 896
        // START S1AS8PE 4817642
        } else {
        // END S1AS8PE 4817642
            // Use the connector's random number generator (if any) to generate
            // a session ID. Fallback to the default session ID generator if
            // the connector does not implement one.
            String id = generateSessionId();
            if (id != null) {
                session = manager.createSession(id);
            } else {
                session = manager.createSession();
            } 
        // START S1AS8PE 4817642
        }
        // END S1AS8PE 4817642

        StandardHost reqHost = (StandardHost) getHost();
        if (reqHost != null) {
            SingleSignOn sso = reqHost.getSingleSignOn();
            if (sso != null) {
                String ssoId = (String) getNote(
                    org.apache.catalina.authenticator.Constants.REQ_SSOID_NOTE);
                if (ssoId != null) {
                    sso.associate(ssoId, session);
                    removeNote(
                        org.apache.catalina.authenticator.Constants.REQ_SSOID_NOTE);
                }
            }
        }

        // START GlassFish 896
        sessionTracker.track(session);
        // END GlassFish 896

        // Creating a new session cookie based on the newly created session
        if ((session != null) && (getContext() != null)) {

            if (manager.isSessionVersioningSupported()) {
                incrementSessionVersion((StandardSession) session, context);
            }

            if (getContext().getCookies()) {
                String id = session.getIdInternal();
                String jvmRoute = ((StandardContext) getContext()).getJvmRoute();
                if (jvmRoute != null) {
                    id += ("." + jvmRoute);
                }
                Cookie cookie = new Cookie(Globals.SESSION_COOKIE_NAME, id);
                configureSessionCookie(cookie);
                ((HttpServletResponse) response).addCookie(cookie);
            }
        }

        if (session != null) {
            session.access();
            return (session);
        } else {
            return (null);
        }

    }

    /**
     * Configures the given JSESSIONID cookie.
     *
     * @param cookie The JSESSIONID cookie to be configured
     */
    protected void configureSessionCookie(Cookie cookie) {
        cookie.setMaxAge(-1);
        String contextPath = null;
        // START GlassFish 1024
        if (isDefaultContext) {
            cookie.setPath("/");
        } else {
            // END GlassFish 1024
            if (getContext() != null) {
                // START OF SJSAS 6231069
                // contextPath = getContext().getEncodedPath();
                contextPath = getContext().getPath();
                // END OF SJSAS 6231069
            }
            if ((contextPath != null) && (contextPath.length() > 0)) {
                cookie.setPath(contextPath);
            } else {
                cookie.setPath("/");
            }
        // START GlassFish 1024
        }
        // END GlassFish 1024
        if (isSecure()) {
            cookie.setSecure(true);
        }
    }


    /**
     * Parse cookies.
     */
    protected void parseCookies() {

        cookiesParsed = true;

        Cookies serverCookies = coyoteRequest.getCookies();
        int count = serverCookies.getCookieCount();
        if (count <= 0)
            return;

        cookies.clear();

        for (int i = 0; i < count; i++) {
            ServerCookie scookie = serverCookies.getCookie(i);
            try {
                /* GlassFish 898
                Cookie cookie = new Cookie(scookie.getName().toString(),
                                           scookie.getValue().toString());
                */
                // START GlassFish 898
                Cookie cookie = makeCookie(scookie);
                // END GlassFish 898
                cookie.setPath(scookie.getPath().toString());
                cookie.setVersion(scookie.getVersion());
                String domain = scookie.getDomain().toString();
                if (domain != null) {
                    cookie.setDomain(scookie.getDomain().toString());
                }
                cookies.add(cookie);
            } catch(IllegalArgumentException e) {
                ; // Ignore bad cookie.
            }
        }
    }


    // START GlassFish 898
    protected Cookie makeCookie(ServerCookie scookie) {
        return makeCookie(scookie, false);
    }

    protected Cookie makeCookie(ServerCookie scookie, boolean decode) {

        String name = scookie.getName().toString();
        String value = scookie.getValue().toString();

        if (decode) {
            try {
                name = URLDecoder.decode(name, "UTF-8");
                value = URLDecoder.decode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                name = URLDecoder.decode(name);
                value = URLDecoder.decode(value);
            }
        }

        return new Cookie(name, value);
    }
    // END GlassFish 898


    /**
     * Parse request parameters.
     */
    protected void parseRequestParameters() {

        /* SJSAS 4936855
        requestParametersParsed = true;
        */

        Parameters parameters = coyoteRequest.getParameters();

        // getCharacterEncoding() may have been overridden to search for
        // hidden form field containing request encoding
        String enc = getCharacterEncoding();
        // START SJSAS 4936855
        // Delay updating requestParametersParsed to TRUE until
        // after getCharacterEncoding() has been called, because
        // getCharacterEncoding() may cause setCharacterEncoding() to be
        // called, and the latter will ignore the specified encoding if
        // requestParametersParsed is TRUE
        requestParametersParsed = true;
        // END SJSAS 4936855
        if (enc != null) {
            parameters.setEncoding(enc);
            parameters.setQueryStringEncoding(enc);
        } else {
            parameters.setEncoding
                (com.sun.grizzly.tcp.Constants.DEFAULT_CHARACTER_ENCODING);
            parameters.setQueryStringEncoding
                (com.sun.grizzly.tcp.Constants.DEFAULT_CHARACTER_ENCODING);
        }

        parameters.handleQueryParameters();

        if (usingInputStream || usingReader)
            return;

        if (!getMethod().equalsIgnoreCase("POST"))
            return;

        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        if (!("application/x-www-form-urlencoded".equals(contentType)))
            return;

        int len = getContentLength();

        if (len > 0) {
            int maxPostSize = ((CoyoteConnector) connector).getMaxPostSize();
            if ((maxPostSize > 0) && (len > maxPostSize)) {
                log(sm.getString("coyoteRequest.postTooLarge"));
                throw new IllegalStateException("Post too large");
            }
            try {
                /* SJSAS 6346738
                byte[] formData = null;
                if (len < CACHED_POST_LEN) {
                    if (postData == null)
                        postData = new byte[CACHED_POST_LEN];
                    formData = postData;
                } else {
                    formData = new byte[len];
                }
                int actualLen = readPostBody(formData, len);
                if (actualLen == len) {
                    parameters.processParameters(formData, 0, len);
                }
                */
                // START SJSAS 6346738
                byte[] formData = getPostBody();
                if (formData != null) {
                    parameters.processParameters(formData, 0, len);
                }
                // END SJSAS 6346738
            } catch (Throwable t) {
                ; // Ignore
            }
        }

    }


    // START SJSAS 6346738
    /**
     * Gets the POST body of this request.
     *
     * @return The POST body of this request
     */
    protected byte[] getPostBody() throws IOException {

        int len = getContentLength();
        byte[] formData = null;

        if (len < CACHED_POST_LEN) {
            if (postData == null)
                postData = new byte[CACHED_POST_LEN];
            formData = postData;
        } else {
            formData = new byte[len];
        }
        int actualLen = readPostBody(formData, len);
        if (actualLen == len) {
            return formData;
        }

        return null;
    }
    // END SJSAS 6346738


    /**
     * Read post body in an array.
     */
    protected int readPostBody(byte body[], int len)
        throws IOException {

        int offset = 0;
        do {
            int inputLen = getStream().read(body, offset, len - offset);
            if (inputLen <= 0) {
                return offset;
            }
            offset += inputLen;
        } while ((len - offset) > 0);
        return len;

    }


    /**
     * Parse request locales.
     */
    protected void parseLocales() {

        localesParsed = true;

        Enumeration values = getHeaders("accept-language");

        while (values.hasMoreElements()) {
            String value = values.nextElement().toString();
            parseLocalesHeader(value);
        }

    }


    /**
     * Parse accept-language header value.
     */
    protected void parseLocalesHeader(String value) {

        // Store the accumulated languages that have been requested in
        // a local collection, sorted by the quality value (so we can
        // add Locales in descending order).  The values will be ArrayLists
        // containing the corresponding Locales to be added
        TreeMap locales = new TreeMap();

        // Preprocess the value to remove all whitespace
        int white = value.indexOf(' ');
        if (white < 0)
            white = value.indexOf('\t');
        if (white >= 0) {
            StringBuffer sb = new StringBuffer();
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char ch = value.charAt(i);
                if ((ch != ' ') && (ch != '\t'))
                    sb.append(ch);
            }
            value = sb.toString();
        }

        // Process each comma-delimited language specification
        parser.setString(value);        // ASSERT: parser is available to us
        int length = parser.getLength();
        while (true) {

            // Extract the next comma-delimited entry
            int start = parser.getIndex();
            if (start >= length)
                break;
            int end = parser.findChar(',');
            String entry = parser.extract(start, end).trim();
            parser.advance();   // For the following entry

            // Extract the quality factor for this entry
            double quality = 1.0;
            int semi = entry.indexOf(";q=");
            if (semi >= 0) {
                try {
                    quality = Double.parseDouble(entry.substring(semi + 3));
                } catch (NumberFormatException e) {
                    quality = 0.0;
                }
                entry = entry.substring(0, semi);
            }

            // Skip entries we are not going to keep track of
            if (quality < 0.00005)
                continue;       // Zero (or effectively zero) quality factors
            if ("*".equals(entry))
                continue;       // FIXME - "*" entries are not handled

            // Extract the language and country for this entry
            String language = null;
            String country = null;
            String variant = null;
            int dash = entry.indexOf('-');
            if (dash < 0) {
                language = entry;
                country = "";
                variant = "";
            } else {
                language = entry.substring(0, dash);
                country = entry.substring(dash + 1);
                int vDash = country.indexOf('-');
                if (vDash > 0) {
                    String cTemp = country.substring(0, vDash);
                    variant = country.substring(vDash + 1);
                    country = cTemp;
                } else {
                    variant = "";
                }
            }

            if (!isAlpha(language) || !isAlpha(country) || !isAlpha(variant)) {
                continue;
            }

            // Add a new Locale to the list of Locales for this quality level
            Locale locale = new Locale(language, country, variant);
            Double key = new Double(-quality);  // Reverse the order
            ArrayList values = (ArrayList) locales.get(key);
            if (values == null) {
                values = new ArrayList();
                locales.put(key, values);
            }
            values.add(locale);

        }

        // Process the quality values in highest->lowest order (due to
        // negating the Double value when creating the key)
        Iterator keys = locales.keySet().iterator();
        while (keys.hasNext()) {
            Double key = (Double) keys.next();
            ArrayList list = (ArrayList) locales.get(key);
            Iterator values = list.iterator();
            while (values.hasNext()) {
                Locale locale = (Locale) values.next();
                addLocale(locale);
            }
        }

    }


    /*
     * Returns true if the given string is composed of upper- or lowercase
     * letters only, false otherwise.
     *
     * @return true if the given string is composed of upper- or lowercase
     * letters only, false otherwise.
     */
    protected static final boolean isAlpha(String value) {

        if (value == null) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }

        return true;
    }
 

    // START CR 6309511
    /**
     * Parse session id in URL.
     */
    protected void parseSessionId() {

        CharChunk uriCC = coyoteRequest.decodedURI().getCharChunk();
        int semicolon = uriCC.indexOf(match, 0, match.length(), 0);

        if (semicolon > 0) {

            // Parse session ID, and extract it from the decoded request URI
            int start = uriCC.getStart();
            int end = uriCC.getEnd();

            int sessionIdStart = start + semicolon + match.length();
            int semicolon2 = uriCC.indexOf(';', sessionIdStart);
            /* SJSAS 6346226
            if (semicolon2 >= 0) {
                setRequestedSessionId
                    (new String(uriCC.getBuffer(), sessionIdStart, 
                                semicolon2 - semicolon - match.length()));
            } else {
                setRequestedSessionId
                    (new String(uriCC.getBuffer(), sessionIdStart, 
                                end - sessionIdStart));
            }
            */
            // START SJSAS 6346226
            String sessionId = null;
            if (semicolon2 >= 0) {
                sessionId = new String(uriCC.getBuffer(), sessionIdStart, 
                                       semicolon2 - semicolon - match.length());
            } else {
                sessionId = new String(uriCC.getBuffer(), sessionIdStart, 
                                       end - sessionIdStart);
            }
            int jrouteIndex = sessionId.lastIndexOf(':');
            if (jrouteIndex > 0) {
                setRequestedSessionId(sessionId.substring(0, jrouteIndex));
                if (jrouteIndex < (sessionId.length()-1)) {
		    setJrouteId(sessionId.substring(jrouteIndex+1));
                }
	    } else {
                setRequestedSessionId(sessionId);
            }
            // END SJSAS 6346226

            setRequestedSessionURL(true);

            /* SJSWS 6376484
            // Extract session ID from request URI
            ByteChunk uriBC = coyoteRequest.requestURI().getByteChunk();
            start = uriBC.getStart();
            end = uriBC.getEnd();
            semicolon = uriBC.indexOf(match, 0, match.length(), 0);

            if (semicolon > 0) {
                sessionIdStart = start + semicolon;
                semicolon2 = uriCC.indexOf
                    (';', start + semicolon + match.length());
                uriBC.setEnd(start + semicolon);
                byte[] buf = uriBC.getBuffer();
                if (semicolon2 >= 0) {
                    for (int i = 0; i < end - start - semicolon2; i++) {
                        buf[start + semicolon + i] 
                            = buf[start + i + semicolon2];
                    }
                    uriBC.setBytes(buf, start, semicolon 
                                   + (end - start - semicolon2));
                }
            }
            */
            // START SJSWS 6376484
            /*
             * Parse the session id from the encoded URI only if the encoded
             * URI is not null, to allow for lazy evaluation
             */
            if (!coyoteRequest.requestURI().getByteChunk().isNull()) {
                parseSessionIdFromRequestURI();
            }
            // END SJSWS 6376484

        } else {
            setRequestedSessionId(null);
            setRequestedSessionURL(false);
        }

    }
    // END CR 6309511


    /**
     * Parses and removes any session version (if present) from the request
     * URI, and returns it
     *
     * @return The session version that was parsed from the request URI
     */
    protected String parseSessionVersion() {

        String sessionVersionString = null;

        CharChunk uriCC = coyoteRequest.decodedURI().getCharChunk();
        int semicolon = uriCC.indexOf(Globals.SESSION_VERSION_PARAMETER, 0,
                                      Globals.SESSION_VERSION_PARAMETER.length(),
                                      0);
        if (semicolon > 0) {

            int start = uriCC.getStart();
            int end = uriCC.getEnd();

            int sessionVersionStart = start + semicolon
                + Globals.SESSION_VERSION_PARAMETER.length();
            int semicolon2 = uriCC.indexOf(';', sessionVersionStart);
            if (semicolon2 >= 0) {
                sessionVersionString = new String(
                    uriCC.getBuffer(),
                    sessionVersionStart, 
                    semicolon2 - semicolon - Globals.SESSION_VERSION_PARAMETER.length());
            } else {
                sessionVersionString = new String(
                    uriCC.getBuffer(),
                    sessionVersionStart, 
                    end - sessionVersionStart);
            }

            if (!coyoteRequest.requestURI().getByteChunk().isNull()) {
                removeSessionVersionFromRequestURI();
            }
        }

        return sessionVersionString;
    }


    // START SJSWS 6376484
    /**
     * Extracts the session ID from the request URI.
     */
    protected void parseSessionIdFromRequestURI() {

        int start, end, sessionIdStart, semicolon, semicolon2;

        ByteChunk uriBC = coyoteRequest.requestURI().getByteChunk();
        start = uriBC.getStart();
        end = uriBC.getEnd();
        semicolon = uriBC.indexOf(match, 0, match.length(), 0);

        if (semicolon > 0) {
            sessionIdStart = start + semicolon;
            semicolon2 = uriBC.indexOf
                (';', semicolon + match.length());
            uriBC.setEnd(start + semicolon);
            byte[] buf = uriBC.getBuffer();
            if (semicolon2 >= 0) {
                for (int i = 0; i < end - start - semicolon2; i++) {
                    buf[start + semicolon + i] 
                        = buf[start + i + semicolon2];
                }
                uriBC.setBytes(buf, start, semicolon 
                               + (end - start - semicolon2));
            }
        }
    }
    // END SJSWS 6376484


    /**
     * Removes the session version from the request URI.
     */
    protected void removeSessionVersionFromRequestURI() {

        int start, end, sessionVersionStart, semicolon, semicolon2;

        ByteChunk uriBC = coyoteRequest.requestURI().getByteChunk();
        start = uriBC.getStart();
        end = uriBC.getEnd();
        semicolon = uriBC.indexOf(Globals.SESSION_VERSION_PARAMETER, 0,
                                  Globals.SESSION_VERSION_PARAMETER.length(),
                                  0);
        if (semicolon > 0) {
            sessionVersionStart = start + semicolon;
            semicolon2 = uriBC.indexOf
                (';', semicolon + Globals.SESSION_VERSION_PARAMETER.length());
            uriBC.setEnd(start + semicolon);
            byte[] buf = uriBC.getBuffer();
            if (semicolon2 >= 0) {
                for (int i = 0; i < end - start - semicolon2; i++) {
                    buf[start + semicolon + i] 
                        = buf[start + i + semicolon2];
                }
                uriBC.setBytes(buf, start, semicolon 
                               + (end - start - semicolon2));
            }
        }
    }


    /*
     * Parses the given session version string into its components. Each
     * component is stored as an entry in a HashMap, which maps a context
     * path to its session version number. The HashMap is stored as a 
     * request attribute, to make it available to any target contexts to which
     * this request may be dispatched.
     *
     * This method also sets the session version number for the context with
     * which this request has been associated.
     */
    void parseSessionVersionString(String sessionVersionString) {

        if (sessionVersionString == null || !isSessionVersionSupported()) {
            return;
        }

        HashMap<String, String> sessionVersions =
            RequestUtil.parseSessionVersions(sessionVersionString);
        if (sessionVersions != null) {
            attributes.put(Globals.SESSION_VERSIONS_REQUEST_ATTRIBUTE,
                           sessionVersions);
            if (context != null) {
                String path = context.getPath();
                if ("".equals(path)) {
                    path = "/";
                }
                this.requestedSessionVersion = sessionVersions.get(path);
            }
        }
    }


    /**
     * Parses the value of the JROUTE cookie, if present.
     */
    void parseJrouteCookie() {

        Cookies serverCookies = coyoteRequest.getCookies();
        int count = serverCookies.getCookieCount();
        if (count <= 0) {
            return;
        }

        for (int i=0; i<count; i++) {
            ServerCookie scookie = serverCookies.getCookie(i);
            if (scookie.getName().equals(Constants.JROUTE_COOKIE)) {
                setJrouteId(scookie.getValue().toString());
                break;
            }
        }
    }

    /**
     * Sets the jroute id of this request.
     * 
     * @param jrouteId The jroute id
     */
    void setJrouteId(String jrouteId) {
        this.jrouteId = jrouteId;
    }

    /**
     * Gets the jroute id of this request, which may have been 
     * sent as a separate <code>JROUTE</code> cookie or appended to the
     * session identifier encoded in the URI (if cookies have been disabled).
     * 
     * @return The jroute id of this request, or null if this request does not
     * carry any jroute id
     */
    public String getJrouteId() {
        return jrouteId;
    }
    // END SJSAS 6346226


    // START CR 6309511
    /**
     * Parse session id in URL.
     */
    protected void parseSessionCookiesId() {

        // Parse session id from cookies
        Cookies serverCookies = coyoteRequest.getCookies();
        int count = serverCookies.getCookieCount();
        if (count <= 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            ServerCookie scookie = serverCookies.getCookie(i);
            if (scookie.getName().equals(Globals.SESSION_COOKIE_NAME)) {
                // Override anything requested in the URL
                if (!isRequestedSessionIdFromCookie()) {
                    // Accept only the first session id cookie
                    B2CConverter.convertASCII(scookie.getValue());
                    setRequestedSessionId
                        (scookie.getValue().toString());
                    // TODO: Pass cookie path into
                    // getSessionVersionFromCookie()
                    String sessionVersionString = getSessionVersionFromCookie();
                    parseSessionVersionString(sessionVersionString);
                    setRequestedSessionCookie(true);
                    setRequestedSessionURL(false);
                    if (log.isDebugEnabled()) {
                        log.debug("Requested cookie session id is " +
                                  ((HttpServletRequest) getRequest())
                                  .getRequestedSessionId());
                    }
                } else {
                    if (!isRequestedSessionIdValid()) {
                        // Replace the session id until one is valid
                        B2CConverter.convertASCII(scookie.getValue());
                        setRequestedSessionId
                            (scookie.getValue().toString());
                        // TODO: Pass cookie path into
                        // getSessionVersionFromCookie()
                        String sessionVersionString =
                            getSessionVersionFromCookie();
                        parseSessionVersionString(sessionVersionString);
                    }
                }
            }
        }
    }
    // END CR 6309511


    /*
     * Returns the value of the first JSESSIONIDVERSION cookie, or null
     * if no such cookie present in the request.
     *
     * TODO: Add cookie path argument, and return value of JSESSIONIDVERSION
     * cookie with the specified path.
     */
    private String getSessionVersionFromCookie() {

        if (!isSessionVersionSupported()) {
            return null;
        }

        Cookies serverCookies = coyoteRequest.getCookies();
        int count = serverCookies.getCookieCount();
        if (count <= 0) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            ServerCookie scookie = serverCookies.getCookie(i);
            if (scookie.getName().equals(
                                Globals.SESSION_VERSION_COOKIE_NAME)) {
                return scookie.getValue().toString();
            }
        }

        return null;
    }


    // START CR 6309511
    /**
     * Character conversion of the URI.
     */
    protected void convertURI(MessageBytes uri) 
        throws Exception {

        ByteChunk bc = uri.getByteChunk();
        CharChunk cc = uri.getCharChunk();
        int length = bc.getLength();
        cc.allocate(length, -1);

        String enc = connector.getURIEncoding();
        if (enc != null) {
            B2CConverter conv = getURIConverter();
            try {
                if (conv == null) {
                    conv = new B2CConverter(enc);
                    setURIConverter(conv);
                } else {
                    conv.recycle();
                }
            } catch (IOException e) {
                // Ignore
                log.error("Invalid URI encoding; using HTTP default");
                connector.setURIEncoding(null);
            }
            if (conv != null) {
                try {
                    conv.convert(bc, cc);
                    uri.setChars(cc.getBuffer(), cc.getStart(), 
                                 cc.getLength());
                    return;
                } catch (IOException e) {
                    log.error("Invalid URI character encoding; trying ascii");
                    cc.recycle();
                }
            }
        }

        // Default encoding: fast conversion
        byte[] bbuf = bc.getBuffer();
        char[] cbuf = cc.getBuffer();
        int start = bc.getStart();
        for (int i = 0; i < length; i++) {
            cbuf[i] = (char) (bbuf[i + start] & 0xff);
        }
        uri.setChars(cbuf, 0, length);

    }
    // END CR 6309511


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = connector.getContainer().getLogger();
        String localName = "CoyoteRequest";
        if (logger != null)
            logger.log(localName + " " + message);
        else
            System.out.println(localName + " " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = connector.getContainer().getLogger();
        String localName = "CoyoteRequest";
        if (logger != null)
            logger.log(localName + " " + message, throwable);
        else {
            System.out.println(localName + " " + message);
            throwable.printStackTrace(System.out);
        }

    }


    // START SJSAS 6419950
    private void populateSSLAttributes() {
        coyoteRequest.action(ActionCode.ACTION_REQ_SSL_ATTRIBUTE, 
                             coyoteRequest);
        Object attr = coyoteRequest.getAttribute(Globals.CERTIFICATES_ATTR);
        if( attr != null) {
            attributes.put(Globals.CERTIFICATES_ATTR, attr);
        }
        attr = coyoteRequest.getAttribute(Globals.CIPHER_SUITE_ATTR);
        if(attr != null) {
            attributes.put(Globals.CIPHER_SUITE_ATTR, attr);
        }
        attr = coyoteRequest.getAttribute(Globals.KEY_SIZE_ATTR);
        if(attr != null) {
            attributes.put(Globals.KEY_SIZE_ATTR, attr);
        }
    }
    // END SJSAS 6419950


    // START GlassFish 896
    private void initSessionTracker() {
        attributes.put(Globals.SESSION_TRACKER, sessionTracker);
    }
    // END GlassFish 896


    /**
     * Increments the version of the given session, and stores it as a
     * request attribute, so it can later be included in a response cookie.
     */
    private void incrementSessionVersion(StandardSession ss,
                                         Context context) {

        if (ss == null || context == null) {
            return;
        }

        ss.incrementVersion();
        String versionString = Long.toString(ss.getVersion());

        HashMap<String, String> sessionVersions = (HashMap<String, String>)
            getAttribute(Globals.SESSION_VERSIONS_REQUEST_ATTRIBUTE);
        if (sessionVersions == null) {
            sessionVersions = new HashMap<String, String>();
            setAttribute(Globals.SESSION_VERSIONS_REQUEST_ATTRIBUTE,
                         sessionVersions);
        }
        String path = context.getPath();
        if ("".equals(path)) {
            path = "/";
        }
        sessionVersions.put(path, versionString);
    }


    private boolean isSessionVersionSupported() {

        return (context != null
            && context.getManager() != null
            && context.getManager().isSessionVersioningSupported());
    }

}
