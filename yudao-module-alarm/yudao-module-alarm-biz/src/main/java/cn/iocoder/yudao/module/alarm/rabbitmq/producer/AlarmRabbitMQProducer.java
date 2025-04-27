package cn.iocoder.yudao.module.alarm.rabbitmq.producer;

import cn.iocoder.yudao.module.alarm.rabbitmq.Messages.AlarmMqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlarmRabbitMQProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public AlarmRabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        // 设置发送确认回调
        rabbitTemplate.setConfirmCallback(this::confirmCallback);
        // 设置返回回调（可选）
        rabbitTemplate.setReturnCallback(this::returnCallback);
    }



    public void sendMessage(String routingKey, String message) {
        CorrelationData correlationData = new CorrelationData(message);
        rabbitTemplate.convertAndSend(AlarmMqMessage.EXCHANGE_ALARM, routingKey, message, correlationData);
        log.info("Sent message: " + message + " to routing key: " + routingKey);
    }

    // 发送确认回调
    public void confirmCallback(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("Message confirmed: " + correlationData.getId());
        } else {
            log.error("Message sending failed: " + correlationData.getId() + ", cause: " + cause);
            // 在这里进行消息发送失败后的处理逻辑
        }
    }

    // 返回回调（处理消息无法路由到队列的情况）
    public void returnCallback(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("Message returned: " + message + ", replyCode: " + replyCode + ", replyText: " + replyText);
        // 在这里进行消息无法路由到队列的处理逻辑
    }
}