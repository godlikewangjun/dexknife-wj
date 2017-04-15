package com.wj.dexknife

import com.wj.dexknife.shell.Callback
import com.wj.dexknife.shell.jiagu.JiaGu
import com.wj.dexknife.shell.jiagu.KeystoreConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * User: mcxiaoke
 * Date: 14/12/19
 * Time: 11:29
 */
class ShellOutputApkTask extends DefaultTask {
    ShellOutputApkTask() {
        setDescription('shell apk in output dir')
    }
    @Input
    KeystoreConfig taskKeystoreConfig
    @Input
    File taskInputFile

    @TaskAction
    void shellApk() {
        JiaGu.encrypt(taskInputFile, taskKeystoreConfig, new Callback<JiaGu.Event>() {
            @Override
            public void callback(JiaGu.Event event) {
                switch (event){
                    case JiaGu.Event.DECOMPILEING:
                        println "正在反编译"
                        break;
                    case JiaGu.Event.ENCRYPTING:
                        println "正在加固"
                        break;
                    case JiaGu.Event.RECOMPILING:
                        println "正在回编译";
                        break;
                    case JiaGu.Event.SIGNING:
                        println "正在签名"
                        break;
                    case JiaGu.Event.DECOMPILE_FAIL:
                        println "反编译失败"
                        break;
                    case JiaGu.Event.RECOMPILE_FAIL:
                        println "回编译失败"
                        break;
                    case JiaGu.Event.ENCRYPT_FAIL:
                        println "加固失败"
                        break;
                    case JiaGu.Event.MENIFEST_FAIL:
                        println "清单文件解析失败"
                        break;
                }
            }
        });
    }
}
