package com.wj.dexknife.shell.jiagu;

import com.wj.dexknife.shell.ApkToolPlus;
import com.wj.dexknife.shell.AppManager;
import com.wj.dexknife.shell.Callback;
import com.wj.dexknife.shell.apkparser.ApkParser;
import com.wj.dexknife.shell.apkparser.bean.CertificateMeta;
import com.wj.dexknife.shell.utils.ClassHelper;
import com.wj.dexknife.shell.utils.DataProtector;
import com.wj.dexknife.shell.utils.Debug;
import com.wj.dexknife.shell.utils.FileHelper;
import com.wj.dexknife.shell.utils.ZipHelper;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class JiaGu {

    public static final String TAG = JiaGu.class.getSimpleName();

    public static final String JIAGU_ZIP = "jiagu.zip";
    private static final String JIAGU_DATA_BIN = "jiagu_data.bin";
    public static  String JIAGU_ZIP_PATH= JiaGu.class.getPackage().getName().replaceAll("\\.","/") + "/" + JIAGU_ZIP;

    private static String PROXY_APPLICATION_NAME = "com.qianfandu.ProxyApplication";
    private static final String METADATA_SRC_APPLICATION = "apktoolplus_jiagu_app";
    public static boolean ISSHELL=false;
    public static String SHELLAPKNAME;

    private static File workDir = new File(AppManager.getTempDir(), "jiagu");
    private static File jiaguZip = new File(workDir, JIAGU_ZIP);

    public enum Event {
        DECOMPILEING,
        ENCRYPTING,
        RECOMPILING,
        SIGNING,
        DECOMPILE_FAIL,
        RECOMPILE_FAIL,
        ENCRYPT_FAIL,
        MENIFEST_FAIL,
    }

    public static boolean isEncrypted(File apk) {
        return ZipHelper.hasFile(apk, "assets/" + JIAGU_DATA_BIN);
    }

    public static File encrypt(File apk,KeystoreConfig config,Callback<Event> callback) {

        if (!FileHelper.exists(apk) || isEncrypted(apk)) {
            return null;
        }
        workDir.mkdir();

        // 1.decompile apk
        handleCallback(callback, Event.DECOMPILEING);
        File decompile = new File(workDir, "decompile");
        FileHelper.cleanDirectory(decompile);
        boolean decompileResult = ApkToolPlus.decompile(apk, decompile, new Callback<Exception>() {
            @Override
            public void callback(Exception e) {
                if (callback != null) {
                    callback.callback(Event.DECOMPILE_FAIL);
                }
            }
        });
        if (!decompileResult) {
            return null;
        }
        handleCallback(callback, Event.ENCRYPTING);

        if (!encryptDex(apk, decompile)) {
            handleCallback(callback, Event.ENCRYPT_FAIL);
            return null;
        }

        if (!jiagu(decompile)) {
            handleCallback(callback, Event.ENCRYPT_FAIL);
            return null;
        }

        signatureProtect(apk, decompile);

        if (!updateMenifest(new File(decompile, "AndroidManifest.xml"))) {
            handleCallback(callback, Event.MENIFEST_FAIL);
            return null;
        }

        // 3.recompile apk
        handleCallback(callback, Event.RECOMPILING);
        String shellNanme=FileHelper.getNoSuffixName(apk) +"_encrypted.apk";
        if(ISSHELL==true && SHELLAPKNAME==null){
            shellNanme=apk.getName();
        }
        File encryptedApk = new File(apk.getParentFile(),shellNanme);
        boolean recompileResult =ApkToolPlus.recompile(decompile, encryptedApk, new Callback<Exception>() {
            @Override
            public void callback(Exception e) {
                handleCallback(callback, Event.RECOMPILE_FAIL);
            }
        });
        if (!recompileResult) {
            return null;
        }

        // 4.sign apk
        if (config != null) {
            handleCallback(callback, Event.SIGNING);
            File signedApk = ApkToolPlus.signApk(encryptedApk, config);
            //FileHelper.cleanDirectory(decompile);
            System.out.println("加固apk完成地址:"+encryptedApk.getAbsolutePath());
            return signedApk;
        }

        //FileHelper.cleanDirectory(decompile);
        return encryptedApk;
    }

    private static void signatureProtect(File apk, File decompile) {
        try(ApkParser parser = new ApkParser(apk)){
            List<CertificateMeta> certList = parser.getCertificateMetaList();
            String certMD5 = certList.get(0).getCertMd5();
            byte[] encryptData = DataProtector.encryptXXTEA(certMD5.getBytes());
            FileUtils.writeByteArrayToFile(new File(decompile,"assets/sign.bin"), encryptData);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void handleCallback(Callback<Event> callback, Event event) {
        if (callback != null) {
            callback.callback(event);
        }
    }

    private static boolean updateMenifest(File menifest) {
        XMLWriter writer = null;
        try {
            SAXReader reader = new SAXReader();
            org.dom4j.Document document = reader.read(menifest);
            Element rootElement = document.getRootElement();

            Element applicationElement = rootElement.element("application");
            Attribute appNameAttribute = applicationElement.attribute("name");
            if (appNameAttribute != null) {
                String appName = appNameAttribute.getValue();
                appNameAttribute.setValue(PROXY_APPLICATION_NAME);
                applicationElement.addElement("meta-data")
                        .addAttribute("android:name", METADATA_SRC_APPLICATION)
                        .addAttribute("android:value", appName);
            } else {
                applicationElement.addAttribute("android:name", PROXY_APPLICATION_NAME);
            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            // OutputFormat format = OutputFormat.createCompactFormat();
            format.setEncoding("UTF-8");
            writer = new XMLWriter(new FileOutputStream(menifest),format);
            writer.write(document);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean jiagu(File decompileDir) {
        if (!jiaguZip.exists()) {
            if(!JIAGU_ZIP_PATH.contains(":")){
                System.out.println(JIAGU_ZIP_PATH+"============"+jiaguZip.getAbsolutePath());
                if (!ClassHelper.releaseResourceToFile(JIAGU_ZIP_PATH, jiaguZip)) {
                    return false;
                }
            }else{
                if(!FileHelper.copy(new File(JIAGU_ZIP_PATH),jiaguZip)){
                    return false;
                }
            }
        }
        if(FileHelper.exists(jiaguZip)){
            jiaguZip.deleteOnExit();
        }

        File smali = new File(decompileDir, "smali");
        FileHelper.delete(smali);
        File lib = new File(decompileDir, "lib");
        String[] platforms = lib.list();
        boolean isHasLib = lib.exists() && platforms != null && platforms.length > 0;

        ZipHelper.list(jiaguZip, new ZipHelper.FileFilter() {
            @Override
            public void handle(ZipFile zipFile, FileHeader fileHeader) {
                if (fileHeader.getFileName().startsWith("smali")) {
                    if (!ZipHelper.unzip(zipFile, fileHeader, smali.getParentFile())) {
                        Debug.e(fileHeader.getFileName() + " unzip failure from " + zipFile.getFile().getAbsolutePath());
                    }
                } else if (fileHeader.getFileName().startsWith("libs")) {
                    if (!ZipHelper.unzip(zipFile, fileHeader, decompileDir)) {
                        Debug.e(fileHeader.getFileName() + " unzip failure from " + zipFile.getFile().getAbsolutePath());
                    }
                }
            }
        });

        File libs = new File(decompileDir, "libs");
        if (isHasLib) {
            for (String platform : platforms) {
                File libFile = new File(libs, platform + "/libapktoolplus_jiagu.so");
                File libSOFile =new File(lib, platform + "/" + libFile.getName());
                if (libFile.exists() &&  !libSOFile.exists()) {
                    FileHelper.move(libFile, new File(lib, platform + "/" + libFile.getName()));
                }
            }
        } else {
            FileHelper.move(libs, lib);
        }
        FileHelper.delete(libs);

        return true;
    }
    private static void deleteFile(File file) {
        if (file.exists()) {//判断文件是否存在
            if (file.isFile()) {//判断是否是文件
                file.delete();//删除文件
            } else if (file.isDirectory()) {//否则如果它是一个目录
                File[] files = file.listFiles();//声明目录下所有的文件 files[];
                for (int i = 0;i < files.length;i ++) {//遍历目录下所有的文件
                    JiaGu.deleteFile(files[i]);//把每个文件用这个方法进行迭代
                }
                file.delete();//删除文件夹
            }
        } else {
            System.out.println("所删除的文件不存在");
        }
    }
    private static boolean encryptDex(File apk, File decompileDir) {
        if(decompileDir.isDirectory()){
            String[] filenames=decompileDir.list();
            int index=0;
            for (int i=0;i<filenames.length;i++){
                if(filenames[i].contains("smali")){
                    index+=1;
                    String dexName="classes2.dex";//加固之后的dex
                    String unzipDexName="classes.dex";//解压之后的dex
                    if(index!=1){
                        dexName="classes"+index+".dex";
                        unzipDexName="classes"+index+".dex";
                        deleteFile(new File(decompileDir, filenames[i]));//删除问价夹
                    }
                    File dexFile = new File(decompileDir, unzipDexName);
                    if (dexFile.exists()) {
                        dexFile.delete();
                    }
                    try {
                        ZipFile zipFile = new ZipFile(apk);
                        ZipHelper.unzip(zipFile,unzipDexName, dexFile.getParentFile());
                    } catch (ZipException e) {
                        e.printStackTrace();
                        return false;
                    }

                    File assets = new File(decompileDir, "assets");
                    assets.mkdirs();
                    assets = new File(decompileDir, "assets");

                    File encryptFile = new File(assets, JIAGU_DATA_BIN);
                    if(encryptFile.exists()){
                        encryptFile.delete();
                    }
                    encryptFile.mkdirs();

                    DataProtector.encrypt(dexFile,  new File(assets, JIAGU_DATA_BIN+"/"+"classes"+(index+1)+".dex"));
                    dexFile.delete();
                }
            }
        }

        return true;
    }
    public static File encryptApk(File apk, KeystoreConfig keystoreConfig){

        return JiaGu.encrypt(apk, keystoreConfig, new Callback<Event>() {
            @Override
            public void callback(Event event) {
                switch (event){
                    case DECOMPILEING:
                        break;
                    case ENCRYPTING:
                        break;
                    case RECOMPILING:
                        break;
                    case SIGNING:
                        break;
                    case DECOMPILE_FAIL:
                        break;
                    case RECOMPILE_FAIL:
                        break;
                    case ENCRYPT_FAIL:
                        break;
                    case MENIFEST_FAIL:
                        break;
                }
            }
        });
    }
}
