package org.kevin.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Kevin.Z on 2018/4/8.
 */
public class NIOUtils {

    public static FileChannel getFileChannel(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        return fileChannel;
    }

    public static byte[] getByteByBuffer(File file) throws IOException {
        FileChannel fileChannel = getFileChannel(file);
        ByteBuffer buffer = ByteBuffer.allocate(256);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int length = -1;
        while ((length = fileChannel.read(buffer)) != -1) {
            buffer.flip(); // change the mode into read

            byte[] bs = new byte[length];
            buffer.get(bs);
            bos.write(bs);

            buffer.clear(); // make buffer ready for writing.
        }

        fileChannel.close();
        bos.close();
        return bos.toByteArray();
    }
}
