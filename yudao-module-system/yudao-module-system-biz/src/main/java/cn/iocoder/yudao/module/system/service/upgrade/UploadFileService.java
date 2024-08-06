package cn.iocoder.yudao.module.system.service.upgrade;

import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UploadFileReqVo;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 14:08
 * @Description: 文件上传
 */
public interface UploadFileService {


    /**
     * 上传文件
     */
    Integer uploadFile(int upgradeDev,List<Integer> roomIds,List<String> ipList, List<MultipartFile> files);

    /**
     * 文件下载
     * @param request
     * @param response
     */
    void downloadFile(HttpServletRequest request, HttpServletResponse response);

    /**
     * 结果通知
     * @param devIp
     * @param code
     * @param message
     */
    void notice(String devIp, String code, String message);

    /**
     * 重发
     * @param id
     * @return
     */
    Integer reNotice(Long id);
}
