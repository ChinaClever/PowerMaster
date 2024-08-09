package cn.iocoder.yudao.module.system.service.upgrade;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.FileRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UpgradeFileRespVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.vo.UpgradeRecordRespVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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

    /**
     * 分页文件上传记录
     * @param pageReqVO
     * @return
     */
    PageResult<UpgradeFileRespVO> getFileRecordPage(FileRecordPageReqVO pageReqVO);

    /**
     * 删除文件上传记录
     * @param ids
     * @return
     */
    Boolean deleteFileRecord(List<Integer> ids);

    /**
     * 分页升级记录
     * @param pageReqVO
     * @return
     */
    PageResult<UpgradeRecordRespVO> getRecordPage(UpgradeRecordPageReqVO pageReqVO);

    /**
     * 删除升级记录
     * @param ids
     * @return
     */
    Boolean deleteRecord(List<Integer> ids);
}
