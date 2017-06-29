package com.wj.dexknife.shell;

import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.googlecode.dex2jar.tools.Jar2Dex;
import com.googlecode.dex2jar.tools.StdApkCmd;
import com.wj.dexknife.shell.jiagu.KeystoreConfig;
import com.wj.dexknife.shell.utils.Cmd;
import com.wj.dexknife.shell.utils.Debug;
import com.wj.dexknife.shell.utils.FileHelper;
import com.wj.dexknife.shell.utils.ZipHelper;

import org.jf.baksmali.baksmali;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import brut.androlib.AndrolibException;
import brut.androlib.res.util.ExtFile;
import brut.androlib.src.SmaliBuilder;
import brut.apktool.Main;
import brut.common.BrutException;


/**
 * Created by linchaolong on 2015/8/30.
 */
public class ApkToolPlus {

    public static final String TAG = ApkToolPlus.class.getSimpleName();

    public static ClassLoader initClassPath(String[] classpaths){
        if (classpaths == null || classpaths.length == 0)
            return null;
        // Add the conf dir to the classpath
        // Chain the current thread classloader
        try {
            List<URL> urls = new ArrayList<>(classpaths.length);
            for(String path : classpaths) {
                urls.add(new File(path).toURI().toURL());
            }
            ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader urlClassLoader = new URLClassLoader((URL[]) urls.toArray(), currentThreadClassLoader);
            // Replace the thread classloader - assumes
            // you have permissions to do so
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            return urlClassLoader;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean decompile(File apk, File outDir, Callback<Exception> onExceptioin){
        try {
            if(!outDir.exists()){
                outDir.mkdirs();
            }
            if(outDir == null){
                runApkTool(new String[]{"d", apk.getPath()});
            }else{
                runApkTool(new String[]{"d", apk.getPath(), "-o", outDir.getPath(), "-f"});
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(onExceptioin != null){
                onExceptioin.callback(e);
            }
            return false;
        }
        return true;
    }

    public static boolean recompile(File folder, File outApk, Callback<Exception> onExceptioin){
        try {
            if(outApk == null){
                runApkTool(new String[]{"b", folder.getPath()});
            }else{
                runApkTool(new String[]{"b", folder.getPath(), "-o", outApk.getPath()});
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(onExceptioin != null){
                onExceptioin.callback(e);
            }
            return false;
        }
        return true;
    }

    public static boolean jar2dex(File jarFile, String outputDexPath){
        return class2dex(jarFile,outputDexPath);
    }
    public static boolean class2dex(File classesDir, String outputDexPath){

        if (!classesDir.exists()){
            Debug.w("class2dex error : classPath is not exists.");
            return false;
        }

        if (!FileHelper.makePath(outputDexPath)){
            Debug.w( "makePath error : outputDexPath '" + outputDexPath + "' make fail");
            return false;
        }

        // class -> dex
        com.android.dx.command.dexer.Main.Arguments arguments = new com.android.dx.command.dexer.Main.Arguments();
        arguments.outName = outputDexPath;
        arguments.strictNameCheck = false;
        arguments.fileNames = new String[]{classesDir.getPath()};
        try {
            new com.android.dx.command.dexer.Main(null).run(arguments);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean dex2smali(File dexFile, File outDir){
        DexBackedDexFile dexBackedDexFile = null;

        if (dexFile == null || !dexFile.exists()){
            Debug.w( "dex2smali dexFile is null or not exists : " + dexFile.getPath());
            return false;
        }

        try {
            //brut/androlib/ApkDecoder.mApi default value is 15
            dexBackedDexFile = DexFileFactory.loadDexFile(dexFile, 15, false);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        baksmaliOptions options = new baksmaliOptions();
        options.outputDirectory = outDir.getPath();
        // default value -1 will lead to an exception
        // this setup is copied from Baksmali project
        options.jobs = Runtime.getRuntime().availableProcessors();
        if (options.jobs > 6) {
            options.jobs = 6;
        }
        return baksmali.disassembleDexFile(dexBackedDexFile, options);
    }

    public static boolean jar2smali(File jarFile, File outDir){

        if (!jarFile.exists() || jarFile.isDirectory()) {
            Debug.w( "jar2smali error : jar file '" + jarFile.getPath() + "' is not exists or is a directory.");
            return false;
        }
        return class2smali(jarFile, outDir);
    }

    public static boolean class2smali(File classesDir, File outDir){
        if (!classesDir.exists()){
            Debug.w("class2smali error : classpath '" + classesDir.getPath() + "' is not exists.");
            return false;
        }

        // clean temp
        File dexFile = new File(classesDir.getParentFile(), "temp.dex");
        dexFile.delete();

        // class -> dex
        if (class2dex(classesDir, dexFile.getPath())){
            // dex -> smali
            if (dex2smali(dexFile,outDir)){
                Debug.d("class2smali succcess");
            }else{
                Debug.e("class2smali error : dex2smali error");
            }

            // clean temp
            dexFile.delete();
            return true;
        }else {
            Debug.e( "class2smali error : class2dex error");
            return false;
        }
    }

    public static boolean smali2dex(String smaliDirPath, String dexOutputPath){
        ExtFile smaliDir = new ExtFile(new File(smaliDirPath));
        if (!smaliDir.exists()){
            Debug.w("smali2dex error : smali dir '" + smaliDirPath + "' is not exists");
            return false;
        }

        if (!FileHelper.makePath(dexOutputPath)){
            Debug.w("makePath error : dexOutputPath '" + dexOutputPath + "' make fail");
            return false;
        }

        File dexFile = new File(dexOutputPath);
        dexFile.delete();

        try {
            // smali -> dex
            SmaliBuilder.build(smaliDir, dexFile);
            return true;
        } catch (AndrolibException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean dex2jar(File file, File jarFile){
        //d2j-dex2jar classes.dex --output output.jar
        if(file == null || !file.exists() || jarFile == null){
            return false;
        }
        Dex2jarCmd.main(file.getPath(),"--output",jarFile.getPath(),"--force"); //--force���Ǵ����ļ�
        return jarFile.exists();
    }

    /**
     * jar2dex
     *
     * @param jarFile
     * @param dexFile
     */
    public static boolean jar2dex(File jarFile, File dexFile){
        if(jarFile == null || !jarFile.exists() || dexFile == null){
            return false;
        }
        Jar2Dex.main(jarFile.getPath(),"--output",dexFile.getPath());
        return dexFile.exists();
    }


    public static boolean apk2zip(File apkFile, File zipFile){
        //d2j-std-apk hqg.apk -o hqg.zip
        if(apkFile == null || !apkFile.exists() || zipFile == null){
            return false;
        }
        StdApkCmd.main(apkFile.getPath(),"-o",zipFile.getPath());
        return zipFile.exists();
    }

    public static File signApk(File apk, KeystoreConfig config){

        if (!apk.exists() || !apk.isFile()){
            throw new RuntimeException("sign apk error : file '" + apk.getPath() + "' is no exits or not a file.");
        }

        File apkCopy = new File(apk.getParentFile(), "copy_"+apk.getName());
        FileHelper.delete(apkCopy);

        FileHelper.copyFile(apk,apkCopy);
        ZipHelper.removeFileFromZip(apkCopy,"META-INF");

        File signedApk = new File(apk.getParentFile(), FileHelper.getNoSuffixName(apk) + ".apk");
        FileHelper.delete(signedApk);

//        String jdkpath=Cmd.execAndGetOutput("cmd set JAVA_HOME");
//        System.out.println(jdkpath+"------------------");
//        Cmd.exec("cd "+jdkpath);
        //jarsigner -digestalg SHA1 -sigalg MD5withRSA -keystore keystore·�� -storepass ���� -keypass �������� -signedjar signed_xxx.apk xxx.apk ����
        StringBuilder cmdBuilder = new StringBuilder("jarsigner -digestalg SHA1 -sigalg MD5withRSA");
        cmdBuilder.append(" -keystore ").append(config.keystorePath);
        cmdBuilder.append(" -storepass ").append(config.keystorePassword);
        cmdBuilder.append(" -keypass ").append(config.aliasPassword);
        cmdBuilder.append(" -signedjar ").append(signedApk.getPath()).append(" ").append(apkCopy.getPath()).append(" ");
        cmdBuilder.append(" ").append(config.alias);
        String cmd = cmdBuilder.toString();

        Cmd.exec(cmd);

        // clean
        FileHelper.delete(apkCopy);

        return signedApk;
    }

    private static void safeRunApkTool(String[] args){
        try {
            Main.main(args);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrutException e) {
            e.printStackTrace();
        }
    }

    public static void installFramework(File apkToolFile, File frameworkFile){
        Cmd.exec("java -jar " + apkToolFile.getAbsolutePath() + " if " + frameworkFile.getAbsolutePath());
    }

    private static void runApkTool(String[] args) throws InterruptedException, BrutException, IOException {
       AppManager.initApkTool();
        //java -jar apktool.jar d test.apk -f
        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append("java -jar ")
                .append(AppManager.getApkTool().getPath());
        for(String arg : args){
            cmdBuilder.append(" ").append(arg);
        }
        Cmd.exec(cmdBuilder.toString());
    }
}


