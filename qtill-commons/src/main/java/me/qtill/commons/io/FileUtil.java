package me.qtill.commons.io;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import me.qtill.commons.text.StringUtil;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * 文件相关工具类
 *
 * ref: https://www.jianshu.com/p/8eb773f8e552
 *
 * @author paranoidq
 * @since 1.0.0
 */
public final class FileUtil {

    private FileUtil() {
        throw new UnsupportedOperationException("Must not instantiate");
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static File getFileByPath(String filePath) {
        Preconditions.checkArgument(StringUtil.isNotEmpty(filePath));
        return new File(filePath);
    }


    /**
     * 判断文件或目录是否存在
     *
     * @param file
     * @return
     */
    public static boolean isExists(File file) {
        Preconditions.checkNotNull(file, "Input argument [file] is null");
        return file.exists();
    }

    /**
     * @see #isExists(File)
     *
     * @param filePath
     * @return
     */
    public static boolean isExists(String filePath) {
        return isExists(getFileByPath(filePath));
    }

    /**
     * 判断是否是目录
     *
     * @param file
     * @return
     */
    public static boolean isDirectory(File file) {
        Preconditions.checkArgument(isExists(file), "File/Directory is not existed");
        return file.isDirectory();
    }

    /**
     * @see #isDirectory(File)
     *
     * @param path
     * @return
     */
    public static boolean isDirectory(String path) {
        return isDirectory(getFileByPath(path));
    }

    /**
     * 判断是否是文件
     *
     * @param file
     * @return
     */
    public static boolean isFile(File file) {
        Preconditions.checkArgument(isExists(file), "File/Directory  is not existed");
        return file.isFile();
    }


    /**
     * @see #isFile(File)
     *
     * @param path
     * @return
     */
    public static boolean isFile(String path) {
        return isFile(getFileByPath(path));
    }


    /**
     * 判断目录是否存在，不存在则创建
     * 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
     *
     * @param file
     * @return
     */
    public static boolean createDirIfNotExist(File file) {
        Preconditions.checkNotNull(file);
        return isExists(file) ? isDirectory(file) : file.mkdirs();
    }


    /**
     * @see #createFileIfNotExist(File)
     *
     * @param path
     * @return
     */
    public static boolean createDirIfNotExist(String path) {
        return createDirIfNotExist(getFileByPath(path));
    }


    /**
     * 判断文件是否存在，不存在则创建
     *
     * 如果存在，是文件则返回true，是目录则返回false
     * 不存在则先创建上级目录，创建成功返回true，然后创建文件，创建成功返回true
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean createFileIfNotExist(File file) {
        Preconditions.checkNotNull(file);
        if (isExists(file)) {
            return isFile(file);
        }
        if (!createDirIfNotExist(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @see #createFileIfNotExist(File)
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static boolean createFileIfNotExist(String path) {
        return createFileIfNotExist(getFileByPath(path));
    }


    /**
     * 创建文件，并覆盖原有文件（如果存在）
     * @param file
     * @return
     */
    public static boolean createFileForce(File file) throws IOException {
        Preconditions.checkNotNull(file);
        // 文件存在并删除失败，则返回false
        if (isExists(file) && isFile(file) && deleteFile(file)) {
            return false;
        }
        return createFileIfNotExist(file);
    }

    /**
     * @see #createFileForce(File)
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static boolean createFileForce(String path) throws IOException {
        return createFileForce(getFileByPath(path));
    }


    /**
     * 复制目录
     *
     * @param src
     * @param des
     * @throws IOException
     */
    public static boolean copyDir(File src, File dest, boolean preserveFileDate) {
        try {
            FileUtils.copyDirectory(src, dest, preserveFileDate);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * @see #copyDir(File, File, boolean)
     *
     * @param src
     * @param dest
     * @param preserveFileDate
     * @return
     */
    public static boolean copyDir(String src, String dest, boolean preserveFileDate) {
        return copyDir(getFileByPath(src), getFileByPath(dest), preserveFileDate);
    }


    /**
     * 移动目录到另一个目录下
     *
     * @param src
     * @param dest
     * @param createIfNotExist 是否创建目标目录
     * @return
     */
    public static boolean moveDir(File src, File dest, boolean createDestDir) {
        try {
            FileUtils.moveDirectoryToDirectory(src, dest, createDestDir);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @see #moveDir(File, File, boolean)
     *
     * @param src
     * @param dest
     * @param createDestDir
     * @return
     */
    public static boolean moveDir(String src, String dest, boolean createDestDir) {
        return moveDir(getFileByPath(src), getFileByPath(dest), createDestDir);
    }


    /**
     * 删除文件
     *
     * 如果不存在，则返回true
     * 如果存在，并且是文件，则返回是否删除成功
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        Preconditions.checkNotNull(file);
        return !isExists(file) || (isFile(file) && file.delete());
    }


    /**
     * @see #deleteFile(File)
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        return deleteFile(getFileByPath(path));
    }


    /**
     * 重命名文件
     *
     * @param file
     * @param newName
     * @return
     */
    public static boolean renameFile(File file, String newName) {
        Preconditions.checkNotNull(file);
        try {
            Files.move(file, getFileByPath(newName));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }





}
