package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import lombok.Data;

import java.util.List;

@Data
public class VoHdaLoopResVO {
        private Integer lineId;

        private List vol;

        private List cur;

        private List<String> dateTimes;
}
