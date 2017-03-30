import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/30.
 */
public class Main {
    private static String path = "F:\\IdeaWorkspace\\PageCapture\\temp\\";

    public static void main(String[] args) {
        System.getProperties().setProperty("webdriver.chrome.driver",
                "F:\\IdeaWorkspace\\PageCapture\\lib\\chromedriver_win32 (1)\\chromedriver.exe");

        // Create a new instance of the Firefox driver
        // Notice that there main der of the code relies on the interface,
        // not the implementation.
        WebDriver driver = new ChromeDriver();//new FirefoxDriver();
        // And now use this to visit Google
        String pageUrl = "http://blog.chinaunix.net/uid-22414998-id-5678340.html";
        driver.get(pageUrl);
        // Alternatively the same thing can be done like this
        // driver.navigate().to("www.google.com");
        // Find the text input element by its name
//        WebElement element = driver.findElement(By.name("q"));
        // Enter something to search for
//        element.sendKeys("Cheese!");
        // Now submit the form.WebDriver will find the form for us from the element
//        element.submit();
        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        WebDriverWait wait = new WebDriverWait(driver, 100);
//        wait.until(new ExpectedCondition<Boolean>() {
//            public Boolean apply(WebDriver webDriver) {
//                System.out.println("Searching ...");
//                return webDriver.findElement(By.id("commentCount1")).getText().length() != 0;
//            }
//        });

        try {
            String temp = pageUrl.substring(pageUrl.indexOf("://") + 3);
            String host = pageUrl.substring(0, pageUrl.indexOf(":")) + "://" + temp.substring(0, temp.indexOf("/"));
            System.out.println(host);
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
            //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
            //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
            String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式
            File htmlFile = new File(path, driver.getTitle() + ".html");
            if (!htmlFile.exists()) {
                htmlFile.getParentFile().mkdirs();
            }
            FileWriter writer = new FileWriter(htmlFile);
            String str = driver.getPageSource();
            str = str.replaceAll("<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>", "");
            List<WebElement> elements = driver.findElements(By.tagName("link"));
            for (WebElement element : elements
                    ) {
                String href = element.getAttribute("href");
                System.out.println(href);
                String fname = getCssNameByHref(href);
                writeCssToFile(href, fname);
                System.out.println(href.replace(host, ""));
                str = str.replace(href.replace(host, ""), "index_files/" + fname);
            }
            List<WebElement> imgs = driver.findElements(By.tagName("img"));
            for (WebElement img : imgs
                    ) {
                String href = img.getAttribute("src");
                System.out.println(href);
                String fname = getCssNameByHref(href);
                writeCssToFile(href, fname);
                System.out.println(img.getText());
                str = str.replace(href.replace(host, ""), "index_files/" + fname);
            }
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Close the browser
        driver.quit();
    }

    public static String getCssNameByHref(String href) {
        String filename = "";
//        String suffixes = "avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|pdf|rar|zip|docx|doc";
        Pattern pattern = Pattern.compile("\\w*\\.(css|png|jpg|gif)+");
        Matcher matcher = pattern.matcher(href);
//        while (matcher.find()) {
        if (matcher.find()) {
            filename = matcher.group();
        }
        System.out.println(filename);
//        }
//        if (href == null || href.length() == 0) {
//            filename = System.currentTimeMillis() + "";
//        } else {
//            filename = href.substring(href.lastIndexOf("/") + 1, href.lastIndexOf("."));
//        }
        if (filename == null || filename.length() == 0) {
            filename = System.currentTimeMillis() + "";
        }
        File f = new File(path + "index_files\\", filename);
        while (f.exists()) {
            if (filename.lastIndexOf(".") > 0) {
                f = new File(path + "index_files\\", System.currentTimeMillis() + filename.substring(filename.lastIndexOf(".") + 1));
            } else {
                f = new File(path + "index_files\\", System.currentTimeMillis() + "");
            }
        }
        return filename;
    }

    public static void writeCssToFile(String href, String fname) {
        FileWriter fw = null;
        try {

            String css = getDocumentAt(href);
            File f = new File(path + "index_files\\", fname);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            fw = new FileWriter(f);
            fw.write(css);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
}
