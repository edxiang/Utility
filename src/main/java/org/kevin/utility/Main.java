package org.kevin.utility;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Kevin.Z on 2018/4/10.
 */
public class Main {
    private ExecutorService executor = Executors.newFixedThreadPool(33);
    private AtomicLong numberDirectory = new AtomicLong(1);
    private AtomicLong totalSize = new AtomicLong(0);
    private CountDownLatch latch = new CountDownLatch(1);


    public static void main(String[] args) {
        //new Main().splitFile();
        //new Main().copy();
        //new Main().combine();

        /**
         * 多个文件下载，开启多个线程，一个文件占一个线程，速度都比单线程要快。
         */
//        new Main().oneByOneThread();
//        new Main().oneByOne();

    }


    /**
     * 7.7G
     * 1. 使用 InputStream & OutputStream (byte[] 的大小为 1024)，直接复制耗费的时间是 170 秒
     * 2. 使用 BufferStream(byte[] 的大小为 4 * 1024)，耗费时间为 127 - 138 秒
     */
    private void copy() {
        long time = System.nanoTime();
        final File sourceFile = IOUtils.getFile("D:\\download\\CentOS-7-x86_64-Everything-1611.iso");
        final File targetFile = IOUtils.createFile("F:\\forVideo\\iso_temp.iso");

        final CountDownLatch cdl = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                IOUtils.copyFile(sourceFile, targetFile, cdl);
            }
        });
        timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("percentage:" + ((targetFile.length() / (7.7 * 1024 * 1024 * 1024)) * 100));
            }
        }, 10, 10, TimeUnit.SECONDS);
        try {
            cdl.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(System.nanoTime() - time);
        executor.shutdown();
        timer.shutdown();
    }

    /**
     * 其实合并也可以开启多个线程的，只是有没有必要呢？
     * <p>
     * 目标文件的大小是 7.7G，以 100M 拆分文件，单线程模式下合并。
     * 1. 使用 InputStream & OutputStream ( byte[] 的大小为 1024 )，耗费的时间为 590秒
     * 2. 使用 Buffer 耗费的时间为 476秒
     */
    private void combine() {
        String basicName = "F:\\forVideo\\CentOS-7-x86_64-Everything-1611.iso.tempDownload";
        int counts = 78;
        String targetName = basicName.substring(0, basicName.lastIndexOf("."));
        File targetFile = IOUtils.createFile(targetName);

        InputStream is = null;
        OutputStream os = null;
        long time = System.nanoTime();
        try {
            os = new BufferedOutputStream(new FileOutputStream(targetFile));
            for (int i = 0; i <= counts; i++) {
                File file = IOUtils.getFile(basicName + i);
                if (file != null) {
                    is = new BufferedInputStream(new FileInputStream(file));
                    int length = -1;
                    byte[] bs = new byte[1024];
                    while ((length = is.read(bs)) != -1) {
                        os.write(bs, 0, length);
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(System.nanoTime() - time);
    }

    /**
     * 多线程复制，耗费的时间是 693 秒……
     */
    private void splitFile() {
        String filePath = "D:\\download\\CentOS-7-x86_64-Everything-1611.iso";
        //String filePath = "F:\\videoForTest\\001.mov";
        File sourceFile = IOUtils.getFile(filePath);
        if (sourceFile != null) {
            ExecutorService executor = null;

            int counts = (int) (sourceFile.length() / IOUtils.SPLIT_SIZE);
            if (counts > 30)
                executor = Executors.newFixedThreadPool(30);
            else
                executor = Executors.newFixedThreadPool(counts);
            String targetPath = "F:\\forVideo\\";
            String suffix = ".tempDownload";
            String originalFileName = sourceFile.getName();

            System.out.println("begin");
            long time = System.nanoTime();
            CountDownLatch cdl = new CountDownLatch(counts);
            for (int i = 0; i <= counts; i++) {
                DownloadThread downloadThread = new DownloadThread(sourceFile, i, targetPath + originalFileName + suffix + i, cdl);
                executor.submit(downloadThread);
            }
            try {
                cdl.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            executor.shutdown();
            System.out.println("takes: " + (System.nanoTime() - time)); // 693s
        }
    }


    class DownloadThread implements Runnable {
        File sourceFile;
        int number;
        String targetFileName;
        CountDownLatch cdl;

        public DownloadThread(File sourceFile, int number, String targetFileName, CountDownLatch cdl) {
            this.sourceFile = sourceFile;
            this.number = number;
            this.targetFileName = targetFileName;
            this.cdl = cdl;
        }

        @Override
        public void run() {
            InputStream is = null;
            OutputStream os = null;
            try {
                File targetFile = IOUtils.createFile(targetFileName);

                is = new FileInputStream(sourceFile);
                os = new FileOutputStream(targetFile);
                int length = -1;
                byte[] bs = new byte[1024];
                long currentLength = 0;
                is.skip(number * IOUtils.SPLIT_SIZE);
                while ((length = is.read(bs)) != -1) {
                    if (currentLength + length > IOUtils.SPLIT_SIZE) {
                        os.write(bs, 0, (int) (IOUtils.SPLIT_SIZE - currentLength));
                        break;
                    }
                    os.write(bs, 0, length);
                    currentLength += length;
                }
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cdl.countDown();
                try {
                    if (is != null)
                        is.close();
                    if (os != null)
                        os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
