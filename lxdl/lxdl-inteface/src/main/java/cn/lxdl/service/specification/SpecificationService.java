package cn.lxdl.service.specification;

import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.specification.Specification;
import cn.lxdl.vo.SpecificationVO;

import java.util.List;
import java.util.Map;

/**
 * 规格功能接口
 */
public interface SpecificationService {

    /**
     * 查询全部
     */
    List<Specification> getAll();

    /**
     * 分页查询
     *
     * --新增:(搜索)根据条件查询,并且分页
     */
    PageResult getByPage(Integer curPage, Integer pageSize, Specification specification);

    /**
     * 保存
     */
    boolean save(SpecificationVO specificationVO);

    /**
     * 根据id查询
     *
     * bug:没有去查询规格选项,要仔细去分析.
     */
    SpecificationVO getById(Integer id);

    /**
     * 修改规格
     */
    boolean update(SpecificationVO specificationVO);

    /**
     * 删除规格
     */
    boolean remove(Long[] ids);

    /**
     * 为下拉框获取规格数据
     */
    List<Map<String,String>> selectOptionList();
}
