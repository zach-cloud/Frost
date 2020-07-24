package stepDefs;

import compression.DeflationCompressionI;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import static utils.ByteUtils.bytesToString;
import static utils.ByteUtils.stringToBytes;

public class DeflationCompressionStepDefs {

    private byte[] input;
    private byte[] results;
    private DeflationCompressionI compression;

    @Given("deflation bytes:")
    public void deflation_bytes(String body) {
        input = stringToBytes(body);
    }

    @When("data is inflated with size {int}")
    public void data_is_inflated_with_size(int desiredSize) {
        compression = new DeflationCompressionI();
        results = new byte[desiredSize];
        results = compression.undo(input, results);
    }

    @Then("inflated data should be:")
    public void inflated_data_should_be(String data) {
        Assert.assertEquals(data, bytesToString(results));
    }
}
