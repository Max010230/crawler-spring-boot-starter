package me.wuxingxing.crawler.api;

import com.gargoylesoftware.htmlunit.WebClient;
import me.wuxingxing.crawler.utils.BingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wxx
 * @create 2020/12/1
 * @email wuxingxing@yunzhichong.com
 */
@RestController
public class BingApi {

    @Autowired
    private WebClient webClient;

    @GetMapping("/bing/index")
    public String index() throws Exception {
        BingUtils.htmlUnitWithJsoupCrawImgFormChinaBingIndex("https://www.bing.com/", webClient);
        return "success";
    }

}
