package cn.iocoder.yudao.framework.common.entity.mysql;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileManage {
    /**
     * 创建时间
     */
  private LocalDateTime  createTime;// create_time	datetime
    /**
     * 文件名称
     */
  private String fileName;//  file_name	varchar
    /**
     * 文件url
     */
  private String fileUrl;//  file_url	varchar	文件url
    /**
     * id
     */
  private Integer id;//   id	int	id
    /**
     * 删除标识
     */
  private Boolean isFelete;//  is_delete	bit

    /**
     * 文件归属
     */
    private Integer sysType;//	int	文件归属
}
