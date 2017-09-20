package com.carlife.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.os.Environment;

public class CacheHelper {

	public static void saveFile(String fileName, String fileContent,Context context) {
		FileOutputStream fos;
		try {
			// fos = context.openFileOutput(fileName, context.MODE_APPEND);
			fos = context.openFileOutput(fileName, context.MODE_PRIVATE);
			fos.write(fileContent.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean saveObject(String fileName, Serializable ser,
			Context context) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = context.openFileOutput(fileName, context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser.toString().getBytes());
			oos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	public static String readObject(String fileName, Context context) {
		try {
			FileInputStream fis = context.openFileInput(fileName);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int len = 0;
			// ����ȡ�����ݷ������ڴ���---ByteArrayOutputStream
			while ((len = fis.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			fis.close();
			baos.close();
			// �����ڴ��д洢�����
			return baos.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	   
	
	///����Ϊ����

    /** * ���Ӧ�����е���� * * @param context * @param filepath */
    public static void cleanApplicationData(Context context, String... filepath) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        cleanDatabases(context);
        //cleanSharedPreference(context);
        cleanFiles(context);
        for (String filePath : filepath) {
            cleanCustomCache(filePath);
        }
    }
	
	
	 /** * ���Ӧ���ڲ�����(/data/data/com.xxx.xxx/cache) * * @param context */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }

    /** * ���Ӧ��������ݿ�(/data/data/com.xxx.xxx/databases) * * @param context */
    public static void cleanDatabases(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/databases"));
    }

    /**
     * * ���Ӧ��SharedPreference(/data/data/com.xxx.xxx/shared_prefs) * * @param
     * context
     */
    public static void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/shared_prefs"));
    }

    /** * ���������Ӧ����ݿ� * * @param context * @param dbName */
    public static void cleanDatabaseByName(Context context, String dbName) {
        context.deleteDatabase(dbName);
    }

    /** * ���/data/data/com.xxx.xxx/files�µ����� * * @param context */
    public static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }

    /**
     * * ����ⲿcache�µ�����(/mnt/sdcard/android/data/com.xxx.xxx/cache) * * @param
     * context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    /** * ����Զ���·���µ��ļ���ʹ����С�ģ��벻Ҫ��ɾ������ֻ֧��Ŀ¼�µ��ļ�ɾ�� * * @param filePath */
    public static void cleanCustomCache(String filePath) {
        deleteFilesByDirectory(new File(filePath));
    }


    /** * ɾ�� ����ֻ��ɾ��ĳ���ļ����µ��ļ���������directory�Ǹ��ļ������������� * * @param directory */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
	
	
	

}
