package ml;

import com.wj.dexknife.shell.AppManager;
import com.wj.dexknife.shell.Callback;
import com.wj.dexknife.shell.jiagu.JiaGu;
import com.wj.dexknife.shell.jiagu.KeystoreConfig;

import java.io.File;

/**
 * 加固
 */
public class UpdateJiaGu {

    public static void main(String[] args) {
        KeystoreConfig keystoreConfig=new KeystoreConfig();
        keystoreConfig.keystorePath=args[0];
        keystoreConfig.keystorePassword=args[1];
        keystoreConfig.aliasPassword=args[2];
        keystoreConfig.alias=args[3];
        JiaGu.ISSHELL=true;
        JiaGu.SHELLAPKNAME=args[4];
        AppManager.APKTOOLJARPATH=args[5];
        JiaGu.JIAGU_ZIP_PATH=args[6];
        encryptApk(new File(args[7]),keystoreConfig);//地址

        if(args.length>8&&args[8]!=null){//判断是否资源混淆
            JiaGu.ANDRESGUARD=Boolean.parseBoolean(args[8]);
            JiaGu.andres_pz=args[9];//配置文件
            JiaGu.andres_map=args[10];//map文件
        }
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
