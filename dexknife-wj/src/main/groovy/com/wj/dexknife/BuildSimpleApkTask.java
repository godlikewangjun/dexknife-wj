package com.wj.dexknife;
import com.wj.dexknife.shell.jiagu.JiaGu;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import task.UpdateJiaGuTask;


/**
 * @author wangjun
 * @version 1.0
 */

public class BuildSimpleApkTask extends DefaultTask {
    @Input
    File file;
    @Input
    File copy2file;
    @Input
    String buildDirName="myapplication";
    @TaskAction
    void buildSimple() {
        UpdateJiaGuTask task = new UpdateJiaGuTask();

        task.setProjectDir(new File(buildDirName));

        String packagePath = JiaGu.class.getPackage().getName().replaceAll("\\.","/");
        //copy
        if(copy2file==null){
            task.addOutFile(new File("dexknife-wj/src/" + packagePath + "/" + UpdateJiaGuTask.JIAGU_ZIP));
        }else{
            task.addOutFile(copy2file);
        }
        //rebuild
        if(file==null){
            task.addOutFile(new File("../qianfandu/src/main"+packagePath+"/" + UpdateJiaGuTask.JIAGU_ZIP));
        }else {
            task.addOutFile(file);
        }


        task.execute();
    }
}
