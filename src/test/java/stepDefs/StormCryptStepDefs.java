package stepDefs;

import encryption.StormSecurity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static utils.ByteUtils.bytesToString;
import static utils.ByteUtils.stringToBytes;

public class StormCryptStepDefs {

    private StormSecurity stormSecurity;
    private byte[] input;
    private ByteBuffer inputByteBuffer;
    private byte[] result;
    private ByteBuffer resultByteBuffer;
    private String toHash;
    private int intHash;

    @Given("bytes:")
    public void bytes(String bytesBody) {
        input = stringToBytes(bytesBody);
        inputByteBuffer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN);
    }

    @When("bytes are decrypted with key {int}")
    public void bytes_are_decrypted_with_key(int key) {
        this.stormSecurity = new StormSecurity();
        result = stormSecurity.decryptBytes(input, key);
        resultByteBuffer = stormSecurity.decryptBuffer(inputByteBuffer, key);
    }

    @When("bytes are encrypted with key {int}")
    public void bytes_are_encrypted_with_key(int key) {
        this.stormSecurity = new StormSecurity();
        result = stormSecurity.encryptBytes(input, key);
        resultByteBuffer = stormSecurity.encryptBuffer(inputByteBuffer, key);
    }

    @Then("result bytes should be:")
    public void result_bytes_should_be(String bytesBody) {
        Assert.assertEquals("Result bytes did not match expected body",
                bytesBody, bytesToString(result));
        Assert.assertEquals("Result byte buffer did not match expected body",
                bytesBody, bytesToString(resultByteBuffer.array()));
    }


    @Given("string value {string}")
    public void string_value(String toHash) {
        this.toHash = toHash;
    }

    @When("integer hash is computed with type {int}")
    public void integer_hash_is_computed(int type) {
        this.stormSecurity = new StormSecurity();
        intHash = stormSecurity.hashAsInt(toHash, type);
    }

    @Then("hash should be {int}")
    public void hash_should_be(int expected) {
        Assert.assertEquals(expected, intHash);
    }
}
