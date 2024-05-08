package cn.iocoder.yudao.module.cabinet.service.historydata;

import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;
import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataServiceImpl;
import org.junit.jupiter.api.Test;
import javax.annotation.Resource;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.PduHdaTotalRealtimeDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.historydata.HistoryDataMapper;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * {@link HistoryDataServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HistoryDataServiceImpl.class)
public class HistoryDataServiceImplTest extends BaseDbUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(HistoryDataServiceImplTest.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    @Resource
    private HistoryDataMapper historyDataMapper;

    @Resource
    private HistoryDataService historyDataService;

    @Test
    public void createHistoryDataTest() {
        Thread insertThread = new Thread(() -> {
            Random random = new Random();
            int totalInserted = 54192643;
            for (int i = 0; i < 1000000000; i++) {
                PduHdaTotalRealtimeDO pduHdaTotalRealtimeDO = new PduHdaTotalRealtimeDO();
//                historyDataDO.setPduId((short) random.nextInt(10001));
//                historyDataDO.setType((short) random.nextInt(11));
//                historyDataDO.setTopic((short) random.nextInt(11));
//                historyDataDO.setIndexes((short) random.nextInt(1001));
//                historyDataDO.setValue(random.nextFloat() * 100);
//                LocalDateTime currentTime = LocalDateTime.now();
//                historyDataDO.setCreateTime(currentTime);
//                historyDataDO.setTest1((short) random.nextInt(10001));
//                historyDataDO.setTest2((short) random.nextInt(10001));
//                historyDataDO.setTest3((short) random.nextInt(10001));
//                historyDataDO.setTest4((short) random.nextInt(10001));
//                historyDataDO.setTest5((short) random.nextInt(10001));
//                historyDataDO.setTest6((short) random.nextInt(10001));
//                historyDataDO.setTest7((short) random.nextInt(10001));
//                historyDataDO.setTest8((short) random.nextInt(10001));
//                historyDataDO.setTest9((short) random.nextInt(10001));
//                historyDataDO.setTest10((short) random.nextInt(10001));
                historyDataMapper.insert(pduHdaTotalRealtimeDO);
                totalInserted++;

                if (totalInserted % 50000 == 0) {
                    LocalDateTime currentTime1 = LocalDateTime.now();
                    String currentTimeFormatted = currentTime1.format(formatter);
                    logger.info("["+ currentTimeFormatted + "] --- " + " 已插入 " + totalInserted + " 条数据");
                }
            }
        });

        insertThread.start();
        // 等待插入线程执行完毕
        try {
            insertThread.join();
        } catch (InterruptedException e) {
            logger.info("插入线程被中断", e);
        }

    }

    @Test
    public void searchHistoryDataTest() {
        Thread searchThread = new Thread(() -> {
            LocalDateTime startTime  = LocalDateTime.now();
            String currentTimeFormatted = startTime.format(formatter);
            logger.info("["+ currentTimeFormatted + "] --- " + " 开始查询数据 条件：pdu_id" );
//            logger.info("["+ currentTimeFormatted + "] --- " + " 当前内存占用10.3GB，只插入不查询时CPU利用率40%左右，查询时CPU利用率60%~75%" );
//            logger.info("["+ currentTimeFormatted + "] --- " + " 开始查询数据 条件：type" );
//            logger.info("["+ currentTimeFormatted + "] --- " + " 开始查询数据 条件：pdu_id、type和topic" );
//            logger.info("["+ currentTimeFormatted + "] --- " + " 开始查询数据 条件：pdu_id、type、topic和indexes" );
//            logger.info("["+ currentTimeFormatted + "] --- " + " 开始查询数据 条件：五分钟时间段" );

            QueryWrapperX<PduHdaTotalRealtimeDO> queryWrapperX = new QueryWrapperX<>();
            queryWrapperX.eq("pdu_id", 150);
//            queryWrapperX.eq("type", 6);
//            queryWrapperX.eq("topic", 2);
//            queryWrapperX.eq("indexes", 202);
//            queryWrapperX.between("create_time", "2024-03-14 13:20:00", "2024-03-14 13:21:00");
            List<PduHdaTotalRealtimeDO> pduHdaTotalRealtimeDOList = historyDataMapper.selectList(queryWrapperX);

            if (!pduHdaTotalRealtimeDOList.isEmpty()) {
                LocalDateTime endTime = LocalDateTime.now();
                String endTimeFormatted = endTime.format(formatter);
                Duration duration = Duration.between(startTime, endTime);
                long milliseconds = duration.toMillis();
                logger.info("["+ endTimeFormatted + "] --- " + " 查询结束--查询到的数据总条数：" + pduHdaTotalRealtimeDOList.size() + "  总耗时："+ milliseconds + " 毫秒" );

            } else {
                LocalDateTime endTime = LocalDateTime.now();
                String endTimeFormatted = endTime.format(formatter);
                logger.info("["+ endTimeFormatted + "] --- " + " 查询结束，未查到数据！" );
            }
        });

        searchThread.start();
        // 等待线程执行完毕
        try {
            searchThread.join();
        } catch (InterruptedException e) {
            logger.info("查询线程被中断", e);
        }
    }

//    @Test
//    public void testEs() {
//        Random random = new Random();
//        EsHistoryDataDO esHistoryDataDO = new EsHistoryDataDO();
//        esHistoryDataDO.setPduId((short) random.nextInt(10001));
//        esHistoryDataDO.setType((short) random.nextInt(11));
//        esHistoryDataDO.setTopic((short) random.nextInt(11));
//        esHistoryDataDO.setIndexes((short) random.nextInt(1001));
//        esHistoryDataDO.setValue(random.nextFloat() * 100);
//        LocalDateTime currentTime = LocalDateTime.now();
//        esHistoryDataDO.setCreateTime(currentTime);
//        esHistoryDataDO.setTest1((short) random.nextInt(10001));
//        esHistoryDataDO.setTest2((short) random.nextInt(10001));
//        esHistoryDataDO.setTest3((short) random.nextInt(10001));
//        esHistoryDataDO.setTest4((short) random.nextInt(10001));
//        esHistoryDataDO.setTest5((short) random.nextInt(10001));
//        esHistoryDataDO.setTest6((short) random.nextInt(10001));
//        esHistoryDataDO.setTest7((short) random.nextInt(10001));
//        esHistoryDataDO.setTest8((short) random.nextInt(10001));
//        esHistoryDataDO.setTest9((short) random.nextInt(10001));
//        esHistoryDataDO.setTest10((short) random.nextInt(10001));
//        historyDataService.save(esHistoryDataDO);
//    }

//    @Test
//    public void findById(){
//        EsHistoryDataDO byId = historyDataService.findById(1L);
//        System.out.println(byId);
//    }

}