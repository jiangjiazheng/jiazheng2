package cn.lxdl.service.item;

import cn.lxdl.dao.item.ItemCatDao;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.item.ItemCat;
import cn.lxdl.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品分类服务
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId(String parentId) {

        //将商品分类写入到redis缓存中.
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList != null && itemCatList.size() > 0) {
            for (ItemCat itemCat : itemCatList) {
                // key:itemCat field:分类名称 value:模板id
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }

        ItemCatQuery query = new ItemCatQuery();
        ItemCatQuery.Criteria criteria = query.createCriteria();
        if (!StringUtils.isEmpty(parentId)) {
            criteria.andParentIdEqualTo(Long.valueOf(parentId));
        }
        return itemCatDao.selectByExample(query);
    }

    @Transactional
    @Override
    public boolean save(ItemCat itemCat) {
        return itemCatDao.insertSelective(itemCat) > 0;
    }

    @Transactional
    @Override
    public void update(ItemCat itemCat) {
        itemCatDao.updateByPrimaryKeySelective(itemCat);
    }

    @Override
    public ItemCat getById(String id) {
        return itemCatDao.selectByPrimaryKey(Long.valueOf(id));
    }

    @Transactional
    @Override
    public void remove(Long[] ids) {
        for (Long id : ids) {
            //先查询该分类下的商品分类信息
            List<ItemCat> child = this.findByParentId(String.valueOf(id));

            //根据三级循环删除商品分类
            if (child != null && child.size() > 0) {
                for (ItemCat itemCat : child) {
                    List<ItemCat> grandson = this.findByParentId(String.valueOf(itemCat.getId()));
                    if (grandson != null && grandson.size() > 0) {
                        for (ItemCat cat : grandson) {
                            itemCatDao.deleteByPrimaryKey(itemCat.getId());
                        }
                    }
                    itemCatDao.deleteByPrimaryKey(itemCat.getId());
                }
            }
            itemCatDao.deleteByPrimaryKey(id);
        }
    }

    @Override
    public List<ItemCat> getAll() {
        return itemCatDao.selectByExample(null);
    }
}
