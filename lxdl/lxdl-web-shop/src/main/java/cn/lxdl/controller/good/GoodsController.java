package cn.lxdl.controller.good;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.good.Goods;
import cn.lxdl.service.good.GoodsService;
import cn.lxdl.vo.GoodsVO;
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
     * 保存商家录入的商品
     *
     * @param goodsVO 封装前端传递过来的数据
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVO goodsVO) {
        try {
            //需要在系统中给goods封装seller_id(商家id即账户)
            //因为安全框架在系统中使用,获取安全框架中的username则需要在系统中获取,无法在服务获取.
            goodsVO.getGoods().setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());
            goodsService.save(goodsVO);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "保存失败");
    }

    /**
     * (商家系统)商品管理查询分页列表数据
     *
     * @param page  当前页
     * @param rows  每页条数
     * @param goods 封装查询数据
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods) {
        //封装登录用户username,从安全框架中获取.
        goods.setSellerId(SecurityContextHolder.getContext().getAuthentication().getName());

        return goodsService.searchByPage(page, rows, goods);
    }

    /**
     * 查询商品管理修改功能需要的商品实体
     *
     * @param id 商品id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsVO findOne(String id) {
        return goodsService.getById(id);
    }

    /**
     * 删除商品
     * (商家系统不能删除,
     * 小平台:运营商+商家系统===运营商系统
     * 大平台:第三方商家系统-->录入的商品(商家自己上架-->规范  其他商品:审核)
     *
     * @param ids 商品id(数组)
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            //goodsService.remove(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "删除失败");
    }

    /**
     * 修改商品
     *
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsVO goodsVO) {
        try {
            goodsService.update(goodsVO);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "修改失败");
    }


}
