package com.ks.service.pagecapture;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2017/3/30.
 */
public class Main {
    private static AttachDao dao = new AttachDao();
    private static WebDriver driver = null;
    private static JavascriptExecutor js = null;

    public static void main(String[] args) {
        //配置文件
        String configFilename = (args == null || args.length == 0) ? "server.properties" : args[0];
        if (new File(configFilename).exists()) {
            Utils.initConfig(configFilename);
        }
        //初始化webdriver配置
        System.getProperties().setProperty(Config.webdriver, Config.webdriver_path);
        System.out.println("网页剪藏程序开始运行...");

        if (Config.webdriver.indexOf("chrome") > 0) {
            driver = new ChromeDriver();//new FirefoxDriver();
        } else if (Config.webdriver.indexOf("ie") > 0) {
            driver = new InternetExplorerDriver();
        }
        js = (JavascriptExecutor) driver;
        driver.manage().window().setSize(new Dimension(414, 736));
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
                    //QQ浏览器
//                    String pageUrl = "https://sc.qq.com/fx/u?r=W1wHysB";
//                    String pageUrl = "https://sc.qq.com/fx/u?r=1ARqttB";
//                    String pageUrl = "https://mp.weixin.qq.com/s/SoKwlm5izC2qCAEit9vGHQ";
//                    String pageUrl = "https://mp.weixin.qq.com/s/Ka8Q7n_wFq0Or437CiInhw";
//                    String pageUrl = "http://s4.uczzd.cn/webapp/webview/article/news.html?app=uc-iflow&aid=12946022970462462619&cid=100&zzd_from=uc-iflow&uc_param_str=dndseiwifrvesvntgipf&rd_type=share&pagetype=share&btifl=100&sdkdeep=3&sdksid=d7a5e485-ca79-54dc-d63c-8d209eb07673&sdkoriginal=d7a5e485-ca79-54dc-d63c-8d209eb07673&from=singlemessage&isappinstalled=1";
//                    String pageUrl = "http://m.sp.uczzd.cn/webview/news?app=uc-iflow&aid=5746954306850374168&cid=100&zzd_from=uc-iflow&uc_param_str=dndsfrvesvntnwpfgicp&recoid=3410570037419189425&rd_type=reco&sp_gz=3";
//                    String pageUrl = "【这媳妇绝对是买来的，兄弟这样下去你要绿啊】" +
//                            "http://m.toutiao.org/group/6407920158597447937/?iid=9776679118&app=news_article&tt_from=android_share&utm_medium=toutiao_android&utm_campaign=client_share" +
//                            "(想看更多合你口味的内容，马上下载 今日头条)" +
//                            "http://app.toutiao.com/news_article/?utm_source=link";
                    if (!Utils.isUrl(pageUrl)) {
                        pageUrl = Utils.getUrlHttpFromStr(pageUrl);
//                        dao.setUrl(m.getNote_ls_id(), m.getNote_id(), pageUrl);
                    }
                    if (pageUrl.contains(".uczzd.cn")) {
                        pageUrl += "&sinvoke=1";
                        driver.manage().window().maximize();
                    } else {
                        driver.manage().window().setSize(new Dimension(414, 736));
                    }
                    String path = Config.root_page_path;
                    String path2 = Config.root_page_path_other;
                    String zipPath = "esoupload/notefiles/";
                    if (m.getGroup_id() == null || m.getGroup_id().length() == 0) {
                        path += File.separator + m.getUser_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                        path2 += File.separator + m.getUser_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                        zipPath += m.getUser_id() + "/" + m.getNote_id() + "/" + m.getNote_ls_id() + ".zip";
                    } else {
                        path += File.separator + m.getGroup_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                        path2 += File.separator + m.getGroup_id() + File.separator + m.getNote_id() + File.separator + m.getNote_ls_id();
                        zipPath += m.getGroup_id() + "/" + m.getNote_id() + "/" + m.getNote_ls_id() + ".zip";
                    }
                    String content = "";
                    String title = "";
                    System.out.println("网页剪藏开始");
                    System.out.println("笔记id:" + m.getNote_id());
                    try {
                        driver.get(pageUrl);
//                        js.executeScript("document.body.scrollTop = document.body.scrollHeight;");
                        title = driver.getTitle();
                        System.out.println("网页标题:" + title);
                        System.out.println("地址:" + driver.getCurrentUrl());
                        if (!pageUrl.equals(driver.getCurrentUrl())) {
                            dao.setUrl(m.getNote_ls_id(), m.getNote_id(), driver.getCurrentUrl());
                        }
                        String str = driver.getPageSource();
                        content = str.replaceAll(Utils.regEx_html, "");
                        List<WebElement> meta = driver.findElements(By.tagName("meta"));
                        if (meta != null && meta.size() > 0) {
                            for (WebElement e : meta
                                    ) {
                                String meta_http_equiv = e.getAttribute("http-equiv");
                                String meta_content = e.getAttribute("content");
                                if (meta_http_equiv != null && meta_content != null && meta_http_equiv.equals("Content-Type") && meta_content.contains("text/html;")) {
                                    str = str.replace(meta_content, "text/html; charset=utf-8");
                                }
                            }
                        }
                        List<WebElement> cssList = driver.findElements(By.tagName("link"));
                        List<WebElement> imageList = driver.findElements(By.tagName("img"));
                        getResource(pageUrl, path, imageList, cssList);
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
                        if (Config.webdriver.indexOf("chrome") > 0) {
                            driver = new ChromeDriver();//new FirefoxDriver();
                        } else if (Config.webdriver.indexOf("ie") > 0) {
                            driver = new InternetExplorerDriver();
                        }
                    } finally {
                        dao.setUrl(m.getNote_ls_id(), m.getNote_id(), content, title, zipPath);
                    }
                    System.out.println("结束网页剪藏:" + m.getNote_id());
//                    break;
                }
                attachments.clear();
            }
//            break;
        }
    }

    public static void getResource(String pageUrl, String path, List<WebElement> imageList, List<WebElement> cssList) {
//        FileWriter writer = null;
        FileOutputStream fout = null;
        OutputStreamWriter osw = null;
        BufferedWriter out = null;
        try {
            if (pageUrl.contains(".uczzd.cn")) {
                js.executeScript("$(\".show-more-detail\").click();");
            }
            int i = 0;
            //获取css资源
            for (WebElement css : cssList
                    ) {
                String rel = css.getAttribute("rel");
                if (rel != null && rel.toLowerCase().equals("stylesheet")) {
                    String href = css.getAttribute("href");
                    String filename = Utils.downLoadFromUrl(href, path + "\\index_files\\", "css");
//                    str = replaceByLocal(str, filename, href);
                    //run JS to modify hidden element
                    js.executeScript("document.getElementsByTagName(\"link\")[" + i + "].href ='index_files/" + filename + "';");
                }
                i++;
            }
            i = 0;
            List<ImageModel> images = new ArrayList<>();
            //获取图片资源
            for (WebElement img : imageList
                    ) {
                try {
                    ImageModel model = new ImageModel();
                    model.setImgid(UUID.randomUUID().toString().replace("-", ""));
                    String filename = "";
                    String href = img.getAttribute("src");
                    //解决微信
                    String datasrc = img.getAttribute("data-src");
                    if (datasrc != null && datasrc.length() > 0) {
                        System.out.println(datasrc);
                        filename = Utils.downLoadFromUrl(datasrc, path + "\\index_files\\", "jpg");
                        model.setSrc("index_files/" + filename);
                        images.add(model);
                        js.executeScript("document.getElementsByTagName(\"img\")[" + i + "].setAttribute('data-imgid','" + model.getImgid() + "');");
//                        js.executeScript("document.getElementsByTagName(\"img\")[" + i + "].src ='index_files/" + filename + "';");
                    } else {
                        if (href != null && href.length() > 0) {
                            System.out.println(href);
                            //解决base64编码图片
                            if (href.startsWith("data:image/png;base64,")) {
                                filename = UUID.randomUUID().toString().replace("-", "") + ".png";
                                Utils.base64ToImage(href.replace("data:image/png;base64,", "").trim(), path + "\\index_files\\" + filename);
                            } else {
                                filename = Utils.downLoadFromUrl(href, path + "\\index_files\\", "jpg");
                            }
//                str = replaceByLocal(str, filename, href);
                            model.setSrc("index_files/" + filename);
                            images.add(model);
                            js.executeScript("document.getElementsByTagName(\"img\")[" + i + "].setAttribute('data-imgid','" + model.getImgid() + "');");
//                            js.executeScript("document.getElementsByTagName(\"img\")[" + i + "].src ='index_files/" + filename + "';");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                i++;
            }

            File htmlFile = new File(path, "index.html");
            if (!htmlFile.exists()) {
                htmlFile.getParentFile().mkdirs();
            }
            fout = new FileOutputStream(htmlFile);
            osw = new OutputStreamWriter(fout, "utf-8");
            out = new BufferedWriter(osw);

            String str = driver.getPageSource().replaceAll(Utils.regEx_script, "");
            if (pageUrl.contains(".uczzd.cn")) {
                try {
                    //去除ucweb中的iframe
                    str = str.replaceAll(Utils.regEx_uc_web_iframe, "");
                    //去除banner
                    WebElement ele = driver.findElement(By.cssSelector("div.top-banner-wrap"));
//                String banner = (String) js.executeScript("return arguments[0].innerHTML;", ele);
//                System.out.println(banner);
//                System.out.println(ele.getAttribute("innerHTML"));
//                System.out.println(ele.getAttribute("outerHTML"));
//                str = str.replaceAll(regEx_uc_web_banner, "");
                    str = str.replace(ele.getAttribute("outerHTML"), "");
                    //去除
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            str = Utils.replaceImageByLocal(images, str);
            out.write(str);
            out.flush();
            out.close();
            osw.close();
            fout.close();
//            writer = new FileWriter(htmlFile, false);
//            writer.write(str);
//            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeWrite(osw);
            closeWrite(out);
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fout = null;
            }
        }
    }

    private static void closeWrite(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                writer = null;
            }
        }
    }

    private static String replaceByLocal(String str, String filename, String href) {
        if (filename != null && filename.length() > 0) {
            if (str.contains(href)) {
                str = str.replace(href, "index_files/" + filename);
            } else {
                if (str.contains(href.replace("https:", "").replace("http:", ""))) {
                    str = str.replace(href.replace("https:", "").replace("http:", ""), "index_files/" + filename);
                } else {
                    try {
                        String temp = URLEncoder.encode(href, "utf-8");
                        if (str.contains(temp)) {
                            str = str.replace(temp, "index_files/" + filename);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return str;
    }
}
