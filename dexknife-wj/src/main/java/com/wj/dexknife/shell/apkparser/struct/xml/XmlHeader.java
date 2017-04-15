package com.wj.dexknife.shell.apkparser.struct.xml;


import com.wj.dexknife.shell.apkparser.struct.ChunkHeader;

/**
 * Binary XML header. It is simply a struct ResChunk_header.
 *
 * @author dongliu
 */
public class XmlHeader extends ChunkHeader {
    public XmlHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
