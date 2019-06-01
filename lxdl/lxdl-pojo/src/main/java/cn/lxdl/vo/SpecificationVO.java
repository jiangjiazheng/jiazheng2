package cn.lxdl.vo;

import cn.lxdl.pojo.specification.Specification;
import cn.lxdl.pojo.specification.SpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * value object 封装前端传递过来的封装规格以及规格对应的选项
 * 序列化 实现Serializable接口
 */
public class SpecificationVO implements Serializable {

    private Specification specification;                        // 规格
    private List<SpecificationOption> specificationOptionList;    // 规格选项

    public SpecificationVO(Specification specification, List<SpecificationOption> specificationOptionList) {
        this.specification = specification;
        this.specificationOptionList = specificationOptionList;
    }

    public SpecificationVO() {
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    public List<SpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<SpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
