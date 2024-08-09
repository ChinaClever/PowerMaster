package cn.iocoder.yudao.module.system.dal.mysql.upgrade;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.upgrade.SysUploadFileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 文件上传记录
 * @date 2024/6/12 10:57
 */
@Mapper
public interface SysUploadFileRecordMapper extends BaseMapperX<SysUploadFileRecord> {
}
