package org.lilystudio.test;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.MethodVisitor;

/**
 * 调试Smarty解析器运行时的值, 在parse或相关函数内使用即可输出
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Smarty 0.1
 */
public class ASMPrinter {

  /**
   * 向控制台输出一个整数值
   * 
   * @param mw
   *          ASM方法访问对象
   * @param local
   *          ASM方法内部的语句栈局部变量起始位置
   */
  public static void printInt(MethodVisitor mw, int local) {
    mw.visitVarInsn(ISTORE, local);
    mw.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
        "Ljava/io/PrintStream;");
    mw.visitVarInsn(ILOAD, local);
    mw.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V");
    mw.visitVarInsn(ILOAD, local);
  }

  /**
   * 向控制台输出一个对象值
   * 
   * @param mw
   *          ASM方法访问对象
   * @param local
   *          ASM方法内部的语句栈局部变量起始位置
   */
  public static void printObject(MethodVisitor mw, int local) {
    mw.visitVarInsn(ASTORE, local);
    mw.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
        "Ljava/io/PrintStream;");
    mw.visitVarInsn(ALOAD, local);
    mw.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
        "(Ljava/lang/Object;)V");
    mw.visitVarInsn(ALOAD, local);
  }
}
