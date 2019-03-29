package com.junipersys.a3_chamber_test;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskTest {
    private int d_runs = 250;
    private int numDirs = 3, numFiles = 10;
    public static final String TEST_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890abcdefghijklmnopqrstuvwxyz";
    public static final String TEST_FILE_NAME = "test_file";
    public FileOutputStream fos;
    public static Context mContext;

    public DiskTest(Context context){
        mContext = context;
    }

    public void runDiskTest(){
        int counter = 0;
        while(counter < d_runs){
            testDisk();
            counter++;
        }
    }

    public void testDisk(){
        // Create directories and fill with text files.
        for(int i = 0; i < numDirs; i++){
            FileUtils.createDirectory(FileUtils.getStorageDir(i));
            for(int j = 0; j < numFiles; j++){
                FileUtils.createTextFile(FileUtils.getStorageFiles(i , j, TEST_FILE_NAME));
            }
            //Copy filled directories and files
            try {
                FileUtils.copyDirectory(FileUtils.getStorageDir(i), FileUtils.getCopyStorageDir(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Delete created files and directories
        for(int i = 0; i < numDirs; i++){
            FileUtils.deleteDirectory(FileUtils.getStorageDir(i));
            FileUtils.deleteDirectory(FileUtils.getCopyStorageDir(i));
        }
    }
}

class FileUtils {

    public static File getStorageDir(int i) {
        String filesDirPath = DiskTest.mContext.getFilesDir().toString() +"/Test_Direct" + i;

        File ret = new File(filesDirPath);
        if(!ret.exists()) {
            ret.mkdirs();
        }
        return ret;
    }

    public static File getCopyStorageDir(int i) {
        String filesDirPath = DiskTest.mContext.getFilesDir().toString() +"/Copy_Test_Direct" + i;

        File ret = new File(filesDirPath);
        if(!ret.exists()) {
            ret.mkdirs();
        }
        return ret;
    }

    public static File getStorageFiles(int i, int j, String filename) {
        String filesDirPath = DiskTest.mContext.getFilesDir().toString() +"/Test_Direct" + i + "/" + filename + j;

        File ret = new File(filesDirPath);
        if(!ret.exists()) {
            try {
                ret.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean delete(String path) {
        final File deleteTarget = new File(path);
        return delete(deleteTarget);
    }

    public static boolean delete(File targetFile) {
        boolean deleted = true;
        try {
            deleted = targetFile.delete();
            if(!deleted && ! targetFile.isFile()) {
                throw new Exception(String.format("File could not be deleted. Path: %s", targetFile.getAbsolutePath()));
            }
            deleted = true;
        } catch (Exception e) {
            deleted = false;
        }

        return deleted;
    }

    public static boolean createDirectory(File directory) {
        boolean created = true;
        try {
            created = directory.mkdirs();
            if(!created && ! directory.isDirectory()) {
                throw new Exception(String.format("Directory could not be created. Path: %s", directory.getAbsolutePath()));
            }
            created = true;
        } catch (Exception e) {
            created = false;
        }

        return created;
    }

    public static boolean createTextFile(File txtfile) {
        boolean created = true;
        try {
            created = txtfile.createNewFile();
            if(!created && ! txtfile.isFile()) {
                throw new Exception(String.format("Text file could not be created. Path: %s", txtfile.getAbsolutePath()));
            }
            created = true;
        } catch (Exception e) {
            created = false;
        }

        FileOutputStream fos = null;
        try {
            fos = DiskTest.mContext.openFileOutput(txtfile.getName() , Context.MODE_APPEND);
            fos.write(DiskTest.TEST_STRING.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return created;
    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(
                        targetLocation, children[i]));
            }
        } else {

            copyFile(sourceLocation, targetLocation);
        }
    }

    public static void copyFile(File sourceLocation, File targetLocation)
            throws IOException {
        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}

