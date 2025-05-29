package cn.iocoder.yudao.module.alarm.utils.audioplayer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.sound.sampled.*;
import java.io.InputStream;

@Component
@Slf4j
public class AudioPlayer {

    private Clip clip;

    /**
     * 初始化音频资源（仅加载一次）
     */
    @PostConstruct
    public void init() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ALARM.WAV");
            if (inputStream == null) {
                log.error("音频文件未找到，请确认路径是否正确");
                return;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                log.info("Line not supported for this audio format.");
                return;
            }

            this.clip = (Clip) AudioSystem.getLine(info);
            this.clip.open(audioInputStream);

        } catch (Exception e) {
            log.error("初始化音频失败：", e);
        }
    }

    /**
     * 播放声音
     */
    public void playAudio() {

        try {
            if (clip == null) {
                init();
            }
            if (clip != null && !clip.isRunning()) {
                clip.setFramePosition(0); // 重置到开头
                clip.loop(2); // 循环播放3次
            }
        } catch (Exception e) {
            log.error("播放异常：", e);
        }
    }


    public void stopAudio() {
        if (clip != null) {
            clip.stop(); // 停止播放
            clip.close(); // 关闭音频资源
            clip = null;//  释放资源，便于GC回收
        }
    }







}
