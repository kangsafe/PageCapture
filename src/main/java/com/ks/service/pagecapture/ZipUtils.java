package com.ks.service.pagecapture;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

/**
 * Created by Admin on 2017/4/6 0006.
 */
public class ZipUtils {
    private static final int BUFFEREDSIZE = 1024;

    /**
     * 解压zip或者rar包的内容到指定的目录下，可以处理其文件夹下包含子文件夹的情况
     *
     * @param zipFilename     要解压的zip或者rar包文件
     * @param outputDirectory 解压后存放的目录
     */
    public static void unzip(String zipFilename, String outputDirectory) throws IOException {
        File outFile = new File(outputDirectory);
        if (!outFile.exists()) {
            outFile.mkdirs();
        }
        ZipFile zipFile = new ZipFile(zipFilename);
        Enumeration en = zipFile.entries();
        ZipEntry zipEntry = null;
        while (en.hasMoreElements()) {
            zipEntry = (ZipEntry) en.nextElement();
            if (zipEntry.isDirectory()) {
                // mkdir directory
                String dirName = zipEntry.getName();
                // System.out.println("=dirName is:=" + dirName + "=end=");
                dirName = dirName.substring(0, dirName.length() - 1);
                File f = new File(outFile.getPath() + File.separator + dirName);
                f.mkdirs();
            } else {
                // unzip file
                String strFilePath = outFile.getPath() + File.separator + zipEntry.getName();
                File f = new File(strFilePath);
                // the codes remedified by can_do on 2010-07-02 =begin=
                // /////begin/////
                // 判断文件不存在的话，就创建该文件所在文件夹的目录
                if (!f.exists()) {
                    // String[] arrFolderName = zipEntry.getName().split("/");
                    // String strRealFolder = "";
                    // for (int i = 0; i < (arrFolderName.length - 1); i++) {
                    // strRealFolder += arrFolderName[i] + File.separator;
                    // }
                    // strRealFolder = outFile.getPath() + File.separator +
                    // strRealFolder;
                    // File tempDir = new File(strRealFolder);
                    // // 此处使用.mkdirs()方法，而不能用.mkdir()
                    // tempDir.mkdirs();
                    f.getParentFile().mkdirs();
                }
                ////// end///
                // the codes remedified by can_do on 2010-07-02 =end=
                f.createNewFile();
                InputStream in = zipFile.getInputStream(zipEntry);
                FileOutputStream out = new FileOutputStream(f);
                try {
                    int c;
                    byte[] by = new byte[BUFFEREDSIZE];
                    while ((c = in.read(by)) != -1) {
                        out.write(by, 0, c);
                    }
                    // out.flush();
                } catch (IOException e) {
                    throw e;
                } finally {
                    out.close();
                    in.close();
                }
            }
        }
    }

    /**
     * APDPlat中的重要打包机制 将jar文件中的某个文件夹里面的内容复制到某个文件夹
     *
     * @param jar    包含静态资源的jar包
     * @param subDir jar中包含待复制静态资源的文件夹名称
     * @param loc    静态资源复制到的目标文件夹
     * @param force  目标静态资源存在的时候是否强制覆盖
     */
    public static void unZip(String jar, String subDir, String loc, boolean force) {
        try {
            File base = new File(loc);
            if (!base.exists()) {
                base.mkdirs();
            }

            ZipFile zip = new ZipFile(new File(jar));
            Enumeration<? extends ZipEntry> entrys = zip.entries();
            while (entrys.hasMoreElements()) {
                ZipEntry entry = entrys.nextElement();
                String name = entry.getName();
                if (!name.startsWith(subDir)) {
                    continue;
                }
                // 去掉subDir
                name = name.replace(subDir, "").trim();
                if (name.length() < 2) {
                    // log.debug(name+" 长度 < 2");
                    continue;
                }
                if (entry.isDirectory()) {
                    File dir = new File(base, name);
                    if (!dir.exists()) {
                        dir.mkdirs();
                        // log.debug("创建目录");
                    } else {
                        // log.debug("目录已经存在");
                    }
                    // log.debug(name+" 是目录");
                } else {
                    File file = new File(base, name);
                    if (file.exists() && force) {
                        file.delete();
                    }
                    if (!file.exists()) {
                        InputStream in = zip.getInputStream(entry);
                        // FileUtils.copyFile(in,file);
                        // log.debug("创建文件");
                    } else {
                        // log.debug("文件已经存在");
                    }
                    // log.debug(name+" 不是目录");
                }
            }
        } catch (ZipException ex) {
            // log.error("文件解压失败",ex);
        } catch (IOException ex) {
            // log.error("文件操作失败",ex);
        }
    }

    /**
     * 创建ZIP文件
     *
     * @param sourcePath 文件或文件夹路径
     * @param zipPath    生成的zip文件存在路径（包括文件名）
     */
    public static void createZip(String sourcePath, String zipPath, String parentPath) {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            File file = new File(zipPath);// 存放照片的文件
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(zipPath);
            zos = new ZipOutputStream(fos);
            writeZip(new File(sourcePath), parentPath, zos);
        } catch (FileNotFoundException e) {
            System.out.print("创建ZIP文件失败" + e.getMessage());
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                System.out.print("创建ZIP文件失败" + e.getMessage());
            }

        }
    }

    private static void writeZip(File file, String parentPath, ZipOutputStream zos) {
        if (file.exists()) {
            if (file.isDirectory()) {// 处理文件夹
                if (parentPath.isEmpty()) {
                    parentPath += File.separator + file.getName();
                }
                File[] files = file.listFiles();
                for (File f : files) {
                    writeZip(f, parentPath, zos);
                }
            } else {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    ZipEntry ze = new ZipEntry(parentPath + file.getName());
                    zos.putNextEntry(ze);
                    byte[] content = new byte[1024];
                    int len;
                    while ((len = fis.read(content)) != -1) {
                        zos.write(content, 0, len);
                        zos.flush();
                    }

                } catch (FileNotFoundException e) {
                    System.out.print("创建ZIP文件失败" + e.getMessage());
                } catch (IOException e) {
                    System.out.print("创建ZIP文件失败" + e.getMessage());
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                        }
                    } catch (IOException e) {
                        System.out.print("创建ZIP文件失败" + e.getMessage());
                    }
                }
            }
        }
    }


    public static final String EXT = ".zip";
    private static final String BASE_DIR = "";

    // 符号"/"用来作为目录标识判断符
    private static final String PATH = "/";
    private static final int BUFFER = 1024;

    /**
     * 压缩
     *
     * @param srcFile
     * @throws Exception
     */
    public static void compress(File srcFile) throws Exception {
        String name = srcFile.getName();
        String basePath = srcFile.getParent();
        String destPath = basePath + name + EXT;
        compress(srcFile, destPath);
    }

    /**
     * 压缩
     *
     * @param srcFile  源路径
     * @param destPath 目标路径
     * @throws Exception
     */
    public static void compress(File srcFile, File destFile) throws Exception {

        // 对输出文件做CRC32校验
        CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(
                destFile), new CRC32());

        ZipOutputStream zos = new ZipOutputStream(cos);

        compress(srcFile, zos, BASE_DIR);

        zos.flush();
        zos.close();
    }

    /**
     * 压缩文件
     *
     * @param srcFile
     * @param destPath
     * @throws Exception
     */
    public static void compress(File srcFile, String destPath) throws Exception {
        compress(srcFile, new File(destPath));
    }

    /**
     * 压缩
     *
     * @param srcFile  源路径
     * @param zos      ZipOutputStream
     * @param basePath 压缩包内相对路径
     * @throws Exception
     */
    private static void compress(File srcFile, ZipOutputStream zos,
                                 String basePath) throws Exception {
        if (srcFile.isDirectory()) {
            compressDir(srcFile, zos, basePath);
        } else {
            compressFile(srcFile, zos, basePath);
        }
    }

    /**
     * 压缩
     *
     * @param srcPath
     * @throws Exception
     */
    public static void compress(String srcPath) throws Exception {
        File srcFile = new File(srcPath);

        compress(srcFile);
    }

    /**
     * 文件压缩
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     */
    public static void compress(String srcPath, String destPath)
            throws Exception {
        File srcFile = new File(srcPath);

        compress(srcFile, destPath);
    }

    /**
     * 压缩目录
     *
     * @param dir
     * @param zos
     * @param basePath
     * @throws Exception
     */
    private static void compressDir(File dir, ZipOutputStream zos,
                                    String basePath) throws Exception {

        File[] files = dir.listFiles();

        // 构建空目录
        if (files.length < 1) {
            ZipEntry entry = new ZipEntry(basePath + dir.getName() + PATH);

            zos.putNextEntry(entry);
            zos.closeEntry();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归压缩
                compress(file, zos, basePath + file.getName() + PATH);
            } else {
                compress(file, zos, basePath);
            }
        }
    }

    /**
     * 文件压缩
     *
     * @param file 待压缩文件
     * @param zos  ZipOutputStream
     * @param dir  压缩文件中的当前路径
     * @throws Exception
     */
    private static void compressFile(File file, ZipOutputStream zos, String dir)
            throws Exception {

        /**
         * 压缩包内文件名定义
         *
         * <pre>
         * 如果有多级目录，那么这里就需要给出包含目录的文件名
         * 如果用WinRAR打开压缩包，中文名将显示为乱码
         * </pre>
         */
        ZipEntry entry = new ZipEntry(dir + file.getName());

        zos.putNextEntry(entry);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = bis.read(data, 0, BUFFER)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();

        zos.closeEntry();
    }

}
