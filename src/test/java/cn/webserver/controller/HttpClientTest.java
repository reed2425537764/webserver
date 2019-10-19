package cn.webserver.controller;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpClientTest {

    @Autowired
    private CloseableHttpClient httpClients;

    @Autowired
    private RequestConfig requestConfig;

    //测试httpclient
    @Test
    public void testHttp() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8081/upload");
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        String s = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println(s);
    }

    //测试上传图片
    @Test
    public void testPicUp() {
        HttpPost httpPost = new HttpPost("http://localhost:8081/upload");
        httpPost.setConfig(requestConfig);

        Header header = new BasicHeader("Content-Disposition","form-data; name=file; filename=file");
        httpPost.setHeader(header);
        //处理文件 后面的setMode是用来解决文件名称乱码的问题:以浏览器兼容模式运行，防止文件名乱码。
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            builder.setCharset(Charset.forName("UTF-8")).addBinaryBody("multipartFile", Files.newInputStream(Paths.get("E:\\文档\\捕获.JPG"))
                    , ContentType.APPLICATION_OCTET_STREAM, "abc");
            HttpEntity httpEntity = builder.build();
            httpPost.setEntity(httpEntity);
            CloseableHttpResponse response = httpClients.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                String s = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println("s = " + s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}