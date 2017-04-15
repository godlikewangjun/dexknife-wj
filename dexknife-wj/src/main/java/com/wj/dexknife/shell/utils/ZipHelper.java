package com.wj.dexknife.shell.utils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by linchaolong on 2015/10/28.
 */
public class ZipHelper {

    public static final String TAG = ZipHelper.class.getSimpleName();

    public static InputStream getEntryInputStream(java.util.zip.ZipFile zip, String entryName){
        if(zip == null){
            return null;
        }
        try {
            java.util.zip.ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                throw new FileNotFoundException("Cannot find "+entryName+" in zip file:"+zip.getName());
            }
            return zip.getInputStream(entry);
        } catch (java.util.zip.ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean zip(File dir, File zipFile){
        try {
            if(!FileHelper.exists(dir)){
                return false;
            }

            ZipFile zip = new ZipFile(zipFile);
            // Initiate Zip Parameters which define various properties such
            // as compression method, etc.
            ZipParameters parameters = new ZipParameters();

            // set compression method to store compression
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

            // Set the compression level
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            // Sets the folder in the zip file to which these new files will be added.
            // In this example, test2 is the folder to which these files will be added.
            // Another example: if files were to be added to a directory test2/test3, then
            // below statement should be parameters.setRootFolderInZip("test2/test3/");
            //parameters.setRootFolderInZip("/test");

            // Add folder to the zip file
            zip.addFolder(dir, parameters);

            return zipFile.exists();
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean zip(List<File> files, File zipFile){
        try {
            ZipFile zip = new ZipFile(zipFile);
            // Initiate Zip Parameters
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

            // Set the compression level.
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            // Sets the folder in the zip file to which these new files will be added.
            // In this example, test2 is the folder to which these files will be added.
            // Another example: if files were to be added to a directory test2/test3, then
            // below statement should be parameters.setRootFolderInZip("test2/test3/");
            //parameters.setRootFolderInZip("/test");

            // Now add files to the zip file
            //zip.addFiles(new ArrayList(files), parameters);
            for(File file : files){
                if(file.exists()){
                    if(file.isDirectory()){
                        zip.addFolder(file,parameters);
                    }else{
                        zip.addFile(file,parameters);
                    }
                }else{
                    Debug.e("zip error file not exits : " + file.getAbsolutePath());
                }
            }
            zip.setRunInThread(false);
            return zipFile.exists();
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean zip(File[] files, File zipFile){
        ArrayList<File> fileList = new ArrayList<>(files.length);
        for(File file : files){
            fileList.add(file);
        }
        return zip(fileList,zipFile);
    }
    public static boolean unzip(File zipFile, File outDir){
        return unzip(zipFile, outDir, true);
    }

    public static boolean unzip(File zipFile, File outDir, boolean isClean){
        try {
            if(!FileHelper.exists(zipFile)){
                return false;
            }
            ZipFile zip = new ZipFile(zipFile);
            if(isClean && outDir.exists()){
                FileHelper.delete(outDir);
            }
            zip.setRunInThread(false);
            zip.extractAll(outDir.getPath());
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static abstract class FileFilter{
        public abstract void handle(ZipFile zipFile, FileHeader fileHeader);
    }

    public static boolean hasFile(File zipFile, String fileName){
        try {
            if(!FileHelper.exists(zipFile)){
                return false;
            }
            return hasFile(new ZipFile(zipFile),fileName);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasFile(ZipFile zip, String fileName){
        try {
            return zip.getFileHeader(fileName) != null;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void list(File zipFile, FileFilter filter){
        try {
            if(!FileHelper.exists(zipFile)){
                return;
            }
            ZipFile zip = new ZipFile(zipFile);
            zip.setRunInThread(false);

            // Get the list of file headers from the zip file
            List fileHeaderList = zip.getFileHeaders();

            // Loop through the file headers
            for (int i = 0; i < fileHeaderList.size(); i++) {
                // Extract the file to the specified destination
                filter.handle(zip, (FileHeader) fileHeaderList.get(i));
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static List<FileHeader> listForPattern(File zipFile, String regex){
        List<FileHeader> list = new ArrayList<>();
        Pattern p = Pattern.compile(regex);
//        Pattern p = Pattern.compile("[^/]*\\.dex");
        try {
            if(!FileHelper.exists(zipFile)){
                return new ArrayList<>(0);
            }
            ZipFile zip = new ZipFile( zipFile);
            zip.setRunInThread(false);

            // Get the list of file headers from the zip file
            List fileHeaderList = zip.getFileHeaders();

            // Loop through the file headers
            for (int i = 0; i < fileHeaderList.size(); i++) {
                // Extract the file to the specified destination
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                Matcher matcher = p.matcher(fileHeader.getFileName());
                if(matcher.matches()){
                    list.add(fileHeader);
                }
            }
            return list;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    public static boolean unzip(ZipFile zip, FileHeader fileHeader, File destPath){
        try {
            zip.extractFile(fileHeader, destPath.getAbsolutePath());
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean unzip(ZipFile zip, String fileName, File destPath){
        try {
            zip.extractFile(fileName, destPath.getAbsolutePath());
            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeFileFromZip(File zipFile, String fileName){
        if(!FileHelper.exists(zipFile)){
            return false;
        }
        try {
            ZipFile zip = new ZipFile(zipFile);

            LinkedList<FileHeader> removeList = new LinkedList<>();
            for(Object fileHeaderObj : zip.getFileHeaders()){
                FileHeader fileHeader = (FileHeader) fileHeaderObj;
                if(fileHeader.getFileName().startsWith(fileName)){
                    removeList.add(fileHeader);
                }
            }
            // Note: If this zip file is a split file then this method throws an exception as
            // Zip Format Specification does not allow updating split zip files

            // Please make sure that this zip file has more than one file to completely test
            // this example

            // Removal of a file from a zip file can be done in two ways:
            //zip.removeFile(target);

            for(FileHeader fileHeader : removeList){
                zip.removeFile(fileHeader);
            }

//            if (zip.getFileHeaders() != null && zip.getFileHeaders().size() > 0) {
//                zip.removeFile((FileHeader)zip.getFileHeaders().get(0));
//            } else {
//                Debug.d("This cannot be demonstrated as zip file does not have any files left");
//            }

            return true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void merge(File f1, File f2, File outFile){
        // init
        File temp = new File(outFile.getParentFile(), FileHelper.getNoSuffixName(outFile));
        FileHelper.cleanDirectory(temp);
        if(f1.isDirectory()){
            FileHelper.copyDir(f1,temp, false);
        }else{
            ZipHelper.unzip(f1,temp);
        }
        // merge
        if(f2.isDirectory()){
            FileHelper.copyDir(f2, temp, false);
        }else{
            // unzip
            ZipHelper.unzip(f2,temp, false);
        }
        // zip
        ZipHelper.zip(temp.listFiles(), outFile);
        // clean
        FileHelper.delete(temp);
    }

}
