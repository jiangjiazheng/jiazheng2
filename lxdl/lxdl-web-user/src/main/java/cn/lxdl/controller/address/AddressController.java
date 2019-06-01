package cn.lxdl.controller.address;

import cn.lxdl.entity.Result;
import cn.lxdl.pojo.address.Address;
import cn.lxdl.pojo.address.Areas;
import cn.lxdl.pojo.address.Cities;
import cn.lxdl.pojo.address.Provinces;
import cn.lxdl.service.address.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地址功能控制器
 */
@RestController
@RequestMapping("/address")
public class AddressController {
    @Reference
    private AddressService addressService;

    /**
     * 查找用户收获地址
     *
     * @return
     * @Author 练志达
     */
    @RequestMapping("/findUserAddress")
    public List<Address> findUserAddress() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 获取用户收获地址
        List<Address> addressList = addressService.getListByLoginUser(username);
        // 将详细地址拼接上省市区
        List<Address> detailedAddressList = addressService.getDetailedAddress(addressList);
        return detailedAddressList;
    }

    /**
     * 更改用户默认收获地址
     *
     * @Author 练志达
     */
    @RequestMapping("/updateIsDefault")
    public Result updateIsDefault(String addressId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            addressService.updateIsDefault(addressId,username);
            return new Result(true, "更改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "更改失败");
    }

    /**
     * 查询省份列表
     *
     * @return
     * @Author 练志达
     */
    @RequestMapping("/findProvincesList")
    private List<Provinces> findProvincesList() {
        return addressService.getProvincesList();
    }

    /**
     * 根据省份id查找城市列表
     *
     * @Author 练志达
     */
    @RequestMapping("/findCitiesListByProvinceId")
    private List<Cities> findCitiesListByProvinceid(String provinceId) {
        return addressService.getCitiesListByProvinceid(provinceId);
    }

    /**
     * 根据城市id查找区镇列表
     *
     * @Author 练志达
     */
    @RequestMapping("/fingAreasListByCityId")
    private List<Areas> fingAreasListByCityId(String cityId) {
        return addressService.getAreasListByCityId(cityId);
    }

    /**
     * 根据id查询收货地址
     *
     * @Author 练志达
     */
    @RequestMapping("/findAddressOne")
    private Address findAddressOne(String addressId) {
        return addressService.getAddressOne(addressId);
    }

    /**
     * 根据id删除收货地址
     *
     * @Author 练志达
     */
    @RequestMapping("/removeAddress")
    private Result removeAddress(String addressId) {
        try {
            addressService.removeAddress(addressId);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "删除失败");
    }

    /**
     * 修改地址
     *
     * @Author 练志达
     */
    @RequestMapping("/updateAddress")
    private Result updateAddress(@RequestBody Address address) {
        try {
            addressService.updateAddress(address);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "保存失败");
    }

    /**
     * 保存地址
     *
     * @Author 练志达
     */
    @RequestMapping("/saveAddress")
    private Result saveAddress(@RequestBody Address address) {
        try {
            addressService.saveAddress(address);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "保存失败");
    }

}
