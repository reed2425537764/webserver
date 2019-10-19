package cn.webserver.domain;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@Data
public class FileData {
    private String uuid;
    private String size;
    private String suffix;
    private String name;
    private Date date;
    private String path;

    public String getFormatDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
