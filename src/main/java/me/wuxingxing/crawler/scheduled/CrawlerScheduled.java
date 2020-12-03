package me.wuxingxing.crawler.scheduled;

import com.gargoylesoftware.htmlunit.WebClient;
import me.wuxingxing.crawler.utils.BingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author wxx
 * @create 2020/12/1
 * @email wuxingxing@yunzhichong.com
 */
@Component
public class CrawlerScheduled {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerScheduled.class);

    @Value("${storageFolder}")
    private String storageFolder;

    @Value("${htmlunit.img.search.url}")
    private String htmlUnitUrl;

    @Value("${jsoup.baidun.img.search.url}")
    private String baiDuJsoupUrl;

    @Value("${jsoup.bing.img.search.url}")
    private String bingJsoupUrl;

    @Autowired
    private WebClient webClient;

    @Scheduled(cron = "0 0 1 * * ?")
    public void bingIndex() {
        logger.info("bing每日美图任务开始！");

        try {
            long startTime = System.currentTimeMillis();
            BingUtils.htmlUnitWithJsoupCrawImgFormChinaBingIndex("https://www.bing.com/", webClient);
            deleteTempFile(new File(storageFolder));
            long endTime = System.currentTimeMillis();
            logger.info("本次搜索耗时：" + ((endTime - startTime) / 1000L) + "s");
        } catch (Exception e) {
            logger.error("图片搜索异常：" + e.getMessage());
        }
        logger.info("图片匹配任务结束！");
    }

    private void deleteTempFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteTempFile(files[i]);
                }
                file.delete();
            }
        }
    }
}
