package cn.lxdl.controller.content;

import java.util.List;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.ad.ContentCategory;
import cn.lxdl.service.content.ContentCategoryService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;


@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {

    @Reference
    private ContentCategoryService categoryService;


}
