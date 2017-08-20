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
//���ͻ���������ת��Ϊ�ֽ�����
            buffer = outStream.toByteArray();
//ȡ������Body ���ݵ��ַ�����ʾ
            String request = new String(buffer, "ISO-8859-1");//��������ַ����룬��ʹ��UTF-8 ����
//��ȡ���󷽷� GET/POST
            String reqMothed = getMothed(request);
//���������У���ȡ����·��
            String reqPath = getReqPath(request);
            if (reqMothed.equals("POST")) {
                HttpRequest h = post(buffer, request);
                String ss = null;
                try {
//��ȡ�ϴ��ļ��������ͻ��˷����������е���ʼλ��
                    ss = getFileIndex(h.getBodyStr(), h.getBoundary());
//��ȡ�ϴ��ļ�����
                    String name = getFileName(h.getBodyStr());
//���ͻ����ϴ����ļ����浽������
                    copyToFile(h.getBody(), Integer.parseInt(ss.split(":")[0]),
                            Integer.parseInt(ss.split(":")[1]), name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (reqMothed.equals("GET")) {
//get ���������߼���������Ӧ������
                String resp = get(reqPath);
                out = new BufferedOutputStream(socket.getOutputStream());
//����http Ӧ��ͷ
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
         * �ű�������
         * @param filename �ű��ļ�����
         * @param user �ű����� �û�����
         * @param password �ű����� �û�����
         * @return �ű�ִ�н��
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


    /** @param path ����·��
    * @return ���ؽű�ִ�н��
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
     * ���ݷ���ʵ�巵��Ӧ��ͷ
     * @param resqBody Ӧ����
     * @return http Ӧ��ͷ
     */
    public static String getResqHeader(String resqBody){
        return "HTTP/1.1 200 OK\r\n"
                + "Server: OneFile 2.0\r\n"
                + "Content-length: " + resqBody.length() + "\r\n"
                + "Content-type: " + "text/html" + "; charset=" + "ISO-8859-1" + "\r\n\r\n";
    }


}
