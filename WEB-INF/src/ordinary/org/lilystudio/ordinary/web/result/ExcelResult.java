package org.lilystudio.ordinary.web.result;

import java.io.FileInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IRelay;

/**
 * Excel表格生成<br>
 * <b>属性</b>
 * 
 * <pre>
 * left--填充开始单元格的横轴坐标
 * top--填充开始单元格的纵轴坐标
 * sheetIndex--填充开始单元格的Sheet序号
 * sheetSize--一页填充的数据数量, 如果不设置, 表示全部填充在同一页中
 * path--预先定义好的Excel模板路径, 如果不设置, 将自动生成新的Excel文件
 * format--填充的格式, 表示每填充完一个数据后, 下一个数据应该填充的位置, 格式为[+行]:[+列],[+行]:[+列],...
 * values--数据来源的键名称
 * titles--表格标题栏, 仅能用于没有指定path的情况
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ExcelResult extends AbstractResult {

  /** 填充开始单元格的横轴坐标 */
  private int left;

  /** 填充开始单元格的纵轴坐标 */
  private int top;

  /** 填充开始单元格的Sheet序号 */
  private int sheetIndex;

  /** 一页填充的数据数量 */
  private int sheetSize;

  /** 预先定义好的Excel模板路径 */
  private String path;

  /** 标题名称 */
  private String[] titles;

  /** 填充的格式 */
  private int[][] format;

  /** 数据来源的键名称 */
  private String values;

  /**
   * 设置数据填充格式
   * 
   * @param value
   *          数据填充格式的描述字符串
   */
  public void setFormat(String value) {
    String[] list = value.split(",");
    int len = list.length;
    format = new int[len][];
    for (int i = 0; i < len; i++) {
      int[] item = new int[2];
      String s = list[i];
      // 如果不细分*-*的格式, 则表示当前行不变
      int index = s.indexOf(':');
      item[0] = index < 0 ? 0 : Integer.parseInt(s.substring(0, index));
      item[1] = Integer.parseInt(s.substring(index + 1));
      format[i] = item;
    }
  }

  /**
   * 设置标题组
   * 
   * @param value
   *          配置文件中定义的数据
   */
  public void setTitles(String value) {
    titles = value.split(",");
  }

  @SuppressWarnings("unchecked")
  public void execute(HttpServletRequest request, HttpServletResponse response,
      IRelay relay) throws Exception {
    List<List<?>> rows = (List<List<?>>) relay.get(values);

    // 打开或生成Excel文件对象
    HSSFWorkbook workbook;
    if (path != null) {
      workbook = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(
          Controller.getContextPath() + relay.getRealPath(path))));
    } else {
      workbook = new HSSFWorkbook();
    }

    int size = rows.size();
    // 如果设置翻页, 先复制若干原始的模板
    int total = workbook.getNumberOfSheets();
    while (total <= sheetIndex) {
      workbook.createSheet();
      total++;
    }

    if (sheetSize > 0) {
      for (int i = sheetIndex + 1, j = sheetSize; j < size; i++, j += sheetSize, total++) {
        workbook.cloneSheet(sheetIndex);
        workbook.setSheetOrder(workbook.getSheetName(total), i);
      }
    }

    // 取出Sheet开始填充
    HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
    int rowIndex = top;
    int columnIndex = left;

    if (path == null && titles != null) {
      sheet.createRow(rowIndex);
      HSSFRow row = sheet.createRow(rowIndex);
      for (String title : titles) {
        row.createCell((short) columnIndex).setCellValue(
            new HSSFRichTextString(title));
        columnIndex++;
      }
      rowIndex++;
      columnIndex = left;
    }

    for (int i = 0; i < size; i++) {
      // 填充每一行
      List<?> list = rows.get(i);
      int rowSize = list.size();
      for (int j = 0; j < rowSize; j++) {
        // 填充每一列
        Object value = list.get(j);
        if (value != null) {
          // 等于空时跳过这一次存储
          sheet.createRow(rowIndex).createCell((short) columnIndex)
              .setCellValue(new HSSFRichTextString(value.toString()));
        }
        // 如果定义了填充格式, 则需要计算新的单元格位置
        if (format != null) {
          rowIndex += format[j][0];
          columnIndex += format[j][1];
        } else {
          columnIndex += 1;
        }
      }
      // 如果没有定义填充格式, 行数加一, 列数自动复位
      if (format == null) {
        rowIndex += 1;
        columnIndex = left;
      }
      // 如果提供多页填充, 计算填充的新页的位置
      if (sheetSize > 0 && ((i + 1) % sheetSize == 0)) {
        sheetIndex++;
        sheet = workbook.getSheetAt(sheetIndex);
        rowIndex = top;
        columnIndex = left;
      }
    }

    // HARDCODE
    response.setContentType("application/vnd.ms-excel");
    workbook.write(response.getOutputStream());
  }
}
