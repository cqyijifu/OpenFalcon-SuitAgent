/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
package com.yiji.falcon.agent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
 * 修订记录:
 * guqiu@yiji.com 2016-06-22 17:48 创建
 */

/**
 * @author guqiu@yiji.com
 */
public class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 判断传入的文件是否存在
     * @param filePath
     * @return
     * 文件不存在或是一个目录都会返回false
     */
    public static boolean isExist(String filePath){
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

	/**
	 * 获取文本文件的文本内容 若没有此文件，返回空串
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String getTextFileContent(String fileName){
        StringBuilder text = new StringBuilder();
		File f = new File(fileName);
		if (!f.exists()) {
            log.error("文件{}不存在",fileName);
            return "";
		}
		try {
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader read = new InputStreamReader(fis,"UTF-8");
            BufferedReader reader = new BufferedReader(read);
            String line = "";
            while((line = reader.readLine()) != null){
                text.append(line).append("\n");
            }
            reader.close();
		} catch (Exception e) {
            log.error("文件读取失败，请检查是否有文件读取权限，或指定文件是否损坏等",e);
		}
		return text.toString();
	}

	/**
	 * 获取制定路径下的所有文件中的文本
	 * 如果不存在则创建传入的路径
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAllFileTextFromDir(String path){
		List<String> filesText = new ArrayList<String>();
		File f = new File(path);
		if (!f.exists()) {
			if(!f.mkdirs()){
                log.warn("目录创建失败");
            }
		}
		File[] fs = f.listFiles();
        if(fs == null){
            return new ArrayList<>();
        }
		try {
			for (File file : fs) {
                StringBuilder text = new StringBuilder();
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader read = new InputStreamReader(fis,"UTF-8");
                BufferedReader reader = new BufferedReader(read);
                String line = "";
                while((line = reader.readLine()) != null){
                    text.append(line).append("\n");
                }
				filesText.add(text.toString());
				reader.close();
			}
		} catch (Exception e) {
            log.error("",e);
            return new ArrayList<>();
		}
		return filesText;
	}

	/**
	 * 获取传入的路径下的文件的文件内容
	 * 如果文件不存在，将自动根据路径及文件名创建一个新的，返回空串
	 * @param path
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static String getFileTextFromDirFile(String path, String fileName){
		StringBuilder text = new StringBuilder();
		File f = new File(path);
		if (!f.exists()) {
            if(!f.mkdirs()){
                log.warn("目录创建失败");
            }
		}
		f = new File(path + File.separator + fileName);
		if(!f.exists()){
			try {
				if(!f.createNewFile()){
                    log.warn("文件已存在");
                }
			} catch (IOException e) {
                log.error("文件读取失败，请检查是否有文件读取权限，或指定文件是否损坏等",e);
			}
		}
		try {
			FileInputStream fis = new FileInputStream(f);
            InputStreamReader read = new InputStreamReader(fis,"UTF-8");
            BufferedReader reader = new BufferedReader(read);
            String line = "";
            while((line = reader.readLine()) != null){
                text.append(line).append("\n");
            }
			reader.close();
		} catch (Exception e) {
            log.error("",e);
            return "文件读取失败，请检查是否有文件读取权限，或指定文件是否损坏等";
		}
		return text.toString();
	}

	/**
	 * 将指定的字符串写入指定路径下的指定文件中
	 * 如果路径及文件不存在，将自动创建
	 * @param text
	 * @param path
	 * @param fileName
	 * @param append : true为在文件后追加内容
	 * @return
	 */
	public static boolean writeTextToTextFile(String text,String path, String fileName,boolean append) {
		File f = new File(path);
		if(!f.exists()){
            if(!f.mkdirs()){
                log.warn("目录创建失败");
            }
		}
		f = new File(path + File.separator + fileName);
		if(!f.exists()){
			try {
				if(!f.createNewFile()){
                    log.warn("文件创建失败");
                    return false;
                }
			} catch (IOException e) {
                log.error("文件创建异常",e);
				return false;
			}
		}
		FileOutputStream fos;
		try {
            fos = new FileOutputStream(f, append);
            OutputStreamWriter write = new OutputStreamWriter(fos,"UTF-8");
            BufferedWriter writer = new BufferedWriter(write);
			writer.write(text);
			writer.close();
			return true;
		} catch (Exception e) {
            log.error("",e);
			return false;
		}
    }

    /**
     * 将指定的字符串写入指定路径下的指定文件中
     * 如果文件不存在，返回false
     * @param text
     * @param file
     * @param append : true为在文件后追加内容
     * @return
     */
    public static boolean writeTextToTextFile(String text,File file,boolean append) {
        if(!file.exists()){
            return false;
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file, append);
            OutputStreamWriter write = new OutputStreamWriter(fos,"UTF-8");
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(text);
            writer.close();
            return true;
        } catch (Exception e) {
            log.error("",e);
            return false;
        }
    }

    /**
     * InputStream输入流转换为File
     * @param ins
     * @param file
     */
    public static boolean inputStreamToFile(InputStream ins,File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
            return true;
        } catch (Exception e) {
            log.error("",e);
            return false;
        }
    }

    /**
     * 遍历获取指定路径下的所有文件绝对路径
     * 如果不存在则创建传入的路径
     * @param path
     * @return
     * @throws Exception
     */
    public static List<String> getAllFileNamesFromDir(String path){
        List<String> fileNames = new ArrayList<>();
        File dir = new File(path);
        if (!dir.exists()) {
            if(!dir.mkdirs()){
                log.warn("目录创建失败");
            }
        }
        File[] fs = dir.listFiles();
        if(fs == null){
            return new ArrayList<>();
        }
        try {
            for (File file : fs) {
                appendFileNames(file.getPath(),fileNames);
            }
        } catch (Exception e) {
            log.error("获取文件名时发生异常",e);
            return new ArrayList<>();
        }
        return fileNames;
    }

    private static void appendFileNames(String dir,List<String> fileNames){
        File file = new File(dir);
        if(file.isFile()){
            fileNames.add(file.getPath());
        }else {
            File[] fs = file.listFiles();
            if(fs != null){
                for (File f : fs) {
                    if(f.isDirectory()){
                        appendFileNames(f.getPath(),fileNames);
                    }else if(f.isFile()){
                        fileNames.add(f.getPath());
                    }else {
                        log.warn("发现一文件即非目录也非文件：" + f.getPath());
                    }
                }
            }
        }
    }

}
