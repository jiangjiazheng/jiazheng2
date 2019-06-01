package cn.lxdl.controller.good;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.good.Goods;
import cn.lxdl.service.good.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * (运营商系统)商品管理查询分页列表数据
     *
     * @param page  当前页
     * @param rows  每页条数
     * @param goods 封装查询数据
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods) {
        return goodsService.searchByPageManager(page, rows, goods);
    }

    /**
     * 审核商品
     *
     * @param ids    商品id
     * @param status 审核状态
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            return new Result(true, "审核成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "审核失败");

    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.remove(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "删除失败");
    }
}
