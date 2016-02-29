package org.github.sipuada.android.client.utils;

import android.util.Log;

public final class SipuadaLog {

    private SipuadaLog() {}

    /**
     * The Constant TAG.
     */
    private static final String TAG = "Sipuada";

    /**
     * The Constant ERROR.
     */
    private static final String ERROR = "ERROR";

    /**
     * The Constant WARNING.
     */
    private static final String WARNING = "WARNING";

    /**
     * The Constant INFO.
     */
    private static final String INFO = "INFO";

    /**
     * The Constant DEBUG.
     */
    private static final String DEBUG = "DEBUG";

    /**
     * The Constant ERROR.
     */
    private static final String VERBOSE = "VERBOSE";

    /**
     * Sends a debug message.
     *
     * @param msg the msg to be sent
     */
    public static void debug(final String msg) {
        Log.d( TAG + ":" + DEBUG, msg);
    }

    /**
     * Sends a debug message and the exception message.
     *
     * @param msg the msg to be sent
     * @param e the exception
     */
    public static void debug(final String msg, final Exception e) {
        Log.d( TAG + ":" + DEBUG, msg + ": " + e.getMessage());
    }

    /**
     * Sends a error message.
     *
     * @param msg the msg to be sent
     */
    public static void error(final String msg) {
        Log.e( TAG + ":" + ERROR, msg);
    }

    /**
     * Sends a error message and the exception message.
     *
     * @param msg the msg to be sent
     * @param e the exception
     */
    public static void error(final String msg, final Exception e) {
        Log.e( TAG + ":" + ERROR, msg + ": " + e.getMessage());
    }

    /**
     * Sends a info message.
     *
     * @param msg the msg to be sent
     */
    public static void info(final String msg) {
        Log.i( TAG + ":" + INFO, msg);
    }

    /**
     * Sends a warning message.
     *
     * @param msg the msg to be sent
     */
    public static void warning(final String msg) {
        Log.w( TAG + ":" + WARNING, msg);
    }

    /**
     * Sends a verbose message.
     *
     * @param msg the msg to be sent
     */
    public static void verbose(final String msg) {
        Log.v( TAG + ":" + VERBOSE, msg);
    }
}
