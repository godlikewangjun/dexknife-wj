package com.qianfandu;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.qianfandu.utils.ApkToolPlus;
import com.qianfandu.utils.DexProtector;
import com.qianfandu.utils.Reflect;
import com.qianfandu.utils.SignatureUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class ProxyApplication extends Application {

    private static final String TAG = ProxyApplication.class.getSimpleName();
    private static int initCount = 0;
    private String srcAppClassName = null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ApkToolPlus.loadLibrary();
        // 自定义ClassLoader
        try {
            Log.e(TAG, "init app " + (++initCount));
           String[] dexName=getAssets().list("jiagu_data.bin");
            ClassLoader classLoader = new DexProtector(this).loadEncryptDex(dexName);
            if (classLoader == null) {
                Log.e(TAG, "loadEncryptDex fail");
            } else {
                Log.e(TAG, "loadEncryptDex success");
                String appClassName = getSrcAppClassName();
                if (!TextUtils.isEmpty(appClassName)) {
                    srcAppClassName = appClassName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取原来Applicatoin的类名
     *
     * @return
     */
    private String getSrcAppClassName() {
        try {
            PackageManager packageManager = getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        return applicationInfo.metaData.getString("apktoolplus_jiagu_app");
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取原来Application的实例
     *
     * @param classLoader
     * @param appClassName
     * @return
     */
    private Application getSrcAppInstance(ClassLoader classLoader, String appClassName) {
        try {
            // 反射出原来的applicatoin
            Class<?> appClass = classLoader.loadClass(appClassName);
            return (Application) appClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 修改应用上下文
    // http://blog.csdn.net/jltxgcy/article/details/50540309
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Application changeTopApplication(String appClassName) {

        //有值的话调用该Applicaiton
        Object currentActivityThread = Reflect.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[] {}, new Object[] {});
        Object mBoundApplication = Reflect.getFieldValue(
                "android.app.ActivityThread", currentActivityThread,
                "mBoundApplication");
        Object loadedApkInfo = Reflect.getFieldValue(
                "android.app.ActivityThread$AppBindData",
                mBoundApplication, "info");
        //把当前进程的mApplication 设置成了null
        Reflect.setFieldValue("android.app.LoadedApk", loadedApkInfo, "mApplication", null);
        Object oldApplication = Reflect.getFieldValue(
                "android.app.ActivityThread", currentActivityThread,
                "mInitialApplication");
        //http://www.codeceo.com/article/android-context.html
        ArrayList<Application> mAllApplications = (ArrayList<Application>) Reflect
                .getFieldValue("android.app.ActivityThread",
                        currentActivityThread, "mAllApplications");
        mAllApplications.remove(oldApplication);//删除oldApplication

        ApplicationInfo loadedApk = (ApplicationInfo) Reflect
                .getFieldValue("android.app.LoadedApk", loadedApkInfo,
                        "mApplicationInfo");
        ApplicationInfo appBindData = (ApplicationInfo) Reflect
                .getFieldValue("android.app.ActivityThread$AppBindData",
                        mBoundApplication, "appInfo");

        loadedApk.className = appClassName;
        appBindData.className = appClassName;


        Application app = (Application) Reflect.invokeMethod(
                "android.app.LoadedApk", loadedApkInfo, "makeApplication",
                new Object[]{false, null},
                boolean.class, Instrumentation.class);//执行 makeApplication（false,null）
        Reflect.setFieldOjbect("android.app.ActivityThread",
                "mInitialApplication", currentActivityThread, app);

        ArrayMap mProviderMap = (ArrayMap) Reflect.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mProviderMap");
        Iterator it = mProviderMap.values().iterator();
        while (it.hasNext()) {
            Object providerClientRecord = it.next();
            Object localProvider = Reflect.getFieldOjbect(
                    "android.app.ActivityThread$ProviderClientRecord",
                    providerClientRecord, "mLocalProvider");
            Reflect.setFieldOjbect("android.content.ContentProvider",
                    "mContext", localProvider, app);
        }
        return app;
    }

    @Override
    public void onCreate() {
        // 签名检查
        SignatureUtils.checkSign(getApplicationContext()); // 注意：不要在attachBaseContext方法中调用，因为应用上下文还没初始化完成
        if (!TextUtils.isEmpty(srcAppClassName)) {
            Application app = changeTopApplication(srcAppClassName);
            if (app != null) {
                app.onCreate();
            } else {
                Log.e(TAG, "changeTopApplication failure!!!");
            }
        }

    }

}
