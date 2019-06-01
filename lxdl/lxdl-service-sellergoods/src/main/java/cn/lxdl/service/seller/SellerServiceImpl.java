package cn.lxdl.service.seller;

import cn.lxdl.dao.seller.SellerDao;
import cn.lxdl.entity.PageResult;
import cn.lxdl.pojo.seller.Seller;
import cn.lxdl.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.management.Query;
import java.util.List;
import java.util.Map;

/**
 * 商家管理服务
 */
@Service
public class SellerServiceImpl implements SellerService {
    @Resource
    private SellerDao sellerDao;

    @Transactional
    @Override
    public boolean save(Seller seller) {
        //设置需要手动封装的参数
        //设置商家的审核状态: 0 待审核
        seller.setStatus("0");
        //密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(seller.getPassword());
        seller.setPassword(encode);

        sellerDao.insertSelective(seller);
        return true;
    }

    @Override
    public PageResult searchByPage(Integer curPage, Integer pageSize, Seller seller) {
        //设置分页条件
        PageHelper.startPage(curPage, pageSize);

        //设置查询条件
        SellerQuery sellerQuery = new SellerQuery();
        SellerQuery.Criteria criteria = sellerQuery.createCriteria();
        if (seller != null) {
            if (!StringUtils.isEmpty(seller.getName())) {
                criteria.andNameLike("%" + seller.getName() + "%");
            }
            if (!StringUtils.isEmpty(seller.getNickName())) {
                criteria.andNickNameLike("%" + seller.getNickName() + "%");
            }
            if (!StringUtils.isEmpty(seller.getStatus())) {
                criteria.andStatusEqualTo(seller.getStatus());
            }
        }
        //根据创建日期降序
        //sellerQuery.setOrderByClause("createTime desc");

        //查询
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public Seller getById(String seller_id) {
        return sellerDao.selectByPrimaryKey(seller_id);
    }

    @Transactional
    @Override
    public boolean updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
        return true;
    }

    @Override
    public void modifyPassword(String username, Map<String, String> passwordMap) {
        String newPassword = passwordMap.get("newPassword");
        String fixPassword = passwordMap.get("fixPassword");
        // 先用新密码 和 重复密码做对比，如果不同返回false并提示密码重复密码错误，相同继续下一步
        if (fixPassword.equals(newPassword)) {
            // 根据当前用户名去数据库中获取数据库中的用户密码
            SellerQuery query = new SellerQuery();
            query.createCriteria().andSellerIdEqualTo(username);
            List<Seller> sellerList = sellerDao.selectByExample(query);
            Seller seller = sellerList.get(0);
            String dbPassword = seller.getPassword();
            String oldPassword = passwordMap.get("oldPassword");
            // 然后使用 原密码 和 数据库中查出来的 密码 做对比错误 提示密码错误，正确继续下一步
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean result = encoder.matches(oldPassword, dbPassword);
            // 将正确的新密码加密存储到数据库
            if (result) {
                newPassword = encoder.encode(newPassword);
                seller.setPassword(newPassword);
                sellerDao.updateByPrimaryKeySelective(seller);
            } else {
                throw new RuntimeException("原密码输入错误");
            }
            // end
        } else {
            throw new RuntimeException("重复密码与新密码不一致");
        }

    }

    @Override
    public Seller getSeller(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    @Override
    public void updateSeller(Seller seller) {
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
