package org.lilystudio.test;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerTest {
  public static void main2(String[] args) throws Exception {
    ServerSocket sock = new ServerSocket(80);
    Socket s = sock.accept();
    byte[] bs = new byte[8192];
    System.out.println(new String(bs, 0, s.getInputStream().read(bs)));
    OutputStream out = s.getOutputStream();
    out.write("HTTP/1.1 400 Bad Request\nConnection: close\n123".getBytes());
    s.close();
  }

  public static void main(String[] args) throws Exception {
    Socket s = new Socket("127.0.0.1", 80);
    byte[] bs = new byte[8192];
    OutputStream out = s.getOutputStream();
    out
        .write("GET /framework/ HTTP/1.1\nAccept: application/x-shockwave-flash, */*\nAccept-Language: zh-cn\nUA-CPU: x86\nAccept-Encoding: gzip, deflate\nIf-Modified-Since: Tue, 07 Apr 2009 03:44:28 GMT\nIf-None-Match: W/\"958-1239075868109\"\nUser-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; (R1 1.3); .NET CLR 1.1.4322)\nHost: localhost\nConnection: Keep-Alive\n\n"
            .getBytes());
    System.out.println(new String(bs, 0, s.getInputStream().read(bs)));
    s.close();
  }
}
