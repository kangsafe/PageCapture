package com.ks.service.pagecapture;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.List;

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
        WebDriver driver = new ChromeDriver();//new FirefoxDriver();
        List<Attachment> attachments = null;
        while (true) {
            attachments = dao.getAttachList(1000);
            if (attachments == null || attachments.size() == 0) {
                System.out.println("暂无网页剪藏，程序休眠10s...");
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                for (Attachment m : attachments
                        ) {
                    String pageUrl = m.getAttach_path();
                    String path = Config.rootImg + m.getAttach_path();
                    String content = "";
                    System.out.println("网页剪藏开始");
                    System.out.println("笔记id:" + m.getAttach_id());
                    try {
                        driver.get(pageUrl);
                        System.out.println("网页标题:" + driver.getTitle() + " 地址:" + driver.getCurrentUrl());
                        String str = driver.getPageSource();
                        content = str.replaceAll(Utils.regEx_html, "");
                        List<WebElement> cssList = driver.findElements(By.tagName("link"));
                        List<WebElement> imageList = driver.findElements(By.tagName("img"));
                        getResource(str, path, Utils.getHostByUrl(pageUrl), imageList, cssList);
                    } catch (Exception e) {
                        System.out.println("剪藏失败");
                        e.printStackTrace();
                        //Close the browser
                        driver.quit();
                        driver = new ChromeDriver();
                    } finally {
                        dao.setAttach("", "");
                    }
                    System.out.println("结束识别附件id:" + m.getAttach_id());
                }
                attachments.clear();
            }
        }
    }

    public static void getResource(String html, String path, String host, List<WebElement> imageList, List<WebElement> cssList) {
        FileWriter writer = null;
        try {
            String str = html.replaceAll(Utils.regEx_script, "");
            //获取css资源
            for (WebElement css : cssList
                    ) {
                String href = css.getAttribute("href");
                String filename = Utils.downLoadFromUrl(href, path + "index_files\\", "css");
                str = str.replace(href.replace(host, ""), "index_files/" + filename);
            }
            //获取图片资源
            for (WebElement img : imageList
                    ) {
                String href = img.getAttribute("src");
                String filename = Utils.downLoadFromUrl(href, path + "index_files\\", "png|jpg|gif");
                str = str.replace(href.replace(host, ""), "index_files/" + filename);
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
