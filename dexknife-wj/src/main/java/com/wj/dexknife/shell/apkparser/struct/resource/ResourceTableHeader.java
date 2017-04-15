package com.wj.dexknife.shell.apkparser.struct.resource;


import com.wj.dexknife.shell.apkparser.struct.ChunkHeader;

/**
 * resource file header
 *
 * @author dongliu
 */
public class ResourceTableHeader extends ChunkHeader {
    // The number of ResTable_package structures. uint32
    private long packageCount;

    public ResourceTableHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    public long getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(long packageCount) {
        this.packageCount = packageCount;
    }
}
