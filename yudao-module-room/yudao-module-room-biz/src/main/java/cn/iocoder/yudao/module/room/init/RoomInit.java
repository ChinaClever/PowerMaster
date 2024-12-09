package cn.iocoder.yudao.module.room.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/5/22 15:08
 */
@Slf4j
@Component
public class RoomInit {


    @PostConstruct
    public void init(){
        log.info("room ......... init");
    }

}
