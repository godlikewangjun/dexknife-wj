package com.wj.dexknife.shell;


import com.wj.dexknife.shell.utils.ClassHelper;
import com.wj.dexknife.shell.utils.Debug;
import com.wj.dexknife.shell.utils.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Manifest;

/**
 * Created by linchaolong on 2015/9/5.
 */
public class Config {

    public static final String TAG = Config.class.getSimpleName();

    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 500;

    public static final String APP_NAME = "ApkToolPlus";

    private static String version;

    private static Properties config;

    private static File configFile = new File(AppManager.getRuntimeDir(), "config.properties");;

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public static final String kAppOutputDir = "appOutputDir";
    public static final String kLogLevel  = "logLevel";
    public static final String kIsLogOutputFile = "isLogOutputFile";
    public static final String kLastPageIndex = "lastPageIndex";
    public static final String kLastOpenDecompileDir = "lastOpenDecompileDir";
    public static final String kLastOpenRecompileDir = "lastOpenRecompileDir";
    public static final String kLastOpenApkSignDir = "lastOpenApkSignDir";
    public static final String kLastOpenApkInfoDir = "lastOpenApkInfoDir";
    public static final String kLastOpenJar2SmaliDir = "lastOpenJar2SmaliDir";
    public static final String kLastOpenClass2SmaliDir = "lastOpenClass2SmaliDir";
    public static final String kLastOpenDex2SmaliDir = "lastOpenDex2SmaliDir";
    public static final String kLastOpenSmali2DexDir = "lastOpenSmali2DexDir";
    public static final String kLastOpenClass2DexDir = "lastOpenClass2DexDir";
    public static final String kLastOpenDex2JarDir = "lastOpenDex2JarDir";
    public static final String kLastOpenJadDir = "lastOpenJadJarDir";
    public static final String kLastOpenJadJarDir = "lastOpenJadJarDir";

    public static final String kLastOpenIconDir = "lastOpenIconDir";
    public static final String kLastOpenJiaoBiaoDir = "lastOpenJiaoBiaoDir";
    public static final String kLastSaveIconDir = "lastSaveIconDir";
    public static final String kIconShowBorder = "iconShowBorder";

    public static final String kLastJiaGuAddApkDir = "lastJiaGuAddApkDir";
    public static final String kLastOpenApkParserDir = "lastOpenApkParserDir";
    public static final String kSublimePath = "sublimePath";
    public static final String kSublimeCmdParams = "sublimeCmdParams";

    public static final String kKeystoreFilePath = "keystoreFilePath";
    public static final String kKeystoreAlias = "alias";
    public static final String kAliasPassword = "aliasPassword";
    public static final String kKeystorePassword = "keystorePassword";

    public static final String kLastEasySDKOpenDir = "lastEasySDKOpenDir";

    public static final URL DEFAULT_LOADING_IMAGE = ClassHelper.getResourceAsURL("res/gif/loading.gif");

    public static final String JIANSHU_URL = "http://www.jianshu.com/u/149dc6683cc7";
    public static final String BLOG_URL = "http://blog.csdn.net/linchaolong";
    public static final String GITHUB_URL = "https://github.com/linchaolong/ApkToolPlus";

    static{
        try {
            config = new Properties();
            if(configFile.exists()){
                FileInputStream in = new FileInputStream(configFile);
                config.load(in);
                in.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

        }
    }

    public static void init(){
        try {
            InputStream in = ClassHelper.getResourceAsStream("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(in);
            version = manifest.getMainAttributes().getValue("Manifest-Version");
            Debug.d("Manifest-Version="+version);
            IO.close(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Debug.setLogLevel(Config.getInt(kLogLevel, Debug.DEBUG));
        if (getDir(kAppOutputDir) == null){
            set(Config.kAppOutputDir, AppManager.getOutputDir().getPath());
        }
    }

    public static String getVersion(){
        return version;
    }

    public static void save() {
        save(null);
    }

    public static void save(final Runnable callback){
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(configFile);
                config.store(out, APP_NAME + " Config");
                if (callback != null) {
                    callback.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IO.close(out);
            }
    }

    public static File getDir(String key){
        String dirPath = Config.get(key, null);
        File dir = null;
        if(dirPath != null){
            dir = new File(dirPath);
            if(dir.exists()){
                if(dir.isFile() ){
                    File parentFile = dir.getParentFile();
                    if(parentFile != null && parentFile.exists()){
                        dir = parentFile;
                    }
                }
            }else{
                dir = null;
            }
        }
        return dir;
    }

    public static boolean getBoolean(String key, boolean defaultVal){
        String result = get(key, null);
        if(result != null){
            return TRUE.equalsIgnoreCase(result);
        }
        return defaultVal;
    }

    public static int getInt(String key, int defaultVal) {
        String result = get(key, null);
        if(result != null){
            return Integer.parseInt(result);
        }
        return defaultVal;
    }

    public static String get(String key){
        return get(key,null);
    }
    public static String get(String key, String defaultVal){
        return config.getProperty(key,defaultVal);
    }

    public static void set(String key, String val){
        config.setProperty(key,val);
    }

    public static void set(String key, Boolean val){
        if(val){
            Config.set(key, Config.TRUE);
        }else{
            Config.set(key,Config.FALSE);
        }
    }

    public static void set(String key, Number value) {
        set(key,String.valueOf(value));
    }
}
