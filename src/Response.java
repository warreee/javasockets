import java.util.Arrays;

public class Response {

    /**
     * Byte array that is built in this response
     */
    private byte[] byteArray;

    public Response() {
        this.byteArray = new byte[0];
    }

    /**
     * Returns the byte array of the response
     */
    public byte[] getByteArray() {
        return this.byteArray;
    }

    /**
     * Add a string to the response
     */
    public void addString(String string) {
        byte[] bytes = string.getBytes();
        byteArray = appendToByteArray(byteArray, bytes);
    }

    /**
     * Add bytes to the response.
     */
    public void addBytes(byte[] bytes) {
        byteArray = appendToByteArray(byteArray, bytes);
    }



    private static byte[] appendToByteArray(byte[] bytes, byte[] addBytes) {
        byte[] result;
        int n = bytes.length;
        int m = addBytes.length;
        result = Arrays.copyOf(bytes, n+m);
        for (int i=0; i < m; i++)
            result[n+i] = addBytes[i];
        return result;
    }

}
