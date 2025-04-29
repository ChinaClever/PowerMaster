package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class CabinetEnvResVO {

    private Integer id;

    private String cabinetName;

    private Integer roomId;

    private String roomName;


    private String location;

    private String address;

    private String createTime;

    private List black;

    private List front;

}
