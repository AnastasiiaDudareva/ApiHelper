package com.apihelper.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

/**
 * @author denis usable loging
 */
public final class L {
    private static boolean isDebugMode = true;

	public static void setIsDebugMode(boolean isDebugMode) {
		L.isDebugMode = isDebugMode;
	}

	private L() {
	}

	/** TODO
	 * send logs to logcat.
	 * 
	 * @param cap
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void log(final Object cap, final Object msg) {
		if (isDebugMode) {
			Log.e(cap.toString(), msg + "");
		}
	}

    public static void log(final Object cap, final Response response) {
        if (isDebugMode) {
			try {
				Log.e(cap.toString(), response.body().string() + "");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

	/** TODO
	 * send logs to logcat.
	 * 
	 * @param cap
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void logI(final Object cap, final Object msg) {
		if (isDebugMode) {
			Log.i(cap.toString(), msg + "");
		}
	}

	/** TODO
	 * send logs to logcat.
	 * 
	 * @param cap
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void logV(final Object cap, final Object msg) {
		if (isDebugMode) {
			Log.v(cap.toString(), msg + "");
		}
	}

	/** TODO
	 * send logs to log chat.
	 * 
	 * @param cap
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            Details from exception.
	 */
	public static void logE(final Object cap, final Object msg, Throwable thr) {
		if (isDebugMode) {
			Log.e(cap.toString(), msg + "", thr);
		}
	}
	
	/** TODO
	 * send logs to logcat.
	 * 
	 * @param msg
	 *            The message you would like logged.
	 * @param thr
	 *            Details from exception.
	 */
	public static void logE(final Object msg, Throwable thr) {
		if (isDebugMode) {
			Log.e(getMethodName(0), msg + "", thr);
		}
	}	
	
	/** TODO
	 * send long logs to logcat.
	 * 
	 * @param cap
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 */
	public static void logLong(final Object cap, final Object msg) {
		if (isDebugMode) {
			String text = "" + msg;
			int maxLogSize = 2000;
			for (int i = 0; i <= text.length() / maxLogSize; i++) {
				int start = i * maxLogSize;
				int end = (i + 1) * maxLogSize;
				end = end > text.length() ? text.length() : end;
				Log.v(cap.toString(), text.substring(start, end) + "");
			}
		}
	}
	
	/** TODO
	 * Get the method name for a depth in call stack. <br />
	 * Utility function
	 * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
	 * @return method name
	 */
	public static String getMethodName(final int depth)
	{
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	  return ste[ste.length - 1 - depth].getMethodName(); 
	}
		
	public static void longLogcat(final Object msg, String tag) {
		if (isDebugMode) {
			String text = "" + msg;
			int maxLogSize = 2000;
			for (int i = 0; i <= text.length() / maxLogSize; i++) {
				int start = i * maxLogSize;
				int end = (i + 1) * maxLogSize;
				end = end > text.length() ? text.length() : end;
				
				JSONObject jObject = new JSONObject();
				try {
					jObject.put("message", text.substring(start, end));
					jObject.put("tag", tag);
					jObject.put("end", end == text.length());
				} catch (JSONException e) {
				}
				Log.v("LongLogger", jObject.toString());
			}
		}
	}
}
