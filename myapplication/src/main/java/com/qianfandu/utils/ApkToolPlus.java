package com.qianfandu.utils;


import android.content.Context;
import android.util.Base64;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import static net.lingala.zip4j.util.InternalZipConstants.BUFF_SIZE;

/**
 * 数据加密解密工具
 *
 * @author linchaolong
 */
public class ApkToolPlus {

    public static void loadLibrary() {
        // 加载动态库，数据的加密解密算法实现在动态库中
        System.loadLibrary("wjshell_jiagu");
    }

    /**
     * 加密数据
     *
     * @param buff 数据
     * @return 加密后的数据
     */
    public native static byte[] encrypt(byte[] buff);

    /**
     * 解密数据
     *
     * @param buff 数据
     * @return 解密后的数据
     */
    public native static byte[] decrypt(byte[] buff);

    /**
     * 获取key
     *
     * @return
     */
    public native static String key();

    /**
     * 解压解密
     *
     * @param file
     */
    public static void decryptDex(Context context, String filename, InputStream inputStream, File outFile) {
        try {
            File file = new File(outFile.getParent());
            if (!file.exists()) {
                file.mkdir();
            }
            file = new File(file, "wjcanch");
            if (!file.exists()) {
                file.mkdir();
            }
            File file1=new File(file,outFile.getName());
            try {
                writeTemp(inputStream, new FileOutputStream(file1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ZipFile zipFile = new ZipFile(file1);
            zipFile.setPassword(key());
            zipFile.extractFile(filename, outFile.getParent());
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入临时文件
     *
     * @throws IOException
     */
    private static void writeTemp(InputStream inputStream, OutputStream outputStream) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buff = new byte[BUFF_SIZE];
        int len;
        while ((len = inputStream.read(buff)) != -1) {
            byteOutput.write(buff, 0, len);
        }
        byte[] dexBytes = byteOutput.toByteArray();
        outputStream.write(dexBytes, 0, dexBytes.length);
        outputStream.flush();

        inputStream.close();
        outputStream.close();
    }
    /**
     * 写入临时文件
     *
     * @throws IOException
     */
    private static String getString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buff = new byte[BUFF_SIZE];
        int len;
        while ((len = inputStream.read(buff)) != -1) {
            byteOutput.write(buff, 0, len);
        }
        byte[] dexBytes = byteOutput.toByteArray();

        inputStream.close();
        return new String(decrypt(dexBytes));
    }
}
