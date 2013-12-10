package org.lilystudio.test;

import static org.lilystudio.smarty4j.INode.*;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.Engine;
import org.lilystudio.smarty4j.INode;
import org.lilystudio.smarty4j.IParser;
import org.lilystudio.smarty4j.Template;
import org.lilystudio.smarty4j.TemplateWriter;
import org.lilystudio.smarty4j.expression.NullExpression;
import org.lilystudio.smarty4j.expression.ObjectExpression;
import org.lilystudio.smarty4j.expression.TranslateString;
import org.lilystudio.smarty4j.expression.check.TranslateCheck;
import org.lilystudio.smarty4j.expression.number.TranslateDouble;
import org.lilystudio.smarty4j.expression.number.TranslateInteger;
import org.lilystudio.smarty4j.statement.PrintStatement;
import org.lilystudio.util.DynamicClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * Smarty节点单元测试用例
 * 
 * @version 0.1.3, 2008/12/12
 * @author 欧阳先伟
 * @since Smarty 0.1
 */
public class SmartyNodeUnitTest {

  private static Context context = new Context();

  private static Engine engine = new Engine();

  private static Template template;

  @Before
  public void setUp() throws Exception {
    template = new Template(engine, "");
    Method m = Context.class.getDeclaredMethod("setTemplate", Template.class);
    m.setAccessible(true);
    m.invoke(context, template);
  }

  @Test
  public void testListExtended() throws Exception {
  }

  @Test
  public void testNullExpression() throws Exception {
    ObjectExpression node = new NullExpression();
    Assert.assertEquals(merge(new PrintStatement(new TranslateString(
        new TranslateCheck(node)))), "false");
    Assert.assertEquals(merge(new PrintStatement(new TranslateString(
        new TranslateInteger(node)))), "0");
    Assert.assertEquals(merge(new PrintStatement(new TranslateString(
        new TranslateDouble(node)))), "0.0");
    Assert.assertEquals(merge(new PrintStatement(new TranslateString(
        new TranslateString(node)))), "null");
    Assert.assertEquals(merge(new PrintStatement(node)), "null");
  }

  private String merge(INode node) throws Exception {
    try {
      merge(node, 1, context);
      Assert.fail();
    } catch (Throwable e) {
    }
    return merge(node, 0, context);
  }

  private String merge(INode node, int pop, Context context) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Writer out = new TemplateWriter(bos, "GBK");

    ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
    MethodVisitor mw;
    String name = "test";
    cw.visit(V1_5, ACC_PUBLIC, name, null, "java/lang/Object",
        new String[] { IParser.NAME });

    mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mw.visitVarInsn(ALOAD, THIS);
    mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();

    // 定义类的merge方法
    mw = cw.visitMethod(ACC_PUBLIC, "merge", "(L" + Context.NAME
        + ";Ljava/io/Writer;)V", null, null);
    mw.visitVarInsn(ALOAD, CONTEXT);
    mw.visitMethodInsn(INVOKEVIRTUAL, Context.NAME, "getTemplate", "()L"
        + Template.NAME + ";");
    mw.visitVarInsn(ASTORE, TEMPLATE);

    node.parse(mw, LOCAL_START, null);
    while (pop-- > 0) {
      mw.visitInsn(POP);
    }

    mw.visitInsn(RETURN);
    mw.visitMaxs(0, 0);
    mw.visitEnd();
    cw.visitEnd();
    byte[] code = cw.toByteArray();
    try {
      IParser parser = (IParser) DynamicClassLoader.getClass(name, code)
          .newInstance();
      parser.merge(context, out);
      out.flush();
      return new String(bos.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException("不能实例化Parser对象");
    }
  }
}