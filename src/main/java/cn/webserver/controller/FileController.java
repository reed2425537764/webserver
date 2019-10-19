package cn.webserver.controller;

import cn.webserver.domain.FileData;
import cn.webserver.service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(MultipartFile file, HttpServletRequest request, Model model) throws Exception {
        //上传文件
        String uuid = fileService.uploadFile(file, request);
        //查询文件信息
        String data = fileService.getData(uuid);
        //读取JSON字符串
        FileData fileData = new ObjectMapper().readValue(data, FileData.class);
        log.info("文件信息  {}", fileData.toString());
        //添加到request域中
        model.addAttribute("data", fileData);
        return "index";
    }

    @PostMapping("/download")
    public void download(@RequestParam("path") String path, @RequestParam("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
        //设置响应头
        response.setContentType(request.getServletContext().getMimeType(filename));
        response.setHeader("content-disposition", "attachment;filename=" + filename);
        try {
            boolean b = fileService.download(path, response);
            if (!b) {
                response.setStatus(HttpStatus.SC_GONE);
            }
        } catch (IOException e) {
            response.setStatus(HttpStatus.SC_GONE);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String s = "{\"uuid\":\"178a97cda885470f8861843a78f4c3dc\",\"size\":\"157321\",\"suffix\":\"JPG\",\"name\":\"4.JPG\",\"date\":2019-10-17,\"path\":\"E:\\20191017\\178a97cda885470f8861843a78f4c3dc.JPG\"}";
        System.out.println(s);
        FileData fileData = new ObjectMapper().readValue(s, FileData.class);
        System.out.println("fileData = " + fileData);
    }

}
