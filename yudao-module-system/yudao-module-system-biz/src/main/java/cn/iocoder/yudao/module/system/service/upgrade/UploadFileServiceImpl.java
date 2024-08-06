package cn.iocoder.yudao.module.system.service.upgrade;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.enums.UpgradeDevEnum;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.enums.UpgradeResultEnum;
import cn.iocoder.yudao.module.system.controller.admin.upgrade.enums.UpgradeStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.upgrade.SysUpgradeRecord;
import cn.iocoder.yudao.module.system.dal.dataobject.upgrade.SysUploadFileRecord;
import cn.iocoder.yudao.module.system.dal.mysql.upgrade.SysUpgradeRecordMapper;
import cn.iocoder.yudao.module.system.dal.mysql.upgrade.SysUploadFileRecordMapper;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Author: chenwany
 * @Date: 2024/8/1 14:17
 * @Description: 文件上传
 */
@Service
@Slf4j
public class UploadFileServiceImpl implements UploadFileService{

    @Resource
    SysUploadFileRecordMapper recordMapper;
    @Autowired
    SysUpgradeRecordMapper upgradeRecordMapper;
    @Resource
    PduIndexDoMapper pduIndexDoMapper;
    @Resource
    BusIndexDoMapper busIndexDoMapper;
    @Autowired
    CabinetPduMapper cabinetPduMapper;
    @Autowired
    AisleBarMapper aisleBarMapper;
    @Autowired
    RoomIndexMapper roomIndexMapper;
    @Autowired
    AisleIndexMapper aisleIndexMapper;
    @Autowired
    CabinetIndexMapper cabinetIndexMapper;


    private BufferedOutputStream bufferedOutputStream = null;
    @Override
    public Integer uploadFile(int upgradeDev,List<Integer> roomIds,List<String> ipList, List<MultipartFile> files) {
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
                downloadNotice(upgradeDev,roomIds,ipList,fileNames);
            }
        }
    }

    @Override
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) {

        try {
            //根据参数名称获取参数值
            String devIp = request.getParameter("devIp");

            //根据参数名称获取参数值的数组
            String[] files = request.getParameterValues("files");

            SysUpgradeRecord record = upgradeRecordMapper.selectOne(new LambdaQueryWrapper<SysUpgradeRecord>()
                    .eq(SysUpgradeRecord::getDevIp,devIp)
                    .eq(SysUpgradeRecord::getStatus,UpgradeStatusEnum.NOTICE.getType()));
            if (Objects.nonNull(record)){
                //获取记录更新状态
                record.setStatus(UpgradeStatusEnum.DOWNLOAD_ING.getType());
                upgradeRecordMapper.updateById(record);
                //下载文件
                String appPath = System.getProperty("user.dir");
                //文件保存路径
                String  path = appPath + "\\upgrade";
                try {
                    for (int i = 0; i < files.length; i++) {
                        String filePath = path + "\\" + files[i];
                        downFile(filePath,files[i],request,response,record);
                    }
                    //下载完成
                    record.setStatus(UpgradeStatusEnum.DOWNLOAD_END.getType());
                    upgradeRecordMapper.updateById(record);
                }catch (Exception e){
                    log.error("异常",e);
                    //文件下载失败
                    record.setStatus(UpgradeStatusEnum.DOWNLOAD_FAIL.getType());
                    record.setResult(UpgradeResultEnum.FAIL.getDesc());
                    //按实际情况修改
                    record.setFinishReason(UpgradeStatusEnum.DOWNLOAD_FAIL.getDesc());
                    record.setEndTime(DateTime.now());
                    upgradeRecordMapper.updateById(record);
                }
            }
        }catch (Exception e){
            log.error("异常",e);

        }

    }

    @Override
    public void notice(String devIp, String code, String message) {
        //结果通知
        SysUpgradeRecord record = upgradeRecordMapper.selectOne(new LambdaQueryWrapper<SysUpgradeRecord>()
                .eq(SysUpgradeRecord::getDevIp,devIp)
                .eq(SysUpgradeRecord::getStatus,UpgradeStatusEnum.DOWNLOAD_END.getType()));
        if (code.equals("")){
            //成功
            record.setStatus(UpgradeStatusEnum.UPGRADE_END.getType());
            record.setResult(UpgradeResultEnum.SUCCESS.getDesc());
            //按实际情况修改
            record.setFinishReason(message);
            record.setEndTime(DateTime.now());
            upgradeRecordMapper.updateById(record);
        }else {
            //失败
            record.setStatus(UpgradeStatusEnum.UPGRADE_END.getType());
            record.setResult(UpgradeResultEnum.FAIL.getDesc());
            //按实际情况修改
            record.setFinishReason(message);
            record.setEndTime(DateTime.now());
            upgradeRecordMapper.updateById(record);
        }
    }

    @Override
    public Integer reNotice(Long id) {
        SysUpgradeRecord record = upgradeRecordMapper.selectById(id);

        String ip = record.getDevIp();
        String port = "";
        String path = "";
        String url = "http://" + ip + ":" + port + path;
        Map<String,String>  params = new HashMap<>();
        params.put("files", record.getFileNames().toString());
        params.put("downloadUrl","");
        //发送通知
        String result = HttpUtil.post(url, JSONObject.toJSONString(params));
        //根据结果判断是否升级(预留，按实际情况修改)
        if (JSONObject.parseObject(result).get("code").equals("1")){
            //需要升级 改为已通知
            record.setStatus(UpgradeStatusEnum.NOTICE.getType());
            upgradeRecordMapper.updateById(record);
        }else {
            //不需要升级
            record.setStatus(UpgradeStatusEnum.UN_UPGRADE.getType());
            record.setResult(UpgradeResultEnum.SUCCESS.getDesc());
            //按实际情况修改
            record.setFinishReason(JSONObject.parseObject(result).getString("msg"));
            record.setEndTime(DateTime.now());
            upgradeRecordMapper.updateById(record);
        }

        new Thread(() -> {

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //十秒后若未下载，改为超时
            SysUpgradeRecord upgradeRecord = upgradeRecordMapper.selectById(id);
            if (upgradeRecord.getStatus() == UpgradeStatusEnum.NOTICE.getType()){
                //设为超时未下载
                upgradeRecord.setStatus(UpgradeStatusEnum.TIMEOUT.getType());
                upgradeRecord.setResult(UpgradeResultEnum.FAIL.getDesc());
                //按实际情况修改
                upgradeRecord.setFinishReason(UpgradeStatusEnum.TIMEOUT.getDesc());
                upgradeRecord.setEndTime(DateTime.now());
                upgradeRecordMapper.updateById(upgradeRecord);
            }

        }).start();

        return record.getStatus();

    }

    /**
     * 下载通知
     * @param upgradeDev
     * @param roomIds
     * @param ipList
     */
    private void downloadNotice(int upgradeDev,List<Integer> roomIds,List<String> ipList,List<String> files) {
        //通知设备
        List<String>  devIps = getDevIps(upgradeDev,roomIds,ipList);

        for (String ip : devIps){
            SysUpgradeRecord record = SysUpgradeRecord.builder()
                    .devIp(ip)
                    .fileNames(files)
                    .startTime(DateTime.now())
                    .status(UpgradeStatusEnum.START.getType())
                    .build();
            upgradeRecordMapper.insert(record);
        }
        //发送通知
        List<SysUpgradeRecord> recordList = upgradeRecordMapper.selectList(new LambdaQueryWrapper<SysUpgradeRecord>()
                .eq(SysUpgradeRecord::getStatus,UpgradeStatusEnum.START.getType())
                .last("limit 10"));
        while (!CollectionUtils.isEmpty(recordList)){
                recordList.forEach(record -> {
                    String ip = record.getDevIp();
                    String port = "";
                    String path = "";
                    String url = "http://" + ip + ":" + port + path;
                    Map<String,String>  params = new HashMap<>();
                    params.put("files", record.getFileNames().toString());
                    params.put("downloadUrl","");
                    //发送通知
                    String result = HttpUtil.post(url, JSONObject.toJSONString(params));
                    //根据结果判断是否升级(预留，按实际情况修改)
                    if (JSONObject.parseObject(result).get("code").equals("1")){
                        //需要升级 改为已通知
                        record.setStatus(UpgradeStatusEnum.NOTICE.getType());
                        upgradeRecordMapper.updateById(record);
                    }else {
                        //不需要升级
                        record.setStatus(UpgradeStatusEnum.UN_UPGRADE.getType());
                        record.setResult(UpgradeResultEnum.SUCCESS.getDesc());
                        //按实际情况修改
                        record.setFinishReason(JSONObject.parseObject(result).getString("msg"));
                        record.setEndTime(DateTime.now());
                        upgradeRecordMapper.updateById(record);
                    }
                });
                //通知发送后
            //等待十秒
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //处理未下载数据
            List<Long> ids = recordList.stream().map(SysUpgradeRecord::getId).collect(Collectors.toList());
            List<SysUpgradeRecord> list = upgradeRecordMapper.selectList(new LambdaQueryWrapper<SysUpgradeRecord>()
                    .in(SysUpgradeRecord::getId,ids));
            list.forEach(record->{
                if (record.getStatus() == UpgradeStatusEnum.NOTICE.getType()){
                    //设为超时未下载
                    record.setStatus(UpgradeStatusEnum.TIMEOUT.getType());
                    record.setResult(UpgradeResultEnum.FAIL.getDesc());
                    //按实际情况修改
                    record.setFinishReason(UpgradeStatusEnum.TIMEOUT.getDesc());
                    record.setEndTime(DateTime.now());
                    upgradeRecordMapper.updateById(record);
                }
            });
            recordList = upgradeRecordMapper.selectList(new LambdaQueryWrapper<SysUpgradeRecord>()
                    .eq(SysUpgradeRecord::getStatus,UpgradeStatusEnum.START.getType())
                    .last("limit 10"));
        }

    }


    /**
     * 获取升级ip
     * @param upgradeDev
     * @param roomIds
     * @param ipList
     * @return
     */
    private List<String>  getDevIps(int upgradeDev,List<Integer> roomIds,List<String> ipList){

        List<String> devIps = new ArrayList<>();
        if (upgradeDev == UpgradeDevEnum.ALL.getType()){
            //全部设备
            //pdu 主ip设备
            List<PduIndexDo> pduIndexDos = pduIndexDoMapper.selectList(new LambdaQueryWrapper<PduIndexDo>()
                    .eq(PduIndexDo::getIsDeleted, DelEnums.NO_DEL.getStatus())
                    .eq(PduIndexDo::getCascadeAddr,"0"));
            if (!CollectionUtils.isEmpty(pduIndexDos)){
                List<String> ips = pduIndexDos.stream().map(PduIndexDo::getIpAddr).distinct().collect(Collectors.toList());
                devIps.addAll(ips);
            }

            //母线
            List<BusIndex> busIndices = busIndexDoMapper.selectList(new LambdaQueryWrapper<BusIndex>()
                    .eq(BusIndex::getIsDeleted,DelEnums.NO_DEL.getStatus()));
            if (!CollectionUtils.isEmpty(busIndices)){
                List<String> ips = busIndices.stream().map(BusIndex::getIpAddr).distinct().collect(Collectors.toList());
                devIps.addAll(ips);
            }
        }else if (upgradeDev == UpgradeDevEnum.ROOM.getType()){
            if (CollectionUtils.isEmpty(roomIds)){
                return devIps;
            }
            //获取机房列表
            List<RoomIndex>  roomList = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>()
                    .eq(RoomIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                    .in(RoomIndex::getId,roomIds));
            if (!CollectionUtils.isEmpty(roomList)){
                //柜列
                List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                        .in(AisleIndex::getRoomId,roomList.stream().map(RoomIndex::getId).collect(Collectors.toList())));
                //获取母线
                if (!CollectionUtils.isEmpty(aisleIndexList)){
                    List<Integer> aisleIds = aisleIndexList.stream().map(AisleIndex::getId).collect(Collectors.toList());
                    List<AisleBar> barList = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                            .in(AisleBar::getAisleId,aisleIds));
                    if (!CollectionUtils.isEmpty(barList)){
                        List<String> ips = barList.stream().map(AisleBar::getDevIp).distinct().collect(Collectors.toList());
                        devIps.addAll(ips);
                    }
                }

                //机柜
                List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDisabled,DelEnums.NO_DEL.getStatus())
                        .in(CabinetIndex::getRoomId,roomList.stream().map(RoomIndex::getId).collect(Collectors.toList())));
                if (!CollectionUtils.isEmpty(cabinetIndices)){
                    List<Integer> cabIds = cabinetIndices.stream().map(CabinetIndex::getId).collect(Collectors.toList());
                    List<CabinetPdu> cabinetPdus  = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>()
                            .in(CabinetPdu::getCabinetId,cabIds));
                    //获取pdu
                    if (!CollectionUtils.isEmpty(cabinetPdus)){
                        List<String> ipsA = cabinetPdus.stream().filter(t -> t.getCasIdA() ==0).map(CabinetPdu::getPduIpA).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
                        List<String> ipsB = cabinetPdus.stream().filter(t -> t.getCasIdB() ==0).map(CabinetPdu::getPduIpB).filter(StringUtils::isNotEmpty).distinct().collect(Collectors.toList());
                        devIps.addAll(ipsA);
                        devIps.addAll(ipsB);
                    }
                }

            }

        }else if (upgradeDev == UpgradeDevEnum.DEV_IP.getType()){
            devIps.addAll(ipList);
        }

        return devIps;
    }


    /**
     * 上传文件处理
     * @param file
     * @param path
     * @return
     */
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



        /**
         * 下载文件通用方法
         *
         * @param filePath
         * @param downFileName
         * @param request
         * @param response
         */
        public void downFile(String filePath, String downFileName,
                                    HttpServletRequest request, HttpServletResponse response,
                                    SysUpgradeRecord record) {
            File file = new File(filePath);
            // 下载文件
            // 设置响应的内容类型，让浏览器知道下载的是一个文件
            ServletContext context = request.getServletContext();
            // get MIME type of the file
            String mimeType = context.getMimeType(filePath);
            if (mimeType == null) {
                // set to binary type if MIME mapping not found
                mimeType = "application/octet-stream";
                log.info("context getMimeType is null");
            }
            log.info("MIME type: " + mimeType);
            // 设置响应头信息，告诉浏览器文件的名称和长度
            // set content attributes for the response
            response.setContentType(mimeType);
            response.setCharacterEncoding("utf-8");
            response.setContentLength((int) file.length());
            String headerValue = String.format("attachment; filename=%s",
                    UriUtils.encode(downFileName, StandardCharsets.UTF_8));
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, headerValue);
            // Copy the stream to the response's output stream.
            try {
                InputStream myStream = new FileInputStream(filePath);
                long fileSize = file.length();

                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytesRead = 0;

                BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                while ((bytesRead = myStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    int percentage = (int) ((totalBytesRead / (float) fileSize) * 100);
                    record.setDownloadProgress(percentage);
                    upgradeRecordMapper.updateById(record);
                    System.out.println("Downloaded " + percentage + "%");
                    outputStream.flush();
                }

                myStream.close();
                outputStream.close();
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
