package org.lilystudio.test;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.Engine;
import org.lilystudio.smarty4j.Template;
import org.lilystudio.util.StringWriter;

/**
 * Smarty单元测试类
 * 
 * @version 0.1.4, 2009/03/01
 * @author 欧阳先伟
 * @since Smarty 0.1
 */
public class SmartyUnit {

  public class Bean {
    public int getNumber() {
      return 10;
    }

    public String toString() {
      return "org.lilystudio.test.SmartyUnit$Bean";
    }
  }

  private static Context context = new Context();

  private static Engine engine = new Engine();

  @Before
  public void setUp() throws Exception {
    List<Object> list = new ArrayList<Object>();
    Map<String, Object> map = new HashMap<String, Object>();
    Bean bean = new Bean();

    context.set("now", new Date());
    context.set("bytes", "测试字节数组".getBytes("UTF-8"));
    context.set("boolean", false);
    context.set("string", "测试字符串");
    context.set("int", new Integer(-1));
    context.set("double", new Double(0.0));
    context.set("bean", bean);
    context.set("array",
        "Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar"
            .split(","));

    context.set("list", list);
    list.add(true);
    list.add(0);
    list.add(3.4);
    list.add("string");
    list.add(bean);

    context.set("map", map);
    map.put("boolean", true);
    map.put("int", 0);
    map.put("double", 3.4);
    map.put("string", "bean");
    map.put("bean", bean);

    engine.setTemplatePath(System.getProperty("user.dir").replace('\\', '/')
        + "/WEB-INF/classes/org/lilystudio/test");
  }

  @Test
  public void testBasicSyntax() throws Exception {
    Assert
    .assertEquals(
        "literal",
        getResult("{literal}aodun.alert_stop(\"设备删除成功！ \", function() { location.reload(); });{/literal}11"),
        "aodun.alert_stop(\"设备删除成功！ \", function() { location.reload(); });11");

    // 基础语法测试
    Assert.assertNull("空语句", getResult("{}"));
    Assert.assertNull("有空格的空语句", getResult("{  }"));
    Assert.assertNull("非法注释", getResult("{*}"));
    Assert.assertNull("非法注释", getResult("{ * }"));
    Assert.assertEquals("注释不受内部格式的影响", getResult("{* \"comment *}"), "");
    Assert.assertEquals("多余的空格测试", getResult("{ * comment * }"), "");
    Assert.assertEquals("注释中间包含多个*号", getResult("{*comment*comment*}"), "");

    // 注释语法测试, 注释不能只有一个*号, 句子末尾必须有*号, 注释中间不能随便使用引号
    Assert.assertNull("注释没有结束*号", getResult("{*comment}"));
    Assert.assertEquals("一般注释语法", getResult("{* comment *}"), "");

    // 函数语法测试
    Assert.assertNull("函数必须的条件不能省略", getResult("{if}{/if}"));
    Assert.assertNull("函数要有结束标记", getResult("{if $boolean}"));
    Assert
        .assertNull(
            "函数嵌入标记有顺序要求",
            getResult("{if $boolean}{foreach from=$list item='item'}{else}{/foreach}{/if}"));
    Assert.assertEquals("函数属性使用字符串可以不用引号",
        getResult("{assign var=out value='string'}{$out}"), "string");
    Assert.assertEquals("引号内的值嵌入", getResult("{'$string.'}"), "测试字符串.");
    Assert.assertEquals("引号内的值嵌入", getResult("{'$list[3]'}"), "string");
    Assert.assertEquals("引号内的值嵌入", getResult("{'`$map.string`'}"), "bean");
    Assert.assertEquals("引号内的转义字符", getResult("{'\\\"\\'\\$\\`'}"), "\"'$`");

    // 错误语法
    Assert.assertNull(".后面必须扩展", getResult("{$test.}"));
    Assert.assertNull("@后面必须扩展", getResult("{$test|@}"));
    Assert.assertNull("[]必须有内容", getResult("{$test[]}"));
    Assert.assertNull("[必须结束", getResult("{$test[}"));

    // 直接计算输出
    Assert.assertEquals("与运算", getResult("{$string && $int}"), "-1");
    Assert.assertEquals("与运算", getResult("{$null && $int}"), "null");
    Assert.assertEquals("或运算", getResult("{$string || $int}"), "测试字符串");
    Assert.assertEquals("或运算", getResult("{$null || $int}"), "-1");
  }

  @Test
  public void testBean() throws Exception {
    Context c = new Context();
    Bean bean = new Bean();
    c.putBean(bean);
    Template template = new Template(engine, "{$number}");
    Writer writer = new StringWriter();
    template.merge(c, writer);
    Assert.assertEquals("Bean导入", writer.toString(), "10");

    context.set("bean", bean);
    Assert.assertEquals("Bean导入",
        getResult("{$bean.number}{$bean.number}{$bean.aaa}{$bean.aaa}"),
        "1010nullnull");
  }

  @Test
  public void testAnalyse() throws Exception {
    // 基本的分词识别, 如果不包含任何内容, 或者内容只是一些空格, 提示语法错误
    Assert.assertNull("空语句", getResult("{}"));
    Assert.assertNull("空语句", getResult("{    }"));
    // 引号没有结束, 不是合法的句型
    Assert.assertEquals("引号", getResult("{\"}"), "{\"}");
    Assert.assertEquals("引号", getResult("{\"'}"), "{\"'}");

    Assert.assertEquals("文本输出", getResult("test\n"), "test\n");
    Assert.assertEquals("转义字符", getResult("{\"}'\\\"\\n\\t\"}"), "}'\"\n\t");
    Assert
        .assertEquals("转义字符", getResult("{'}\"\\'\\n\\t\\\\'}"), "}\"'\n\t\\");

    engine.setLeftDelimiter("<!--");
    engine.setRightDelimiter("-->");
    Assert.assertEquals("自定义左右边界符", getResult("<!--$string-->"), "测试字符串");
    engine.setLeftDelimiter("{");
    engine.setRightDelimiter("}");
  }

  @Test
  public void testPrint() throws Exception {
    Assert.assertNull("缺.引用", getResult("{$map(status)}"));
    Assert.assertNull("括号缺失", getResult("{$map.status)}"));
    Assert.assertNull("括号缺失", getResult("{$map.(status}"));
    Assert.assertNull("括号缺失", getResult("{$list[0}"));
    Assert.assertNull("括号缺失", getResult("{$list[0]]}"));
    Assert.assertEquals("普通变量", getResult("{$string}"), "测试字符串");
    Assert.assertEquals("普通映射", getResult("{$map.int}"), "0");
    Assert.assertEquals("Bean映射", getResult("{$bean.number}"), "10");
    Assert.assertEquals("普通列表", getResult("{$list[0]}"), "true");
    Assert.assertEquals("数组列表", getResult("{$array[4]}"), "Dragon");
    Assert.assertEquals("字符串", getResult("{$array[4][2]}"), "a");
    Assert.assertEquals("列表表达式", getResult("{$array[3 + 1]}"), "Dragon");
    Assert.assertEquals("NULL数据", getResult("{$list2.status[2]}"), "null");
    Assert.assertEquals("加括号组合", getResult("{($map.`$list[3]`)}"), "bean");
  }

  @Test
  public void testOperation() throws Exception {
    Assert.assertEquals("全等于", getResult("{if $null===$null}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if $null===$double}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if $int===$null}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if $null===null}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if $null===0}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if $null===0.0}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if $null===''}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 0===null}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 0===0}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if 0===0.0}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 0===''}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 0===$double}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 0.0===0.0}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if 0.0===$double}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if 0 + 0===$double}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if 1==='1'}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 1===-$int}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 1===$double-$int}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if 1===$double+(-$int)}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if -1===$int}1{/if}"), "1");
    Assert.assertEquals("全等于", getResult("{if 0-1===$int}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if $double-1===$int}1{/if}"), "");
    Assert.assertEquals("全等于", getResult("{if -$int===1}1{/if}"), "");

    Assert.assertEquals("全不等于", getResult("{if $null!==$null}1{/if}"), "");
    Assert.assertEquals("全不等于", getResult("{if $null!==$double}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if $int!==$null}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if $null!==null}1{/if}"), "");
    Assert.assertEquals("全不等于", getResult("{if $null!==0}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if $null!==0.0}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if $null!==''}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 0!==null}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 0!==0}1{/if}"), "");
    Assert.assertEquals("全不等于", getResult("{if 0!==0.0}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 0!==''}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 0!==$double}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 0.0!==0.0}1{/if}"), "");
    Assert.assertEquals("全不等于", getResult("{if 0.0!==$double}1{/if}"), "");
    Assert.assertEquals("全不等于", getResult("{if 0 + 0!==$double}1{/if}"), "");
    Assert.assertEquals("全不等于", getResult("{if 1!=='1'}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 1!==-$int}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 1!==$double-$int}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if 1!==$double+(-$int)}1{/if}"),
        "1");
    Assert.assertEquals("全不等于", getResult("{if 0-1!==$int}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if $double-1!==$int}1{/if}"), "1");
    Assert.assertEquals("全不等于", getResult("{if -$int!==1}1{/if}"), "1");

    Assert.assertEquals("等于", getResult("{if $null==$null}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if $null==$double}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if $int==$null}1{/if}"), "");
    Assert.assertEquals("等于", getResult("{if $null==null}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if $null==0}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if $null==0.0}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if $null==''}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0==null}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0==0}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0==0.0}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0==''}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0==$double}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0.0==0.0}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0.0==$double}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0 + 0==$double}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 1=='1'}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 1==-$int}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 1==$double-$int}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 1==$double+(-$int)}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if -1==$int}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if 0-1==$int}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if $double-1==$int}1{/if}"), "1");
    Assert.assertEquals("等于", getResult("{if -$int==1}1{/if}"), "1");

    Assert.assertEquals("不等于", getResult("{if $null!=$null}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if $null!=$double}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if $int!=$null}1{/if}"), "1");
    Assert.assertEquals("不等于", getResult("{if $null!=null}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if $null!=0}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if $null!=0.0}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if $null!=''}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0!=null}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0!=0}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0!=0.0}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0!=''}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0!=$double}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0.0!=0.0}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0.0!=$double}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0 + 0!=$double}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 1!='1'}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 1!=-$int}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 1!=$double-$int}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 1!=$double+(-$int)}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if -1!=$int}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if 0-1!=$int}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if $double-1!=$int}1{/if}"), "");
    Assert.assertEquals("不等于", getResult("{if -$int!=1}1{/if}"), "");

    Assert.assertEquals("一元检测", getResult("{if $null}1{/if}"), "");
    Assert.assertEquals("一元检测", getResult("{if $list}1{/if}"), "1");
    Assert.assertEquals("一元检测", getResult("{if null}1{/if}"), "");
    Assert.assertEquals("一元检测", getResult("{if 0}1{/if}"), "");
    Assert.assertEquals("一元检测", getResult("{if 0.0}1{/if}"), "");
    Assert.assertEquals("一元检测", getResult("{if ''}1{/if}"), "");
    Assert.assertEquals("一元检测", getResult("{if '0'}1{/if}"), "1");

    Assert.assertEquals("小于", getResult("{if 11<11.0}1{/if}"), "");
    Assert.assertEquals("小于", getResult("{if '11'<11.0}1{/if}"), "");
    Assert.assertEquals("小于", getResult("{if 11<'11.0'}1{/if}"), "");
    Assert.assertEquals("小于", getResult("{if '11'<'11.0'}1{/if}"), "1");
    Assert.assertEquals("小于等于", getResult("{if 11<=11.0}1{/if}"), "1");
    Assert.assertEquals("小于等于", getResult("{if '11'<=11.0}1{/if}"), "1");
    Assert.assertEquals("小于等于", getResult("{if 11<='11.0'}1{/if}"), "1");
    Assert.assertEquals("小于等于", getResult("{if '11'<='11.0'}1{/if}"), "1");
    Assert.assertEquals("大于", getResult("{if 11>11.0}1{/if}"), "");
    Assert.assertEquals("大于", getResult("{if '11'>11.0}1{/if}"), "");
    Assert.assertEquals("大于", getResult("{if 11>'11.0'}1{/if}"), "");
    Assert.assertEquals("大于", getResult("{if '11'>'11.0'}1{/if}"), "");
    Assert.assertEquals("大于等于", getResult("{if 11>=11.0}1{/if}"), "1");
    Assert.assertEquals("大于等于", getResult("{if '11'>=11.0}1{/if}"), "1");
    Assert.assertEquals("大于等于", getResult("{if 11>='11.0'}1{/if}"), "1");
    Assert.assertEquals("大于等于", getResult("{if '11'>='11.0'}1{/if}"), "");
    Assert.assertEquals("与", getResult("{if '' && null}1{/if}"), "");
    Assert.assertEquals("与", getResult("{if '' && 1.1}1{/if}"), "");
    Assert.assertEquals("与", getResult("{if 1.1 && ''}1{/if}"), "");
    Assert.assertEquals("与", getResult("{if '0.1' && 1.1}1{/if}"), "1");
    Assert.assertEquals("或", getResult("{if '' || null}1{/if}"), "");
    Assert.assertEquals("或", getResult("{if '' || 1.1}1{/if}"), "1");
    Assert.assertEquals("或", getResult("{if 1.1 || ''}1{/if}"), "1");
    Assert.assertEquals("或", getResult("{if '0.1' || 1.1}1{/if}"), "1");
    Assert.assertEquals("非", getResult("{if !''}1{/if}"), "1");
    Assert.assertEquals("非", getResult("{if !$null}1{/if}"), "1");
    Assert.assertEquals("非", getResult("{if !0}1{/if}"), "1");
    Assert.assertEquals("表达式计算", getResult("{if 1.1+1>2}1{/if}"), "1");
    Assert.assertEquals("表达式计算", getResult("{if 1.1+'1'>2}1{/if}"), "1");
    Assert.assertEquals("表达式计算", getResult("{if (1.1+!'1')>2}1{/if}"), "");
    Assert.assertEquals("表达式计算", getResult("{if (1.1+'1')>2}1{/if}"), "1");
    Assert.assertEquals("表达式计算", getResult("{if 2 is even}1{/if}"), "1");
    Assert.assertEquals("表达式计算", getResult("{if 1.1+null<1.11}1{/if}"), "1");
    Assert.assertEquals("表达式计算", getResult("{`$int + 1`}"), "0");
  }

  @Test
  public void testFunction() throws Exception {
    Assert
        .assertNull(getResult("{foreach from=}kick\n{else}{/foreach}\n\n{if a+}{$c|indent:'1':1:1}"));
    Assert
    .assertEquals(
        "assign",
        getResult("{assign var=\"name\" value=$list[0]}{$name}."),
        "true.");
    Assert
        .assertEquals(
            "assign",
            getResult("{assign var=\"name\" value=\"Bob\"}The value of $name is {$name}."),
            "The value of $name is Bob.");
    Assert
        .assertEquals(
            "assign",
            getResult("{assign var=\"name\" value=\"Bob,Andy\" delimiter=\",\"}The value of $name is {$name[0]}."),
            "The value of $name is Bob.");
    context.set("name", null);
    Assert
        .assertEquals(
            "break",
            getResult("{section loop=$array name=\"item\" start=5 step=-2 max=2}{break}{$item}\n{/section}"),
            "");
    Assert
        .assertEquals(
            "break",
            getResult("{foreach from=$array item=\"item1\"}{foreach from=$array item=\"item\" key=\"key\"}{if $key==4}{break 2}{/if}{eval var='{\\$key}:{\\$item}'}\n{/foreach}{/foreach}"),
            "0:Rat\n1:Ox\n2:Tiger\n3:Hare\n");
    Assert
        .assertNull(
            "break",
            getResult("{foreach from=$array item=\"item\" key=\"key\"}{if $key==4}{break 2}{/if}{$key}:{$item}\n{/foreach}"));
    Assert
        .assertNull(
            "break",
            getResult("{foreach from=$array item=\"item\" key=\"key\"}{if $key==4}{break 1.0}{/if}{$key}:{$item}\n{/foreach}"));
    {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Template template = new Template(engine, "{bytes $bytes}");
      template.merge(context, out);
      Assert.assertEquals("bytes", out.toString("UTF-8"), "测试字节数组");
    }
    Assert.assertNull("bytes", getResult("{bytes \"key\"}"));
    Assert
        .assertEquals(
            "capture",
            getResult("{capture assign=\"default\"}commands must be paired{/capture}{$default}"),
            "commands must be paired");
    Assert
        .assertEquals(
            "capture",
            getResult("{capture name=\"default\"}commands must be paired{/capture}{$smarty.capture.default}"),
            "commands must be paired");
    Assert
        .assertEquals(
            "continue",
            getResult("{foreach from=$array item=\"item\" key=\"key\"}{if $key>=4}{continue}{/if}{$key}:{$item}\n{/foreach}"),
            "0:Rat\n1:Ox\n2:Tiger\n3:Hare\n");
    Assert
        .assertEquals(
            "continue",
            getResult("{section loop=$array name=\"item1\"}{foreach from=$array item=\"item\" key=\"key\"}{if $key==1}{continue 2}{/if}{$item}\n{/foreach}{$item1}{/section}"),
            "Rat\nRat\nRat\nRat\nRat\nRat\nRat\nRat\nRat\nRat\nRat\nRat\n");
    Assert
        .assertNull(
            "continue",
            getResult("{section loop=$array name=\"item\" start=5 step=-2 max=2}{continue 2}{$item}\n{/section}"));
    Assert
        .assertNull(
            "continue",
            getResult("{section loop=$array name=\"item\" start=5 step=-2 max=2}{continue -1}{$item}\n{/section}"));
    Assert
        .assertEquals(
            "counter",
            getResult("{counter start=0 skip=2 print=false direction=\"keep\"}{counter direction=\"up\"}-{counter}-{counter skip=1}-{counter}"),
            "0-2-4-5");
    Assert.assertEquals("cycle",
        getResult("{cycle values=\"#eeeeee,#d0d0d0\"}{cycle}"),
        "#eeeeee#d0d0d0");
    Assert
        .assertEquals(
            "date_format",
            getResult("{date_format from=\"yyyy-MM-dd\" to=\"MM/dd/yyyy HH:mm:ss\" date=\"2006-01-01\" timezone=8}"),
            "01/01/2006 08:00:00");
    Assert.assertEquals("else", getResult("{if 0}1{else}2{/if}"), "2");
    Assert.assertEquals("elseif", getResult("{if 0}1{elseif 1}2{/if}"), "2");
    engine.setLeftDelimiter("<!--");
    Assert
        .assertEquals(
            "eval",
            getResult("<!--capture name=\"test\"}<!--ldelim}$smarty.capture.test<!--rdelim}<!--/capture}<!--eval var=$smarty.capture.test}"),
            "<!--$smarty.capture.test}");
    engine.setLeftDelimiter("{");
    Assert
        .assertEquals(
            "foreach",
            getResult("{assign var=\"animals\" value=\"Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar\" delimiter=\",\"}{foreach name=test from=$animals item=\"item\" key=\"key\"}{$smarty.foreach.test.first}:{$smarty.foreach.test.last}\n{/foreach}"),
            "true:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:false\nfalse:true\n");
    Assert
        .assertEquals(
            "foreach",
            getResult("{assign var=\"animals\" value=\"Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar\" delimiter=\",\"}{foreach from=$animals item=\"item\" key=\"key\"}{$key}:{$item}\n{/foreach}"),
            "0:Rat\n1:Ox\n2:Tiger\n3:Hare\n4:Dragon\n5:Serpent\n6:Horse\n7:Sheep\n8:Monkey\n9:Rooster\n10:Dog\n11:Boar\n");
    Assert
        .assertEquals(
            "foreach",
            getResult("{assign var=\"animals\" value=\"Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar\" delimiter=\",\"}{assign var=\"item\" value='1'}{assign var=\"key\" value='A'}{foreach from=$animals item=\"item\" key=\"key\"}{assign var=\"key\" value=\"1\"}{$key}:{$item}\n{/foreach}{$item}{$key}"),
            "1:Rat\n1:Ox\n1:Tiger\n1:Hare\n1:Dragon\n1:Serpent\n1:Horse\n1:Sheep\n1:Monkey\n1:Rooster\n1:Dog\n1:Boar\n1A");
    Assert
        .assertEquals(
            "foreach",
            getResult("{assign var=\"animals\" value=\"Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar\" delimiter=\",\"}{assign var=\"item\" value='1'}{assign var=\"key\" value='A'}{foreach from=$animals item=\"item\"}{assign var=\"key\" value=\"1\"}{$key}:{$item}\n{/foreach}{$item}{$key}"),
            "1:Rat\n1:Ox\n1:Tiger\n1:Hare\n1:Dragon\n1:Serpent\n1:Horse\n1:Sheep\n1:Monkey\n1:Rooster\n1:Dog\n1:Boar\n11");
    Assert
        .assertEquals(
            "foreach",
            getResult("{foreach from=$map item=\"item\" key=\"key\"}{if $key=='boolean'}{eval var='{\\$key}:{\\$item}'}\n{/if}{/foreach}"),
            "boolean:true\n");
    Assert
        .assertEquals(
            "section",
            getResult("{assign var=\"animals\" value=\"Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar\" delimiter=\",\"}{section loop=$animals name=\"item\" start=5 step=-2 max=2}{$item}\n{/section}"),
            "Serpent\nHare\n");
    Assert
        .assertEquals(
            "section",
            getResult("{assign var=\"animals\" value=\"Rat,Ox,Tiger,Hare,Dragon,Serpent,Horse,Sheep,Monkey,Rooster,Dog,Boar\" delimiter=\",\"}{section loop=$animals name=\"item\" start=5 step=-2 max=2}{assign var=\"key\" value=\"1\"}{$item}\n{/section}"),
            "Serpent\nHare\n");
    Assert
        .assertEquals(
            "foreachelse",
            getResult("{foreach from=$animals1 item=\"item\" key=\"key\"}{$key}:{$item}\n{foreachelse}2{/foreach}"),
            "2");
    Assert.assertEquals("literal",
        getResult("{literal}a\\nb\n{assign}aa{/literal}"), "a\\nb\n{assign}aa");
    Assert.assertEquals("math",
        getResult("{math equation=\"(x+(x+y * y))*x\" x='3' y=1}"), "21");
    context.set("border", 0);
    context.set("URL", "http://my.domain.com");
    Assert
        .assertEquals(
            "strip",
            getResult("{strip}\n<table border={$border}>\n  <tr>\n    <td>\n<pre>\n <b >Test</ b><radio   checked>\n</pre   >\n<textarea>\n <b >Test</ b><radio   checked>\n< / textarea >\t<a   href  =  \"{$URL}\"  >\n      <  font   color=\"red\"  >This is a test</font>\n      <  /  a  >\n    </td>\n  </tr>\n\r</table>{/strip}1111"),
            "<table border=0><tr><td><pre>\n <b>Test</b><radio checked>\n</pre><textarea>\n <b >Test</ b><radio   checked>\n</textarea><a href=\"http://my.domain.com\"><font color=\"red\">This is a test</font></a></td></tr></table>1111");
    Assert
        .assertEquals(
            "strip",
            getResult("{strip}<style>\n  .aa   #bb \n {  ;;background  :  no-repeat    #FFF   ;  \n  border:1px solid red;}  .bb   {   \n\ttext-align:center\n  }  \n</style><script>\n\tvar a=\"</script>\"\n</script>{/strip}"),
            "<style>.aa #bb{background:no-repeat #FFF;border:1px solid red}.bb{text-align:center}</style><script>\n\tvar a=\"</script>\"\n</script>");
    Assert.assertEquals("while",
        getResult("{while $int<5}{assign var='int' value=10}{$int}{/while}"),
        "10");
  }

  @Test
  public void testModifier() throws Exception {
    Assert.assertEquals("count$LIST", getResult("{if $list|@count == 5}true{/if}"), "true");
    Assert.assertEquals("default$list", getResult("{$aaa|default:$list[0]}"), "true");
    Assert.assertNull(getResult("{$cat|cat:12}"));
    Assert.assertNull(getResult("{$cat|}"));
    Assert.assertEquals(getResult("{$cat|default|cat::12}"), "");
    Assert.assertEquals("b2s", getResult("{$bytes|b2s:'UTF-8'}"), "测试字节数组");
    Assert
        .assertEquals(
            "capitalize",
            getResult("{\"Police begin campaign to rundown jaywalkers.\"|capitalize}"),
            "Police Begin Campaign To Rundown Jaywalkers.");
    Assert.assertEquals("cat",
        getResult("{\"Psychics predict world didn't end\"|cat:' yesterday.'}"),
        "Psychics predict world didn't end yesterday.");
    Assert.assertEquals("count_characters",
        getResult("{\"Cold Wave Linked to Temperatures.\"|count_characters}"),
        "33");
    context
        .set(
            "count_paragraphs",
            "War Dims Hope for Peace. Child's Death Ruins Couple's Holiday.\n\nMan is Fatally Slain. Death Causes Loneliness, Feeling of Isolation.");
    Assert.assertEquals("count_paragraphs",
        getResult("{$count_paragraphs|count_paragraphs}"), "2");
    Assert
        .assertEquals(
            "count_sentences",
            getResult("{\"Two Soviet Ships Collide - One Dies. Enraged Cow Injures Farmer with Axe.\"|count_sentences}"),
            "2");
    context.set("count_words", "");
    Assert
        .assertEquals("count_words",
            getResult("{\"Dealers Will Hear Car Talk at Noon.\"|count_words}"),
            "7");
    Assert.assertNotNull("date_format", getResult("{$now|date_format:::'US'}"));
    Assert
        .assertEquals(
            "default$NOTNULL",
            getResult("{\"Dealers Will Hear Car Talk at Noon.\"|default:\"no title\"}"),
            "Dealers Will Hear Car Talk at Noon.");
    Assert.assertEquals("default$NULL",
        getResult("{$default1|default:\"no \\\\title\"}"), "no \\title");
    context.set("escape", "\\\"<html>");
    Assert.assertEquals("escape$HTML", getResult("{$escape|escape}"),
        "\\&#34;&#60;html&#62;");
    Assert.assertEquals("escape$QUOTE", getResult("{$escape|escape:'quotes'}"),
        "\\\\\\\"<html>");
    context.set("html_format", "<html><body link=\"asdf\"><div link='");
    Assert.assertEquals("html_format", getResult("{$html_format|html_format}"),
        "<html><body link=\"asdf\"></body></html>");
    context.set("indent", "kick\ntest\n");
    Assert.assertEquals("indent", getResult("{$indent|indent}"),
        "    kick\n    test\n    ");
    context.set("length", "kick");
    Assert.assertEquals("count$STRING", getResult("{$length|count}"), "4");
    Assert.assertEquals("count$LIST", getResult("{$list|@count}"), "5");
    Assert.assertEquals("lower",
        getResult("{\"Two Convicts Evade Noose, Jury Hung.\"|lower}"),
        "two convicts evade noose, jury hung.");
    context.set("nl2br", "Sun or rain expected\ntoday, dark tonight");
    Assert.assertEquals("nl2br", getResult("{$nl2br|nl2br}"),
        "Sun or rain expected<br/>today, dark tonight");
    Assert
        .assertEquals(
            "regex_replace",
            getResult("{\"Infertility unlikely to\\nbe passed on, experts say.\"|regex_replace:'[\\r\\t\\n]':' '}"),
            "Infertility unlikely to be passed on, experts say.");
    Assert
        .assertEquals(
            "replace",
            getResult("{\"Child's Stool Great for Use in Garden.$\"|replace:' ':'   '|replace:'$':'$1'}"),
            "Child's   Stool   Great   for   Use   in   Garden.$1");
    Assert
        .assertEquals(
            "spacify",
            getResult("{\"Something Went Wrong in Jet Crash, Experts Say.\"|spacify:'^^'}"),
            "S^^o^^m^^e^^t^^h^^i^^n^^g^^ ^^W^^e^^n^^t^^ ^^W^^r^^o^^n^^g^^ ^^i^^n^^ ^^J^^e^^t^^ ^^C^^r^^a^^s^^h^^,^^ ^^E^^x^^p^^e^^r^^t^^s^^ ^^S^^a^^y^^.");
    context
        .set(
            "strip_tags",
            "Blind Woman Gets <font face=\"helvetica>\">New Kidney</font> from Dad she Hasn't Seen in <b>years</b>.");
    Assert.assertEquals("strip_tags", getResult("{$strip_tags|strip_tags}"),
        "Blind Woman Gets New Kidney from Dad she Hasn't Seen in years.");
    context.set("strip", "Grandmother of\neight makes\t    hole in one.");
    Assert
        .assertEquals("strip", getResult("{$strip|strip:\"&nbsp;\"}"),
            "Grandmother&nbsp;of&nbsp;eight&nbsp;makes&nbsp;hole&nbsp;in&nbsp;one.");
    Assert.assertEquals("strip", getResult("{$strip|strip:\"\\\\\"}"),
        "Grandmother\\of\\eight\\makes\\hole\\in\\one.");
    context.set("truncate",
        "中国Two Sisters Reunite after Eighteen Years at Checkout Counter.");
    Assert
        .assertEquals(
            "truncate",
            getResult("{$truncate|truncate:30:\"...\"}"),
            "中国Two Sisters Reunite aft<span title=\"中国Two Sisters Reunite after Eighteen Years at Checkout Counter.\">...</span>");
    Assert
        .assertEquals(
            "truncate",
            getResult("{$truncate|truncate:4:\".\"}"),
            "中<span title=\"中国Two Sisters Reunite after Eighteen Years at Checkout Counter.\">.</span>");
    Assert
        .assertEquals(
            "truncate",
            getResult("{$truncate|truncate:30:\"---\":true}"),
            "中国Two Sisters Reunite<span title=\"中国Two Sisters Reunite after Eighteen Years at Checkout Counter.\">---</span>");
    Assert
        .assertEquals(
            "upper",
            getResult("{\"If Strike isn't Settled Quickly it may Last a While.\"|upper}"),
            "IF STRIKE ISN'T SETTLED QUICKLY IT MAY LAST A WHILE.");
  }

  private String getResult(String data) throws Exception {
    try {
      Template template = new Template(engine, data);
      Writer writer = new StringWriter();
      template.merge(context, writer);
      return writer.toString();
    } catch (Exception e) {
      return null;
    }
  }
}