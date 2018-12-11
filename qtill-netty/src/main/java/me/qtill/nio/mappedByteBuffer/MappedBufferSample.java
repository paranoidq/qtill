package me.qtill.nio.mappedByteBuffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class MappedBufferSample {

    public static void main(String[] args) {
        File file = new File("/Users/paranoidq/test.txt");
        long len = file.length();
        byte[] ds = new byte[(int) len];

        try {
            MappedByteBuffer mappedByteBuffer =
                new RandomAccessFile(file, "r")
                    .getChannel().map(FileChannel.MapMode.READ_ONLY, 0, len);

            // 逐个读取字节
            for (int offset = 0; offset < len; offset++) {
                byte b = mappedByteBuffer.get();
                ds[offset] = b;
            }

            // 按word输出
            Scanner scanner = new Scanner(new ByteArrayInputStream(ds)).useDelimiter(" ");
            while (scanner.hasNext()) {
                System.out.println(scanner.next());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
