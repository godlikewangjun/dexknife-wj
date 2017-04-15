package com.wj.dexknife.shell.apkparser.struct.resource;


import com.wj.dexknife.shell.apkparser.struct.StringPool;
import com.wj.dexknife.shell.apkparser.utils.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongliu
 */
public class ResourceTable {
    private Map<Short, ResourcePackage> packageMap = new HashMap<>();
    private StringPool stringPool;

    public static Map<Integer, String> sysStyle = ResourceLoader.loadSystemStyles();

    public void addPackage(ResourcePackage resourcePackage) {
        this.packageMap.put(resourcePackage.getId(), resourcePackage);
    }

    public ResourcePackage getPackage(short id) {
        return this.packageMap.get(id);
    }

    public StringPool getStringPool() {
        return stringPool;
    }

    public void setStringPool(StringPool stringPool) {
        this.stringPool = stringPool;
    }
}
