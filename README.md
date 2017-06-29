##介绍
###插件实现了加固部分的签名校验和dex分包加载，dex加密等，其余功能是多渠道打包和自定义分包
先说说写这个插件的目的，其实就是第三方加固不方便还要钱，没有插件打包方便集成，最主要的是不知道别人怎么弄的出现bug和修改需求不方便，所以我就写了个插件，虽然是基础加固但是好过只能用混淆简单保护代码要好，最主要是自己可以随意改。当然还集成分包和多渠道打包的插件功能。
默认是要使用分包和多渠道打包的，资源混淆在1.1.0上新加，默认不开启需要自己开启。
**感谢**
<br>[packer-ng-plugin](https://github.com/mcxiaoke/packer-ng-plugin) 版本1.0.8，多渠道打包<br>
[Android-Easy-MultiDex](https://github.com/TangXiaoLv/Android-Easy-MultiDex)版本1.0.1,分包<br>
[ApkToolPlus](https://github.com/linchaolong/ApkToolPlus)加固<br>
[AndResguard](https://github.com/shwenzhang/AndResGuard)资源混淆<br>
使用方法可以参见上述的项目，本插件略微有些改动。详情见说明或者demo或者源码

####更新说明
**1.1.1**
<br>1.更新加密方式，加固之后压缩体积大幅度缩小，转移到这个版本请更新jiagu.zip。
<br>2.添加自动打包成jiagu.zip的执行类。暂时不支持命令，请查看OutShellZip.java
<br>3.优化部分流程
<br>**1.1.0**
<br>1.修改了单独加固方式 需要在gradle wjshell点击有对应apk的task
<br>2.新增了shellname的修改，可以自己定义修改后的名称
<br>3.最主要的是新加了资源混淆，主要用到了andresguard的混淆代码，需要配置文件andreshuard.xml和resource_mapping.txt，具体查看demo或者andresguard
<br>**1.0.3**
<br>去掉了多余加固 兼容gradle3.3**

<br>用法介绍

```
dependencies {
   classpath 'com.library.wj:dexknife-wj:1.1.1'//分包
}
```

使用插件

```
apply plugin: 'dexknifeWj'
```

接下来是配置

```
dexKnife {
    //必选参数
    enabled true //if false,禁用分包插件
    //可选参数/
    //1.如果没有可选参数，将根据enabled决定是否分包。
    //2.如果有可选参数，需满足必选参数和可选参数的条件才允许分包

    /*
    *eg:当前productFlavors = dev，buildType = debug，
    *参数组合1：enabled = true，productFlavor = dev，buildType = debug 分包
    *参数组合2：enabled = true，productFlavor = mock，buildType = debug 不分包
    *参数组合1：enabled = true，buildType = debug 所有buildType = debug分包
    *参数组合1：enabled = true，productFlavor = dev 所有productFlavor = dev分包
    * */
    //=======================加固
    shell true
    andresguard true//是否开启资源混淆
    packerNgShell false
    shellname ''
    apktoolpath 'C://android_work/android_workspace/android_studio_xs/dexknife-wj/src/apktool/apktool.jar'
    jiaguzippath 'C:/android_work/android_workspace/android_studio_xs/dexknife-wj/src/main/java/com/wj/dexknife/shell/jiagu/jiagu.zip'
    //=======================多渠道
    // 指定渠道打包输出目录
    archiveOutput = file(new File(project.buildDir.path, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "_apks"))
    // 指定渠道打包输出文件名格式
    // 默认是 `${appPkg}-${flavorName}-${buildType}-v${versionName}-${versionCode}`
    archiveNameFormat = 'qianfandu-${flavorName}-${versionName}'
    // 是否检查Gradle配置中的signingConfig，默认不检查
    // checkSigningConfig = false
    // 是否检查Gradle配置中的zipAlignEnabled，默认不检查
    // checkZipAlign = false
}
```

其中加固注释的是加固所要填的，其他配置请见其他两个插件的写法。
apktoolpath 是打包工具
jiaguzippath 是壳的文件 只个文件后面介绍
shell 是编译时打包
packerNgShell 是配合多渠道打包用的 就是packer-ng这个插件，默认是集成了的
application '你的壳的application 名称'
andresguard true or false 是否开启混淆
shellname '' 可填可不填 设置加固打包的名称
####命令打包方式
#####1.加固(请在jdk bin 目录下执行，有好的改进请留言)
```
命令说明 java -jar jar路径 签名文件路径 keystorePassword aliasPassword alias 加固后缀名 apktool.jar路径 jiagu.zip路径 要加固的apk路径
例子 java -jar D:\wangjun\github\dexknife-wj2\dexknife-wj-1.1.1.jar D:\wangjun\github\dexknife-wj2\test.jks 123456 123456 test _  D:\wangjun\github\dexknife-wj2\apktool.jar D:\wangjun\github\dexknife-wj2\jiagu.zip D:\wangjun\github\dexknife-wj2\app\build\outputs\apk\app-debug.apk
```
#####2.制作jiagu.zip即是壳文件
```
命令说明 java -cp jar路径 加壳的方法 制作壳文件工作的路径 需要制作成壳文件的apk路径 输出的文件夹  apktool路径
例子 java -cp C:\Users\MoreStrongW\Desktop\dexknife-wj-1.1.1.jar ml.OutShellZip C:\Users\MoreStrongW\Desktop\deco D:\wangjun\github\dexknife-wj2\myapplication\build\outputs\apk\myapplication-debug.apk  C:\Users\MoreStrongW\Desktop\1213 D:\wangjun\github\dexknife-wj2\apktool.jar
```
好了上效果图

没有加固之前的  
![这里写图片描述](http://img.blog.csdn.net/20170228152800529?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDUyMzgzMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
加固之后的  
![这里写图片描述](http://img.blog.csdn.net/20170228152624571?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDUyMzgzMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
区别在于 你只看的到壳文件 原项目的dex加密了放在asset了。动态加载了dex 和分包是一样的 只不过是解密要点时间。  
如果要自定义加壳文件请在配置文件加上 application '你的壳的application 名称'  
单独加固需要点击task 如图
![这里写图片描述](http://img.blog.csdn.net/20170612153610175?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDUyMzgzMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
**多渠道打包敲命令 和packer-ng插件一样**
**gradlew clean apkRelease**
**请注意要配置签名 和 普通配置没有什么不同**

**demo请注意修改 apktoolpath jiaguzippath 和签名 在项目里面已经加了所需的资源包请修改路径即可**

**关于壳文件的制作**
myapplication项目是壳文件的项目 build之后有apk，用ApkToolPlus反编译得到文件，按照jiagu.zip的结构即可。
apktool.jar不用改。 项目文件可以修改加载策略和包名，其他不建议大改，如果你确实搞的定。


**未来的完善功能
添加更完善的加固方式，请期待。。。



