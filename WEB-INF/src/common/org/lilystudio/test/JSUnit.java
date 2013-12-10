package org.lilystudio.test;

import java.io.StringReader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lilystudio.javascript.JSCompressor;
import org.lilystudio.util.StringWriter;

/**
 * Smarty单元测试类
 * 
 * @version 0.1.4, 2009/03/01
 * @author 欧阳先伟
 * @since Smarty 0.1
 */
public class JSUnit {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void expression() throws Exception {
    Assert.assertEquals("分隔符", "i- -a", getResult("i- -a"));
    Assert.assertEquals("分隔符", "i-+a", getResult("i-+a"));
    Assert.assertEquals("分隔符", "i+-a", getResult("i+-a"));
    Assert.assertEquals("分隔符", "i+ +a", getResult("i+ +a"));
    Assert.assertEquals("分隔符", "i++ +a", getResult("i++ +a"));
    Assert.assertEquals("分隔符", "i-- -a", getResult("i-- -a"));
    Assert.assertEquals("分隔符", "i++ + ++a", getResult("i++ + ++a"));
    Assert.assertEquals("分隔符", "i-- - --a", getResult("i-- - --a"));
    Assert.assertEquals("分隔符", "i++-++a", getResult("i++ - ++a"));

    Assert.assertEquals("空循环语句", "for(;;);", getResult("for(;;);"));
    Assert.assertEquals("空循环语句", "for(;;);", getResult("while(true);"));
    Assert.assertEquals("空循环语句", "for(;;);", getResult("do;while(true);"));

    Assert.assertEquals("this表达式", "this", getResult("this"));
    Assert.assertEquals("标识符表达式", "test", getResult("test"));
    Assert.assertEquals("true表达式", "true", getResult("true"));
    Assert.assertEquals("false表达式", "false", getResult("false"));
    Assert.assertEquals("null表达式", "null", getResult("null"));
    Assert.assertEquals("字符串表达式", "'\"'", getResult("\"\\\"\""));
    Assert.assertEquals("数字表达式", "123", getResult("123"));
    Assert.assertEquals("数字表达式", "123.1", getResult("123.1"));
    Assert.assertEquals("数字表达式", ".1", getResult(".1"));
    Assert.assertEquals("数字表达式", ".1", getResult("0.1"));
    Assert.assertEquals("正则表达式", "/ad/", getResult("/ad/"));
    Assert.assertEquals("正则表达式", "/ad/ig", getResult("/ad/ig"));
    Assert.assertEquals("正则表达式", "/\\/ad/", getResult("/\\/ad/"));
    Assert.assertEquals("数组表达式", "[]", getResult("[]"));
    Assert.assertEquals("数组表达式", "[12]", getResult("[12]"));
    Assert.assertEquals("数组表达式", "[a,12]", getResult("[a,12]"));
    Assert.assertEquals("对象表达式", "({\"a..\":12,b:\"a\"})",
        getResult("({'a..':12,'b':'a'})"));
    Assert.assertEquals("对象表达式", "({a:b})", getResult("({a:b})"));

    Assert.assertEquals("成员表达式", "a.b.c", getResult("a.b.c"));
    Assert.assertEquals("成员属性表达式", "a.b.c", getResult("a['b']['c']"));
    Assert.assertEquals("成员属性表达式", "a[b][c]", getResult("a[b][c]"));
    Assert.assertEquals("成员属性表达式优先级", "({a:b,c:d}).a",
        getResult("({a:b,c:d}).a"));
    Assert.assertEquals("函数调用", "a()", getResult("a()"));
    Assert.assertEquals("函数调用(带一个参数)", "a(b)", getResult("a(b)"));
    Assert.assertEquals("函数调用(带多个参数)", "a(b,c)", getResult("a(b,c)"));
    Assert.assertEquals("new 调用(带参数)", "new a(b)", getResult("new a(b)"));
    Assert.assertEquals("new 调用优化", "({})", getResult("new Object()"));
    Assert.assertEquals("new 调用优化", "a={}", getResult("a=new Object()"));
    Assert.assertEquals("new 调用优化", "a=Array(a+b)",
        getResult("a=new Array(a + b)"));
    Assert.assertEquals("new 调用优化", "a=Array(a-b)",
        getResult("a=new Array(a - b)"));
    Assert.assertEquals("new 调用优化", "a=[]", getResult("a=new Array()"));
    Assert.assertEquals("new 调用优化", "a=[]", getResult("a=new Array(0)"));
    Assert.assertEquals("new 调用优化", "a=Array(1)", getResult("a=new Array(1)"));
    Assert.assertEquals("new 调用优化", "a=[1,2]", getResult("a=new Array(1,2)"));
    Assert.assertEquals("new 调用优化", "a={}", getResult("a=Object()"));
    Assert.assertEquals("new 调用优化", "a=[]", getResult("a=Array()"));
    Assert.assertEquals("new 调用优化", "a=[]", getResult("a=Array(0)"));
    Assert.assertEquals("new 调用优化", "a=Array(1)", getResult("a=Array(1)"));
    Assert.assertEquals("new 调用优化", "a=[1,2]", getResult("a=Array(1,2)"));

    Assert.assertEquals("自增表达式", "a++", getResult("a++"));
    Assert.assertEquals("自增表达式", "a.a++", getResult("a.a++"));
    Assert.assertEquals("自减表达式", "a--", getResult("a--"));

    Assert.assertEquals("delete表达式", "delete a", getResult("delete a"));
    Assert.assertEquals("delete表达式", "delete a.a", getResult("delete a.a"));
    Assert.assertEquals("delete表达式", "delete a.a", getResult("delete a['a']"));
    Assert.assertEquals("delete表达式", "delete a['this']",
        getResult("delete a['this']"));
    Assert.assertEquals("delete表达式", "delete a.a[b]",
        getResult("delete a.a[b]"));
    Assert.assertEquals("void表达式", "void a", getResult("void a"));
    Assert.assertEquals("typeof表达式", "typeof a", getResult("typeof(a)"));
    Assert.assertEquals("复杂typeof表达式", "typeof a.b", getResult("typeof(a.b)"));
    Assert.assertEquals("++表达式", "++a", getResult("++a"));
    Assert.assertEquals("++表达式", "++a.a", getResult("++a.a"));
    Assert.assertEquals("--表达式", "--a", getResult("--a"));
    Assert.assertEquals("+表达式", "+a", getResult("+a"));
    Assert.assertEquals("-表达式", "-a", getResult("-a"));
    Assert.assertEquals("-表达式", "-a", getResult("-a"));
    Assert.assertEquals("~表达式", "~a", getResult("~a"));
    Assert.assertEquals("!表达式", "!a", getResult("!a"));
    Assert.assertEquals("单目表达式优先级", "!(++a)", getResult("!(++a)"));

    Assert.assertEquals("*表达式", "a*b", getResult("a*b"));
    Assert.assertEquals("/表达式", "a/b", getResult("a/b"));
    Assert.assertEquals("%表达式", "a%b", getResult("a%b"));

    Assert.assertEquals("+表达式", "a+b", getResult("a+b"));
    Assert.assertEquals("-表达式", "a-b", getResult("a-b"));
    Assert.assertEquals("字符串+表达式优化", "\"ab\"", getResult("'a'+'b'"));

    Assert.assertEquals("<<表达式", "a<<b", getResult("a<<b"));
    Assert.assertEquals(">>表达式", "a>>b", getResult("a>>b"));
    Assert.assertEquals(">>>表达式", "a>>>b", getResult("a>>>b"));

    Assert.assertEquals(">表达式", "a>b", getResult("a>b"));
    Assert.assertEquals(">=表达式", "a>=b", getResult("a>=b"));
    Assert.assertEquals("<表达式", "a<b", getResult("a<b"));
    Assert.assertEquals("<=表达式", "a<=b", getResult("a<=b"));
    Assert.assertEquals("instanceof表达式", "a instanceof b",
        getResult("a instanceof b"));
    Assert.assertEquals("instanceof表达式", "a instanceof(b instanceof c)",
        getResult("a instanceof (b instanceof c)"));
    Assert.assertEquals("in表达式", "a in b", getResult("a in b"));

    Assert.assertEquals("==表达式", "a==b", getResult("a==b"));
    Assert.assertEquals("!=表达式", "a!=b", getResult("a!=b"));
    Assert.assertEquals("===表达式", "a===b", getResult("a===b"));
    Assert.assertEquals("!==表达式", "a!==b", getResult("a!==b"));

    Assert.assertEquals("&表达式", "a&b", getResult("a&b"));
    Assert.assertEquals("^表达式", "a^b", getResult("a^b"));
    Assert.assertEquals("|表达式", "a|b", getResult("a|b"));

    Assert.assertEquals("&&表达式", "a&&b", getResult("a&&b"));
    Assert.assertEquals("||表达式", "a||b", getResult("a||b"));

    Assert.assertEquals("?:表达式", "a?b:c", getResult("a?b:c"));

    Assert.assertEquals("=表达式", "a=b", getResult("a=b"));
    Assert.assertEquals("=表达式", "a=b+c", getResult("a=b+c"));
    Assert.assertEquals("=表达式", "a.a=b", getResult("a.a=b"));
    Assert.assertEquals("=表达式", "a.a=b", getResult("a['a']=b"));
    Assert.assertEquals("=表达式", "a[a]=b", getResult("a[a]=b"));
    Assert.assertEquals("=表达式", "a=b=c", getResult("a=b=c"));
    Assert.assertEquals("*=表达式", "a*=b", getResult("a*=b"));
    Assert.assertEquals("*=表达式", "a.a*=b", getResult("a.a*=b"));
    Assert.assertEquals("/=表达式", "a/=b", getResult("a/=b"));
    Assert.assertEquals("%=表达式", "a%=b", getResult("a%=b"));
    Assert.assertEquals("+=表达式", "a+=b", getResult("a+=b"));
    Assert.assertEquals("-=表达式", "a-=b", getResult("a-=b"));
    Assert.assertEquals("<<=表达式", "a<<=b", getResult("a<<=b"));
    Assert.assertEquals(">>=表达式", "a>>=b", getResult("a>>=b"));
    Assert.assertEquals(">>>=表达式", "a>>>=b", getResult("a>>>=b"));
    Assert.assertEquals("&=表达式", "a&=b", getResult("a&=b"));
    Assert.assertEquals("^=表达式", "a^=b", getResult("a^=b"));
    Assert.assertEquals("|=表达式", "a|=b", getResult("a|=b"));
    Assert.assertEquals("表达式优化", "a+=b", getResult("a=a+b"));
    Assert.assertEquals("表达式优化", "a=b+a", getResult("a=b+a"));
    Assert.assertEquals("表达式优化", "a*=b", getResult("a=a*b"));
    Assert.assertEquals("表达式优化", "a*=b", getResult("a=b*a"));
    Assert.assertEquals("表达式优化", "a&=b", getResult("a=a&b"));
    Assert.assertEquals("表达式优化", "a&=b", getResult("a=b&a"));
    Assert.assertEquals("表达式优化", "a^=b", getResult("a=a^b"));
    Assert.assertEquals("表达式优化", "a^=b", getResult("a=b^a"));
    Assert.assertEquals("表达式优化", "a|=b", getResult("a=a|b"));
    Assert.assertEquals("表达式优化", "a|=b", getResult("a=b|a"));

    Assert.assertEquals(",表达式", "a,b", getResult("a,b"));
    Assert.assertEquals("=表达式", "a,b,c", getResult("a,b,c"));

    Assert.assertEquals("表达式优先级", "a&&b&&c", getResult("a&&(b&&c)"));
    Assert.assertEquals("表达式优先级", "a&&b&&c", getResult("(a&&b)&&c"));
    Assert.assertEquals("表达式优先级", "a&&(b||c)", getResult("a&&(b||c)"));
    Assert.assertEquals("表达式优先级", "a&&b||c", getResult("(a&&b)||c"));
    Assert.assertEquals("表达式优先级", "a||b||c", getResult("a||(b||c)"));
    Assert.assertEquals("表达式优先级", "a||b||c", getResult("(a||b)||c"));
    Assert.assertEquals("表达式优先级", "a&b&c", getResult("a&(b&c)"));
    Assert.assertEquals("表达式优先级", "a&b&c", getResult("(a&b)&c"));
    Assert.assertEquals("表达式优先级", "a|b|c", getResult("a|(b|c)"));
    Assert.assertEquals("表达式优先级", "a|b|c", getResult("(a|b)|c"));
    Assert.assertEquals("表达式优先级", "a^b^c", getResult("a^(b^c)"));
    Assert.assertEquals("表达式优先级", "a^b^c", getResult("(a^b)^c"));
    Assert.assertEquals("表达式优先级", "a+b+c", getResult("a+(b+c)"));
    Assert.assertEquals("表达式优先级", "a+b+c", getResult("(a+b)+c"));
    Assert.assertEquals("表达式优先级", "a+b-c", getResult("a+(b-c)"));
    Assert.assertEquals("表达式优先级", "a+b-c", getResult("(a+b)-c"));
    Assert.assertEquals("表达式优先级", "a*b*c", getResult("a*(b*c)"));
    Assert.assertEquals("表达式优先级", "a*b*c", getResult("(a*b)*c"));
    Assert.assertEquals("表达式优先级", "a*b/c", getResult("a*(b/c)"));
    Assert.assertEquals("表达式优先级", "a*b/c", getResult("(a*b)/c"));
    Assert.assertEquals("表达式优先级", "a-(b+c)", getResult("a-(b+c)"));
    Assert.assertEquals("表达式优先级", "a-(b-c)", getResult("a-(b-c)"));
    Assert.assertEquals("表达式优先级", "a/b*c", getResult("a/b*c"));
    Assert.assertEquals("表达式优先级", "a/(b*c)", getResult("a/(b*c)"));
    Assert.assertEquals("表达式优先级", "a/(b/c)", getResult("a/(b/c)"));
    Assert.assertEquals("表达式优先级", "a/(b+c)", getResult("a/(b+c)"));
    Assert.assertEquals("表达式优先级", "a+b*c", getResult("a+(b*c)"));

    Assert.assertEquals("语句块", "a;b", getResult("{a\nb}"));
    Assert.assertEquals("语句块", "a", getResult("{a}"));
    Assert.assertEquals("语句块", "a;({})", getResult("a;({})"));

    Assert.assertEquals("单个变量声明", "var a", getResult("var a"));
    Assert.assertEquals("多个变量声明", "var a,b", getResult("var a,b"));
    Assert.assertEquals("变量声明并赋值", "var a=10,b", getResult("var a=10,b"));
    Assert.assertEquals("var语句优化", "var a=10,b", getResult("var a=10;var b"));

    Assert.assertEquals("空语句", "", getResult(";;"));
    Assert.assertEquals("空语句", "", getResult("{;}"));
    Assert.assertEquals("空语句", "", getResult("{{;}}"));

    Assert.assertEquals("if语句", "if(a<10)a++", getResult("if(a<10)a++"));
    Assert.assertEquals("if语句", "if(a<10){a++;b++}",
        getResult("if(a<10){a++;b++}"));
    Assert.assertEquals("if语句", "if(a<10)a++;else b++",
        getResult("if(a<10){a++}else{b++}"));
    Assert.assertEquals("if语句", "if(a<10){a++;b++}else b++",
        getResult("if(a<10){a++;b++}else{b++}"));
    Assert.assertEquals("if语句", "if(a<10)a++;else{a++;b++}",
        getResult("if(a<10){a++}else{a++;b++}"));
    Assert.assertEquals("if语句", "if(a<10){a++;b++}else{a++;b++}",
        getResult("if(a<10){a++;b++}else{a++;b++}"));
    Assert.assertEquals("if语句", "if(a<10)if(b)a++;else b++",
        getResult("if(a<10)if(b){a++}else{b++;}"));
    Assert.assertEquals("if语句", "if(a<10)a++;else if(b)b++",
        getResult("if(a<10){a++}else if(b){b++;}"));
    Assert.assertEquals("if语句", "if(a<10){if(b)b++}else b++",
        getResult("if(a<10){if(b){b++}}else{b++;}"));
    Assert.assertEquals("if语句优化", "a<10", getResult("if(a<10);"));
    Assert.assertEquals("if语句优化", "a<10", getResult("if(a<10){}"));
    Assert.assertEquals("if语句优化", "if(!a)a++", getResult("if(a);else a++;"));
    Assert.assertEquals("if语句优化", "if(!(a<10))a++",
        getResult("if(a<10);else a++;"));
    Assert.assertEquals("if语句优化", "if(a<10)a++;else b++",
        getResult("if(a<10){a++;}else{b++;}"));
    Assert.assertEquals("if语句优化", "a++", getResult("if(true){a++;}else{b++;}"));
    Assert
        .assertEquals("if语句优化", "b++", getResult("if(false){a++;}else{b++;}"));

    Assert.assertEquals("do while语句", "do a++;while(a<10)",
        getResult("do{a++}while(a<10)"));
    Assert.assertEquals("do while语句", "do{a++;b++}while(a<10)",
        getResult("do{a++;b++}while(a<10)"));
    Assert.assertEquals("while语句", "while(a<10)a++",
        getResult("while(a<10){a++;}"));
    Assert.assertEquals("while语句", "while(a<10){a++;b++}",
        getResult("while(a<10){a++;b++}"));
    Assert.assertEquals("for语句", "for(;;)a++", getResult("for(;;)a++"));
    Assert.assertEquals("for语句", "for(a;;)a++", getResult("for(a;;)a++"));
    Assert.assertEquals("for语句", "for(;a;)a++", getResult("for(;a;)a++"));
    Assert.assertEquals("for语句", "for(;;a)a++", getResult("for(;;a)a++"));
    Assert.assertEquals("for语句", "for(a;a;)a++", getResult("for(a;a;)a++"));
    Assert.assertEquals("for语句", "for(a;;a)a++", getResult("for(a;;a)a++"));
    Assert.assertEquals("for语句", "for(;a;a)a++", getResult("for(;a;a)a++"));
    Assert.assertEquals("for语句", "for(a;a;a)a++", getResult("for(a;a;a)a++"));
    Assert.assertEquals("for语句", "for(var a;a;a)a++",
        getResult("for(var a;a;a)a++"));
    Assert.assertEquals("for语句", "for(a in b)a()", getResult("for(a in b)a()"));
    Assert.assertEquals("for语句", "for(var a in b)a()",
        getResult("for(var a in b)a()"));
    Assert.assertEquals("do while语句优化", "for(;;)a++",
        getResult("do{a++}while(true);"));
    Assert.assertEquals("while语句优化", "for(;;)a++", getResult("while(true)a++"));
    Assert.assertEquals("for语句优化", "for(;;)a++", getResult("for(;true;)a++"));

    Assert.assertEquals("continue语句", "for(;;)continue",
        getResult("for(;;)continue"));
    Assert.assertEquals("break语句", "for(;;)break", getResult("for(;;)break"));

    Assert.assertEquals("with语句", "with(a)a++", getResult("with(a)a++"));
    Assert.assertEquals("with语句", "with(a)a++", getResult("with(a){a++}"));
    Assert
        .assertEquals("with语句", "with(a)a++;b++", getResult("with(a)a++;b++"));
    Assert.assertEquals("with语句", "with(a){a++;b++}",
        getResult("with(a){a++;b++}"));

    Assert.assertEquals("switch语句", "switch(a){case 1:a++}",
        getResult("switch(a){case 1:a++}"));
    Assert.assertEquals("switch语句", "switch(a){case 1:a++;b++}",
        getResult("switch(a){case 1:a++;b++}"));
    Assert.assertEquals("switch语句", "switch(a){case 1:case 2:a++}",
        getResult("switch(a){case 1:case 2:a++}"));
    Assert.assertEquals("switch语句", "switch(a){case 1:case 2:a++;case 3:b++}",
        getResult("switch(a){case 1:case 2:a++;case 3:b++}"));
    Assert.assertEquals("switch语句", "switch(a){default:a++}",
        getResult("switch(a){default:a++}"));
    Assert
        .assertEquals(
            "switch语句",
            "switch(a){case 1:case 2:a++;break;case 3:a++;b++;default:c++}",
            getResult("switch(a){case 1:case 2:a++;break;case 3:a++;b++;default:c++}"));
    Assert.assertEquals("switch语句优化",
        "switch(a){case 1:case 2:a++;break;case 3:b++}",
        getResult("switch(a){case 1:case 2:a++;break;case 3:b++;break;}"));

    Assert.assertEquals("标签语句", "a:for(;;)for(;;)continue a",
        getResult("a:for(;;)for(;;)continue a"));
    Assert.assertEquals("标签语句", "a:for(;;)for(;;)break a",
        getResult("a:for(;;)for(;;)break a"));
    Assert.assertEquals("多个标签",
        "b:for(;;){a:for(;;){break a;continue a}continue b}",
        getResult("a:for(;;){b:for(;;){break b;continue b;}continue a;}"));
    Assert.assertEquals("标签语句优化", "for(;;)for(;;)break",
        getResult("a:for(;;)b:for(;;)break"));

    Assert.assertEquals("throw语句", "throw 1", getResult("throw 1"));
    Assert.assertEquals("throw语句", "throw Err()", getResult("throw Err()"));

    Assert.assertEquals("try语句", "try{a}catch(e){}",
        getResult("try{a}catch(e){}"));
    Assert.assertEquals("try语句", "try{a}finally{a}",
        getResult("try{a}finally{a}"));
    Assert.assertEquals("try语句", "try{a}catch(b){}finally{a}",
        getResult("try{a}catch(b){}finally{a}"));
    Assert.assertEquals("try语句", "try{a;b}catch(e){a;b}finally{a;b}",
        getResult("try{a;b}catch(e){a;b}finally{a;b}"));
    Assert.assertEquals("try语句优化", "a", getResult("try{a}finally{}"));
    Assert.assertEquals("try语句优化", "try{a}catch(e){}",
        getResult("try{a}catch(e){}finally{}"));

    Assert.assertEquals("function表达式", "(function(){var b;a++;c++})()",
        getResult("(function (){var b;a++;c++})()"));
    Assert.assertEquals("function表达式", "f=function(){var b;a++;c++}",
        getResult("f=function (){var b;a++;c++}"));
    Assert.assertEquals("function语句", "function f(a){var b;a++;c++}",
        getResult("function f(a){var b;a++;c++}"));
    Assert.assertEquals("function语句", "function f(a){var b;a++;c++;return c}",
        getResult("function f(a){var b;a++;c++;return c}"));
    Assert.assertEquals("function语句",
        "contain=dom.contain=function(a){a++;b++}",
        getResult("contain=dom.contain=function(a){a++;b++;}"));
    Assert.assertEquals("function语句",
        "contain=dom.contain=function f(a){a++;b++}",
        getResult("contain=dom['contain']=function f(a){a++;b++;}"));
    Assert.assertEquals("function语句",
        "var contain=dom.contain=function f(a){a++;b++}",
        getResult("var contain=dom.contain=function f(a){a++;b++;}"));
    Assert.assertEquals("function语句优化", "function f(a){var b;a++;c++}",
        getResult("function f(a){var d;a++;c++;return}"));

    Assert.assertEquals("非ascii字符转义", "\"\\u1000\\u0100\"",
        getResult("'\u1000\u0100'"));

    Assert.assertEquals("默认数值转义", "+a", getResult("a-0"));
    Assert.assertEquals("字符串合并", "a+\"a\"+\"b\"", getResult("a+'a'+'b'"));
  }

  private String getResult(String data) throws Exception {
    Writer writer = new StringWriter();
    new JSCompressor().compress(new StringReader(data), writer, false,
        JSCompressor.SEMANTICS);
    return writer.toString();
  }
}