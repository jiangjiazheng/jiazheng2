package cn.lxdl.service.good;

import cn.lxdl.entity.PageResult;
import cn.lxdl.entity.Result;
import cn.lxdl.pojo.good.Brand;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询全部
     * @return
     */
    List<Brand> getAll();

    /**
     * 分页查询
     * curPage:当前页
     * pageSize:每页显示条数
     */
    PageResult getByPage(Integer curPage,Integer pageSize);

    /**
     * 有查询条件的分页查询
     */
    PageResult searchByPage(Integer curPage,Integer pageSize,Brand brand);

    /**
     * 添加品牌
     */
    Boolean save(Brand brand);

    /**
     * 根据id查询品牌
     */
    Brand getById(Long id);

    /**
     * 更新品牌
     */
    Boolean update(Brand brand);

    /**
     * 删除品牌
     */
    Boolean remove(Long[] ids);

    /**
     * 为下拉框获取品牌数据
     */
    List<Map<String,String>> selectOptionList();

}
