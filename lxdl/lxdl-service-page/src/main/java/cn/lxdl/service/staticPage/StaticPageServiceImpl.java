package cn.lxdl.service.staticPage;

import cn.lxdl.dao.good.GoodsDao;
import cn.lxdl.dao.good.GoodsDescDao;
import cn.lxdl.dao.item.ItemCatDao;
import cn.lxdl.dao.item.ItemDao;
import cn.lxdl.pojo.good.Goods;
import cn.lxdl.pojo.good.GoodsDesc;
import cn.lxdl.pojo.item.Item;
import cn.lxdl.pojo.item.ItemCat;
import cn.lxdl.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceImpl implements StaticPageService, ServletContextAware {

    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;
    @Resource
    private ItemDao itemDao;

    private FreeMarkerConfigurer freeMarkerConfigurer;

    private ServletContext servletContext;

    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.freeMarkerConfigurer = freeMarkerConfigurer;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void getHtml(Long goodsId) {
        try {
            // 1.获取Configuration
            Configuration configuration = freeMarkerConfigurer.getConfiguration();

            // 2.获取模板
            Template template = configuration.getTemplate("item.ftl");

            // 3.准备业务数据
            Map<String, Object> dataModel = getDataModel(goodsId);

            // 4.数据+模板,进行输出成为静态页面
            /**
             * 获取上下文对象,第一种方式为request.getRealPath(pathName);第二种为servletContext.getRealPath
             * 不在系统,在服务中,spring提供了接口ServletContextAware,实现该接口,则可以注入servletContext,从而获的上下文对象
             * 之前获取servletContext对象则是控制器中,使用session.getServletContext获取到上下文对象.
             */
            String pathName = "/" + goodsId + ".html"; // 根据商品id命名,静态页面放置在Tomcat中.
            String realPath = servletContext.getRealPath(pathName);
            File file = new File(realPath);
            template.process(dataModel, new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取模板需要的业务数据
     *
     * @param goodsId 商品id
     * @return
     */
    private Map<String, Object> getDataModel(Long goodsId) {
        Map<String, Object> map = new HashMap<>();
        // 获取商品基本信息
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        map.put("goods", goods);
        // 获取商品描述信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        map.put("goodsDesc", goodsDesc);
        // 获取商品分类信息
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        map.put("itemCat1", itemCat1);
        map.put("itemCat2", itemCat2);
        map.put("itemCat3", itemCat3);
        // 获取商品库存信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        map.put("itemList", itemList);
        return map;
    }
}
