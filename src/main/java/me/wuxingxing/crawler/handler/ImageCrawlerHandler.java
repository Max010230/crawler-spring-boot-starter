package me.wuxingxing.crawler.handler;

import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author wxx
 * @create 2020/12/1
 * @email wuxingxing@yunzhichong.com
 */
public class ImageCrawlerHandler extends WebCrawler {
    private static final Pattern filters = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");


    private static final Pattern imgPatterns = Pattern.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");

    private static File storageFolder;
    private static String[] crawlDomains;

    public static void configure(String[] domain, String storageFolderName) {
        crawlDomains = domain;

        storageFolder = new File(storageFolderName);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (filters.matcher(href).matches()) {
            return false;
        }

        if (href.contains("error")) {
            return false;
        }

        if (imgPatterns.matcher(href).matches()) {
            return true;
        }

        for (String domain : crawlDomains) {
            if (href.startsWith(domain)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();


        if (!(page.getParseData() instanceof edu.uci.ics.crawler4j.parser.BinaryParseData) && (page
                .getContentData()).length >= 10240) {
            return;
        } else if (!imgPatterns.matcher(url).matches()) {
            return;
        }


        String extension = url.substring(url.lastIndexOf('.'));
        String hashedName = UUID.randomUUID() + extension;


        String filename = storageFolder.getAbsolutePath() + "/" + hashedName;
        try {
            Files.write(page.getContentData(), new File(filename));
            logger.info("Stored: {}", url);
        } catch (IOException iox) {
            logger.error("Failed to write file: " + filename, iox);
        }
    }
}
