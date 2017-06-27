import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Admin
 * @version 1.0
 * @date 2017/6/24
 */

public class Test {
    public static void main(String[] args) {
        File file=new File("C:\\Users\\MoreStrongW\\Desktop\\test.txt");
        File outFile=new File("C:\\Users\\MoreStrongW\\Desktop\\test2.zip");
        try {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            // Set password
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            parameters.setPassword("wjshell");


            ZipFile zipFile=new ZipFile(outFile.getAbsolutePath());
            zipFile.addFile(file,parameters);

            parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            // Set password
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipFile = new ZipFile("C:\\Users\\MoreStrongW\\Desktop\\test.zip");
            zipFile.addStream(new FileInputStream(file),parameters);
            zipFile.extractFile("test.txt","C:\\Users\\MoreStrongW\\Desktop\\test");
        }  catch (ZipException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}
