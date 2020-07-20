import encryption.StormCrypt;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StormCryptStepDefs {

    private StormCrypt stormCrypt;
    private byte[] input;
    private ByteBuffer inputByteBuffer;
    private byte[] result;
    private ByteBuffer resultByteBuffer;

    private static String bytesToString(byte[] ar) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < ar.length; i++) {
            builder.append(ar[i]).append(",");
        }
        builder.setLength(builder.length()-1);
        return builder.toString();
    }

    private static byte[] stringToBytes(String s) {
        String[] split = s.split(",");
        byte[] ar = new byte[split.length];
        for(int i = 0; i < split.length; i++) {
            ar[i] = (byte)Integer.parseInt(split[i]);
        }
        return ar;
    }

    @Given("bytes:")
    public void bytes(String bytesBody) {
        input = stringToBytes(bytesBody);
        inputByteBuffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN);
    }

    @When("bytes are decrypted with key {int}")
    public void bytes_are_decrypted_with_key(int key) {
        this.stormCrypt = new StormCrypt();
        result = stormCrypt.decryptBytes(input, key);
        resultByteBuffer = stormCrypt.decryptBuffer(inputByteBuffer, key);
    }

    @When("bytes are encrypted with key {int}")
    public void bytes_are_encrypted_with_key(int key) {
        this.stormCrypt = new StormCrypt();
        result = stormCrypt.encryptBytes(input, key);

    }

    @Then("result bytes should be:")
    public void result_bytes_should_be(String bytesBody) {
        Assert.assertEquals("Result bytes did not match expected body",
                bytesBody, bytesToString(result));
        Assert.assertEquals("Result byte buffer did not match expected body",
                bytesBody, bytesToString(resultByteBuffer.array()));
    }
}
