package task;


import com.wj.dexknife.shell.ApkToolPlus;
import com.wj.dexknife.shell.Callback;
import com.wj.dexknife.shell.jiagu.JiaGu;
import com.wj.dexknife.shell.utils.Debug;
import com.wj.dexknife.shell.utils.FileHelper;
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

        File bin=new File(projectDir.getParent(),"wjcanch");
        if(!bin.exists()){
            bin.mkdirs();
        }else {
            FileHelper.cleanDirectory(bin);
        }
        //反编译
        File decompile = new File(JiaGu.workDir, "decompile");
        FileHelper.cleanDirectory(decompile);
        boolean decompileResult = ApkToolPlus.decompile(projectDir, decompile, new Callback<Exception>() {
            @Override
            public void callback(Exception e) {
            }
        });
       //samil
        File smali=new File(bin,"smali");
        FileHelper.copyDirInclude(new File(decompile,"smali"),smali);
        // lib
        File projectLibs = new File(decompile, "lib");
        File libs = new File(bin, "libs");
        FileHelper.copyDirInclude(projectLibs,libs);
        // zip
        Debug.d("genearte "+ JIAGU_ZIP +"..");
        File jiaguZip = outFileList.get(0);
        jiaguZip.delete();
        ZipHelper.zip(new File[]{smali, libs}, jiaguZip);

//        for(int i = 1; i<outFileList.size(); ++i){
//            File outFile = outFileList.get(i);
//            if(FileHelper.exists(outFile.getParentFile())){
//                FileHelper.delete(outFile);
//                FileHelper.copyFile(jiaguZip, outFile);
//            }
//        }

        Debug.d(JIAGU_ZIP +" update finish");
    }

}
