package cn.lxdl.controller.seller;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.seller.Seller;
import cn.lxdl.service.seller.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家审核
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
    private SellerService sellerService;

    /**
     * 商家审核页面分页列表
     *
     * @param page   当前页
     * @param rows   每页显示条数
     * @param seller 封装前端查询条件
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller) {
        return sellerService.searchByPage(page, rows, seller);
    }

    /**
     * 查看商家详情
     *
     * @param id 商家id(账户)
     * @return
     */
    @RequestMapping("/findOne")
    public Seller findOne(String id) {
        return sellerService.getById(id);
    }

    /**
     * 商家审核
     *
     * @param sellerId 商家id(账户)
     * @param status   审核状态 0:未审核 1:审核通过 2:审核未通过 3:关闭商家
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId, String status) {
        try {
            sellerService.updateStatus(sellerId, status);
            return new Result(true, "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "操作失败");
    }
}
