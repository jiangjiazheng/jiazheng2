package cn.lxdl.service.template;

import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

/**
 * 商品模板服务接口
 */
public interface TypeTemplateService {
    /**
     * 有条件分页查询
     */
    PageResult searchByPage(Integer curPage, Integer pageSize, TypeTemplate typeTemplate);

    /**
     * 增加
     */
    Boolean save(TypeTemplate typeTemplate);

    /**
     * 根据id查询
     */
    TypeTemplate getById(Integer id);

    /**
     * 修改(更新)
     */
    Boolean update(TypeTemplate typeTemplate);

    /**
     * 删除
     */
    Boolean remove(Long[] ids);

    /**
     * 为下拉框获取商品模板数据
     */
    List<Map<String,String>> selectOptionList();

    /**
     * 新增商品页面,根据模板id查找规格以及规格选项
     */
    List<Map> findBySpecList(String id);
}
