package cn.lxdl.service.search;

import java.util.Map;

/**
 * 商品检索
 */
public interface ItemSearchService {

    /**
     * 商品检索
     * 入参用Map封装,出参也用Map封装
     */
    Map<String,Object> search(Map<String,String> searchMap);

    /**
     * 商品上架,加入到索引库中
     */
    void saveItemToSolr(Long goodsId);

    /**
     * 商品删除,从索引库中删除商品
     */
    void removeItemToSolr(Long goodsId);

}
