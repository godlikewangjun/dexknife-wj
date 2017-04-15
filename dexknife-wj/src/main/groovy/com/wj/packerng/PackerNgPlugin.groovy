package com.wj.packerng

import com.android.build.gradle.api.BaseVariant
import com.wj.dexknife.DexKnifeExtension
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

// Android Multi Packer Plugin Source
public class PackerNgPlugin {
    static final String TAG = "PackerNg"
    static final String P_MARKET = "market"

    Project project
    List<String> markets;
    DexKnifeExtension dexKnifeExtension

    /**初始化*/
    PackerNgPlugin(Project project, DexKnifeExtension dexKnifeExtension) {
        this.project = project
        this.dexKnifeExtension = dexKnifeExtension
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new ProjectConfigurationException("the android plugin must be applied", null)
        }
        parseMarkets()
        applyPluginTasks()
    }


    void applyPluginTasks() {
        project.afterEvaluate {
            checkCleanTask()
            debug(":${project.name} flavors: ${project.android.productFlavors.collect { it.name }}")
            //applySigningConfigs()
            project.android.applicationVariants.all { BaseVariant variant ->
                checkPackerNgTask(variant)
            }
        }
    }

/**
 *  parse markets file
 * @param project Project
 * @return found markets file
 */
    boolean parseMarkets() {
        markets = new ArrayList<String>();

        if (!project.hasProperty(P_MARKET)) {
            debug("parseMarkets() market property not found, ignore")
            return false
        }

        // check markets file exists
        def marketsFilePath = project.property(P_MARKET).toString()
        if (!marketsFilePath) {
            println(":${project.name} markets property not found, using default")
            // if not set, use default ./markets.txt
            marketsFilePath = "markets.txt"
        }

        File file = project.rootProject.file(marketsFilePath)
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Invalid market file: ${file.absolutePath}")
        }
        println(":${project.name} market: ${file.absolutePath}")
        markets = readMarkets(file)
        debug(":${project.name} found markets:$markets")
        return true
    }

    List<String> readMarkets(File file) {
        // add all markets
        List<String> allMarkets = []
        file.eachLine { line, number ->
            String[] parts = line.split('#')
            if (parts && parts[0]) {
                def market = parts[0].trim()
                if (market) {
                    allMarkets.add(market)
                }
            } else {
                debug(":${project.name} skip invalid market line ${number}:'${line}'")
            }
        }
        return allMarkets
    }

/**
 *  add archiveApk tasks
 * @param variant current Variant
 */
    void checkPackerNgTask(BaseVariant variant) {
        debug("checkPackerNgTask() for ${variant.name}")
        def File inputFile = variant.outputs[0].outputFile
        def File tempDir = dexKnifeExtension.tempOutput
        def File outputDir = dexKnifeExtension.archiveOutput
        debug("checkPackerNgTask() input: ${inputFile}")
        debug("checkPackerNgTask() temp: ${tempDir}")
        debug("checkPackerNgTask() output: ${outputDir}")
        def archiveTask = project.task("apk${variant.name.capitalize()}",
                type: ArchiveAllApkTask) {
            theVariant = variant
            theExtension = dexKnifeExtension
            theMarkets = markets
            dependsOn variant.assemble
        }

        debug("checkPackerNgTask() new variant task:${archiveTask.name}")

        def buildTypeName = variant.buildType.name
        if (variant.name != buildTypeName) {
            def taskName = "apk${buildTypeName.capitalize()}"
            def task = project.tasks.findByName(taskName)
            if (task == null) {
                debug("checkPackerNgTask() new build type task:${taskName}")
                task = project.task(taskName, dependsOn: archiveTask)
            }
        }
    }

    /**
     *  add cleanArchives task if not added
     * @return task
     */
    void checkCleanTask() {
        def output = dexKnifeExtension.archiveOutput
        debug("checkCleanTask() create clean archived apks task, path:${output}")
        def task = project.task("cleanApks",
                type: CleanArchivesTask) {
            target = output
        }

        project.getTasksByName("clean", true)?.each {
            it.dependsOn task
        }
    }

/**
 *  print debug messages
 * @param msg msg
 * @param vars vars
 */
    void debug(String msg) {
        project.logger.info(msg)
    }

}
