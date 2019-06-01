package cn.lxdl.service.content;


import cn.lxdl.dao.ad.ContentDao;
import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.ad.Content;
import cn.lxdl.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Resource
    private ContentDao contentDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Content> page = (Page<Content>) contentDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    @Override
    public void add(Content content) {
        contentDao.insertSelective(content);

        // 添加广告时候清除缓存中的数据
        clearCacheForRedis(content.getCategoryId());
    }

    @Transactional
    @Override
    public void edit(Content content) {
        // 修改广告时候清除缓存中的数据,ps:需要注意商品分类id是否修改了.
        // 先查找到旧数据
        Content oldContent = contentDao.selectByPrimaryKey(content.getId());
        if (oldContent.getCategoryId() != content.getCategoryId()) {
            //分类发生改变
            clearCacheForRedis(oldContent.getCategoryId());
        }

        clearCacheForRedis(content.getCategoryId());

        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Transactional
    @Override
    public void delAll(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //删除广告时候清除缓存中的数据
                clearCacheForRedis(contentDao.selectByPrimaryKey(id).getCategoryId());

                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 清除缓存
     *
     * @param categoryId 广告分类id
     */
    public void clearCacheForRedis(Long categoryId) {
        redisTemplate.boundHashOps("content").delete(categoryId);
    }

    /**
     * 进行优化,使用Hash,减少与redis客服端交互的次数.
     * 缓存击穿,加锁排队
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Content> findByCategoryId(String categoryId) {
        //先从缓存中查找
        List<Content> contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        //判断缓存中是否存在
        if (contentList == null) {
            synchronized (this) {
                contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
                if (contentList == null) {
                    //若无,则去数据库中查询
                    ContentQuery contentQuery = new ContentQuery();
                    if (categoryId != null) {
                        contentQuery.createCriteria().andCategoryIdEqualTo(Long.valueOf(categoryId));
                    }
                    contentList = contentDao.selectByExample(contentQuery);
                    //存进缓存中
                    redisTemplate.boundHashOps("content").put(categoryId, contentList);
                }
            }
        }
        return contentList;
    }
}
