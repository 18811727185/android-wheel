package com.letv.mobile.core.log;


/**
 * Log Level
 * 
 * @author Fengwx
 * 
 */
public class Level {

	/**
	 * lowest level, turn on all logging
	 */
	public static final int ALL = 0x1;

	/**
	 * verbose level
	 */
	public static final int VERBOSE = 0x2;

	/**
	 * devug level
	 */
	public static final int DEBUG = 0x3;

	/**
	 * info level
	 */
	public static final int INFO = 0x4;

	/**
	 * warn level
	 */
	public static final int WARN = 0x5;

	/**
	 * error level
	 */
	public static final int ERROR = 0x6;

	/**
	 * highest level, turn off loading
	 */
	public static final int OFF = 0x7;

}
