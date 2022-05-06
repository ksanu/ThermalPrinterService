package pl.jakub.paragon.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paragon")
@Data
public class ConfigProperties {
    private String printerName;
    private Integer maxCharsInLine;
    private String title;
    private String qrIntro;
    private String qrData;
}
