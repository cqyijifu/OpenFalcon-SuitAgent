package com.yiji.falcon.agent.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ZipUtil {

    /**
     * zip压缩文件
     *
     * @param dir
     * @param zippath
     */
    public static void zip(String dir, String zippath) {
        List<String> paths = getFiles(dir);
        compressFilesZip(paths.toArray(new String[paths.size()]), zippath, dir);
    }

    /**
     * 递归取到当前目录所有文件
     *
     * @param dir
     * @return
     */
    private static List<String> getFiles(String dir) {
        List<String> lstFiles = new ArrayList<>();
        File file = new File(dir);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    lstFiles.add(f.getAbsolutePath());
                    lstFiles.addAll(getFiles(f.getAbsolutePath()));
                } else {
                    String str = f.getAbsolutePath();
                    lstFiles.add(str);
                }
            }
        }
        return lstFiles;
    }

    /**
     * 文件名处理
     *
     * @param dir
     * @param path
     * @return
     */
    private static String getFilePathName(String dir, String path) {
        String p = path.replace(dir + File.separator, "");
        p = p.replace("\\", "/");
        return p;
    }

    /**
     * 把文件压缩成zip格式
     *
     * @param files       需要压缩的文件
     * @param zipFilePath 压缩后的zip文件路径
     */
    private static void compressFilesZip(String[] files, String zipFilePath, String dir) {
        if (files == null || files.length <= 0) {
            return;
        }
        ZipArchiveOutputStream zaos = null;
        try {
            File zipFile = new File(zipFilePath);
            zaos = new ZipArchiveOutputStream(zipFile);
            zaos.setUseZip64(Zip64Mode.AsNeeded);
            //将每个文件用ZipArchiveEntry封装  
            //再用ZipArchiveOutputStream写到压缩文件中  
            for (String strfile : files) {
                File file = new File(strfile);
                String name = getFilePathName(dir, strfile);
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, name);
                zaos.putArchiveEntry(zipArchiveEntry);
                if (file.isDirectory()) {
                    continue;
                }
                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(file));
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        //把缓冲区的字节写入到ZipArchiveEntry
                        zaos.write(buffer, 0, len);
                    }
                    zaos.closeArchiveEntry();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (is != null)
                        is.close();
                }

            }
            zaos.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (zaos != null) {
                    zaos.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    /**
     * 把zip文件解压到指定的文件夹
     *
     * @param zipFilePath zip文件路径
     * @param saveFileDir 解压后的文件存放路径
     */
    public static void unzip(String zipFilePath, String saveFileDir) {
        if (!saveFileDir.endsWith("\\") && !saveFileDir.endsWith("/")) {
            saveFileDir += File.separator;
        }
        File dir = new File(saveFileDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("目录创建失败");
            }
        }
        File file = new File(zipFilePath);
        if (file.exists()) {
            InputStream is = null;
            ZipArchiveInputStream zais = null;
            try {
                is = new FileInputStream(file);
                zais = new ZipArchiveInputStream(is);
                ArchiveEntry archiveEntry = null;
                while ((archiveEntry = zais.getNextEntry()) != null) {
                    // 获取文件名  
                    String entryFileName = archiveEntry.getName();
                    // 构造解压出来的文件存放路径  
                    String entryFilePath = saveFileDir + entryFileName;
                    OutputStream os = null;
                    try {
                        // 把解压出来的文件写到指定路径  
                        File entryFile = new File(entryFilePath);
                        if (entryFileName.endsWith("/")) {
                            if (!entryFile.mkdirs()) {
                                throw new RuntimeException("目录创建失败");
                            }
                        } else {
                            os = new BufferedOutputStream(new FileOutputStream(
                                    entryFile));
                            byte[] buffer = new byte[1024];
                            int len = -1;
                            while ((len = zais.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                        }
                    } catch (IOException e) {
                        throw new IOException(e);
                    } finally {
                        if (os != null) {
                            os.flush();
                            os.close();
                        }
                    }

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (zais != null) {
                        zais.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}