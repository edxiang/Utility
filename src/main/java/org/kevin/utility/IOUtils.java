package org.kevin.utility;

import java.io.*;

/**
 * Created by Kevin.Z on 2017/7/27.
 */
public class IOUtils {

    public static final long SPLIT_SIZE = 100 * 1024 * 1024L; // 100M

    public static File getFile(String path) {
        File file = new File(path);
        if (file.exists())
            return file;
        return null;
    }

    public static File createFile(String path){
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return file;
    }

    public static byte[] getBytes(File file) {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            int len = -1;
            byte[] bytes = new byte[1024];
            while ((len = is.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null)
                closeInputStream(is);
            if (bos != null)
                closeOutputStream(bos);
        }
    }

    public static String getContent(File file) {
        BufferedReader br = null;
        try {
            StringBuilder builder = new StringBuilder();
            String s;

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((s = br.readLine()) != null) {
                builder.append(s + "\n");
            }

            return builder.substring(0, builder.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
                else
                    System.err.println("have been closed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean copyFile(File original, File target) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(original);
            os = new FileOutputStream(target);
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (is != null)
                closeInputStream(is);
            if (os != null)
                closeOutputStream(os);
        }
    }

    private static void closeInputStream(InputStream is) {
        try {
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeOutputStream(OutputStream os) {
        try {
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
