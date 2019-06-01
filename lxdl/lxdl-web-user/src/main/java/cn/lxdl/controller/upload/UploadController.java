package cn.lxdl.controller.upload;

import cn.lxdl.entity.Result;
import cn.lxdl.utils.fastDFS.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    /**
     * 附件上传
     *
     * MultipartFile参数
     * 1. 表示整个上传的文件
     * 2. 参数名称不能随便写，必须域页面的文件域的名称一致：
     * 相当于:<input type="file" name="file">
     *
     * @param file 文件
     * @return Result(flag, message)
     */
    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) {
        try {
            //将附件上传到FastDFS中
            String conf = "classpath:fastDFS/fdfs_client.conf";
            FastDFSClient fastDFSClient = new FastDFSClient(conf);
            //文件扩展名
            String filename = file.getOriginalFilename();
            String extname = FilenameUtils.getExtension(filename);
            //调用工具类的方法上传到FastDFS
            String path = fastDFSClient.uploadFile(file.getBytes(), extname, null);
            String url = FILE_SERVER_URL + path;
            //上传成功,回显图片/文件
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "上传失败");
    }
}
