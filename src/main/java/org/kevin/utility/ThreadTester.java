package org.kevin.utility;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Kevin.Z on 2018/4/12.
 */
public class ThreadTester {
    private AtomicLong numberDirectory = new AtomicLong(1);
    private AtomicLong totalSize = new AtomicLong(0);
    private ExecutorService executor = Executors.newFixedThreadPool(33);

    /**
     * 计算文件夹 dir 的大小。
     * 文件夹下的文件多的话，会有bug: 线程会出在 WAITING 的状态……
     *
     * @param dir    the folder
     */
    public void getDirSize(File dir) {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                numberDirectory.incrementAndGet();
                System.out.println("in " + f.getAbsolutePath());
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        getDirSize(f);
                    }
                });
            } else {
                totalSize.addAndGet(f.length());
            }
        }

        if(numberDirectory.decrementAndGet() == 0){
            System.out.println("the total size is :" + totalSize.get());
            executor.shutdown();
        }
    }

    /**
     * 4秒：20个文件（总大小为 977M ），单线程下载
     * 1.468秒：使用 Buffer 的话
     */
    private void oneByOne() {
        InputStream is = null;
        OutputStream os = null;
        long time = System.nanoTime();
        try {
            String dirPath = "F:\\videoForTest";
            String targetPath = "D:\\threadTest\\";
            File dir = IOUtils.getFile(dirPath);
            File[] files = dir.listFiles();
            for (File file : files) {
                is = new BufferedInputStream(new FileInputStream(file));
                File targetFile = IOUtils.createFile(targetPath + file.getName());
                os = new BufferedOutputStream(new FileOutputStream(targetFile));
                int length = -1;
                byte[] bs = new byte[4 * 1024];
                while ((length = is.read(bs)) != -1) {
                    os.write(bs, 0, length);
                }
                os.flush();
            }
            System.out.println(System.nanoTime() - time);
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
    }

    /**
     * 3 秒：20个文件（总大小为 977M ），开 20 个线程。
     * 0.930秒：使用 Buffer 的话
     */
    private void once() {
        try {
            String dirPath = "F:\\videoForTest";
            String targetPath = "D:\\threadTest\\";
            File dir = IOUtils.getFile(dirPath);
            long time = System.nanoTime();
            if (dir != null && dir.isDirectory()) {
                File[] files = dir.listFiles();
                ExecutorService executor = Executors.newFixedThreadPool(files.length);
                CountDownLatch cdl = new CountDownLatch(files.length);
                for (File file : files) {
                    String targetFilePath = targetPath + file.getName();
                    File targetFile = IOUtils.createFile(targetFilePath);
                    ThreadDownload d = new ThreadDownload(file, targetFile, cdl);
                    executor.submit(d);
                }
                executor.shutdown();
                cdl.await();
                System.out.println(System.nanoTime() - time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
