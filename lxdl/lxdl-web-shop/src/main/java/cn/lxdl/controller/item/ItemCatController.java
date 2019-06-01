package cn.lxdl.controller.item;

import cn.lxdl.pojo.item.ItemCat;
import cn.lxdl.service.item.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 查询全部商品分类
     * @return
     */
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
        return itemCatService.getAll();
    }
}
