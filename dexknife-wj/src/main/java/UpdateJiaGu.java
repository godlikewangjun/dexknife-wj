import com.wj.dexknife.shell.AppManager;
import com.wj.dexknife.shell.Callback;
import com.wj.dexknife.shell.jiagu.JiaGu;
import com.wj.dexknife.shell.jiagu.KeystoreConfig;

import java.io.File;

public class UpdateJiaGu {

    public static void main(String[] args) {
//        UpdateJiaGuTask task = new UpdateJiaGuTask();
//
//        task.setProjectDir(new File("C:/android_work/android_workspace/android_studio_xs/myapplication"));
//
//        String packagePath = JiaGu.class.getPackage().getName().replaceAll("\\.","/");
//        task.addOutFile(new File("dexknife-wj/src/" + packagePath + "/" + UpdateJiaGuTask.JIAGU_ZIP));
//
//        task.addOutFile(new File("../qianfandu/src/main"+packagePath+"/" + UpdateJiaGuTask.JIAGU_ZIP));
//
//        task.execute();
        KeystoreConfig keystoreConfig=new KeystoreConfig();
        keystoreConfig.keystorePassword="123456";
        keystoreConfig.aliasPassword="123456";
        keystoreConfig.alias="test";
        keystoreConfig.keystorePath="D:\\wangjun\\github\\dexknife-wj2\\test.jks";
        JiaGu.ISSHELL=true;
        JiaGu.SHELLAPKNAME="";
        AppManager.APKTOOLJARPATH="D://wangjun/github/dexknife-wj2/apktool.jar";
        JiaGu.JIAGU_ZIP_PATH="D://wangjun/github/dexknife-wj2/jiagu.zip";
        encryptApk(new File("D:\\wangjun\\github\\dexknife-wj2\\app\\build\\outputs\\apk\\app-debug.apk"),keystoreConfig);//地址
    }
    public static File encryptApk(File apk, KeystoreConfig keystoreConfig){

        return JiaGu.encrypt(apk, keystoreConfig, new Callback<JiaGu.Event>() {
            @Override
            public void callback(JiaGu.Event event) {
                switch (event){
                    case DECOMPILEING:
                        System.out.println("正在反编译");
                        break;
                    case ENCRYPTING:
                        System.out.println("正在加固");
                        break;
                    case RECOMPILING:
                        System.out.println("正在回编译");
                        break;
                    case SIGNING:
                        System.out.println("正在签名");
                        break;
                    case DECOMPILE_FAIL:
                        System.out.println("反编译失败");
                        break;
                    case RECOMPILE_FAIL:
                        System.out.println("回编译失败");
                        break;
                    case ENCRYPT_FAIL:
                        System.out.println("加固失败");
                        break;
                    case MENIFEST_FAIL:
                        System.out.println("清单文件解析失败");
                        break;
                }
            }
        });
    }

}
