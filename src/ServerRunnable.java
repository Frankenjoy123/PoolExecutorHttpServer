import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import bsh.Interpreter;

/**
 * Created by frank on 2017/8/20.
 */
public class ServerRunnable implements Runnable {
    static String nextLine = System.lineSeparator();
    Socket socket;
    public ServerRunnable(Socket socket) {
        this.socket = socket;
    }

    public static String getMothed(String request) {
        int i = request.indexOf(" ");
        return request.substring(0, i);
    }

    public static HttpRequest post(byte[] buffer, String request) {
        int head = request.indexOf(nextLine + nextLine);
        HttpRequest httpRequest = new HttpRequest(Arrays.copyOfRange(buffer, head + (nextLine +
                nextLine).length(), buffer.length));
        httpRequest.setReq(buffer);
        httpRequest.setBodyStr(request.substring(head + (nextLine + nextLine).length()));
        String header = request.substring(0, head);
        int h = header.indexOf(nextLine);
        String headerbody = header.substring(h + nextLine.length(), header.length());
        String[] ll = headerbody.split(nextLine);
        for (String line : ll) {
            if (line.contains("Content-Length")) {
                httpRequest.setRequestLength(line.replace("Content-Length", "").replace(":", "").trim());
            }
            if (line.contains("Content-Type")) {
                String ct = line.split(";")[0];
                String bo = line.split(";")[1];
                httpRequest.setContentType(ct.replace("Content-Type:", "").trim());
                httpRequest.setBoundary(bo.replace("boundary=", "").trim());
            }
        }
        return httpRequest;
    }

    public static String getFileIndex(String textBody, String boundary) throws Exception {
        int pos = textBody.indexOf("Content-Type");
        pos = textBody.indexOf("\n", pos) + 1;
        pos = textBody.indexOf("\n", pos) + 1;
        int boundaryBegin = textBody.indexOf(boundary, pos) - 4;
        int begin = ((textBody.substring(0, pos)).getBytes("ISO-8859-1")).length;
        int end = ((textBody.substring(0, boundaryBegin)).getBytes("ISO-8859-1")).length;
        return begin + ":" + end;
    }

    public static void copyToFile(byte[] input, int s, int e, String filename) {
        File file = new File("E:\\" + filename);
        DataOutputStream d = null;
        try {
            d = new DataOutputStream(new FileOutputStream(file));
            d.write(input, s, e - s);
            d.flush();
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (d != null) {
                    d.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static String getFileName(String body) {
        String fileName = body.substring(body.indexOf("filename=\"") + 10);
        fileName = fileName.substring(0, fileName.indexOf("\n"));
        fileName = fileName.substring(fileName.indexOf("\n") + 1, fileName.indexOf("\""));
        return fileName;
    }
    public static String getReqPath(String request) {
        String[] params = request.split(" ");
        return params[1].substring(1);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        InputStream in = null;
        OutputStream out;
        ByteArrayOutputStream outStream = null;
        try {
            in = new BufferedInputStream(socket.getInputStream());
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;

            int contentLength;
            while (true ){
                contentLength = in.available();
                if (contentLength ==0){
                    break;
                }
                len = in.read(buffer,0,1024>contentLength?contentLength:1024);
                outStream.write(buffer, 0, len);
                if (len ==-1){
                    break;
                }
            }
            outStream.flush();
//将客户端输入流转化为字节数组
            buffer = outStream.toByteArray();
//取得所有Body 内容的字符串表示
            String request = new String(buffer, "ISO-8859-1");//如果不加字符编码，是使用UTF-8 编码
//获取请求方法 GET/POST
            String reqMothed = getMothed(request);
//根据请求行，获取请求路径
            String reqPath = getReqPath(request);
            if (reqMothed.equals("POST")) {
                HttpRequest h = post(buffer, request);
                String ss = null;
                try {
//获取上传文件在整个客户端发来的数据中的起始位置
                    ss = getFileIndex(h.getBodyStr(), h.getBoundary());
//获取上传文件名称
                    String name = getFileName(h.getBodyStr());
//将客户端上传的文件保存到本地中
                    copyToFile(h.getBody(), Integer.parseInt(ss.split(":")[0]),
                            Integer.parseInt(ss.split(":")[1]), name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (reqMothed.equals("GET")) {
//get 方法处理逻辑，并返回应答数据
                String resp = get(reqPath);
                out = new BufferedOutputStream(socket.getOutputStream());
//生成http 应答头
                String respheader = getResqHeader(resp);
                out.write(respheader.getBytes("ISO-8859-1"));
                out.write(resp.getBytes("ISO-8859-1"));
                out.flush();
            } else {
                socket.getOutputStream().write("method error".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (socket != null)
                    socket.close();
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        }

        /**
         * 脚本处理方法
         * @param filename 脚本文件名称
         * @param user 脚本参数 用户名称
         * @param password 脚本参数 用户密码
         * @return 脚本执行结果
         */
    public static String doShell(String filename,String user,String password){
        Interpreter interpreter = null;
        try {
            interpreter = new Interpreter();
            interpreter.set("username",user);
            interpreter.set("password",password);
            interpreter.source("D:\\"+filename);
            String r = interpreter.get("flag").toString();
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    /** @param path 请求路径
    * @return 返回脚本执行结果
    */
    public static String get(String path) {
        int index = path.indexOf("?");
        String fileName = path.substring(0,index);
        String paramStr = path.substring(index+1);
        String[] param = paramStr.split("&");
        String user = "";
        String password = "";
        for (String p :param) {
            if(p.contains("username")){
                user = p.split("=")[1];
            }
            if(p.contains("password")){
                password = p.split("=")[1];
            }
        }
        return doShell(fileName,user,password);
    }

    /**
     * 根据返回实体返回应答头
     * @param resqBody 应答结果
     * @return http 应答头
     */
    public static String getResqHeader(String resqBody){
        return "HTTP/1.1 200 OK\r\n"
                + "Server: OneFile 2.0\r\n"
                + "Content-length: " + resqBody.length() + "\r\n"
                + "Content-type: " + "text/html" + "; charset=" + "ISO-8859-1" + "\r\n\r\n";
    }


}
