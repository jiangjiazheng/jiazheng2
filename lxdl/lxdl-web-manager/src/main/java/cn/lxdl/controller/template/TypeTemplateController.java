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
     * 根据条件分页查询商品模板
     *
     * @param page         当前页
     * @param rows         每页显示条数
     * @param typeTemplate 封装查询数据
     * @return 返回封装给前端分页数据 总条数+分页查询的结果
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate) {
        return typeTemplateService.searchByPage(page, rows, typeTemplate);
    }

    /**
     * 添加
     *
     * @param typeTemplate 封装前端需要保存的数据
     * @return 返回页面友好信息及成果与否数据
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TypeTemplate typeTemplate) {
        try {
            typeTemplateService.save(typeTemplate);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 根据id获取模板
     *
     * @param id 模板id(主键)
     * @return 根据id查询的模板数据
     */
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Integer id) {
        return typeTemplateService.getById(id);
    }

    /**
     * 修改模板
     *
     * @param typeTemplate 前端封装用户修改的模板信息
     * @return 返回友好信息
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TypeTemplate typeTemplate) {
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            typeTemplateService.remove(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 为下拉框获取商品模板数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map<String,String>> selectOptionList(){
        return typeTemplateService.selectOptionList();
    }


}
