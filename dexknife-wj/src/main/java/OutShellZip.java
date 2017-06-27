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
        UpdateJiaGuTask task = new UpdateJiaGuTask();

        task.setProjectDir(new File("D:\\wangjun\\github\\dexknife-wj2\\myapplication\\build\\outputs\\apk\\myapplication-debug.apk"));

        String packagePath = JiaGu.class.getPackage().getName().replaceAll("\\.", "/");
        task.addOutFile(new File("D:\\wangjun\\github\\dexknife-wj2\\" + UpdateJiaGuTask.JIAGU_ZIP));

        task.execute();
    }

}
