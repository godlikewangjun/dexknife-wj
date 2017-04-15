package com.wj.dexknife.shell.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class DataProtector {

	private static final int BUFF_SIZE = 1024*1024*5; // 10MB

	public static byte[] encryptXXTEA(byte[] data){
		return XXTEA.encrypt(data,"lcl_apktoolplus");
	}

    @Deprecated
	public static byte[] encrypt(byte[] data){
		String key = "linchaolong";
		int keyLen = key.length();
		int size = data.length;
		
		 int i = 0;
		 int offset = 0;
		 for(; i<size; ++i, ++offset){
			 if (offset >= keyLen){
				offset = 0;
			}
			 data[i] ^= key.charAt(offset);
		 }
		 
		 return data;
	}

	public static void encrypt(File file, File outFile){
		if (!FileHelper.exists(file)){
			Debug.e("file not exists!!! : " + file.getAbsolutePath());
			return;
		}

		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(file);
			out = new FileOutputStream(outFile);
			ByteArrayOutputStream byteOutput;

			try {
				byteOutput = new ByteArrayOutputStream();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return;
			}

			byte[] buff = new byte[BUFF_SIZE];
			int len;
			while ((len = in.read(buff)) != -1) {
				byteOutput.write(buff, 0, len);
			}

			byte[] encryptData = encryptXXTEA(byteOutput.toByteArray());
			out.write(encryptData);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IO.close(in);
			IO.close(out);
		}
	}
}
