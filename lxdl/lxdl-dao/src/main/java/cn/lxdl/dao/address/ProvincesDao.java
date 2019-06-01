package cn.lxdl.dao.address;

import cn.lxdl.pojo.address.Provinces;
import cn.lxdl.pojo.address.ProvincesQuery;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ProvincesDao {
    int countByExample(ProvincesQuery example);

    int deleteByExample(ProvincesQuery example);

    int deleteByPrimaryKey(Integer id);

    int insert(Provinces record);

    int insertSelective(Provinces record);

    List<Provinces> selectByExample(ProvincesQuery example);

    Provinces selectByPrimaryKey(Integer id);

    /**
     * 根据省份id查询省份
     *
     * @param id 省份id
     * @return
     */
    Provinces selectByProvinceId(Integer id);

    int updateByExampleSelective(@Param("record") Provinces record, @Param("example") ProvincesQuery example);

    int updateByExample(@Param("record") Provinces record, @Param("example") ProvincesQuery example);

    int updateByPrimaryKeySelective(Provinces record);

    int updateByPrimaryKey(Provinces record);
}