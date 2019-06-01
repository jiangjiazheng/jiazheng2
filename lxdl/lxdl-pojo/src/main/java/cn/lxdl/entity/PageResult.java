package cn.lxdl.entity;

import cn.lxdl.pojo.good.Brand;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询返回给前端的参数
 */
public class PageResult implements Serializable {
    /**
     * 总条数
     */
    private Long total;
    /**
     * Brand分页每页数据
     */
    private List rows;

    public PageResult() {
    }

    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
