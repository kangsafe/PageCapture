package com.ks.service.pagecapture;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/3/30.
 */
public class Main {
    private static AttachDao dao = new AttachDao();

    public static void main(String[] args) {
        //配置文件
        String configFilename = (args == null || args.length == 0) ? "server.properties" : args[0];
        if (new File(configFilename).exists()) {
            Utils.initConfig(configFilename);
        }
        //初始化webdriver配置
        System.getProperties().setProperty(Config.webdriver, Config.webdriver_path);
        System.out.println("网页剪藏程序开始运行...");
        WebDriver driver = null;
        if (Config.webdriver.indexOf("chrome") > 0) {
            driver = new ChromeDriver();//new FirefoxDriver();
        } else if (Config.webdriver.indexOf("ie") > 0) {
            driver = new InternetExplorerDriver();
        }
//        WebDriverWait wait = new WebDriverWait(driver, 10);
//        wait.withTimeout(10, TimeUnit.SECONDS);
        //设置10秒
//        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        List<NoteModel> attachments = null;
        while (true) {
            attachments = dao.getNoteList(100);
            if (attachments == null || attachments.size() == 0) {
                System.out.println("暂无网页剪藏，程序休眠10s...");
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                for (NoteModel m : attachments
                        ) {
                    String pageUrl = m.getUrl();
                    String path = Config.root_page_path;
                    String path2 = Config.root_page_path_other;
                    if (m.getGroup_id() == null || m.getGroup_id().length() == 0) {
                        path += File.separator + m.getUser_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                        path2 += File.separator + m.getUser_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                    } else {
                        path += File.separator + m.getGroup_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                        path2 += File.separator + m.getGroup_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                    }
                    String content = "";
                    System.out.println("网页剪藏开始");
                    System.out.println("笔记id:" + m.getNote_id());
                    try {
                        driver.get(pageUrl);
                        System.out.println("网页标题:" + driver.getTitle() + " 地址:" + driver.getCurrentUrl());
                        String str = driver.getPageSource();
                        content = str.replaceAll(Utils.regEx_html, "");
                        List<WebElement> cssList = driver.findElements(By.tagName("link"));
                        List<WebElement> imageList = driver.findElements(By.tagName("img"));
                        getResource(str, path, imageList, cssList);
                        String zipFileName = m.getNote_ls_id();
                        // 压缩文件
//                        ZipUtils.createZip(path, path + ".zip", "");
                        ZipUtils.compress(path, path + ".zip");
                        FileUtils.copyDirectory(new File(path), new File(path2));
                        FileUtils.copyFile(new File(path + ".zip"), new File(path2 + ".zip"));
                    } catch (Exception e) {
                        System.out.println("剪藏失败");
                        e.printStackTrace();
                        //Close the browser
                        driver.quit();
                        driver = new ChromeDriver();
                    } finally {
                        dao.setUrl(m.getNote_ls_id(), m.getNote_id(), content);
                    }
                    System.out.println("结束网页剪藏:" + m.getNote_id());
                }
                attachments.clear();
            }
        }
    }

    public static void getResource(String html, String path, List<WebElement> imageList, List<WebElement> cssList) {
        FileWriter writer = null;
        try {
            String str = html.replaceAll(Utils.regEx_script, "");
            //获取css资源
            for (WebElement css : cssList
                    ) {
                String href = css.getAttribute("href");
                String filename = Utils.downLoadFromUrl(href, path + "\\index_files\\", "css");
                if (filename != null && filename.length() > 0) {
                    str = str.replace(href, "index_files/" + filename);
                }
            }
            //获取图片资源
            for (WebElement img : imageList
                    ) {
                String href = img.getAttribute("src");
                String filename = Utils.downLoadFromUrl(href, path + "\\index_files\\", "png|jpg|gif");
                if (filename != null && filename.length() > 0) {
                    str = str.replace(href, "index_files/" + filename);
                }
            }

            File htmlFile = new File(path, "index.html");
            if (!htmlFile.exists()) {
                htmlFile.getParentFile().mkdirs();
            }
            writer = new FileWriter(htmlFile);
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
