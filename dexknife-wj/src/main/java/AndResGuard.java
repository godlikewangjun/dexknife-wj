import com.wj.dexknife.shell.jiagu.KeystoreConfig;
import com.wj.dexknife.shell.res.ApkDecoder;
import com.wj.dexknife.shell.res.Configuration;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import brut.androlib.AndrolibException;
import brut.directory.DirectoryException;

/**
 * @author Admin
 * @version 1.0
 * @date 2017/6/10
 */

public class AndResGuard {
    public static void main(String[] args) {
        KeystoreConfig keystoreConfig=new KeystoreConfig();
        keystoreConfig.keystorePassword="123456";
        keystoreConfig.aliasPassword="123456";
        keystoreConfig.alias="test";
        keystoreConfig.keystorePath="D:\\wangjun\\github\\dexknife-wj2\\test.jks";
        //资源混淆
        try {
            Configuration configuration=new Configuration(new File("D:\\wangjun\\github\\dexknife-wj2\\app\\andreshuard.xml"),new File("D:\\wangjun\\github\\dexknife-wj2\\app\\resource_mapping.txt"),new File(keystoreConfig.keystorePath),keystoreConfig.keystorePassword,keystoreConfig.aliasPassword,keystoreConfig.aliasPassword);
            ApkDecoder apkDecoder=new ApkDecoder(configuration);
            apkDecoder.setApkFile(new File("D:\\wangjun\\github\\dexknife-wj2\\app\\build\\outputs\\apk\\app-debug.apk"));
            apkDecoder.setOutDir(new File("C:\\Users\\MoreStrongW\\Desktop\\apk"));
            apkDecoder.decode();
        } catch (AndrolibException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DirectoryException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
