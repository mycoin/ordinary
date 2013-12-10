package org.lilystudio.ordinary.web.result;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.TableOrder;
import org.lilystudio.ordinary.web.IRelay;

/**
 * 报表图片生成<br>
 * <b>属性</b>
 * 
 * <pre>
 * title--报表的名称
 * labelFormat--标签的显示格式
 * type--图片类型, pie(饼状图),vbar(纵向柱状图),hbar(横向柱状图),line(线性图),spider(蛛网图)
 * keys--报表配置信息, 用于指定特定报表格式的参数
 * groupNames--报表数据组名称值(决定画多少组数据)
 * itemNames--报表数据类型名称值
 * width--报表的宽度
 * height--报表的高度
 * values--数据来源的键名称
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class ChartResult extends AbstractImage {

  /**
   * 高级蜘蛛图
   */
  private class AdvancedSpiderWebPlot extends SpiderWebPlot {

    /** 序列码 */
    private static final long serialVersionUID = 1L;

    /**
     * 创建高级蜘蛛图
     * 
     * @param dataset
     *          数据集合
     */
    public AdvancedSpiderWebPlot(CategoryDataset dataset) {
      super(dataset);
    }

    @Override
    protected void drawLabel(Graphics2D g2, Rectangle2D plotArea, double value,
        int cat, double startAngle, double extent) {
      FontRenderContext frc = g2.getFontRenderContext();

      CategoryItemLabelGenerator labelGenerator = getLabelGenerator();
      TableOrder dataExtractOrder = getDataExtractOrder();
      CategoryDataset dataset = getDataset();

      String label = null;
      if (dataExtractOrder == TableOrder.BY_ROW) {
        label = labelGenerator.generateLabel(dataset, 0, cat);
      } else {
        label = labelGenerator.generateLabel(dataset, cat, 0);
      }

      Rectangle2D labelBounds = getLabelFont().getStringBounds(label, frc);
      LineMetrics lm = getLabelFont().getLineMetrics(label, frc);
      double ascent = lm.getAscent();

      Point2D labelLocation = calculateLabelLocation(labelBounds, ascent,
          plotArea, startAngle);

      Composite saveComposite = g2.getComposite();

      g2
          .setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
              1.0f));
      g2.setPaint(getLabelPaint());
      g2.setFont(getLabelFont());
      g2.drawString(label, (float) labelLocation.getX(), (float) labelLocation
          .getY());
      g2.setComposite(saveComposite);
    }
  }

  /** 标题字体 */
  private static final Font titleFont = new Font("隶书", Font.BOLD, 25);

  /** 各类标签字体 */
  private static final Font labelFont = new Font("宋体", Font.TRUETYPE_FONT, 13);

  /** 各类数值字体 */
  private static final Font tickLabelFont = new Font("宋体", Font.TRUETYPE_FONT,
      12);

  /** 饼状图 */
  private static final int PIE = 0;

  /** 纵向柱状图 */
  private static final int VBAR = 1;

  /** 横向柱状图 */
  private static final int HBAR = 2;

  /** 线形图 */
  private static final int LINE = 3;

  /** 蛛网图 */
  private static final int SPIDER = 4;

  /** 报表的名称 */
  private String title = "";

  /** 标签的显示格式 */
  private String labelFormat;

  /** 报表类型 */
  private int type;

  /** 报表键值 */
  private String[] keys;

  /** 报表数据组名称值(决定画多少组数据) */
  private String[] groupNames;

  /** 报表数据单项名称值 */
  private String[] itemNames;

  /** 报表值的数据集合键名称 */
  private String values;

  /** 报表宽度 */
  private int width;

  /** 报表高度 */
  private int height;

  /**
   * 设置报表类型
   * 
   * @param value
   *          配置文件中设定的值
   */
  public void setType(String value) throws Exception {
    // HARDCODE
    if (value.equals("pie")) {
      type = PIE;
    } else if (value.equals("vbar") || value.equals("bar")) {
      type = VBAR;
    } else if (value.equals("hbar")) {
      type = HBAR;
    } else if (value.equals("line")) {
      type = LINE;
    } else if (value.equals("spider")) {
      type = SPIDER;
    } else {
      throw new Exception("The type don't support");
    }
  }

  /**
   * 设置报表使用的键值组, 使用,号分隔
   * 
   * @param value
   *          配置文件中设定的值
   */
  public void setKeys(String value) {
    keys = value.split(",");
  }

  /**
   * 设置报表使用的组名称, 使用,号分隔
   * 
   * @param value
   *          配置文件中设定的值
   */
  public void setGroupNames(String value) {
    groupNames = value.split(",");
  }

  /**
   * 设置报表使用的类型名称组, 使用,号分隔
   * 
   * @param value
   *          配置文件中设定的值
   */
  public void setItemNames(String value) {
    itemNames = value.split(",");
  }

  @Override
  public void execute(OutputStream out, IRelay relay, boolean alpha) throws Exception {
    JFreeChart chart = null;
    Object[] data;
    {
      Object o = relay.get(values);
      data = o instanceof List ? ((List<?>) o).toArray() : (Object[]) o;
    }
    int size = data.length;
    switch (type) {
    case PIE: {
      // 设置填充的数据集
      DefaultPieDataset dataset = new DefaultPieDataset();
      for (int i = 0; i < size; i++) {
        String name;
        if (itemNames != null) {
          name = itemNames[i];
        } else {
          name = data[i].toString();
          i++;
        }
        dataset.setValue(name, Double.parseDouble(data[i].toString()));
      }

      chart = ChartFactory.createPieChart(title, dataset, true, false, false);
      PiePlot plot = (PiePlot) chart.getPlot();
      // 设置显示格式, {0}表示名称, {1}表示值, {2}表示百分比值
      if (labelFormat != null) {
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
            labelFormat, NumberFormat.getNumberInstance(), new DecimalFormat(
                "0.0%")));
      }
      plot.setLabelFont(tickLabelFont);
      break;
    }
    case VBAR:
    case HBAR:
    case LINE:
    case SPIDER: {
      // 设置填充的数据集
      DefaultCategoryDataset dataset = new DefaultCategoryDataset();

      int step = 1 + (itemNames == null ? 1 : 0)
          + (groupNames != null && groupNames[0].length() == 0 ? 1 : 0);
      for (int i = 0; i < size; i++) {
        String name;
        if (itemNames != null) {
          name = itemNames[groupNames != null && groupNames[0].length() > 0 ? (i / step)
              / groupNames.length
              : i * itemNames.length / size];
        } else {
          name = data[i].toString();
          i++;
        }
        String category;
        if (groupNames != null) {
          if (groupNames[0].length() == 0) {
            category = data[i].toString();
            i++;
          } else {
            category = groupNames[((i / step) % groupNames.length)];
          }
        } else {
          category = "";
        }
        dataset
            .addValue(Double.parseDouble(data[i].toString()), category, name);
      }

      if (type == SPIDER) {
        AdvancedSpiderWebPlot plot = new AdvancedSpiderWebPlot(dataset);
        chart = new JFreeChart(title, titleFont, plot, groupNames != null);
        if (labelFormat != null) {
          plot.setLabelGenerator(new StandardCategoryItemLabelGenerator(
              labelFormat, NumberFormat.getNumberInstance(), new DecimalFormat(
                  "0.0%")));
        }
        plot.setLabelFont(labelFont);
        if (keys != null) {
          plot.setMaxValue(Double.parseDouble(keys[0]));
        }
      } else {
        if (type == LINE) {
          chart = ChartFactory.createLineChart(title, keys[0], keys[1],
              dataset, PlotOrientation.VERTICAL, groupNames != null, false,
              false);
        } else {
          chart = ChartFactory.createBarChart(title, keys[0], keys[1], dataset,
              type == VBAR ? PlotOrientation.VERTICAL
                  : PlotOrientation.HORIZONTAL, groupNames != null, false,
              false);
        }
        CategoryPlot plot = chart.getCategoryPlot();
        // 设置显示值的数字
        AbstractCategoryItemRenderer renderer = (AbstractCategoryItemRenderer) plot
            .getRenderer();
        if (labelFormat != null) {
          renderer.setBaseItemLabelFont(labelFont);
          renderer.setBaseItemLabelsVisible(true);
          renderer
              .setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        }
        if (type == LINE) {
          // 线条模式设置显示节点图形
          ((LineAndShapeRenderer) renderer).setBaseShapesVisible(true);
        }

        // 设置数值轴的信息
        {
          NumberAxis axis = (NumberAxis) plot.getRangeAxis();
          axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
          axis.setUpperMargin(0.12D);
          axis.setLabelFont(labelFont);
          axis.setTickLabelFont(tickLabelFont);
        }

        // 设置分类/标题轴的信息
        {
          CategoryAxis axis = plot.getDomainAxis();
          axis.setLabelFont(labelFont);
          axis.setTickLabelFont(tickLabelFont);
        }
      }
      break;
    }
    }
    // 设置字体边缘清晰化
    chart.setTextAntiAlias(false);
    chart.getTitle().setFont(titleFont);
    LegendTitle legend = chart.getLegend();
    if (legend != null) {
      legend.setItemFont(labelFont);
    }
    // HARDCODE
    chart.getPlot().setNoDataMessage("No data available");
    // 输出图片
    if (alpha) {
      chart.setBackgroundPaint(TRANSLUCENT);
      ChartUtilities.writeChartAsPNG(out, chart, width, height);
    } else {
      chart.setBackgroundPaint(Color.WHITE);
      ChartUtilities.writeChartAsJPEG(out, chart, width, height);
    }
  }
}