package cn.lxdl.service.template;

import cn.lxdl.dao.specification.SpecificationOptionDao;
import cn.lxdl.dao.template.TypeTemplateDao;
import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.specification.SpecificationOption;
import cn.lxdl.pojo.specification.SpecificationOptionQuery;
import cn.lxdl.pojo.template.TypeTemplate;
import cn.lxdl.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品模板服务接口实现类
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public PageResult searchByPage(Integer curPage, Integer pageSize, TypeTemplate typeTemplate) {

        // 将模板数据写入到redis缓存中(预热)
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(null);
        if (typeTemplateList != null && typeTemplateList.size() > 0) {
            for (TypeTemplate template : typeTemplateList) {

                // 先封装品牌数据
                String brandIds = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);

                // 再封装规格数据 -->规格数据还需要规格选项数据.
                List<Map> specList = findBySpecList(String.valueOf(template.getId()));
                redisTemplate.boundHashOps("specList").put(template.getId(), specList);
            }
        }

        // 设置分页条件
        PageHelper.startPage(curPage, pageSize);

        // 设置查询条件
        TypeTemplateQuery query = new TypeTemplateQuery();
        if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName())) {
            query.createCriteria().andNameLike("%" + typeTemplate.getName() + "%");
        }
        query.setOrderByClause("id desc");

        // 查询
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(query);

        return new PageResult(p.getTotal(), p.getResult());
    }

    @Transactional
    @Override
    public Boolean save(TypeTemplate typeTemplate) {
        return typeTemplateDao.insertSelective(typeTemplate) > 0;
    }

    @Override
    public TypeTemplate getById(Integer id) {
        return typeTemplateDao.selectByPrimaryKey(Long.valueOf(id));
    }

    @Transactional
    @Override
    public Boolean update(TypeTemplate typeTemplate) {
        return typeTemplateDao.updateByPrimaryKeySelective(typeTemplate) > 0;
    }

    @Transactional
    @Override
    public Boolean remove(Long[] ids) {
        if (ids.length > 0) {
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
        return true;
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return typeTemplateDao.selectOptionList();
    }

    @Override
    public List<Map> findBySpecList(String id) {
        //根据模板id查找到模板
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(Long.valueOf(id));

        //获取关联规格
        //specIds在数据库的数据格式为json字符串格式:[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        //需要解析json,根据id获取规格选项
        String specIds = typeTemplate.getSpecIds();
        List<Map> list = JSON.parseArray(specIds, Map.class);
        if (list != null && list.size() > 0) {
            for (Map map : list) {
                String specId = map.get("id").toString();
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(Long.valueOf(specId));
                List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(query);
                map.put("options", specificationOptionList);
            }
        }

        return list;
    }
}
