package org.lilystudio.ordinary.module;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * 邮件模块, 配置到邮件服务器的连接, 提供接口用于发送邮件, 需要包含第三方的mail包
 * 
 * <b>属性</b>
 * 
 * <pre>
 * encoding--邮件碥码方式, 默认为UTF-8
 * server--邮件服务器地址
 * from--发信人地址, 将用于在发送的邮件中标注发信人的邮箱
 * password--邮件服务器登录验证密码
 * attachmentSize--单个附件的大小限制, 默认1M
 * mailSize--全部附件的大小限制, 默认5M
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class MailModule extends AbstractModule {

  /**
   * MailModule的附件超大异常
   */
  public static class AttachmentTooLargeException extends Exception {

    /** 序列化号码 */
    private static final long serialVersionUID = 1L;
  }

  /**
   * 附件信息类
   */
  private static class Attachment {

    /** 附件名称 */
    private String name;

    /** 附件内容 */
    private byte[] data;

    /**
     * 创建附件信息类
     * 
     * @param name
     *          附件名称
     * @param data
     *          附件数据
     */
    private Attachment(String name, byte[] data) {
      this.name = name;
      this.data = data;
    }
  }

  /** 编码方式 */
  private String encoding = "UTF-8";

  /** SMTP服务器地址, 如smtp.tom.com */
  private String server;

  /** 完整的邮件地址, 如ouyangxianwei@tom.com */
  private String from;

  /** 登录密码 */
  private String password;

  /** 登录用户名 */
  private String user;

  /** 单个附件的大小限制 */
  private int attachmentSize = 1024 * 1024;

  /** 全部邮件的大小限制 */
  private int mailSize = 5 * 1024 * 1024;

  /**
   * 初始化对象
   */
  public void init() {
    // 如果设置登录密码不为空, 则生成user的值, 默认下直接从完整的邮件地址中取
    if (password != null) {
      user = from.substring(0, from.indexOf('@'));
    }
  }

  /**
   * 发送邮件
   * 
   * @param subject
   *          邮件标题
   * @param to
   *          收信人地址
   * @param content
   *          邮件正文
   * @throws Exception
   *           发送异常
   */
  public void send(String subject, String to, String content) throws Exception {
    send(subject, to, null, content);
  }

  /**
   * 发送邮件
   * 
   * @param subject
   *          邮件标题
   * @param to
   *          收信人地址
   * @param content
   *          邮件正文
   * @throws Exception
   *           发送异常
   */
  public void send(String subject, String to, String[] cc, String content)
      throws Exception {
    send(subject, to, join(cc), content);
  }

  /**
   * 发送邮件
   * 
   * @param subject
   *          邮件标题
   * @param to
   *          收信人地址
   * @param content
   *          邮件正文
   * @throws Exception
   *           发送异常
   */
  public void send(String subject, String[] to, String[] cc, String content)
      throws Exception {
    send(subject, join(to), join(cc), content);
  }

  /**
   * 发送邮件
   * 
   * @param subject
   *          邮件标题
   * @param to
   *          收信人地址
   * @param cc
   *          抄送人地址
   * @param content
   *          邮件正文
   * @param fileNames
   *          附件名列表
   * @throws Exception
   *           发送异常
   */
  public void send(String subject, String[] to, String[] cc, String content,
      String... fileNames) throws Exception {
    if (fileNames != null) {
      int size = fileNames.length;
      Attachment[] attachments = new Attachment[size];
      for (int i = 0; i < size; i++) {
        File file = new File(fileNames[i]);

        // 不允许发送过大的附件
        long length = file.length();
        if (length > attachmentSize) {
          throw new AttachmentTooLargeException();
        }

        byte[] data = new byte[(int) length];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        try {
          in.readFully(data);
        } finally {
          in.close();
        }
        attachments[i] = new Attachment(file.getName(), data);
      }
      send(subject, join(to), join(cc), content, attachments);
    }
  }

  /**
   * 发送邮件
   * 
   * @param subject
   *          邮件标题
   * @param to
   *          收信人地址, 多个收信人使用,分隔
   * @param cc
   *          抄送人地址, 多个抄送人使用,分隔
   * @param content
   *          邮件正文
   * @param attachments
   *          附件列表
   * @throws Exception
   *           发送异常
   */
  private void send(String subject, String to, String cc, String content,
      Attachment... attachments) throws Exception {
    Properties props = new Properties();
    // 设置邮件服务器与帐号信息
    props.put("mail.smtp.host", server);
    if (password != null) {
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.user", user);
      props.put("mail.smtp.password", password);
    }

    // 创建邮件服务器连接会话对象
    Session session = Session.getDefaultInstance(props, null);

    // 以附件的方式发送内容
    // 创建邮件体
    MimeMessage message = new MimeMessage(session);
    // 设置收发信人, 邮件标题等
    message.setFrom(new InternetAddress(from));

    message.addRecipients(MimeMessage.RecipientType.TO, to);
    if (cc != null) {
      message.addRecipients(MimeMessage.RecipientType.CC, cc);
    }
    message.setSubject(subject, encoding);

    // 创建邮件内容对象
    MimeMultipart multiPart = new MimeMultipart();
    // 创建内容块对象, 正文和附件必须是不同的内容块
    MimeBodyPart bodyPart = new MimeBodyPart();
    bodyPart.setText(content, encoding);
    multiPart.addBodyPart(bodyPart);

    // 添加附件
    if (attachments != null) {
      for (Attachment attachment : attachments) {
        if (attachment.data.length > attachmentSize) {
          throw new AttachmentTooLargeException();
        }
        bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(
            attachment.data, encoding)));
        // 设置附件名称
        bodyPart.setFileName(attachment.name);
        multiPart.addBodyPart(bodyPart);
      }
    }

    message.setContent(multiPart);

    // 设置信件头的发送日期
    message.setSentDate(new Date());
    // 保存设置好的邮件
    message.saveChanges();

    if (message.getSize() > mailSize) {
      throw new AttachmentTooLargeException();
    }

    // 连接邮件服务器并发送邮件
    Transport transport = session.getTransport("smtp");
    transport.connect(server, user, password);
    transport.sendMessage(message, message.getAllRecipients());
    transport.close();
  }

  /**
   * 将字符串数组使用,符号连接成一个新的字符串, 如果为<tt>null</tt>返回
   * <tt>null</tt>
   * 
   * @param array
   *          字符串数组
   * @return 连接后的字符串
   */
  private String join(String[] array) {
    if (array == null) {
      return null;
    }
    StringBuilder s = new StringBuilder(64);
    int size = array.length;
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        s.append(',');
      }
      s.append(array[i]);
    }
    return s.toString();
  }
}