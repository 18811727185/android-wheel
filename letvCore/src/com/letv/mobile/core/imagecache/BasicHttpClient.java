package com.letv.mobile.core.imagecache;

/**
 * BasicHttpClient
 * 
 * @author Fengwx
 * 
 */
public interface BasicHttpClient {

	/**
	 * Sets the maximum number of connections allowed.
	 */
	int MAX_CONNECTIONS = 30;

	/**
	 * Set the timeout of the Connection Manager.
	 */
	int TIMEOUT = 3 * 1000;

	/**
	 * Sets the default socket timeout (SO_TIMEOUT) in milliseconds which is the
	 * timeout for waiting for data. A timeout value of zero is interpreted as
	 * an infinite timeout. This value is used when no socket timeout is set in
	 * the method parameters.
	 */
	int SOCKET_TIMEOUT = 2 * 1000;

	/**
	 * Sets the timeout until a connection is etablished. A value of zero means
	 * the timeout is not used. The default value is zero.
	 */
	int CONNECTION_TIMEOUT = 2 * 1000;

	/**
	 * SOCKET BUFFER SIZE
	 */
	int SOCKET_BUFFER_SIZE = 8 * 1024;

	/**
	 * CHARSET
	 */
	String CHARSET = "UTF-8";

	/**
	 * RETRY COUNT
	 */
	int RETRY_COUNT = 3;

	/**
	 * USERAGENT
	 */
	String USERAGENT = "Android client";

	/**
	 * Sets lookup interface for maximum number of connections allowed per
	 * route.
	 */
	int CONN_PER_ROUTE_MAX = 30;

}
