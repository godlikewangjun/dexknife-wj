package com.wj.dexknife.shell.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
public class Cmd {

    public static boolean exec(String cmd){
        return exec(cmd, true);
    }

    public static boolean exec(String cmd, boolean isInput){
        return exec(cmd, null, isInput);
    }

    public static boolean exec(String cmd, File workDir){
        return exec(cmd, workDir, true);
    }

    public static boolean exec(String cmd, File workDir, boolean isOutput){
        return exec(cmd, null, workDir, isOutput);
    }

    public static boolean exec(String cmd, String[] env, File workDir, boolean isOutput){
       Debug.d( "exec=" + cmd);
        boolean isSuccess  = true;
        if(FileHelper.exists(workDir) && OS.isWindows()){
            cmd = String.format("cmd /c %s",cmd);
        }
        Runtime runtime = Runtime.getRuntime();
        try {
            if (!isOutput){
                runtime.exec(cmd, env, workDir);
                return true;
            }
            Process proc = runtime.exec(cmd, env, workDir);

            String encoding = System.getProperty("sun.jnu.encoding");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream(),encoding));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream(),encoding));

            // read the _out from the command
            //Debug.d("Here is the standard _out of the command:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
               Debug.d(s);
            }

            // read any errors from the attempted command
            //Debug.e( "Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
               Debug.e(s);
                isSuccess = false;
            }
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static String execAndGetOutput(String cmd){
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
