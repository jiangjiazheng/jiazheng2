package cn.lxdl.service.content;

import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.ad.Content;

import java.util.List;


public interface ContentService {

    public List<Content> findAll();

    public PageResult findPage(Content content, Integer pageNum, Integer pageSize);

    public void add(Content content);

    public void edit(Content content);

    public Content findOne(Long id);

    public void delAll(Long[] ids);

    /**
     * 加载首页大广告的轮播图
     * @param categoryId
     * @return
     */
    List<Content> findByCategoryId(String categoryId);
}
