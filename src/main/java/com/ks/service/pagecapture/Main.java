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

import static com.ks.service.pagecapture.Config.driver;

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
//                    String pageUrl = "https://mp.weixin.qq.com/s/SoKwlm5izC2qCAEit9vGHQ";
//                    String pageUrl = "https://mp.weixin.qq.com/s/Ka8Q7n_wFq0Or437CiInhw";
                    if (!Utils.isUrl(pageUrl)) {
                        pageUrl = Utils.getUrlFromStr(pageUrl);
                        dao.setUrl(m.getNote_ls_id(), m.getNote_id());
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
                        js.executeScript("document.body.scrollTop = document.body.scrollHeight;");
                        title = driver.getTitle();
                        System.out.println("网页标题:" + title);
                        System.out.println("地址:" + driver.getCurrentUrl());
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

    public static void getResource(String html, String path, List<WebElement> imageList, List<WebElement> cssList) {
        FileWriter writer = null;
        FileOutputStream fout = null;
        OutputStreamWriter osw = null;
        BufferedWriter out = null;
        try {
//            String str = html.replaceAll(Utils.regEx_script, "");
//            js.executeScript("var script=document.getElementsByTagName('script');\n" +
//                    "    for(i=0;i<script.length;i++){\n" +
//                    "        script[i].parentNode.removeChild(script[i]);\n" +
//                    "    }");
            int i = 0;
//            List<WebElement> scripts=driver.findElements(By.tagName("script"));
//            for (WebElement elemet:scripts
//                 ) {
//                try {
//                    js.executeScript("document.getElementsByTagName(\"script\")[" + i + "].remove()';");
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                i++;
//            }
            i = 0;
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
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out = null;
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                osw = null;
            }
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
