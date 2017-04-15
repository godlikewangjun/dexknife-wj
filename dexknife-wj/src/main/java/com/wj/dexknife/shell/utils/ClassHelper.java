package com.wj.dexknife.shell.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by linchaolong on 2015/9/9.
 */
public class ClassHelper {

    public static final String TAG = ClassHelper.class.getSimpleName();

    public static String getPackageName(Class clazz){
        return clazz.getPackage().getName();
    }
    public static File getCodeSourcePath(){
        return new File(ClassHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

    public static URL getResourceAsURL(String resPath){
        return ClassLoader.getSystemResource(resPath);
    }

    public static File getResourceAsFile(String resPath){
        URL url = ClassLoader.getSystemResource(resPath);
        if(url == null){
            Debug.d("getResourceAsFile " + resPath + " not found.");
            return null;
        }
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getResourceAsStream(String resPath){
        return ClassLoader.getSystemResourceAsStream(resPath);
    }

    public static boolean releaseResourceToFile(Class<?> clazz, String resName, File outFile){
        return releaseResourceToFile(clazz.getPackage().getName().replaceAll("\\.","/")+"/"+resName, outFile);
    }

    /**
     * 释放类路径下资源到指定路径
     *
     * @param resPath
     * @param outFile
     * @return
     */
    public static boolean releaseResourceToFile(String resPath, File outFile){
        InputStream in = getResourceAsStream(resPath);
        if(in == null){
            return false;
        }
        try {
            FileUtils.copyInputStreamToFile(in,outFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return outFile.exists();
    }
}
