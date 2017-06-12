package com.wj.dexknife

import org.gradle.api.Project


class DexKnifeExtension {
    boolean enabled = false
    boolean shell = false
    boolean packerNgShell = false
    String productFlavor = ""
    String buildType = ""
    String application=""
    String apktoolpath=""
    String jiaguzippath=""
    String shellname=""
    boolean andresguard=""

    //多渠道打包
    static final String DEFAULT_NAME_TEMPLATE = '${appPkg}-${flavorName}-${buildType}-v${versionName}-${versionCode}'
    File archiveOutput
    File tempOutput
    boolean checkSigningConfig
    boolean checkZipAlign
    String archiveNameFormat
    DexKnifeExtension(Project project) {
        archiveOutput = new File(project.rootProject.buildDir, "archives")
        tempOutput = new File(project.rootProject.buildDir, "temp")
        archiveNameFormat = DEFAULT_NAME_TEMPLATE
        checkSigningConfig = false
        checkZipAlign = false
    }
}
