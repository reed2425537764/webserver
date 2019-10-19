package cn.webserver.service;

import cn.webserver.config.FileServiceConfig;
import cn.webserver.utils.RsaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class FileService {

    @Autowired
    private CloseableHttpClient httpClient;

    @Autowired
    private RequestConfig requestConfig;

    @Autowired
    private FileServiceConfig fileServiceConfig;


    public String uploadFile(MultipartFile file, HttpServletRequest request) throws Exception {
        //建立post请求
        HttpPost httpPost = new HttpPost(fileServiceConfig.getUrl());
        httpPost.setConfig(requestConfig);
        //设置上传文件的头信息
        Header header = new BasicHeader("Content-Disposition","form-data; name=file; filename="+file.getContentType().split("/")[1]);
        httpPost.setHeader(header);

        //添加加密验证头 sid signature
        String sid = getSid();
        httpPost.setHeader("X-SID", sid);
        httpPost.setHeader("X-Signature", RsaUtils.encryptStr(sid));

        //处理文件 后面的setMode是用来解决文件名称乱码的问题:以浏览器兼容模式运行，防止文件名乱码。
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        String uuid = null;
        CloseableHttpResponse response = null;
        try {
            //构建上传文件的请求体
            builder.setCharset(Charset.forName("UTF-8")).addBinaryBody("multipartFile", file.getInputStream()
                    , ContentType.MULTIPART_FORM_DATA, file.getOriginalFilename());
            HttpEntity httpEntity = builder.build();
            httpPost.setEntity(httpEntity);

            //发起请求
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                uuid = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.info("上传文件成功 uuid: {}",uuid);
            }
        } catch (IOException e) {
            log.info("上传文件失败");
            e.printStackTrace();
        }finally {
            //释放资源
            response.close();
        }

        return uuid;
    }

    public String getData(String uuid) throws Exception {
        //构建get请求
        HttpGet httpGet = new HttpGet(fileServiceConfig.getDataUrl()+"?uuid="+uuid);
        httpGet.setConfig(requestConfig);

        //添加加密验证头 sid signature
        String sid = getSid();
        httpGet.setHeader("X-SID", sid);
        httpGet.setHeader("X-Signature", RsaUtils.encryptStr(sid));

        String fileData = null;
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                fileData = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.info("获取上传文件的信息成功: {}", fileData);
            }
            response.close();
        } catch (IOException e) {
            log.info("获取上传文件信息失败");
            e.printStackTrace();
        }

        return fileData;
    }


    public boolean download(String path, HttpServletResponse response) throws Exception {
        //必须URL编码 否则报错
        String uri = fileServiceConfig.getDownloadUrl()+"?path=" + URLEncoder.encode(path, StandardCharsets.UTF_8);
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(requestConfig);

        //添加加密验证头 sid signature
        String sid = getSid();
        httpGet.setHeader("X-SID", sid);
        httpGet.setHeader("X-Signature", RsaUtils.encryptStr(sid));

        //发起请求
        CloseableHttpResponse resq = httpClient.execute(httpGet);
        HttpEntity entity = resq.getEntity();

        //使用流传输文件
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            if (entity.getContentLength() <= 0) {
                return false;
            }
            is = entity.getContent();

            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buffer = new byte[1024 * 8 * 3];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer,0,len);
            }
            bos.flush();
            log.info("下载文件成功  {}", path);
        } catch (IOException | UnsupportedOperationException e) {
            log.info("下载文件失败  {}", path);
            e.printStackTrace();
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (is != null) {
                is.close();
            }
        }

        return true;
    }


    private String getSid() {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            sb.append(s.charAt(random.nextInt(52)));
        }
        return sb.toString();
    }

}
