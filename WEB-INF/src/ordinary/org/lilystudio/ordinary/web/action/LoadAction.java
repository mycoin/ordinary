package org.lilystudio.ordinary.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.lilystudio.coder.JSONDecoder;
import org.lilystudio.coder.XMLDecoder;
import org.lilystudio.ordinary.web.Controller;
import org.lilystudio.ordinary.web.IExecute;
import org.lilystudio.ordinary.web.IRelay;
import org.xml.sax.InputSource;

/**
 * 设置常量, 请不要同时设置value与file属性<br>
 * <b>属性</b>
 * 
 * <pre>
 * name--常量名
 * value--常量值
 * file--通过文件加载, 目前仅支持xml,json,xls为尾缀的文件, 文件的默认的目录为WEB-INF/, 如果需要改变请指定/路径, 文件的编码格式使用框架自身的编码格式
 * override--如果数据容器中已经加载了值, 是否要覆盖式加载当前指定的值
 * </pre>
 * 
 * @version 0.1.4, 2009/03/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class LoadAction implements IExecute {

  /** 常量名 */
  private String name;

  /** 常量值 */
  private Object value;

  /** 文件路径名称 */
  private String path;

  /** 文件的最后更新时间, 如果有值, 每次都需要重新判断 */
  private long lastModified;

  /** 是否覆盖 */
  private boolean override = true;

  /**
   * 直接设置对象值
   * 
   * @param value
   *          属性的值
   * @throws Exception
   *           初始化异常
   */
  public void setValue(String value) throws Exception {
    // 读入xml数据文件, 生成对象
    this.value = value;
  }

  /**
   * 读入文件生成对象操作
   * 
   * @param value
   *          属性的值
   * @throws Exception
   *           初始化异常
   */
  public void setFile(String value) throws Exception {
    // 读入xml数据文件, 生成对象
    path = Controller.getContextPath()
        + (value.charAt(0) == '/' ? "" : "WEB-INF/") + value;
    load(path);
  }

  /**
   * 对象初始化
   * 
   * @throws Exception
   *           初始化异常
   */
  public void init() throws Exception {
    if (path == null && value == null) {
      // HARDCODE
      throw new Exception("Either file or value must be specified");
    }
  }

  /**
   * 实际的文件读入操作
   * 
   * @param path
   *          文件路径
   */
  private void load(String path) throws Exception {
    try {
      File file = new File(path);
      if (file.exists()) {
        lastModified = file.lastModified();
        if (path.endsWith(".xml")) {
          InputSource in = new InputSource(new FileInputStream(file));
          try {
            value = XMLDecoder.decode(DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(in).getDocumentElement());
          } finally {
            in.getByteStream().close();
          }
        } else if (path.endsWith(".json")) {
          StringBuilder sb = new StringBuilder(256);
          Reader reader = new InputStreamReader(new FileInputStream(file),
              Controller.getEncoding());
          try {
            char[] chars = new char[4096];
            while (true) {
              int len = reader.read(chars);
              if (len < 0) {
                break;
              }
              sb.append(chars, 0, len);
            }
          } finally {
            reader.close();
          }
          value = JSONDecoder.decode(sb);
        } else if (path.endsWith(".xls")) {
          List<Object> data = new ArrayList<Object>();
          HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(
              new FileInputStream(file)));
          int sheetSize = workbook.getNumberOfSheets();
          for (int i = 0; i < sheetSize; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            if (sheet == null) {
              // 数据页没有内容, 跳过处理
              continue;
            }
            List<Object> rowList = new ArrayList<Object>();
            data.add(rowList);

            int lastRowIndex = sheet.getLastRowNum();
            for (int j = 0; j <= lastRowIndex; j++) {
              HSSFRow row = sheet.getRow(j);
              if (row == null) {
                // 插入空行
                rowList.add(null);
                continue;
              }

              int lastCellIndex = row.getLastCellNum();
              List<Object> cellList = new ArrayList<Object>();
              rowList.add(cellList);
              for (int k = 0; k <= lastCellIndex; k++) {
                HSSFCell cell = row.getCell(k);
                if (cell == null) {
                  // 插入空单元格
                  cellList.add(null);
                  continue;
                }
                if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                  double d = cell.getNumericCellValue();
                  if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    cellList.add(HSSFDateUtil.getJavaDate(d));
                  } else {
                    cellList.add(d);
                  }
                } else {
                  HSSFRichTextString o = cell.getRichStringCellValue();
                  cellList.add(o != null ? o.getString() : null);
                }
              }
            }
          }
          value = data;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void execute(IRelay relay) throws Exception {
    // 检查数据来源文件是否更新
    if (override || relay.get(name) == null) {
      if (path != null) {
        File file = new File(path);
        if (file.exists() && file.lastModified() > lastModified) {
          load(path);
          lastModified = file.lastModified();
        }
      }
      relay.set(name, value);
    }
  }
}
