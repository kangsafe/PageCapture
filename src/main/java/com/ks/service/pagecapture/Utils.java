package com.ks.service.pagecapture;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/18.
 */
public class Utils {
    public static String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
    //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
    public static String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
    //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
    public static String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param savePath
     * @throws IOException
     */
    public static String downLoadFromUrl(String urlStr, String savePath, String reg) throws IOException {
        String pageUrl = urlStr;
        String fileName = "";
        try {
            URL url = new URL(pageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            if (conn.getResponseCode() == 301) {
                pageUrl = conn.getHeaderField("Location");
                url = new URL(pageUrl);
                conn = (HttpURLConnection) url.openConnection();
                //设置超时间为3秒
                conn.setConnectTimeout(3 * 1000);
                //防止屏蔽程序抓取而返回403错误
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            }

            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] getData = readInputStream(inputStream);

            //文件保存位置
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            fileName = getRealDiskFileNameByUrl(pageUrl, savePath, reg);
            File file = new File(saveDir + File.separator + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (ExportException e) {
            e.printStackTrace();
        }
        System.out.println("info:" + pageUrl + " download success");
        return fileName;
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static boolean saveUrlAs(String photoUrl, String fileName) {
        //此方法只能用户HTTP协议
        try {
            URL url = new URL(photoUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDocumentAt(String urlString) {
        //此方法兼容HTTP和FTP协议
        StringBuffer document = new StringBuffer();
        URL url = null;
        BufferedReader reader = null;
        URLConnection conn = null;
        try {
            url = new URL(urlString);
            conn = url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            Map<String, List<String>> map = conn.getRequestProperties();//conn.getHeaderFields();
            for (String key : map.keySet()
                    ) {
                System.out.println(key + ":" + map.get(key).toString());
            }
            reader = new BufferedReader(new InputStreamReader(conn.
                    getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                document.append(line + "/n");
            }
            reader.close();
        } catch (MalformedURLException e) {
            System.out.println("Unable to connect to URL: " + urlString);
        } catch (IOException e) {
            System.out.println("IOException when connecting to URL: " + urlString);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            conn = null;
            url = null;
        }
        return document.toString();
    }

    public static String getFileNameByUrl(String url, String reg) {
        String filename = "";
        try {
            Pattern pattern = null;
            if (reg == null || reg.length() == 0) {
                pattern = Pattern.compile("\\w*\\.(css|png|jpg|gif)+");
            } else {
                pattern = Pattern.compile(reg);
            }
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                filename = matcher.group();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filename;
    }

    public static String getNameByFilename(String filename) {
        if (filename.lastIndexOf(".") > 0) {
            return filename.substring(0, filename.lastIndexOf("."));
        } else {
            return filename;
        }
    }

    public static String getExtByFilename(String filename) {
        if (filename.lastIndexOf(".") > 0) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static String getRealDiskFileNameByUrl(String url, String path, String reg) {
        String filename = "";
        try {
            filename = getFileNameByUrl(url, reg);
            int i = 1;
            while (new File(path, filename).exists()) {
                filename = getNameByFilename(filename) + i + "." + getExtByFilename(filename);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filename.length() == 0) {
            filename = System.currentTimeMillis() + "";
        }
        return filename;
    }

    public static String getHostByUrl(String url) {
        String host = "";
        try {
            String temp = url.substring(url.indexOf("://") + 3);

            if (temp.lastIndexOf("/") > 0) {
                host = url.substring(0, url.indexOf(":")) + "://" + temp.substring(0, temp.indexOf("/"));
            } else {
                host = url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return host;
    }

    public static void initConfig(String filename) {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            p.load(in);
            in.close();
            if (p.containsKey("driver")) {
                Config.driver = p.getProperty("driver");
            }
            if (p.containsKey("url")) {
                Config.url = p.getProperty("url");
            }
            if (p.containsKey("username")) {
                Config.username = p.getProperty("username");
            }
            if (p.containsKey("password")) {
                Config.password = p.getProperty("password");
            }
            if (p.containsKey("exePath")) {
                Config.exePath = p.getProperty("exePath");
            }
            if (p.containsKey("rootImg")) {
                Config.rootImg = p.getProperty("rootImg");
            }
            if (p.containsKey("lang")) {
                Config.lang = p.getProperty("lang");
            }
            if (p.containsKey("webdriver")) {
                Config.webdriver = p.getProperty("webdriver");
            }
            if (p.containsKey("webdriver_path")) {
                Config.webdriver_path = p.getProperty("webdriver_path");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
