/**
 * Created by frank on 2017/8/20.
 */
public class HttpRequest {
    //���󷽷� get post
    String method;
    //��������
    String contentType;
    //�����峤��
    String requestLength;
    //�ָ���
    String boundary;
    //�������ֽ�����
    byte[] body;
    //����http �������ֽ�����
    byte[] req;
    //��������ת�����ַ���
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
