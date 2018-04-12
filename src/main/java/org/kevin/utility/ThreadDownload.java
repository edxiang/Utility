package org.kevin.utility;

import java.io.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Kevin.Z on 2018/4/12.
 */
public class ThreadDownload implements Runnable{
    File sourceFile;
    File targetFile;
    CountDownLatch cdl;

    public ThreadDownload(File sourceFile, File targetFile, CountDownLatch cdl) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
        this.cdl = cdl;
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new BufferedInputStream(new FileInputStream(sourceFile));
            os = new BufferedOutputStream(new FileOutputStream(targetFile));

            int length = -1;
            byte[] bs = new byte[4 * 1024];
            while ((length = is.read(bs)) != -1) {
                os.write(bs, 0, length);
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
