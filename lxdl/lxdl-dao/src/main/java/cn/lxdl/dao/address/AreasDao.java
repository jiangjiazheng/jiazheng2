package cn.lxdl.dao.address;

import cn.lxdl.pojo.address.Areas;
import cn.lxdl.pojo.address.AreasQuery;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface AreasDao {
    int countByExample(AreasQuery example);

    int deleteByExample(AreasQuery example);

    int deleteByPrimaryKey(Integer id);

    int insert(Areas record);

    int insertSelective(Areas record);

    List<Areas> selectByExample(AreasQuery example);

    Areas selectByPrimaryKey(Integer id);

    /**
     * 根据区域,镇id查询
     *
     * @param id
     * @return
     */
    Areas selectByAreaid(Integer id);

    int updateByExampleSelective(@Param("record") Areas record, @Param("example") AreasQuery example);

    int updateByExample(@Param("record") Areas record, @Param("example") AreasQuery example);

    int updateByPrimaryKeySelective(Areas record);

    int updateByPrimaryKey(Areas record);
}