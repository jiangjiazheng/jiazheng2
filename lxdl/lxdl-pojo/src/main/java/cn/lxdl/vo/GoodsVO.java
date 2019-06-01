package cn.lxdl.vo;

import cn.lxdl.pojo.good.Goods;
import cn.lxdl.pojo.good.GoodsDesc;
import cn.lxdl.pojo.item.Item;

import java.io.Serializable;
import java.util.List;

public class GoodsVO implements Serializable {
    private Goods goods;          //商品基础数据
    private GoodsDesc goodsDesc;  //商品描述数据
    private List<Item> itemList;            //库存(规格,颜色等信息)  1 --> n

    public GoodsVO() {
    }

    public GoodsVO(Goods goods, GoodsDesc goodsDesc, List<Item> itemList) {
        this.goods = goods;
        this.goodsDesc = goodsDesc;
        this.itemList = itemList;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }
}



