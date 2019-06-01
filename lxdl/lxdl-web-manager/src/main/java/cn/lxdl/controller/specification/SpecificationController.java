package cn.lxdl.controller.specification;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.specification.Specification;
import cn.lxdl.service.specification.SpecificationService;
import cn.lxdl.vo.SpecificationVO;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;

    /**
     * 查询全部
     */
    @RequestMapping("/findAll")
    public List<Specification> findAll() {
        return specificationService.getAll();
    }

    /**
     * 查询分页
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer page, Integer rows) {
        return specificationService.getByPage(page, rows, new Specification());
    }

    /**
     * (搜索)根据条件查询,并且分页
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification) {
        return specificationService.getByPage(page, rows, specification);
    }

    /**
     * 增加规格
     * (规格和规格选项,使用一个封装的VO{pojo}来封装前端传递的参数)
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVO specificationVO) {
        try {
            return new Result(specificationService.save(specificationVO), "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }

    }

    /**
     * 根据id查找规格
     * bug:还需要查找规格选项.封装成VO返回给前端
     */
    @RequestMapping("/findOne")
    public SpecificationVO findOne(Integer id) {
        return specificationService.getById(id);
    }

    /**
     * 修改规格
     * ps:规格选项可以先删除,再批量插入新数据
     */
    @RequestMapping("update")
    public Result update(@RequestBody SpecificationVO specificationVO) {
        try {
            return new Result(specificationService.update(specificationVO), "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 删除规格
     * ps:记得,也要删除掉规格选项
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            return new Result(specificationService.remove(ids), "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 获取规格下拉框数据(返回给模板管理页面,下拉框数据初始化)
     * @return 返回List<Map<String,String>> 前端需要[{},{}]格式,故需要这样的格式数据返回
     */
    @RequestMapping("/selectOptionList")
    public List<Map<String,String>> selectOptionList(){
        List<Map<String, String>> maps = specificationService.selectOptionList();
        System.out.println(maps);
        return maps;
    }
}
