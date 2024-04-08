package cn.iocoder.yudao.module.statis.service;

import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/3/29 13:55
 * @Description: ES操作类
 */
public interface EsHandleService {


    /**
     * 批量插入
     *
     * @param list      数据
     * @param indexName 索引名称
     */
    <T> void batchInsert(List<T> list, String indexName);
}
