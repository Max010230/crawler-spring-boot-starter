package me.wuxingxing.crawler.utils;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author wxx
 * @create 2020/12/1
 * @email wuxingxing@yunzhichong.com
 */
public class FileDownloadUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadUtils.class);


    public static void downLoad(String storageFolder, String commodityName, String url) {
        String folder = storageFolder + "/" + commodityName;

        downLoad(url, folder);
    }

    static void downLoad(String url, String foder) {
        InputStream in = null;
        FileOutputStream fos = null;
        File fileFolder = new File(foder);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        if (url.startsWith("data:image")) {
            GenerateImage(foder, url);
        } else {
            try {
                URL uri = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) uri.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                httpURLConnection.connect();
                in = httpURLConnection.getInputStream();
                String fileName = getFileName(httpURLConnection);

                File file = new File(foder + "/" + fileName);
                fos = new FileOutputStream(file);
                byte[] size = new byte[1024];
                int num = 0;
                while ((num = in.read(size)) != -1) {
                    for (int i = 0; i < num; i++) {
                        fos.write(size[i]);
                    }
                }
                logger.info("生成图片成功：" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getFileName(HttpURLConnection httpURLConnection) throws Exception {
        MagicMatch match = Magic.getMagicMatch(new byte[httpURLConnection.getContentLength()]);


        return LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "." + ("text/plain".equals(match.getMimeType()) ? "jpg" : match.getMimeType());
    }


    private static void GenerateImage(String foder, String imgStr) {
        BASE64Decoder decoder = new BASE64Decoder();
        OutputStream out = null;
        try {
            byte[] b = decoder.decodeBuffer(imgStr.substring(imgStr.indexOf(",") + 1));
            String ext = imgStr.substring(imgStr.indexOf("data:image/") + 11, imgStr.indexOf(";"));

            if ("jpeg".equalsIgnoreCase(ext)) {
                ext = "jpg";
            } else if ("x-icon".equalsIgnoreCase(ext)) {
                ext = "ico";
            }

            String hashedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;

            out = new FileOutputStream(foder + "/" + hashedName);
            logger.info("生成图片成功：" + hashedName);
            out.write(b);
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
