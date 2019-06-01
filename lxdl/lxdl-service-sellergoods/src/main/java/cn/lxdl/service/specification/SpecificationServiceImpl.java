package cn.lxdl.service.specification;

import cn.lxdl.dao.specification.SpecificationDao;
import cn.lxdl.dao.specification.SpecificationOptionDao;
import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.specification.Specification;
import cn.lxdl.pojo.specification.SpecificationOption;
import cn.lxdl.pojo.specification.SpecificationOptionQuery;
import cn.lxdl.pojo.specification.SpecificationQuery;
import cn.lxdl.vo.SpecificationVO;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 规范功能接口实现类
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Resource
    private SpecificationDao specificationDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Override
    public List<Specification> getAll() {
        return specificationDao.selectByExample(null);
    }

    @Override
    public PageResult getByPage(Integer curPage, Integer pageSize, Specification specification) {
        //设置分页条件
        PageHelper.startPage(curPage, pageSize);
        //设置查询条件
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (specification != null && !StringUtils.isEmpty(specification.getSpecName())) {
            criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
        }
        specificationQuery.setOrderByClause("id desc");
        //查询
        Page<Specification> page = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    @Override
    public boolean save(SpecificationVO specificationVO) {
        //先保存规格
        //ps:需要插入的同时返回id,返回的id会保存在specification对象中,调用方法返回的值是成功执行的条数
        Specification specification = specificationVO.getSpecification();
        specificationDao.insertSelective(specification);

        //后保存规格选项
        List<SpecificationOption> sol = specificationVO.getSpecificationOptionList();
        if (sol != null && sol.size() > 0) {
            for (SpecificationOption specificationOption : sol) {
                //存入外键id
                specificationOption.setSpecId(specification.getId());
            }
            specificationOptionDao.insertSelectives(sol);
        }
        return true;
    }

    @Override
    public SpecificationVO getById(Integer id) {
        //查询规格
        Specification specification = specificationDao.selectByPrimaryKey(Long.valueOf(id));

        //封装查询规格选项的条件
        SpecificationOptionQuery soo = new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = soo.createCriteria();
        criteria.andSpecIdEqualTo(specification.getId());
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(soo);

        //封装成SpecificationVO返回
        return new SpecificationVO(specification, specificationOptionList);
    }

    @Transactional
    @Override
    public boolean update(SpecificationVO specificationVO) {
        //更新规格
        Specification specification = specificationVO.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);

        //更新规格选项(ps:先删除旧数据,再插入新数据)
        List<SpecificationOption> sol = specificationVO.getSpecificationOptionList();
        //封装删除条件(删除旧数据,根据外键删除)
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);

        //插入新数据
        if (sol != null && sol.size() > 0) {
            for (SpecificationOption specificationOption : sol) {
                //存入外键id
                specificationOption.setSpecId(specification.getId());
            }
            specificationOptionDao.insertSelectives(sol);
        }

        return true;
    }

    @Transactional
    @Override
    public boolean remove(Long[] ids) {
        //先删除从表-->规格选项数据
        if (ids.length > 0) {
            for (Long id : ids) {
                //封装删除条件(删除旧数据,根据外键删除)
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);

                //再删除主表数据
                specificationDao.deleteByPrimaryKey(id);
            }
        }
        return true;
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
