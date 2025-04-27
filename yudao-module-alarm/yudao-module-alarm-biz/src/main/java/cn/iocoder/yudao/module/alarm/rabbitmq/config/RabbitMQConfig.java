package cn.iocoder.yudao.module.alarm.rabbitmq.config;

import cn.iocoder.yudao.module.alarm.rabbitmq.Messages.AlarmMqMessage;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(AlarmMqMessage.EXCHANGE_ALARM);
    }

    @Bean
    public Queue queueAlarm() {
        return new Queue(AlarmMqMessage.QUEUE_ALARM, true);
    }

    @Bean
    public Binding bindingAlarm (DirectExchange directExchange, Queue queueAlarm) {
        return BindingBuilder.bind(queueAlarm).to(directExchange).with(AlarmMqMessage.KEY_ALARM);
    }

}