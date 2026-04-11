package com.million.learnify;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * FileUtils - Utility to ensure data persistence by moving external files 
 * to the app's permanent internal storage.
 */
public class FileUtils {

    public static String saveImageToInternalStorage(Context context, Uri uri, String folderName, String fileName) {
        try {
            File folder = new File(context.getFilesDir(), folderName);
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, fileName + ".jpg");
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return uri.toString(); // Fallback to original URI if copy fails
        }
    }
}
