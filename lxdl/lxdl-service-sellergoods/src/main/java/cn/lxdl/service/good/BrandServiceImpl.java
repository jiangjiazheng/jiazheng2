package cn.lxdl.service.good;


import cn.lxdl.dao.good.BrandDao;
import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.good.Brand;
import cn.lxdl.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Brand接口实现类
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Resource
    private BrandDao brandDao;

    public BrandServiceImpl() {
        System.out.println("创建brandServiceImpl");
    }

    @Override
    public List<Brand> getAll() {

        return brandDao.selectByExample(null);
    }

    @Override
    public PageResult getByPage(Integer curPage, Integer pageSize) {
        PageHelper.startPage(curPage, pageSize);
        //这个pageHelper提供的 page,继承了ArrayList,且封装分页数据.
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult searchByPage(Integer curPage, Integer pageSize, Brand brand) {
        PageHelper.startPage(curPage, pageSize);
        BrandQuery brandQuery = new BrandQuery();
        //为查询条件 where后的查询条件 BrandQuery.Criteria
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (!StringUtils.isEmpty(brand.getName())) {
            criteria.andNameLike("%" + brand.getName() + "%");
        }
        if (!StringUtils.isEmpty(brand.getFirstChar())) {
            criteria.andFirstCharEqualTo(brand.getFirstChar());
        }

        //添加按id降序
        brandQuery.setOrderByClause("id desc");

        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    @Override
    public Boolean save(Brand brand) {
        return brandDao.insertSelective(brand) > 0;
    }

    @Override
    public Brand getById(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public Boolean update(Brand brand) {
        return brandDao.updateByPrimaryKeySelective(brand) > 0;
    }

    @Transactional
    @Override
    public Boolean remove(Long[] ids) {
        if (ids != null && ids.length > 0) {
            //for (Long id : ids) {
            //    brandDao.deleteByPrimaryKey(id);
            //}
            //批量删除
            brandDao.deleteByPrimaryKeys(ids);
        }
        return true;
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
