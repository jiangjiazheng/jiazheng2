package cn.lxdl.dao.address;

import cn.lxdl.pojo.address.Cities;
import cn.lxdl.pojo.address.CitiesQuery;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface CitiesDao {
    int countByExample(CitiesQuery example);

    int deleteByExample(CitiesQuery example);

    int deleteByPrimaryKey(Integer id);

    int insert(Cities record);

    int insertSelective(Cities record);

    List<Cities> selectByExample(CitiesQuery example);

    Cities selectByPrimaryKey(Integer id);

    /**
     * 根据城市id查询
     *
     * @param id 城市id
     * @return
     */
    Cities selectByCityid(Integer id);

    int updateByExampleSelective(@Param("record") Cities record, @Param("example") CitiesQuery example);

    int updateByExample(@Param("record") Cities record, @Param("example") CitiesQuery example);

    int updateByPrimaryKeySelective(Cities record);

    int updateByPrimaryKey(Cities record);
}