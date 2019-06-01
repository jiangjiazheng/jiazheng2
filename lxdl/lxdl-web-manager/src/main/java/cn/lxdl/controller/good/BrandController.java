package cn.lxdl.controller.good;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.good.Brand;
import cn.lxdl.service.good.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Brand品牌控制类
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 获取全部品牌
     *
     * @return
     */
    @RequestMapping("/getAll")
    public List<Brand> getAll() {
        return brandService.getAll();
    }

    /**
     * 无条件分页获取品牌
     *
     * @param curPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/getByPage")
    public PageResult getByPage(Integer curPage, Integer pageSize) {
        return brandService.getByPage(curPage, pageSize);
    }

    /**
     * 有查询条件分页获取品牌
     *
     * @param curPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/searchByPage")
    public PageResult searchByPage(Integer curPage, Integer pageSize, @RequestBody Brand brand) {
        return brandService.searchByPage(curPage, pageSize, brand);
    }

    /**
     * 保存品牌
     *
     * @param brand
     * @return
     */
    @RequestMapping("/save")
    public Result save(@RequestBody Brand brand) {
        try {
            brandService.save(brand);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "保存失败");
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @RequestMapping("/getById")
    public Brand getById(Long id) {
        return brandService.getById(id);
    }

    /**
     * 更新品牌
     * 要注意前端传过来的是json,需要使用注解@RequestBody,把前端传过来的json转为对象
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand) {
        try {
            brandService.update(brand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "修改失败");
    }

    /**
     * 删除品牌
     * @param ids
     * @return
     */
    @RequestMapping("/remove")
    public Result remove(Long[] ids) {
        try {
            brandService.remove(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "删除失败");
    }

    /**
     * 获取品牌下拉框数据(返回给模板管理页面,下拉框数据初始化)
     * @return 返回List<Map<String,String>> 前端需要[{},{}]格式,故需要这样的格式数据返回
     */
    @RequestMapping("/selectOptionList")
    public List<Map<String,String>> selectOptionList(){
        List<Map<String, String>> maps = brandService.selectOptionList();
        System.out.println(maps);
        return maps;
    }
}
