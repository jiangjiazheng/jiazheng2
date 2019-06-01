package cn.lxdl.controller.item;

import cn.lxdl.entity.Result;
import cn.lxdl.pojo.item.ItemCat;
import cn.lxdl.service.item.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    /**
     * 根据父级id查询商品分类
     *
     * @param parentId 父级id
     * @return
     */
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(String parentId) {
        return itemCatService.findByParentId(parentId);
    }

    /**
     * 保存商品分类
     *
     * @param itemCat
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ItemCat itemCat) {
        try {
            itemCatService.save(itemCat);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "保存失败");
    }

    /**
     * 修改商品分类
     *
     * @param itemCat
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody ItemCat itemCat) {
        try {
            itemCatService.update(itemCat);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "修改失败");
    }

    /**
     * 根据id查询商品分类
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public ItemCat findOne(String id) {
        return itemCatService.getById(id);
    }

    /**
     * 根据id删除
     */
    @RequestMapping("/delete")
    public Result del(Long[] ids) {
        try {
            itemCatService.remove(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "删除失败");
    }

    /**
     * 查询全部商品分类
     * @return
     */
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
        return itemCatService.getAll();
    }
}
