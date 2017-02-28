先说说写这个插件的目的，其实就是第三方加固不方便还要钱，没有插件打包方便集成，最主要的是不知道别人怎么弄的出现bug和修改需求不方便，所以我就写了个插件，虽然是基础加固但是好过只能用混淆简单保护代码要好，最主要是自己可以随意改。当然还集成分包和多渠道打包的插件功能。
首先感谢 packer-ng-plugin 版本1.0.8，Android-Easy-MultiDex版本1.0.1和ApkToolPlus，插件功能是在这些个插件优化而来。默认是要使用分包和多渠道打包的。这个两个配置请见链接地址。
首先是用法介绍

```
dependencies {
   classpath 'com.library.wj:dexknife-wj:1.0.2'//分包
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
    packerNgShell false
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
好了上效果图

没有加固之前的
![这里写图片描述](http://img.blog.csdn.net/20170228152800529?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDUyMzgzMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
加固之后的
![这里写图片描述](http://img.blog.csdn.net/20170228152624571?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDUyMzgzMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
区别在于 你只看的到壳文件 原项目的dex加密了放在asset了。动态加载了dex 和分包是一样的 只不过是解密要点时间。
如果要自定义加壳文件请在配置文件加上 application '你的壳的application 名称'
原理在反响好的情况下再写吧。github demo地址如果有需要请留言，要求多了话我会上传。
apktool和jiagu.zip 下载http://download.csdn.net/detail/u010523832/9766204
单独加固需要点击task 如图
![这里写图片描述](http://img.blog.csdn.net/20170228160226480?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDUyMzgzMg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
**多渠道打包敲命令 和packer-ng插件一样**
**gradlew clean apkRelease**
**请注意要配置签名 和 普通配置没有什么不同**

**github demo地址 https://github.com/godlikewangjun/dexknife-wj**



