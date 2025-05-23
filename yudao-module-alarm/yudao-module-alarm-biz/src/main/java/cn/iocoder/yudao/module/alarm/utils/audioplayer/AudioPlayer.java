package cn.iocoder.yudao.module.alarm.utils.audioplayer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.InputStream;

@Component
@Slf4j
public class AudioPlayer {

    private Clip clip;


    /**
     * 播放声音
     */
    public void playAudio() {

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ALARM.WAV");

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            AudioFormat format = audioInputStream.getFormat();
            if (!AudioSystem.isLineSupported(new DataLine.Info(Clip.class, format))) {
                log.info("Line not supported for this audio format.");
            } else {
                this.clip = AudioSystem.getClip(); // 使用类成员变量
                this.clip.open(audioInputStream);

                // 获取音频格式
                AudioFormat audioFormat = audioInputStream.getFormat();
                // 获取帧长度（如果可用）
                long frameLength = audioInputStream.getFrameLength();

                // 如果音频流没有指定帧长度，则需要找到音频文件的总字节数
                if (frameLength == AudioSystem.NOT_SPECIFIED) {
                    frameLength = audioInputStream.getFrameLength() * audioFormat.getFrameSize();
                }

                // 计算总播放时间（秒）
                float frameRate = audioFormat.getFrameRate();
                // 秒
                float totalSeconds = frameLength / frameRate;

                this.clip.loop(Clip.LOOP_CONTINUOUSLY); // 循环播放
            }
        } catch (Exception e) {
            log.error("播放异常：", e);
        }
    }


    public void stopAudio() {
        if (clip != null) {
            clip.stop(); // 停止播放
            clip.close(); // 关闭音频资源
        }
    }







}
