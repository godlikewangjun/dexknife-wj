package com.wj.dexknife.shell;


import com.wj.dexknife.shell.utils.ClassHelper;
import com.wj.dexknife.shell.utils.Cmd;
import com.wj.dexknife.shell.utils.Debug;
import com.wj.dexknife.shell.utils.FileHelper;
import com.wj.dexknife.shell.utils.StringUtils;

import org.apache.tools.ant.taskdefs.Ant;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;


public class AppManager {

    public static final String TAG = AppManager.class.getSimpleName();


    public static boolean isReleased = false;

    public static  String APKTOOL_JAR = "apktool.jar";
//    public static  String FRAMEWORK_RES = "framework-res.apk";
    public static  String APKTOOL_JAR_PATH = "src/apktool/"+APKTOOL_JAR;
    public static  String APKTOOLJARPATH = null;
    private static File apkTool = new File(getTempDir(), APKTOOL_JAR);

    public static boolean isReleased() {
        return isReleased;
    }

    public static File getApkTool(){
        initApkTool();
        return apkTool;
    }

    public static void initApkTool(){
        if(!FileHelper.exists(apkTool)){
//            File frameworkRes = new File(apkTool.getParentFile(), FRAMEWORK_RES);
            if(AppManager.isReleased()){
                ClassHelper.releaseResourceToFile(APKTOOL_JAR_PATH,apkTool);
//                ClassHelper.releaseResourceToFile("apktool/" + FRAMEWORK_RES,frameworkRes);
            }else{
                File apkToolFile = null;
                if(APKTOOLJARPATH==null){
                    apkToolFile  = new File(getProjectDir(),APKTOOL_JAR_PATH);
                }else{
                    apkToolFile  = new File(APKTOOLJARPATH);
                }
//                File frameworkResFile = new File(getProjectDir(),"src/apktool/" + FRAMEWORK_RES);
                FileHelper.copyFile(apkToolFile,apkTool);
//                FileHelper.copyFile(frameworkResFile,frameworkRes);
            }
//            ApkToolPlus.installFramework(apkTool, frameworkRes);
//            FileHelper.delete(frameworkRes);
        }
    }

    public static void init() {
        isReleased = !getRoot().isDirectory();
        Config.init();
        initApkTool();
    }



    public static void browser(URI uri){
        try {
            if(uri == null){
                return;
            }
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void browser(String uri){
        try {
            browser(new URI(uri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void browser(File file){
        if(file == null){
            return;
        }
        if(!file.exists()){
            return;
        }
        browser(file.toURI());
    }

    public static String getVersion(){
        return Config.getVersion();
    }

    public static File getRoot(){
        return new File(ClassHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    public static File getRuntimeDir(){
        return getRoot().getParentFile();
    }

    public static File getProjectDir(){
        return getRuntimeDir().getParentFile().getParentFile();
    }

    public static File getTempDir(){
        File tempDir = new File(getRuntimeDir(), "Temp");
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }
        return tempDir;
    }

    public static File getOutputDir(){
        String dirPath = Config.get(Config.kAppOutputDir, null);
        File dir;
        if(!StringUtils.isEmpty(dirPath)){
            dir = new File(dirPath);
            dir.mkdirs();
            if(dir.isDirectory()){
                return dir;
            }
            FileHelper.delete(dir);
        }
        dir = new File(getRuntimeDir(),"ApkToolPlus_Files");
        dir.mkdirs();
        return dir;
    }

    public static File getLogDir(){
        File logDir = new File(getOutputDir(), "Log");
        if(!logDir.exists()){
            logDir.mkdirs();
        }
        return logDir;
    }

    public static File copyToTemp(File file, String outName){
        if(file == null || !file.exists() || StringUtils.isEmpty(outName)){
            return null;
        }
        File outFile = new File(getTempDir(), outName);
        if(file.isFile()){
            FileHelper.copyFile(file, outFile);
        }else{
            FileHelper.copyDir(file,outFile);
        }
        return outFile;
    }

    public static boolean showInSublime(File file){
        return showInSublime(file,false);
    }

    public static boolean showInSublime(File file, boolean isAdd){
        if(!FileHelper.exists(file)){
            Debug.e("file must exist");
            return false;
        }
        String sublimePath = Config.get(Config.kSublimePath);
        if(sublimePath != null){
            File sublime = new File(sublimePath);
            if(sublime.exists()){
                String sublimeCmdParams = Config.get(Config.kSublimeCmdParams);
                String cmd;
                if(!StringUtils.isEmpty(sublimeCmdParams)){
                    String finalCmdParams = sublimeCmdParams.replaceAll("(%target%)+", Matcher.quoteReplacement(file.getAbsolutePath()));
                    cmd = sublimePath + " "  + finalCmdParams;
                }else{
                    cmd = sublimePath + " "  + file.getAbsolutePath();
                }
                if(isAdd){
                    cmd = cmd + " --add";
                }
                Cmd.exec(cmd,false);
                return true;
            }
        }else{
            Debug.e("sublime path can'n be null");
        }
        return false;
    }

    public static void showInSublime(List<File> fileList){
        if(fileList == null || fileList.isEmpty()){
            return;
        }
        for(File file : fileList){
            showInSublime(file);
        }
    }
}
