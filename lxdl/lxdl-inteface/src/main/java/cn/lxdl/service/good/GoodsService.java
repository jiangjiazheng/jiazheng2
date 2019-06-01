package cn.lxdl.service.good;

import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.good.Goods;
import cn.lxdl.vo.GoodsVO;

/**
 * 商品服务接口
 */
public interface GoodsService {
    /**
     * 保存商品
     */
    void save(GoodsVO goodsVO);

    /**
     * (商家系统)查询分页列表数据
     */
    PageResult searchByPage(Integer page, Integer rows, Goods goods);

    /**
     * (运营商系统)查询分页列表数据
     */
    PageResult searchByPageManager(Integer page, Integer rows, Goods goods);

    /**
     * 查询商品VO
     */
    GoodsVO getById(String id);

    /**
     * 批量删除商品
     */
    void remove(Long[] ids);

    /**
     * 修改
     */
    void update(GoodsVO goodsVO);

    /**
     * 审核商品
     *
     * @param ids    审核id
     * @param status 审核状态
     */
    void updateStatus(Long[] ids, String status);
}
