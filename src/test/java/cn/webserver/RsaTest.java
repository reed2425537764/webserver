package cn.webserver;

import cn.webserver.utils.RsaUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RsaTest {

    @Autowired
    private CloseableHttpClient httpClients;
    @Autowired
    private RequestConfig requestConfig;

    //测试加header(SID Signature)
    @Test
    public void test() throws Exception {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        HttpGet httpGet = new HttpGet("http://localhost:8081/data?uuid=15f4c8868d43408bae40086b8d7a9046");
        StringBuilder sb = new StringBuilder();
        httpGet.setConfig(requestConfig);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(s.charAt(random.nextInt(53)));
        }
        String str = sb.toString();
        httpGet.setHeader("X-SID", str);
        httpGet.setHeader("X-Signature", RsaUtils.encryptStr(str));
        CloseableHttpResponse execute = httpClients.execute(httpGet);
        System.out.println(execute.getStatusLine().getStatusCode());
        String string = EntityUtils.toString(execute.getEntity(), StandardCharsets.UTF_8);
        System.out.println("string = " + string);
        execute.close();
    }
}
