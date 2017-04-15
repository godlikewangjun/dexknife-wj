package com.wj.dexknife.shell.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Debug {
	
	public static final int VERBOSE = 0;
	public static final int INFO = 1;
	public static final int DEBUG = 2;
	public static final int WARN= 3;
	public static final int ERROR = 4;
	
	private static int mLogLevel = VERBOSE;
	public static void setLogLevel(int logLevel){
		mLogLevel = logLevel;
	}
	public static int getLogLevel(){
		return mLogLevel;
	}

    private static DateFormat localDateFromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static StackTraceElement getCurrentStackTraceElement() {
        return Thread.currentThread().getStackTrace()[6];
	}

    public static String getDateLog(){
        StringBuilder sBuff = new StringBuilder();
        StackTraceElement s = getCurrentStackTraceElement();
        sBuff.append(localDateFromat.format(new Date()))
                .append(format(s));
        return sBuff.toString();
    }

    public static String format(StackTraceElement s){
        //return String.format("%s.%s(%s:%s)%n", s.getClassName(), s.getMethodName(),s.getFileName(), s.getLineNumber());
        return String.format(".(%s:%s)",s.getFileName(), s.getLineNumber());
    }

	private static String format(int logLevel, String log) {

//		INFO: Could not find stylesheet: jar:file:/C:/Users/Administrator/AppData/Local/Temp/e4jA5CD.tmp_dir1460126084/ApkToolPlus_release_1.0.6.jar!/css/jiagu.css

        StringBuilder sBuff = new StringBuilder();
        switch (logLevel){
            case VERBOSE:
                sBuff.append("VERBOSE");
            break;
            case INFO:
                sBuff.append("INFO");
                break;
            case DEBUG:
                sBuff.append("DEBUG");
                break;
            case WARN:
                sBuff.append("WARN");
                break;
            case ERROR:
                sBuff.append("ERROR");
                break;
        }
        sBuff.append(":");
        sBuff.append(getDateLog()).append(": ");
        // log
        if (log != null){
            sBuff.append(log);
        }else{
            sBuff.append("null");
        }
		return sBuff.toString();
	}

	private static void stdOutput(String log){
		System.out.println(log);
	}

	private static void errOutput(String log){
		System.err.println(log);
	}

	private static void formatOutput(int logLevel, String log) {
        if(logLevel < WARN){
            stdOutput(format(logLevel, log));
        }else{
            errOutput(format(logLevel, log));
        }
	}

    public static void v(String msg){
        if (VERBOSE >= mLogLevel) {
            formatOutput(VERBOSE, msg);
        }
    }

    public static void i(String msg){
        if (INFO >= mLogLevel) {
            formatOutput(INFO, msg);
        }
    }

	public static void d(String msg){
		if (DEBUG >= mLogLevel) {
            formatOutput(DEBUG, msg);
		}
	}

    public static void w(String msg){
        if (WARN >= mLogLevel) {
            formatOutput(WARN, msg);
        }
    }

	public static void e(String msg){
		if (ERROR >= mLogLevel) {
            formatOutput(ERROR, msg);
		}
	}

    public static void d(Throwable e){
        if(DEBUG >= mLogLevel && e != null ){
            stdOutput(getDateLog());
            e.printStackTrace();
        }
    }

    public static void e(Throwable e){
        if(ERROR >= mLogLevel && e != null ){
            errOutput(getDateLog());
            e.printStackTrace();
        }
    }
}
