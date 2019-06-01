package cn.lxdl.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 测试序列化
 */
public class Test implements Serializable {
    private long total;
    private List result;

    public Test(long total, List result) {
        this.total = total;
        this.result = result;
    }

    public Test() {
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getResult() {
        return result;
    }

    public void setResult(List result) {
        this.result = result;
    }
}
