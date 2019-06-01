package cn.lxdl.service.address;

import cn.lxdl.pojo.address.Address;
import cn.lxdl.pojo.address.Areas;
import cn.lxdl.pojo.address.Cities;
import cn.lxdl.pojo.address.Provinces;

import java.util.List;

/**
 * 地址服务接口
 */
public interface AddressService {

    /**
     * 获取当前登录用户的所有收件地址
     *
     * @param userId 用户名
     * @return
     */
    List<Address> getListByLoginUser(String userId);

    /**
     * 更改用户默认的收件地址
     *
     * @param addressId 地址id
     */
    void updateIsDefault(String addressId,String username);

    /**
     * 查找省份列表
     *
     * @return
     * @Author 练志达
     */
    List<Provinces> getProvincesList();

    /**
     * 根据省份id查找城市列表
     *
     * @Author 练志达
     */
    List<Cities> getCitiesListByProvinceid(String provinceId);

    /**
     * 根据城市id查找区镇列表
     *
     * @Author 练志达
     */
    List<Areas> getAreasListByCityId(String cityId);

    /**
     * 根据id查询收货地址
     *
     * @Author 练志达
     */
    Address getAddressOne(String addressId);

    /**
     * 根据id删除收货地址
     *
     * @Author 练志达
     */
    void removeAddress(String addressId);

    /**
     * 修改地址
     *
     * @Author 练志达
     */
    void updateAddress(Address address);

    /**
     * 保存地址
     *
     * @Author 练志达
     */
    void saveAddress(Address address);

    /**
     * 将详细地址拼接上省市区
     *
     * @param addressList
     * @return
     * @Author 练志达
     */
    List<Address> getDetailedAddress(List<Address> addressList);
}
