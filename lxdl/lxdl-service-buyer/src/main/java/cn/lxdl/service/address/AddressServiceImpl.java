package cn.lxdl.service.address;

import cn.lxdl.dao.address.AddressDao;
import cn.lxdl.dao.address.AreasDao;
import cn.lxdl.dao.address.CitiesDao;
import cn.lxdl.dao.address.ProvincesDao;
import cn.lxdl.pojo.address.*;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Resource
    private AddressDao addressDao;
    @Resource
    private ProvincesDao provincesDao;
    @Resource
    private CitiesDao citiesDao;
    @Resource
    private AreasDao areasDao;

    @Override
    public List<Address> getListByLoginUser(String userId) {
        AddressQuery query = new AddressQuery();
        if (!StringUtils.isEmpty(userId)) {
            query.createCriteria().andUserIdEqualTo(userId);
        }
        return addressDao.selectByExample(query);
    }

    @Transactional
    @Override
    public void updateIsDefault(String addressId, String username) {
        AddressQuery query = new AddressQuery();
        if (!StringUtils.isEmpty(username)) {
            query.createCriteria().andUserIdEqualTo(username);
        }
        List<Address> addressList = addressDao.selectByExample(query);

        Address oldAddress = null;
        Address newAddress = null;

        if (addressList != null && addressList.size() > 0) {
            for (Address address : addressList) {
                // 找到旧的默认地址信息
                if ("1".equals(address.getIsDefault()) && !addressId.equals(String.valueOf(address.getId()))) {
                    oldAddress = address;
                    oldAddress.setIsDefault("0");
                }
                // 根据addressId查询到要更改默认用户的地址
                if (addressId.equals(String.valueOf(address.getId()))) {
                    newAddress = address;
                    newAddress.setIsDefault("1");
                }
                // 若两者都找出,就跳出循环
                if (oldAddress != null && newAddress != null) {
                    break;
                }
            }
        }

        // 更新两者默认状态
        if (oldAddress != null && newAddress != null) {
            addressDao.updateByPrimaryKeySelective(oldAddress);
            addressDao.updateByPrimaryKeySelective(newAddress);
        }
    }

    @Override
    public List<Provinces> getProvincesList() {
        return provincesDao.selectByExample(null);
    }

    @Override
    public List<Cities> getCitiesListByProvinceid(String provinceId) {
        CitiesQuery query = new CitiesQuery();
        query.createCriteria().andProvinceidEqualTo(provinceId);
        return citiesDao.selectByExample(query);
    }

    @Override
    public List<Areas> getAreasListByCityId(String cityId) {
        AreasQuery query = new AreasQuery();
        query.createCriteria().andCityidEqualTo(cityId);
        return areasDao.selectByExample(query);
    }

    @Override
    public Address getAddressOne(String addressId) {
        return addressDao.selectByPrimaryKey(Long.parseLong(addressId));
    }

    @Transactional
    @Override
    public void removeAddress(String addressId) {
        addressDao.deleteByPrimaryKey(Long.parseLong(addressId));
    }

    @Transactional
    @Override
    public void updateAddress(Address address) {
        addressDao.updateByPrimaryKeySelective(address);
    }

    @Transactional
    @Override
    public void saveAddress(Address address) {
        addressDao.insertSelective(address);
    }

    @Override
    public List<Address> getDetailedAddress(List<Address> addressList) {
        if (addressList != null && addressList.size() > 0) {
            // 对集合中地址字段进行处理拼接上省市区
            for (Address address : addressList) {
                // 获取需要拼接上省市区的地址
                String detailedAddress = address.getAddress();
                StringBuilder builder = new StringBuilder();

                // 获取省份
                if (address.getProvinceId() != null) {
                    Provinces provinces = provincesDao.selectByProvinceId(Integer.valueOf(address.getProvinceId()));
                    if (provinces != null) {
                        builder.append(provinces.getProvince()).append(" ");
                    }
                }

                // 获取城市
                if (address.getCityId() != null) {
                    Cities cities = citiesDao.selectByCityid(Integer.valueOf(address.getCityId()));
                    if (cities != null) {
                        builder.append(cities.getCity()).append(" ");
                    }
                }

                // 获取区镇
                if (address.getTownId() != null) {
                    Areas areas = areasDao.selectByAreaid(Integer.valueOf(address.getTownId()));
                    if (areas != null) {
                        builder.append(areas.getArea()).append(" ");
                    }
                }
                builder.append(detailedAddress);
                address.setAddress(builder.toString());
            }
        }
        return addressList;
    }
}
