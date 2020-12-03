package me.wuxingxing.crawler.utils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.gson.Gson;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import me.wuxingxing.crawler.handler.ImageCrawlerHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wxx
 * @create 2020/12/1
 * @email wuxingxing@yunzhichong.com
 */
public class BaiduUtils {
    @Value("${rootFolder}")
    private static String rootFolder;
    @Value("${storageFolder}")
    private static String storageFolder;
    @Value("${numberOfCrawlers}")
    private static String numberOfCrawlers;
    private static final Logger logger = LoggerFactory.getLogger(BaiduUtils.class);


    public static List<String> jsoupCrawlImgFormBaidu(String baiDuJsoupUrl, String name) throws Exception {
        Connection conn = Jsoup.connect(baiDuJsoupUrl + URLEncoder.encode(name, "UTF-8"));
        conn.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        conn.method(Connection.Method.GET);
        conn.timeout(10000);
        conn.ignoreContentType(true);
        Connection.Response response = conn.execute();
        Document document = response.parse();
        Elements element = document.getElementsByTag("script");

        List<String> strings = new ArrayList<>();
        for (Element e : element) {
            if (e.html().contains("imgData")) {
                Gson gson = new Gson();
                String str = e.html().replace("<", "&lt;").replace(">", "&gt;");
                str = str.substring(str.indexOf("imgData") + 9);
                str = str.substring(0, str.indexOf("fcadData") - 20);
                logger.info("输出字符串：" + str);
                HashMap map = gson.fromJson(str, HashMap.class);
                List<Map<String, Object>> imgList = (List<Map<String, Object>>) map.get("data");

                imgList.stream().filter(m -> (m.get("thumbURL") != null)).forEach(m -> strings.add(String.valueOf(m.get("thumbURL"))));
            }
        }
        if (CollectionUtils.isEmpty(strings)) {
            logger.info("百度搜索商品" + name + "图片未找到！");
        }
        return strings;
    }


    public static void htmlunitCrawlImgFormBaidu(String rootFolder, String storageFolder, String numberOfCrawlers, String commodityName, String categoryName, WebClient webClient, HtmlPage page) throws Exception {
        HtmlForm form = page.getFormByName("f1");
        HtmlTextInput textField = form.getInputByName("word");
        textField.setValueAttribute(commodityName + " " + categoryName);

        List<HtmlSubmitInput> list = page.getByXPath("//form/span/input[@type=\"submit\"]");
        HtmlSubmitInput go = list.get(0);
        HtmlPage p = go.click();
        webClient.waitForBackgroundJavaScript(3000L);

        List<HtmlDivision> imgList = p.getByXPath("//div[@class='imgbox']");
        if (imgList.size() != 0) {
            return;
        }

        List<String> strings = new ArrayList<>();

        strings.add((imgList.get(0)).getLastElementChild().getLastElementChild().getAttribute("data-imgurl"));


        String fodder = storageFolder + "/" + commodityName;

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(rootFolder);


        config.setIncludeBinaryContentInCrawling(true);

        String[] crawlDomains = strings.toArray(new String[0]);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        for (String domain : crawlDomains) {
            controller.addSeed(domain);
        }
        ImageCrawlerHandler.configure(crawlDomains, fodder);
        controller.start(ImageCrawlerHandler.class, Integer.parseInt(numberOfCrawlers));
    }
}
