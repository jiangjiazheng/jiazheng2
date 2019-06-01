package cn.lxdl.service.seller;

import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.seller.Seller;

import java.util.Map;

/**
 * 商家管理接口
 */
public interface SellerService {

    /**
     * 商家注册(入驻申请)
     */
    boolean save(Seller seller);

    /**
     * 商家分页列表数据
     */
    PageResult searchByPage(Integer curPage, Integer pageSize, Seller seller);

    /**
     * 根据id获取商家
     */
    Seller getById(String seller_id);

    /**
     * 商家审核
     */
    boolean updateStatus(String sellerId, String status);

    /**
     * 商家资料回显
     */
    Seller getSeller(String sellerId);

    /**
     * 商家资料修改
     */
    void updateSeller(Seller seller);

    void modifyPassword(String username, Map<String, String> passwordMap);
}
