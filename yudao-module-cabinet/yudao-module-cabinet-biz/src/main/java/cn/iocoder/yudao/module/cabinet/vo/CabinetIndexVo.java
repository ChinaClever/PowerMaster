package cn.iocoder.yudao.module.cabinet.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/28 13:32
 */
@Data
public class CabinetIndexVo extends PageParam {



    /**
     * 机柜id
     */
    private Integer cabinetId;

    /**
     * 机房编号
     */
    private Integer roomId;

    /**
     * 运行状态
     */
    private List<Integer> runStatus;

    /**
     * 数据来源
     */
    private Integer pduBox;

    /**
     * 所属于公司
     */
    private String company;

}
