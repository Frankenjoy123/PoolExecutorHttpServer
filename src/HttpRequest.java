/**
 * Created by frank on 2017/8/20.
 */
public class HttpRequest {
    //请求方法 get post
    String method;
    //请求类型
    String contentType;
    //请求体长度
    String requestLength;
    //分隔符
    String boundary;
    //请求体字节数组
    byte[] body;
    //整个http 请求报文字节数组
    byte[] req;
    //将请求体转化的字符串
    String bodyStr;

    public HttpRequest(byte[] bytes) {
        this.req=bytes;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRequestLength() {
        return requestLength;
    }

    public void setRequestLength(String requestLength) {
        this.requestLength = requestLength;
    }

    public String getBoundary() {
        return boundary;
    }

    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getReq() {
        return req;
    }

    public void setReq(byte[] req) {
        this.req = req;
    }

    public String getBodyStr() {
        return bodyStr;
    }

    public void setBodyStr(String bodyStr) {
        this.bodyStr = bodyStr;
    }
}
