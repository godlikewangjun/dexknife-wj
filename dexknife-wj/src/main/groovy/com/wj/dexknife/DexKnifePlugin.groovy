/*
 * Copyright (C) 2016 ceabie (https://github.com/ceabie/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wj.dexknife

import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.SigningConfig
import com.wj.dexknife.shell.AppManager
import com.wj.dexknife.shell.Callback
import com.wj.dexknife.shell.jiagu.JiaGu
import com.wj.dexknife.shell.jiagu.KeystoreConfig
import com.wj.packerng.PackerNgPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

/**
 * the spilt tools plugin.
 */
public class DexKnifePlugin implements Plugin<Project> {
    static final String PLUGIN_NAME = "dexKnife"
    static final String andres_pz = "andreshuard.xml"
    static final String andres_map = "resource_mapping.txt"
    Project project
    DexKnifeExtension dexKnifeExtension
    String taskGroup="wjShell"//分组名称

    @Override
    void apply(Project project) {
        this.project = project
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new ProjectConfigurationException("the android plugin must be applied", null)
        }
        project.configurations.create(PLUGIN_NAME).extendsFrom(project.configurations.compile)
        dexKnifeExtension = project.extensions.create(PLUGIN_NAME, DexKnifeExtension,project)

        //多渠道打包初始化
        new PackerNgPlugin(project,dexKnifeExtension)

        project.afterEvaluate {
            boolean hasApp = project.plugins.hasPlugin("com.android.application")
            print("-hasApp = ${hasApp}\n")
            if (!hasApp) {
                throw new IllegalStateException("'android' plugin required.")
            }
//            checkCleanTask()//加固清理
            final def variants = project.android.applicationVariants
            variants.each{variant ->
                if (dexKnifeExtension.enabled) {
                    boolean checkProductFlavor = dexKnifeExtension.productFlavor == "" || dexKnifeExtension.productFlavor.equalsIgnoreCase(variant.flavorName)
                    boolean checkBuildType = dexKnifeExtension.buildType == "" || dexKnifeExtension.buildType.equalsIgnoreCase(variant.buildType.name)
                    if (checkProductFlavor && checkBuildType) {
                        printf "-DexKnifePlugin Enable = true\n";
                        printf "-DexKnifePlugin checkProductFlavor = ${checkProductFlavor}\n";
                        printf "-DexKnifePlugin checkBuildType = ${checkBuildType}\n";
                        printf "-DexKnifePlugin buildType.name = ${variant.buildType.name}\n";
                        printf "-DexKnifePlugin flavorName = ${variant.flavorName}\n";

                        filterActivity(project);

                        if (isMultiDexEnabled(variant)) {
                            if (SplitToolsFor130.isCompat(variant)) {
                                System.err.println("DexKnife: Compat 1.3.0.");
                                SplitToolsFor130.processSplitDex(project, variant)
                            } else if (SplitToolsFor150.isCompat()) {
                                SplitToolsFor150.processSplitDex(project, variant)
                            } else {
                                System.err.println("DexKnife Error: DexKnife is not compatible your Android gradle plugin.");
                            }
                        } else {
                            System.err.println("DexKnife : MultiDexEnabled is false, it's not work.");
                        }
                    }
                }
                //打包的时候加固
//                if(dexKnifeExtension.shell &&  AppManager.APKTOOLJARPATH==null){
//                    JiaGu.ISSHELL=true
//                    checkShellTask(variant)
//                }
                printf "-DexKnifePlugin Enable = false\n";
            }
            //加固task 单独加固
            project.android.applicationVariants.all { BaseVariant variant ->
                if(dexKnifeExtension.shell){
                    def File inputFile = variant.outputs[0].outputFile
                    if (inputFile==null || !inputFile.exists()){//不存在文件就直接退出
                        return
                    }

                    JiaGu.ISSHELL=true

                    AppManager.APKTOOLJARPATH=dexKnifeExtension.apktoolpath//apktool地址
                    JiaGu.JIAGU_ZIP_PATH=dexKnifeExtension.jiaguzippath
                    JiaGu.SHELLAPKNAME=dexKnifeExtension.shellname
                    //资源混淆
                    JiaGu.ANDRESGUARD=dexKnifeExtension.andresguard
                    JiaGu.andres_pz=project.file(andres_pz)
                    JiaGu.andres_map=project.file(andres_map)

                    if(!dexKnifeExtension.application.isEmpty()){
                        JiaGu.PROXY_APPLICATION_NAME=dexKnifeExtension.application//初始化
                    }

                    def archiveTask = project.task("apkshell${variant.name.capitalize()}",
                            type: ShellOutputApkTask) {
                        taskKeystoreConfig=getSigningConfig(variant)
                        taskInputFile=inputFile
                    }
                    archiveTask.group=taskGroup
                    def buildTypeName = variant.buildType.name
                    if (variant.name == buildTypeName) {
                        def taskName = "apkshell${buildTypeName.capitalize()}"
                        def task = project.tasks.findByName(taskName)
                        if (task == null) {
                            project.task(archiveTask, dependsOn: archiveTask)
                        }
                    }
                }
            }
        }
    }
    /**获取加固所需要的签名信息*/
    public static KeystoreConfig getSigningConfig(BaseVariant variant){
        def SigningConfig signingConfig=variant.buildType.signingConfig
        KeystoreConfig keystoreConfig=new KeystoreConfig()
        keystoreConfig.keystorePassword=signingConfig.storePassword
        keystoreConfig.aliasPassword=signingConfig.keyPassword
        keystoreConfig.alias=signingConfig.keyAlias
        keystoreConfig.keystorePath=signingConfig.storeFile.getAbsolutePath();
        return keystoreConfig
    }
    /**
     *  add cleanArchives task if not added
     * @return task
     */
//    void checkCleanTask() {
//        def task = project.task("cleanOutApks",
//                type:CleanApksTask ) {
//            shellApkPath=project.buildDir.getAbsolutePath()+"/outputs/apk"
//        }
//
//        project.getTasksByName("clean", true)?.each {
//            it.dependsOn task
//        }
//    }
    /**
     *  add archiveApk tasks
     * @param variant current Variant
     */
    void checkShellTask(BaseVariant variant) {
        println "开始加固==========================";
        def File inputFile = variant.outputs[0].outputFile


        AppManager.APKTOOLJARPATH=dexKnifeExtension.apktoolpath;//apktool地址
        JiaGu.JIAGU_ZIP_PATH=dexKnifeExtension.jiaguzippath;
        if(!dexKnifeExtension.application.isEmpty()){
            JiaGu.PROXY_APPLICATION_NAME=dexKnifeExtension.application//初始化
        }
        println "没有加固的apk地址:"+inputFile.absolutePath

        JiaGu.encrypt(inputFile, getSigningConfig(variant), new Callback<JiaGu.Event>() {
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
        println "完成加固==========================";
    }
    //filter Activity
    private static void filterActivity(Project project) {
        File file = project.file(DexSplitTools.DEX_KNIFE_CFG_TXT)
        if (file != null) {
            def justActivitys = [];
            file.eachLine { line ->
                //printf "read line ${line}\n";
                if (line.startsWith('-just activity')) {
                    line = line.replaceAll('-just activity', '').trim();
                    justActivitys.add(line)
                }
            }
            printf "-just activity size = ${justActivitys.size()}\n";
            if (justActivitys.size() != 0) {
                project.tasks.each { task ->
                    if (task.name.startsWith('collect') && task.name.endsWith('MultiDexComponents')) {
                        println "main-dex-filter: found task $task.name"
                        task.filter { name, attrs ->
                            String componentName = attrs.get('android:name')
                            if ('activity'.equals(name)) {
                                def result = justActivitys.find {
                                    componentName.endsWith("${it}")
                                }
                                def bool = result != null;
                                if (bool) {
                                    printf "main-dex-filter: keep ${componentName}\n"
                                }
                                return bool
                            }
                            return true
                        }
                    }
                }
            }
        }
    }

    private static boolean isMultiDexEnabled(variant) {
        def is = variant.buildType.multiDexEnabled
        if (is != null) {
            return is;
        }

        is = variant.mergedFlavor.multiDexEnabled
        if (is != null) {
            return is;
        }

        return false
    }

}