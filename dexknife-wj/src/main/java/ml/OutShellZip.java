package ml;

import com.wj.dexknife.shell.AppManager;
import com.wj.dexknife.shell.jiagu.JiaGu;

import java.io.File;

import task.UpdateJiaGuTask;

/**
 * 输出壳zip
 *
 * @author Admin
 * @version 1.0
 * @date 2017/6/24
 */

public class OutShellZip {
    public static void main(String[] args) {
        JiaGu.workDir=new File(args[0],"jiagu");
        UpdateJiaGuTask task = new UpdateJiaGuTask();

        task.setProjectDir(new File(args[1]));

        String packagePath = JiaGu.class.getPackage().getName().replaceAll("\\.", "/");
        File jiagu=new File(args[2] + File.separator+UpdateJiaGuTask.JIAGU_ZIP);
        if(jiagu.exists()){
            jiagu.delete();
        }
        task.addOutFile(jiagu);

        AppManager.APKTOOLJARPATH=args[3];//apktool

        task.execute();
    }

}
