package cn.lxdl.service.staticPage;

/**
 * 生成静态页面服务接口
 */
public interface StaticPageService {

    /**
     * 生成商品详情的静态页
     */
    void getHtml(Long goodsId);
}
