package cn.lxdl.task;

import cn.lxdl.dao.item.ItemCatDao;
import cn.lxdl.dao.item.ItemDao;
import cn.lxdl.dao.specification.SpecificationOptionDao;
import cn.lxdl.dao.template.TypeTemplateDao;
import cn.lxdl.pojo.item.ItemCat;
import cn.lxdl.pojo.specification.SpecificationOption;
import cn.lxdl.pojo.specification.SpecificationOptionQuery;
import cn.lxdl.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component // 需要被spring管理
public class RedisTask {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    @Resource
    private RedisTemplate redisTemplate;


    // cron:时间表达式,指定该程序的执行时间
    // 将商品分类的数据写入缓存
    @Scheduled(cron = "00 36 14 * * ?")
    public void autoDataToRedisForItemCat() {

        // 将商品分类写入到redis缓存中.
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList != null && itemCatList.size() > 0) {
            for (ItemCat itemCat : itemCatList) {
                // key:itemCat field:分类名称 value:模板id
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }
        System.out.println("定时器启动,运行autoDataToRedisForItemCat()方法");
    }

    // 将商品模板的数据写入缓存
    @Scheduled(cron = "30 36 14 * * ?")
    public void autoDataToRedisForTemplate() {
        // 将模板数据写入到redis缓存中(预热)
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(null);
        if (typeTemplateList != null && typeTemplateList.size() > 0) {
            for (TypeTemplate template : typeTemplateList) {

                // 先封装品牌数据
                String brandIds = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);

                // 再封装规格数据 -->规格数据还需要规格选项数据.
                List<Map> specList = findBySpecList(String.valueOf(template.getId()));
                redisTemplate.boundHashOps("specList").put(template.getId(), specList);
            }
        }
        System.out.println("定时器启动,运行autoDataToRedisForTemplate()方法");

    }

    public List<Map> findBySpecList(String id) {
        // 根据模板id查找到模板
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(Long.valueOf(id));

        // 获取关联规格
        // specIds在数据库的数据格式为json字符串格式:[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        // 需要解析json,根据id获取规格选项
        String specIds = typeTemplate.getSpecIds();
        List<Map> list = JSON.parseArray(specIds, Map.class);
        if (list != null && list.size() > 0) {
            for (Map map : list) {
                String specId = map.get("id").toString();
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(Long.valueOf(specId));
                List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(query);
                map.put("options", specificationOptionList);
            }
        }

        return list;
    }


}
