package cn.iocoder.yudao.module.system.service.upgrade;

import cn.iocoder.yudao.module.system.dal.dataobject.upgrade.SysUploadFileRecord;
import cn.iocoder.yudao.module.system.dal.mysql.upgrade.SysUploadFileRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 14:17
 * @Description: 文件上传
 */
@Service
public class UploadFileServiceImpl implements UploadFileService{

    @Resource
    SysUploadFileRecordMapper recordMapper;

    private BufferedOutputStream bufferedOutputStream = null;
    @Override
    public Integer uploadFile(int upgradeDev,List<Integer> roomIds,List<Integer> ipList, List<MultipartFile> files) {
        AtomicReference<String> uploadResult = new AtomicReference<>("");
        List<String> fileNames = new ArrayList<>();
        //项目路径
        String appPath = System.getProperty("user.dir");
        //文件保存路径
        String  path = appPath + "\\upgrade";
        try {
            if (!CollectionUtils.isEmpty(files)){
                files.forEach(file -> {
                    String result = fileUtil(file,path) ;
                    fileNames.add(file.getOriginalFilename());
                    uploadResult.set(uploadResult.get().concat(result).concat(";"));
                });
            }
            //上传记录
            SysUploadFileRecord record = SysUploadFileRecord.builder()
                    .uploadResult(uploadResult.get())
                    .fileNames(fileNames)
                    .filePath(path)
                    .upgradeDev( upgradeDev).build();
            return recordMapper.insert(record);

        }finally {
            //发送通知
            if (!uploadResult.get().contains("失败")){
                //没有上传失败文件
                //发送通知

            }
        }
    }


    private String fileUtil(MultipartFile file, String path) {

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(new File(path + file.getOriginalFilename()).toPath()));
                bufferedOutputStream.write(bytes);
                bufferedOutputStream.close();
                return file.getOriginalFilename() + "上传成功";
            } catch (Exception e) {
                return file.getOriginalFilename() + "上传失败，错误信息为：" + e;
            }
        } else {
            return "上传文件为空";
        }
    }
}
