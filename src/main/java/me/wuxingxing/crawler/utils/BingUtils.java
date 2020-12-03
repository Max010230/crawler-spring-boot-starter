package me.wuxingxing.crawler.utils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.apache.commons.collections4.CollectionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BingUtils {
    private static final Logger logger = LoggerFactory.getLogger(BingUtils.class);


    public static List<String> htmlUnitWithJsoupCrawImgFormBing(String bingJsoupUrl, WebClient webClient, String commodityName) throws Exception {
        HtmlPage page = webClient.getPage(String.format(bingJsoupUrl, URLEncoder.encode(commodityName, "UTF-8")));
        webClient.waitForBackgroundJavaScript(3000L);
        Document document = Jsoup.parse(page.asXml());
        List<String> strings = new ArrayList<>();
        try {
            Elements element = document.getElementsByTag("img");
            Elements script = document.getElementsByTag("script");
            Map<String, String> dataFromScript = new HashMap<>();
            script.stream().filter(e -> e.html().contains("x=_ge")).forEach(e -> {
                String result = e.html();
                String key = result.substring(result.indexOf("x=_ge") + 7);
                key = key.substring(0, key.indexOf(")") - 1);
                String value = result.substring(result.indexOf("x.src=") + 7);
                value = value.substring(0, value.indexOf("}") - 2);
                dataFromScript.put(key, value);
            });

            element.stream().filter(e -> e.hasClass("mimg")).forEach(e -> {
                if (e.hasClass("rms_img")) {
                    strings.add(dataFromScript.get(e.id()));
                } else if (e.hasClass("vimgld")) {
                    if (!e.hasAttr("data-src")) {
                        strings.add(e.attr("src"));
                    } else {
                        strings.add(e.attr("data-src"));
                    }
                } else {
                    strings.add(e.attr("src"));
                }
            });
            if (CollectionUtils.isEmpty(strings)) {
                logger.info("Bing搜索商品" + commodityName + "图片未找到！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strings;
    }

    public static void htmlUnitWithJsoupCrawImgFormGlobalBingIndex(String bingJsoupUrl, WebClient webClient) throws Exception {
        String globalXmlUrl = "http://global.bing.com/HPImageArchive.aspx?format=xml&idx=%s&n=1&mkt=en-US";
        XmlPage page = webClient.getPage(String.format(globalXmlUrl, new Object[]{Integer.valueOf(0)}));
        String url = (page.getElementsByTagName("url").get(0)).asText();
        FileDownloadUtils.downLoad(bingJsoupUrl + url, "/usr/local/data/bing/global");
    }

    public static void htmlUnitWithJsoupCrawImgFormChinaBingIndex(String baseUrl, WebClient webClient) throws Exception {
        String chinaXmlUrl = "https://www.bing.com/HPImageArchive.aspx?ids=%s&n=1&format=xml";
        XmlPage page = webClient.getPage(String.format(chinaXmlUrl, 0));
        String url = (page.getElementsByTagName("url").get(0)).asText();
        FileDownloadUtils.downLoad(baseUrl + url, "/usr/local/data/bing/china");
    }
}
