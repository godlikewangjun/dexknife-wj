package task;


import com.wj.dexknife.shell.ApkToolPlus;
import com.wj.dexknife.shell.jiagu.JiaGu;
import com.wj.dexknife.shell.utils.Cmd;
import com.wj.dexknife.shell.utils.Debug;
import com.wj.dexknife.shell.utils.FileHelper;
import com.wj.dexknife.shell.utils.OS;
import com.wj.dexknife.shell.utils.ZipHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UpdateJiaGuTask extends Task {

    public static final String JIAGU_ZIP = JiaGu.JIAGU_ZIP;

    private File projectDir;

    private List<File> outFileList = new ArrayList<>(2);

    public void setProjectDir(File projectDir) {
        this.projectDir = projectDir;
    }
    public File getProjectDir() {
        return projectDir;
    }

    public List<File> getOutFileList(){
        return outFileList;
    }
    public void addOutFile(File outFile) {
        if(!outFileList.contains(outFile)){
            outFileList.add(outFile);
        }
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
        updateJiaGuZip();
    }

    public void updateJiaGuZip() {

        File jiaguProject = getProjectDir();

        Debug.d("update "+ JIAGU_ZIP +" start");
        // 1.build project
        File buildFile = new File(jiaguProject, "build.xml");
        if (!FileHelper.exists(buildFile)) {
            Debug.e("build.xml is not exists!!!");
            return;
        }
        Debug.d("build project..");
        if(OS.isWindows()){
            Cmd.exec("ant.bat compile -f " + buildFile.getAbsolutePath());
        }else{
            Cmd.exec("ant compile -f " + buildFile.getAbsolutePath());
        }

        File bin = new File(jiaguProject, "bin");
        if (!bin.exists() || bin.list().length < 1) {
            Debug.e("project is build failure!!!");
            return;
        }

        // 2.generate proguard jar file
        Debug.d("generate smali...");
        File jarProguardFile = new File(jiaguProject, "bin/jiagu_proguard.jar");
        if (!jarProguardFile.exists()) {
            Debug.e("jar file is not exists : " + jarProguardFile.getPath());
            return;
        }
        File smali = new File(bin, "smali");
        FileHelper.delete(smali);
        ApkToolPlus.jar2smali(jarProguardFile, smali);

        // lib
        Debug.d("copying libs..");
        File projectLibs = new File(jiaguProject, "libs");
        File libs = new File(bin, "libs");
        FileHelper.delete(libs);
        for (File file : projectLibs.listFiles()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (FileHelper.isSuffix(subFile, "so")) {
                        FileHelper.copy(file, libs);
                        break;
                    }
                }
            }
        }

        // zip
        Debug.d("genearte "+ JIAGU_ZIP +"..");
        File jiaguZip = outFileList.get(0);
        jiaguZip.delete();
        ZipHelper.zip(new File[]{smali, libs}, jiaguZip);

        for(int i = 1; i<outFileList.size(); ++i){
            File outFile = outFileList.get(i);
            if(FileHelper.exists(outFile.getParentFile())){
                FileHelper.delete(outFile);
                FileHelper.copyFile(jiaguZip, outFile);
            }
        }

        Debug.d(JIAGU_ZIP +" update finish");
    }

}
