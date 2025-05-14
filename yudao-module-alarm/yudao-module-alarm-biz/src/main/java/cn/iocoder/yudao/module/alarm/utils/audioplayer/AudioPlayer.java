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
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

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

                clip.start();
                //开始后睡眠播放时间秒
                Thread.sleep((long) (totalSeconds * 1000L));
                //关闭
                clip.close();
            }
        }catch (Exception e){
            log.error("播放异常：",e);
        }
    }

    public void stopAudio() {
        if (clip != null) {
            // 停止播放
            clip.stop();

            // 关闭音频资源
            clip.close();
        }
    }






}
