package cn.iocoder.yudao.module.alarm.constants;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class BinLogConstants {
    @Value("${spring.datasource.dynamic.datasource.master.host}")
    private String host;

    @Value("${spring.datasource.dynamic.datasource.master.port}")
    private int port;

    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String passwd;

    @Value("${spring.datasource.dynamic.datasource.master.name}")
    private String db;

    private List<String> tableList;
}
