package cn.lxdl.service.item;

import cn.lxdl.entity.Result;
import cn.lxdl.pojo.item.ItemCat;

import java.util.List;

/**
 * 商品分类接口
 */
public interface ItemCatService {

    /**
     * 根据父级id查询商品分类(商品分类列表查询)
     * @param parentId 父级id
     * @return
     */
    List<ItemCat> findByParentId(String parentId);

    /**
     * 保存商品分类
     * @param itemCat
     * @return
     */
    boolean save(ItemCat itemCat);

    /**
     * 修改商品分类
     * @param itemCat
     */
    void update(ItemCat itemCat);

    /**
     * 根据id查询商品分类
     * @param id
     * @return
     */
    ItemCat getById(String id);

    /**
     * 根据id删除商品分类
     * ps:需要删除其下商品分类
     * @param ids
     */
    void remove(Long[] ids);

    /**
     * 获取全部商品分类
     * @return
     */
    List<ItemCat> getAll();
}
