package cn.lxdl.controller.template;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.template.TypeTemplate;
import cn.lxdl.service.template.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TypeTemplateService typeTemplateService;


    /**
     * 根据id获取模板
     *
     * @param id 模板id(主键)
     * @return
     */
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Integer id) {
        return typeTemplateService.getById(id);
    }

    /**
     * 新增商品页面,根据模板id查找规格以及规格选项
     * @param id 模板id
     * @return
     */
    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(String id) {
        return typeTemplateService.findBySpecList(id);
    }

}
