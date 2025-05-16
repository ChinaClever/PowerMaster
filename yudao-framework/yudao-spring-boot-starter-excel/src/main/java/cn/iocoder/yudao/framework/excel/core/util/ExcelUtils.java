package cn.iocoder.yudao.framework.excel.core.util;

import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.excel.core.enums.ExcelColumn;
import cn.iocoder.yudao.framework.excel.core.handler.SelectSheetWriteHandler;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Excel 工具类
 *
 * @author 芋道源码
 */
public class ExcelUtils {

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param <T>       泛型，保证 head 和 data 类型的一致性
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        write(response, filename, sheetName, head, data, null);
    }

    public static List extractedEditAisle(List<Map<Integer, String>> pduList1, Class cls) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List list = new ArrayList();
        Field[] fields = cls.getDeclaredFields();
        for (Map<Integer, String> map : pduList1) {
            Object obj = cls.getDeclaredConstructor().newInstance(); // 创建实例
            for (Field field : fields) {
                ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                if (ObjectUtils.isEmpty(annotation)) {
                    continue;
                }
                int index = annotation.index();
                String s = map.get(index);
                if (Objects.isNull(s)){
                    continue;
                }
                field.setAccessible(true); // 确保可以访问私有字段
                if (field.getType().isAssignableFrom(Integer.class)){
                    field.set(obj, Integer.parseInt(s)); // 设置值
                }else {
                    field.set(obj, s);
                }
            }
            list.add(obj);
        }
        return list;
    }

    public static <T> void writeA(HttpServletResponse response, String filename, String sheetName,
                                  Class<T> head, List<T> data, List<KeyValue<ExcelColumn, List<String>>> selectMap, Set<String> columnsToExclude) throws IOException {
        // 输出 Excel
        try (OutputStream outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream, head).excludeColumnFieldNames(columnsToExclude)
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                    .registerWriteHandler(new SelectSheetWriteHandler(selectMap)) // 基于固定 sheet 实现下拉框
                    .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                    .sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    /**
     * 将列表以 Excel 响应给前端
     *
     * @param response  响应
     * @param filename  文件名
     * @param sheetName Excel sheet 名
     * @param head      Excel head 头
     * @param data      数据列表哦
     * @param selectMap 下拉选择数据 Map<下拉所对应的列表名，下拉数据>
     * @throws IOException 写入失败的情况
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data, List<KeyValue<ExcelColumn, List<String>>> selectMap) throws IOException {
        // 输出 Excel
        try (OutputStream outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream, head)
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                    .registerWriteHandler(new SelectSheetWriteHandler(selectMap)) // 基于固定 sheet 实现下拉框
                    .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                    .sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        return EasyExcel.read(file.getInputStream(), head, null)
                .autoCloseStream(false)  // 不要自动关闭，交给 Servlet 自己处理
                .doReadAllSync();
    }

    public static void write(HttpServletResponse response, String filename, String sheetName, List<String> head, List<Object> data, List<KeyValue<ExcelColumn, List<String>>> selectMap) throws IOException {
        // 输出 Excel
        try (OutputStream outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream.toString())
                    .head(generateHead(head))
                    .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                    .registerWriteHandler(new SelectSheetWriteHandler(selectMap)) // 基于固定 sheet 实现下拉框
                    .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                    .sheet(sheetName).doWrite(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    // 生成表头方法
    private static List<List<String>> generateHead(List<String> headers) {
        List<List<String>> head = new ArrayList<>();
        for (String header : headers) {
            List<String> headColumn = new ArrayList<>();
            headColumn.add(header);
            head.add(headColumn);
            System.out.println(head);
        }
        return head;
    }
}
