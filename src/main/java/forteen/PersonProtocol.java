package forteen;


public class PersonProtocol {
    private int length;//附加在消息前的长度值
    private byte[] content;//数据

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
