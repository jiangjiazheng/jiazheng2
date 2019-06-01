package cn.lxdl.service.good;

import cn.lxdl.dao.good.BrandDao;
import cn.lxdl.dao.good.GoodsDao;
import cn.lxdl.dao.good.GoodsDescDao;
import cn.lxdl.dao.item.ItemCatDao;
import cn.lxdl.dao.item.ItemDao;
import cn.lxdl.dao.seller.SellerDao;
import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.good.Goods;
import cn.lxdl.pojo.good.GoodsDesc;
import cn.lxdl.pojo.good.GoodsQuery;
import cn.lxdl.pojo.item.Item;
import cn.lxdl.pojo.item.ItemQuery;
import cn.lxdl.service.staticPage.StaticPageService;
import cn.lxdl.vo.GoodsVO;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.opensaml.xml.signature.G;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemDao itemDao;
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private SellerDao sellerDao;
    @Resource
    private BrandDao brandDao;
    @Resource
    private SolrTemplate solrTemplate;
    // @Resource
    // private StaticPageService staticPageService;
    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private ActiveMQTopic topicPageAndSolrDestination;
    @Resource
    private ActiveMQQueue queueSolrDeleteDestination;

    @Transactional
    @Override
    public void save(GoodsVO goodsVO) {
        //获取商品基础对象
        Goods goods = goodsVO.getGoods();
        //手动封装参数
        //封装审核状态 0:未审核 1:审核通过 2:审核未通过
        goods.setAuditStatus("0");
        //封装是否上架 0:未上架 1:上架
        //goods.setIsMarketable("0");
        //封装是否删除 0:未删除 1:删除
        //goods.setIsDelete("0");
        //封装是否启动规格 0:启用 1:不启用
        //goods.setIsEnableSpec("0");

        //1.保存商品的基本信息,需要返回自增主键的id
        goodsDao.insertSelective(goods);


        //2.保存商品的描述信息,获取商品描述对象
        GoodsDesc goodsDesc = goodsVO.getGoodsDesc();
        if (goodsDesc != null) {
            goodsDesc.setGoodsId(goods.getId());
            goodsDescDao.insertSelective(goodsDesc);
        }

        //3.保存商品对应的库存信息,存储sku
        //1 spu --> n* sku *(0~n)
        //判断是否启用规格
        if ("1".equals(goods.getIsEnableSpec())) {
            //库存
            List<Item> itemList = goodsVO.getItemList();
            if (itemList != null && itemList.size() > 0) {
                for (Item item : itemList) {
                    //商品标题 = spu名称+spu副标题+规格名称
                    String title = goods.getGoodsName() + " " + goods.getCaption();
                    // 栗子：   spec:{"机身内存":"16G","网络":"联通3G"}
                    String spec = item.getSpec();
                    //json串-->对象
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);

                    setItemAttribute(goods, goodsDesc, item);

                    itemDao.insertSelective(item);
                }
            }
        } else {
            //未启用规格
            //1.spu --> 1sku
            Item item = new Item();
            String title = goods.getGoodsName() + " " + goods.getCaption();
            item.setTitle(title); //设置标题 商品名称+副标题
            item.setStatus("1"); //设置审核状态
            item.setIsDefault("1"); //设置默认规格
            item.setPrice(goods.getPrice()); //商品基本数据中的价格
            item.setNum(9999);  // 不合理

            setItemAttribute(goods, goodsDesc, item);

            itemDao.insertSelective(item);
        }

    }

    // 设置库存商品公共的属性
    private void setItemAttribute(Goods goods, GoodsDesc goodsDesc, Item item) {
        // 商品图片---goods_desc(取一张)
        // [{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},
        // {"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String images = goodsDesc.getItemImages();
        List<Map> imageList = JSON.parseArray(images, Map.class);
        if (imageList != null && imageList.size() > 0) {
            String image = imageList.get(0).get("url").toString();
            item.setImage(image);
        }
        // 商品分类的id(三级)
        item.setCategoryid(goods.getCategory3Id());
        // 商品的状态
        // item.setStatus("1");
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        // 商品id
        item.setGoodsId(goods.getId());
        // 商家id
        item.setSellerId(goods.getSellerId());
        // 商品分类名称、商品品牌名称、商家店铺名称
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());
    }

    @Override
    public PageResult searchByPage(Integer page, Integer rows, Goods goods) {
        //设置分页条件
        PageHelper.startPage(page, rows);
        //设置查询条件 1.登录商家id
        GoodsQuery goodsQuery = new GoodsQuery();

        Page<Goods> p = new Page<>();
        if (!StringUtils.isEmpty(goods.getSellerId())) {
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId());

            //查询
            p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        }

        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public PageResult searchByPageManager(Integer page, Integer rows, Goods goods) {
        //设置分页条件
        PageHelper.startPage(page, rows);
        //设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        //隐藏条件:待审核+未删除 isDelete为Null或者 非1
        if (!StringUtils.isEmpty(goods.getAuditStatus())) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        //不是已删除状态
        //criteria.andIsDeleteNotEqualTo("1");  //TODO:单单只有这个查不到数据了...数据库(Null)数据找不到,判断不了
        criteria.andIsDeleteIsNull(); //(is_delete <> '1' OR is_delete IS NULL)这样才能查到(Null)的数据.
        //查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public GoodsVO getById(String id) {
        GoodsVO goodsVO = new GoodsVO();

        //查找商品基础数据
        goodsVO.setGoods(goodsDao.selectByPrimaryKey(Long.valueOf(id)));

        //查找商品描述数据
        goodsVO.setGoodsDesc(goodsDescDao.selectByPrimaryKey(Long.valueOf(id)));

        //查询库存数据
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goodsVO.getGoods().getId());
        List<Item> items = itemDao.selectByExample(itemQuery);
        goodsVO.setItemList(items);

        return goodsVO;
    }

    @Transactional
    @Override
    public void remove(Long[] ids) {
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            goods.setIsDelete("1");
            for (Long id : ids) {
                goods.setId(id);
                // 1.逻辑删除
                goodsDao.updateByPrimaryKeySelective(goods);

                // 2.发送消息到消息队列,进行业务-->商品从索引库删除
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        // 商品id封装到消息体中
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });
                // TODO:删除静态页(暂时不做)
            }
        }
    }

    @Transactional
    @Override
    public void update(GoodsVO goodsVO) {
        Goods goods = goodsVO.getGoods();
        GoodsDesc goodsDesc = goodsVO.getGoodsDesc();

        // 更新库存表数据 --> 先删除再添加
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goodsVO.getGoods().getId());
        itemDao.deleteByExample(itemQuery);


        // 再插入
        // 判断是否启用规格
        if ("1".equals(goods.getIsEnableSpec())) { //启用规格
            //  1 spu ---> n sku
            List<Item> itemList = goodsVO.getItemList();
            if (itemList != null && itemList.size() > 0) {
                for (Item item : itemList) {
                    // 商品标题 = spu名称 + spu副标题 + 规格名称
                    String title = goods.getGoodsName() + " " + goods.getCaption();
                    // 栗子：   spec:{"机身内存":"16G","网络":"联通3G"}
                    String spec = item.getSpec();
                    //json串-->对象
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);

                    setItemAttribute(goods, goodsDesc, item);

                    itemDao.insertSelective(item);
                }
            }
        } else {
            //未启用规格
            //1.spu --> 1sku
            Item item = new Item();
            String title = goods.getGoodsName() + " " + goods.getCaption();
            item.setTitle(title); //设置标题 商品名称+副标题
            item.setStatus("1"); //设置审核状态
            item.setIsDefault("1"); //设置默认规格
            item.setPrice(goods.getPrice()); //商品基本数据中的价格
            item.setNum(9999);  // 不合理

            setItemAttribute(goods, goodsDesc, item);

            itemDao.insertSelective(item);
        }


        //更新商品描述信息
        goodsDescDao.updateByPrimaryKeySelective(goodsVO.getGoodsDesc());

        //更新商品基础信息
        //将商品审核状态改为未审核 TODO 这里不清楚上架审核的产品是否也要操作索引库
        goods.setAuditStatus("0");
        goodsDao.updateByPrimaryKeySelective(goods);
    }

    @Transactional
    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null && ids.length > 0) {
            Goods goods = new Goods();
            //更新商品审核状态
            goods.setAuditStatus(status);
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)) {
                    // TODO:对该商品进行上架 真实的需求
                    // 假定需求:将所有数据库数据导入到索引库中
                    // dataImportMysqlToSolr();
                    // 现在不用全部数据导入到索引库,只导入审核通过的商品.
                    // dataImportMysqlToSolrByIsDefault(id);

                    // TODO:生成该商品详情的静态页面
                    // staticPageService.getHtml(id);


                    // 改造代码,使用ActiveMQ进行业务解耦.
                    // sellergoods只进行商品审核状态改变,上架加到索引库和生成静态页面交由其他业务处理.
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            // 将商品id封装到消息体中,发送到消息队列中.
                            TextMessage textMessage = session.createTextMessage();
                            textMessage.setText(String.valueOf(id));
                            return textMessage;
                        }
                    });


                }
            }
        }
    }

    /**
     * 保存审核上架的商品到索引库中.
     * 默认为价格最低即(isDefault = 1).
     */
    private void dataImportMysqlToSolrByIsDefault(Long goodsId) {
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

    /**
     * 将数据导入到索引库中(清空之前索引库中的数据)
     */
    public void dataImportMysqlToSolr() {
        //查询库存表的数据
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1");
        List<Item> itemList = itemDao.selectByExample(itemQuery);

        //将数据导入到索引库中
        if (itemList != null && itemList.size() > 0) {
            //拼接动态字段
            for (Item item : itemList) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec, Map.class);
                item.setSpecMap(map);
            }

            solrTemplate.saveBeans(itemList);
            //需要手动提交
            solrTemplate.commit();
        }
    }


}
