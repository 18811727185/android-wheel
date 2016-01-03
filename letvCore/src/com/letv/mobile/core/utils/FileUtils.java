package com.letv.mobile.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

/**
 * General file manipulation utilities.The methods of this class, although
 * static, are not
 * thread safe and cannot be invoked by several threads at the same time.
 * Synchronization
 * is required by the caller.
 * @author bosu@yunrang.com (Bo Su)
 */
public class FileUtils {
    private static final int FILE_HEAD_LENGTH = 10;
    private static final long STORAGE_MARGIN_SIZE = 1024 * 1024 * 2;// 存储余量
    public static boolean isDeleteing = false;

    /**
     * Returns the absolute path to the cache file on the filesystem. These
     * files will be
     * ones that get deleted first when the device runs on storage.
     * @param context
     *            Global information about an application environment
     * @param name
     *            File name
     * @return Returns the absolute path to the application specific cache
     *         directory on the
     *         filesystem. Returns null if external storage is not currently
     *         mounted.
     */
    public static File getInternalCacheFile(Context context, String name) {
        File dir = context.getCacheDir();
        if (dir == null) {
            return null;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(context.getCacheDir(), name);
    }

    /**
     * Returns the absolute path to the cache file on the filesystem
     * @param context
     *            Global information about an application environment
     * @param name
     *            File name
     * @returnReturns the absolute path to the application specific cache
     *                directory on the filesystem. Returns null if internal
     *                storage is not currently mounted.
     */
    public static File getInternalFiles(Context context, String name) {
        File dir = context.getFilesDir();
        if (dir == null) {
            return null;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(context.getFilesDir(), name);
    }

    /**
     * Returns the absolute path to the cache file on the external filesystem
     * @param context
     *            Global information about an application environment
     * @param name
     *            File name
     * @return Returns the path of the cache file on external storage. Returns
     *         null if
     *         external storage is not currently mounted.
     */
    public static File getExternalCacheFile(Context context, String name) {
        File dir = getExternalCacheDir(context);
        if (dir == null) {
            return null;
        }
        return new File(getExternalCacheDir(context), name);
    }

    /**
     * Returns the absolute path to the directory on the external filesystem
     * (that is
     * somewhere on {@link android.os.Environment#getExternalStorageDirectory()
     * Environment.getExternalStorageDirectory()} where the application can
     * place cache
     * files it owns.
     * <p>
     * This is like {@link android.content.Context#getCacheDir()
     * Context.getCacheDir()} in that these files will be deleted when the
     * application is uninstalled.
     * <p>
     * <b>API level 8+</b>:
     * {@link android.content.Context#getExternalCacheDir()
     * Context.getExternalCacheDir()}
     * @param context
     *            Global information about an application environment
     * @return Returns the path of the directory holding application cache files
     *         on external
     *         storage. Returns null if external storage is not currently
     *         mounted so it
     *         could not ensure the path exists; you will need to call this
     *         method again
     *         when it is available.
     * @see android.content.Context#getCacheDir
     */
    public static File getExternalCacheDir(Context context) {
        boolean isSDCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (!isSDCardExist) {
            return null;
        }
        final File externalCacheDir = new File(
                Environment.getExternalStorageDirectory(), "/Android/data/"
                        + context.getPackageName() + "/cache/");
        if (!externalCacheDir.exists()) {
            externalCacheDir.mkdirs();
        }
        return externalCacheDir;
    }

    /**
     * get external download dir
     * @param context
     * @return
     */
    public static File getExternalDownloadDir(Context context) {
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        boolean isSDCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (!isSDCardExist) {
            return null;
        }
        final File externalCacheDir = new File(
                Environment.getExternalStorageDirectory(), "/letv/download/");
        if (!externalCacheDir.exists()) {
            externalCacheDir.mkdirs();
        }
        return externalCacheDir;
    }

    /**
     * get external download file
     * @param context
     * @param name
     * @return
     */
    public static File getExternalDownloadFile(Context context, String name) {
        File dir = getExternalDownloadDir(context);
        if (dir == null) {
            return null;
        }
        return new File(getExternalDownloadDir(context), name);
    }

    /**
     * Deletes a file. If file is a directory, delete it and all
     * sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     * (java.io.File methods returns a boolean)</li>
     * </ul>
     * @param file
     *            file or directory to delete, must not be <code>null</code>
     * @throws NullPointerException
     *             if the directory is <code>null</code>
     * @throws FileNotFoundException
     *             if the file was not found
     * @throws IOException
     *             in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: "
                            + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Deletes a directory recursively.
     * @param directory
     *            directory to delete
     * @throws IOException
     *             in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);
        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Cleans a directory without deleting it.
     * @param directory
     *            directory to clean
     * @throws IOException
     *             in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the
     * specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this
     * method will overwrite it.
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however it
     * is not guaranteed that the operation will succeed. If the modification
     * operation fails, no indication is provided.
     * @param srcFile
     *            an existing file to copy, must not be {@code null}
     * @param destFile
     *            the new file, must not be {@code null}
     * @throws NullPointerException
     *             if source or destination is {@code null}
     * @throws IOException
     *             if source or destination is invalid
     * @throws IOException
     *             if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file to the
     * specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this
     * method will overwrite it.
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified date/times using
     * {@link File#setLastModified(long)}, however it is not guaranteed that the
     * operation will succeed. If the modification operation fails, no
     * indication is provided.
     * @param srcFile
     *            an existing file to copy, must not be {@code null}
     * @param destFile
     *            the new file, must not be {@code null}
     * @param preserveFileDate
     *            true if the file date of the copy should be the same as the
     *            original
     * @throws NullPointerException
     *             if source or destination is {@code null}
     * @throws IOException
     *             if source or destination is invalid
     * @throws IOException
     *             if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File, boolean)
     */
    public static void copyFile(File srcFile, File destFile,
            boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile
                    + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile
                    + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException("Source '" + srcFile + "' and destination '"
                    + destFile + "' are the same");
        }
        File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException("Destination '" + parentFile
                        + "' directory cannot be created");
            }
        }
        if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException("Destination '" + destFile
                    + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Internal copy file method.
     * @param srcFile
     *            the validated source file, must not be {@code null}
     * @param destFile
     *            the validated destination file, must not be {@code null}
     * @param preserveFileDate
     *            whether to preserve the file date
     * @throws IOException
     *             if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile,
            boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile
                    + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            IOUtils.copy(fis, fos);
        } finally {
            IOUtils.closeStream(fis);
            IOUtils.closeStream(fos);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '"
                    + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    /**
     * Returns the size of the specified file or directory. If the provided
     * {@link File} is
     * a regular file, then the file's length is returned. If the argument is a
     * directory,
     * then the size of the directory is calculated recursively. If a directory
     * or
     * subdirectory is security restricted, its size will not be included.
     * @param file
     *            the regular file or directory to return the size of (must not
     *            be {@code null}).
     * @return the length of the file, or recursive size of the directory,
     *         provided (in
     *         bytes).
     * @throws NullPointerException
     *             if the file is {@code null}
     * @throws IllegalArgumentException
     *             if the file does not exist.
     */
    public static long sizeOf(File file) {

        if (file == null || !file.exists()) {
            String message = file + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (file.isDirectory()) {
            return sizeOfDirectory(file);
        } else {
            return file.length();
        }

    }

    /**
     * Counts the size of a directory recursively (sum of the length of all
     * files).
     * @param directory
     *            directory to inspect, must not be {@code null}
     * @return size of directory in bytes, 0 if directory is security restricted
     * @throws NullPointerException
     *             if the directory is {@code null}
     */
    public static long sizeOfDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            return 0L;
        }
        for (File file : files) {
            size += sizeOf(file);
        }

        return size;
    }

    /**
     * Strips out the path from a string. If "C:\documents\file.doc", will
     * return
     * "file.doc"; if "file.doc", will return "file.doc"; if "/home/file.doc"
     * will return
     * "file.doc"
     * @param filename
     *            The file name.
     * @return filename stripped down
     */
    public static String stripFilename(String filename) {
        return new File(filename).getName();
    }

    /**
     * Strips out the path and file name from a string. Dot is included in the
     * returned
     * string.
     * @param filename
     *            The file name.
     * @return file extension stripped down
     */
    public static String stripFileExtension(String fileName) {
        final String normalizedFileName = normalizePath(fileName);

        int dotInd = normalizedFileName.lastIndexOf('.');
        int separatorInd = normalizedFileName.lastIndexOf(File.separatorChar);

        // if dot is in the first position,
        // we are dealing with a hidden file rather than an extension
        return (dotInd > 0 && dotInd < normalizedFileName.length() && dotInd > separatorInd) ? normalizedFileName
                .substring(dotInd) : "";
    }

    /**
     * 返回文件的扩展名，如果"abc.txt"，则返回"txt"
     * @param filename
     *            The file name.
     * @return file extension stripped down
     */
    public static String getFileExtension(String fileName) {
        final String normalizedFileName = normalizePath(fileName);

        int dotInd = normalizedFileName.lastIndexOf('.') + 1;
        int separatorInd = normalizedFileName.lastIndexOf(File.separatorChar);

        // if dot is in the first position,
        // we are dealing with a hidden file rather than an extension
        return (dotInd > 0 && dotInd < normalizedFileName.length() && dotInd > separatorInd) ? normalizedFileName
                .substring(dotInd) : "";
    }

    /**
     * Filter the path and replace all illegal separator charater. '/' is not
     * included in
     * the returned string.
     * @param filename
     *            The file name.
     * @return file legal path string
     */
    public static String normalizePath(String path) {
        String normalizedFileName = path.replace('/', File.separatorChar);
        return normalizedFileName.replace('\\', File.separatorChar);
    }

    /**
     * Traverse folder to find specific suffix files returned the list of the
     * files.
     * @author junfengli@yunrang.com (junfeng li)
     * @param path
     *            : The file directory.
     * @param suffix
     *            : The file's suffix.
     * @return loggerList: fit to suffix's files list
     */
    public static List<String> getFileList(String path, String suffix) {
        List<String> loggerList = new LinkedList<String>();
        File dir = new File(path);
        if (dir.exists()) {
            try {
                for (File child : dir.listFiles()) {
                    if (!child.isDirectory()) {
                        String fileSuffix = stripFileExtension(child.getPath());
                        if (fileSuffix.toLowerCase().equals(
                                suffix.toLowerCase())) {
                            loggerList.add(child.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return loggerList;
    }

    /**
     * Traverse folder to find specific suffix files returned the list of the
     * files.
     * @author junfengli@yunrang.com (junfeng li)
     * @param path
     *            : The file directory.
     * @param suffix
     *            : The file's suffix.
     * @return loggerList: fit to suffix's files list
     */
    public static String read(String fileFullName) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileFullName));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(br);
        }
        return sb.toString().trim();
    }

    /**
     * Write file's content to the filePath
     * @param content
     *            :The file content
     * @param filePath
     *            :The file path
     */
    public static void write(String content, String filePath) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(filePath)) {
            throw new NullPointerException("file content or path is empty");
        }
        File saveFile = new File(filePath);
        if (saveFile.exists()) {
            saveFile.delete();
        }
        File parent = new File(saveFile.getParent());
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream outStream = null;
        try {
            saveFile.createNewFile();
            outStream = new FileOutputStream(saveFile);
            outStream.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(outStream);
        }
    }

    public static boolean isGif(File file) {
        try {
            byte[] head = IOUtils
                    .readFileToByteArray(file, 0, FILE_HEAD_LENGTH);
            return isGif(head);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isGif(byte[] head) {
        if (head == null || head.length < 6) {
            return false;
        }
        return 'G' == head[0] && 'I' == head[1] && 'F' == head[2]
                && '8' == head[3] && ('7' == head[4] || '9' == head[4])
                && 'a' == head[5];
    }

    /**
     * @param size
     *            file size in byte
     * @return file size in string format
     */
    public static String formatFileSize(long size) {
        String FileSize = "";
        if (size >= 0) {
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
            // larger than 0.5GB
            if (size >= 536870912) {
                FileSize = df.format(size * 1.0 / 1073741824) + "GB"; // GB
            }
            // larger than 0.5MB to 0.5GB
            else if (size >= 524288) {
                FileSize = df.format(size * 1.0 / 1048576) + "MB";
            }
            // larger than 0.5KB to 0.5MB
            else if (size >= 512) {
                FileSize = df.format(size * 1.0 / 1024) + "KB";
            } else {
                FileSize = size + "B";
            }
        }
        return FileSize;
    }

    /*
     * when sdcard is full , delete image cache file in new thread.
     */
    public static void deleteFileInThread(final File file) {
        ThreadUtils.startRunInThread(new Runnable() {
            @Override
            public void run() {
                try {
                    isDeleteing = true;
                    if (file.exists()) {
                        if (file.isFile()) {
                            file.delete();
                        } else if (file.isDirectory()) {
                            File files[] = file.listFiles();
                            if (files != null) {
                                for (int i = 0; i < files.length; i++) {
                                    deleteFile(files[i]);
                                }
                            }
                        }
                        file.delete();
                    } else {
                        // FIXME file not exist
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isDeleteing = false;
                }
            }
        });
    }

    /*
     * delete file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        deleteFile(files[i]);
                    }
                }
            }
            file.delete();
        } else {
            // FIXME file not exist
        }
    }

    /**
     * Returns a boolean indicating whether this file can be found on cache
     * @param context
     * @param fileName
     * @return
     */
    public static boolean isFileExistInCache(Context context, String fileName) {
        File file = getExternalCacheFile(context, fileName);
        if (file != null && file.exists()) {
            return true;
        }
        file = getInternalCacheFile(context, fileName);
        return file != null && file.exists();
    }

    /**
     * Create a file by given file path.
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Create a dir by given file path.
     * @param dirPath
     * @return
     */
    public static boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        return dir.exists() || dir.mkdir();

    }

    /**
     * get SDCard Spare Quantity
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getExternalStorageSpareQuantity() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            // 为外部存储流出2M余量
            return blockSize * availCount - STORAGE_MARGIN_SIZE;
        }
        return -1;
    }

    /**
     * get Data Spare Quantity
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getDataSpareQuantity() {
        File data = Environment.getDataDirectory();
        StatFs sf = new StatFs(data.getAbsolutePath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        // 为内部存储流出2M余量
        return blockSize * availCount - STORAGE_MARGIN_SIZE;
    }

}
