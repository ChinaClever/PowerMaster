package cn.iocoder.yudao.framework.common.util;

import cn.hutool.core.lang.Validator;
import cn.iocoder.yudao.framework.common.exception.BusinessAssert;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import lombok.extern.log4j.Log4j2;

import java.util.List;


import static com.alibaba.excel.read.listener.PageReadListener.BATCH_COUNT;


@Log4j2
public class ReadDataListener<T> extends AnalysisEventListener<T> {


    /**
     * 数据列表
     */
    private List<T> dataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * 验证器
     */
//    private final Validator validator;
    /**
     * 当前处理行号
     */
    private long currentRowNo = 1;
//    private static final Logger LOGGER = LoggerFactory.getLogger(TianHengServiceImpl.class);
    /**
     * 获取当前行号
     *
     * @return long 行号
     */
    public long getCurrentRowNo() {
        return currentRowNo;
    }


    /**
     * 解析数据
     *
     * @param data    单行记录
     * @param context 解析上下文
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        // 处理数据
//        E entity = handleData(data);
        // 添加数据
        dataList.add(data);
        currentRowNo++;
    }

    /**
     * 所有数据解析完成了 就会来调用
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成！");
    }

    /**
     * 数据预处理，可转化实体类中的字典数据，也可以设置默认字段
     *
     * @param vo 视图对象
     */
//    public T handleData(T vo) {
//        // 数据校验
//        validateData(vo);
//        // 设置默认值
//        setDefaultValue(vo);
//        // 数据转换
//        return convertData(vo);
//    }

//    private void validateData(M model) {
//        Set<ConstraintViolation<M>> violations = validator.validate(model, ExcelImportService.class);
//        if (!violations.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (ConstraintViolation<M> violation : violations) {
//                sb.append(violation.getMessage()).append("\n");
//            }
//            throw new OuterException(RetCode.BAD_REQUEST, sb.toString());
//        }
//    }


    /**
     * 设置默认值
     *
     * @param model 模型对象
     */
//    protected void setDefaultValue(M model) {
//        // 如无需设置，则该方法可为空
//    }


    /**
     * 转换数据
     *
     * @param model 模型对象
     * @return {@link E} 数据库实体对象
     */
//    protected E convertData(M model) {
//        BusinessAssert.error(10001,"未实现convertData方法");
//        return null;
////        throw new InnerException(RetCode.INTERNAL_SERVER_ERROR,"未实现convertData方法");
//    }


//    public List<E> getDataList() {
//        return dataList;
//    }


}
