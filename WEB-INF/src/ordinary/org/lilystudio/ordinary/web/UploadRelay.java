package org.lilystudio.ordinary.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * 用于文件上传下载时处理的数据对象, 请在客户端使用以下形式的form标签,
 * 需要包含apache的fileupload包<br>
 * 
 * <pre>
 * &lt;form enctype=&quot;multipart/form-data&quot; ...&gt;
 * ...
 * &lt;/form&gt;
 * </pre>
 * 
 * @version 0.1.4, 2008/12/12
 * @author 欧阳先伟
 * @since Ordinary 0.1
 */
public class UploadRelay extends DefaultRelay {

  @Override
  public void init(HttpServletRequest request, HttpServletResponse response) {
    DiskFileItemFactory factory = new DiskFileItemFactory();
    factory.setSizeThreshold(5 * 1024 * 1024);
    ServletFileUpload upload = new ServletFileUpload(factory);
    // 设置单个文件最大20M
    upload.setSizeMax(20 * 1024 * 1024);
    try {
      String encoding = request.getCharacterEncoding();
      for (Object fileItem : upload.parseRequest(request)) {
        FileItem item = (FileItem) fileItem;
        if (item.getContentType() != null) {
          // 向数据键值名后附加@号保存原始的文件名, 附加%号保存文件的数据
          String name = item.getName();
          set(item.getFieldName() + "@", name);
          int pos = name.lastIndexOf('/');
          if (pos < 0) {
            pos = name.lastIndexOf('\\');
          }
          if (pos >= 0) {
            name = name.substring(pos + 1);
          }
          set(item.getFieldName(), name);
          set(item.getFieldName() + "%", item.get());
        } else {
          set(item.getFieldName(), item.getString(encoding));
        }
      }
    } catch (Exception e) {
    }
    // 初始化Session信息
    initUserInformation(request, response);
  }
}