package cn.webserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@Data
public class FileServiceConfig {
    private String url;
    private String dataUrl;
    private String downloadUrl;
}
