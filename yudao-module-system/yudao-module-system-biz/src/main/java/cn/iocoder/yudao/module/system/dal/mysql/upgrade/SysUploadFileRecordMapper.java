package cn.iocoder.yudao.module.system.dal.mysql.upgrade;

import cn.iocoder.yudao.module.system.dal.dataobject.upgrade.SysUpgradeRecord;
import cn.iocoder.yudao.module.system.dal.dataobject.upgrade.SysUploadFileRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 文件上传记录
 * @date 2024/6/12 10:57
 */
@Mapper
public interface SysUploadFileRecordMapper extends BaseMapper<SysUploadFileRecord> {
}
