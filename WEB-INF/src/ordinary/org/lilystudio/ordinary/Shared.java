package org.lilystudio.ordinary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定对象的加载方式, 如果类使用了这个Tag描述类, 对象初次在对象池中加载时,
 * 将默认定义为共享模式, 即每次从类管理器中取对象都是同一个对象, 共享模式下,
 * 操作对象的属性需要考虑多线程的互斥.
 * 例如下例是指定AAA类默认使用共享模式加载进类管理器池中:
 * 
 * <pre>
 * \@Share
 * public class AAA {
 *   ...
 * }
 * </pre>
 * 
 * @version 0.1.3, 2008/09/01
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Shared {
}
