package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

@Data
public class CabinetEnvAndHumRes {

    private Integer id;

    private String  location;

    private Double iceTopTem;

    private Double iceTopHum;

    private Double iceMidTem;

    private Double iceMidHum;

    private Double iceBomTem;

    private Double iceBomHum;

    private Double hotTopTem;

    private Double hotTopHum;

    private Double hotMidTem;

    private Double hotMidHum;

    private Double hotBomTem;

    private Double hotBomHum;
}
