package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.Data;

@Data
public class CabinetEnvAndHumRes {

    private Integer id;

    private String  location;

    private Double iceTopTem;

    private String iceTopTemColor;

    private Double iceTopHum;

    private Double iceMidTem;

    private String iceMidTemColor;

    private Double iceMidHum;

    private Double iceBomTem;

    private String iceBomTemColor;

    private Double iceBomHum;

    private Double hotTopTem;

    private String hotTopTemColor;

    private Double hotTopHum;

    private Double hotMidTem;

    private String hotMidTemColor;

    private Double hotMidHum;

    private Double hotBomTem;

    private String hotBomTemColor;

    private Double hotBomHum;
}
