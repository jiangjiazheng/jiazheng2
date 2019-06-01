package cn.lxdl.service.search;

import cn.lxdl.dao.item.ItemCatDao;
import cn.lxdl.dao.item.ItemDao;
import cn.lxdl.dao.specification.SpecificationOptionDao;
import cn.lxdl.dao.template.TypeTemplateDao;
import cn.lxdl.pojo.item.Item;
import cn.lxdl.pojo.item.ItemCat;
import cn.lxdl.pojo.item.ItemCatQuery;
import cn.lxdl.pojo.item.ItemQuery;
import cn.lxdl.pojo.specification.SpecificationOption;
import cn.lxdl.pojo.specification.SpecificationOptionQuery;
import cn.lxdl.pojo.template.TypeTemplate;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Resource
    private SolrTemplate solrTemplate;
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    @Resource
    private ItemDao itemDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        // 创建一个map,封装所有的结果
        Map<String, Object> resultMap = new HashMap<>();

        //根据关键字检索商品的列表
        //Map<String, Object> map = searchForPage(searchMap);

        //根据关键字检索商品的列表并且关键字高亮显示
        Map<String, Object> map = searchForHighLightPage(searchMap);
        resultMap.putAll(map);

        // TODO:商品分类列表 list
        Map<String, Object> itemCatMap = searchForItemCat(searchMap);
        resultMap.putAll(itemCatMap);

        // TODO:商品品牌列表,规格列表
        Map<String, Object> brandAndSpecMap = searchForBrandAndSpecRedis(itemCatMap);
        resultMap.putAll(brandAndSpecMap);

        return resultMap;
    }

    /**
     * 从缓存中拿取数据(预热型)
     *
     * @param itemCatMap
     * @return
     */
    private Map<String, Object> searchForBrandAndSpecRedis(Map<String, Object> itemCatMap) {
        // 封装map返回
        HashMap<String, Object> map = new HashMap<>();

        // 获得分类名称集合
        List<String> categoryList = (List<String>) itemCatMap.get("categoryList");

        if (categoryList != null && categoryList.size() > 0) {
            // 默认第一个
            String itemCatName = categoryList.get(0);

            // 先从redis中拿取
            Object typeId = redisTemplate.boundHashOps("itemCat").get(itemCatName);
            if (!StringUtils.isEmpty(typeId)) {
                List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
                // 封装品牌
                map.put("brandList", brandList);

                List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
                // 封装规格
                map.put("specList", specList);
            }
        }

        return map;
    }

    /**
     * 检索出品牌+规格(没有预热,从数据库查找)
     * 根据商品分类查出模板id,使用模板id查找到品牌+规格,规格还需要找到规格选项.
     */
    private Map<String, Object> searchForBrandAndSpec(Map<String, Object> itemCatMap) {
        // 封装map返回
        HashMap<String, Object> map = new HashMap<>();

        List<String> categoryList = (List<String>) itemCatMap.get("categoryList");
        if (categoryList != null && categoryList.size() > 0) {
            // 默认查找第一个商品分类的商品和规格列表
            String itemCatName = categoryList.get(0);

            ItemCatQuery itemCatQuery = new ItemCatQuery();
            itemCatQuery.createCriteria().andNameEqualTo(itemCatName);
            // 根据itemCatName商品分类名称查找到模板id
            List<ItemCat> itemCats = itemCatDao.selectByExample(itemCatQuery);
            if (itemCats != null && itemCats.size() > 0) {
                ItemCat itemCat = itemCats.get(0);
                // 获取到模板id
                Long typeId = itemCat.getTypeId();

                // 查找模板
                TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(typeId);
                // 获取brandIds 数据库格式:[{"id":1,"text":"联想"},{"id":3,"text":"三星"},{"id":2,"text":"华为"}]
                String brandIds = typeTemplate.getBrandIds();
                List<Map<String, String>> brandList = JSON.parseObject(brandIds, List.class);
                // 封装品牌
                map.put("brandList", brandList);

                // 获取到specIds
                String specIds = typeTemplate.getSpecIds();
                List<Map<String, Object>> specList = JSON.parseObject(specIds, List.class);

                // 获取到规格id,从而去获取规格选项
                for (Map<String, Object> specMap : specList) {
                    SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                    //String idStr = String.valueOf(specMap.get("id"));
                    String idStr = specMap.get("id") + "";
                    specificationOptionQuery.createCriteria().andSpecIdEqualTo(Long.valueOf(idStr));
                    List<SpecificationOption> specOptList = specificationOptionDao.selectByExample(specificationOptionQuery);

                    // 封装 options
                    specMap.put("options", specOptList);
                }

                // 封装规格
                map.put("specList", specList);
            }

        }

        return map;
    }

    /**
     * 检索出商品分类 list
     */
    private Map<String, Object> searchForItemCat(Map<String, String> searchMap) {
        // 处理关键字中的空格
        String keywords = searchMap.get("keywords");
        if (!StringUtils.isEmpty(keywords)) {
            keywords = keywords.replace(" ", "");
            searchMap.put("keywords", keywords);
        }

        // 封装检索条件
        Criteria criteria = new Criteria("item_keywords");
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            criteria.is(searchMap.get("keywords"));
        }
        SimpleQuery query = new SimpleQuery(criteria);

        // 增加分组字段
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        // 分组检索
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);

        // 封装商品分类名称为List
        List<String> categoryList = new ArrayList<>();
        List<GroupEntry<Item>> item_category = groupPage.getGroupResult("item_category").getGroupEntries().getContent();
        if (item_category != null && item_category.size() > 0) {
            for (GroupEntry<Item> itemGroupEntry : item_category) {
                categoryList.add(itemGroupEntry.getGroupValue());
            }
        }

        //封装
        HashMap<String, Object> map = new HashMap<>();
        map.put("categoryList", categoryList);
        return map;
    }

    /**
     * 根据关键字检索商品的列表并且关键字高亮显示
     */
    private Map<String, Object> searchForHighLightPage(Map<String, String> searchMap) {
        // 封装检索条件
        Criteria criteria = new Criteria("item_keywords");
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            criteria.is(searchMap.get("keywords"));
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);

        // 封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        // 起始行
        Integer start = (pageNo - 1) * pageSize;
        query.setOffset(start);
        query.setRows(pageSize);

        // 封装高亮条件
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<font color='red'>"); // 前缀
        highlightOptions.setSimplePostfix("</font>");           // 后缀
        query.setHighlightOptions(highlightOptions);

        // 添加过滤条件
        // 获取分类名称
        String category = searchMap.get("category");
        if (!StringUtils.isEmpty(category)) {
            // 对商品分类进行过滤
            Criteria cri = new Criteria("item_category");
            cri.is(category);
            SimpleFilterQuery fq = new SimpleFilterQuery(cri);
            query.addFilterQuery(fq);
        }

        // 获取品牌
        String brand = searchMap.get("brand");
        if (!StringUtils.isEmpty(brand)) {
            //对商品分类进行过滤
            Criteria cri = new Criteria("item_brand");
            cri.is(brand);
            SimpleFilterQuery fq = new SimpleFilterQuery(cri);
            query.addFilterQuery(fq);
        }

        // 获取价格
        String price = searchMap.get("price"); // 格式为 300-500,若3000-*则为3000以上的价格.
        if (!StringUtils.isEmpty(price)) {
            // 添加商品价格的过滤
            String[] prices = price.split("-");
            Criteria cri = new Criteria("item_price");
            if (price.contains("*")) {    // xxx以上
                cri.greaterThan(prices[0]);
            } else {  // min-max
                cri.between(prices[0], prices[1], true, true);
            }
            SimpleFilterQuery fq = new SimpleFilterQuery(cri);
            query.addFilterQuery(fq);
        }

        // 获取规格 ps:注意这个为动态字段
        String spec = searchMap.get("spec");
        if (!StringUtils.isEmpty(spec)) {
            // 数据：{"机身内存":"16G","网络":"联通3G"}
            Map<String, String> map = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria cri = new Criteria("item_spec_" + entry.getKey());
                cri.is(entry.getValue());
                SimpleFilterQuery fq = new SimpleFilterQuery(cri);
                query.addFilterQuery(fq);
            }
        }

        // 添加排序
        // sortField：排序的字段
        // sort：排序的规则
        String sort = searchMap.get("sort");
        if (!StringUtils.isEmpty(sort)) {
            String sortField = "item_" + searchMap.get("sortField");
            if ("ASC".equals(sort)) { // 升序,根据某个条件进行排序.
                Sort s = new Sort(Sort.Direction.ASC, sortField);
                query.addSort(s);
            } else {  // 降序
                Sort s = new Sort(Sort.Direction.DESC, sortField);
                query.addSort(s);
            }
        }

        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);

        // 封装高亮后的结果
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
                String highLightTitle = itemHighlightEntry.getHighlights().get(0).getSnipplets().get(0);// 高亮标题
                itemHighlightEntry.getEntity().setTitle(highLightTitle);                  // 换普通标题为高亮标题
            }
        }

        // 封装
        HashMap<String, Object> map = new HashMap<>();
        map.put("totalPages", highlightPage.getTotalPages()); // 总页数
        map.put("total", highlightPage.getTotalElements());   // 总条数
        map.put("rows", highlightPage.getContent());          // 结果集

        return map;
    }

    /**
     * 根据关键字检索商品的列表
     */
    private Map<String, Object> searchForPage(Map<String, String> searchMap) {
        // 封装检索条件
        Criteria criteria = new Criteria("item_keywords");
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            criteria.is(searchMap.get("keywords"));
        }
        SimpleQuery query = new SimpleQuery(criteria);

        // 封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        // 起始行
        Integer start = (pageNo - 1) * pageSize;
        query.setOffset(start);
        query.setRows(pageSize);

        // 根据条件查询
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);

        // 封装
        HashMap<String, Object> map = new HashMap<>();
        map.put("totalPages", scoredPage.getTotalPages());
        map.put("total", scoredPage.getTotalElements());
        map.put("rows", scoredPage.getContent());

        return map;
    }


    @Override
    public void saveItemToSolr(Long goodsId) {
        // 查询库存表的数据
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goodsId).andIsDefaultEqualTo("1")
                .andStatusEqualTo("1").andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);

        // 将数据导入到索引库中
        if (itemList != null && itemList.size() > 0) {
            // 拼接动态字段
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(itemList);
            //需要手动提交
            solrTemplate.commit();
        }
    }

    @Override
    public void removeItemToSolr(Long goodsId) {
        // 商品下架(id为商品id,对应在索引库中的item_goodsid,*:*,第一个*为索引库的域,第二个*为值)
        SimpleQuery query = new SimpleQuery("item_goodsid:" + goodsId);
        solrTemplate.delete(query);
        // 需要手动提交
        solrTemplate.commit();
    }
}
