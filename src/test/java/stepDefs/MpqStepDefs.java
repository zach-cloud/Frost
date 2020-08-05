package stepDefs;

import frost.FrostMpq;
import interfaces.IFrostMpq;
import io.FileWriter;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.mockito.Mockito;
import settings.MpqContext;
import settings.MpqSettings;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.mockito.Matchers.any;

public class MpqStepDefs {

    private File mpqFile;
    private IFrostMpq mpq;
    private MpqContext context;
    private FileWriter mockFileWriter;
    private Set<String> fileNames;

    @Given("MPQ file: {string}")
    public void mpq_file(String filePath) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        mpqFile = new File(url.getPath());
    }

    @When("MPQ file is read")
    public void mpq_file_is_read() {
        this.context = new MpqContext();
        this.context.setSettings(new MpqSettings(MpqSettings.LogSettings.NONE,
                MpqSettings.MpqOpenSettings.ANY));
        this.mpq = new FrostMpq(mpqFile, context);
    }

    private void makeMockFileWriter() {
        this.mockFileWriter = Mockito.mock(FileWriter.class);
        this.context.setFileWriter(mockFileWriter);
    }

    @When("File is extracted: {string}")
    public void file_is_extracted(String fileName) {
        try {
            makeMockFileWriter();
            mpq.extractFile(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @When("All known files are extracted")
    public void all_known_files_are_extracted() {
        makeMockFileWriter();
        mpq.extractAllKnown();
    }

    @When("All known files are extracted with listfile {string}")
    public void all_known_files_are_extracted_with_listfile(String listfilePath) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(listfilePath);
        File listfile = new File(url.getPath());
        makeMockFileWriter();
        mpq.extractAllKnown(listfile);
    }

    @When("File names are retrieved")
    public void file_names_are_retrieved() {
        this.fileNames = mpq.getFileNames();
    }

    @Then("MPQ should have {int} total files")
    public void mpq_should_have_total_files(int count) {
        Assert.assertEquals(count, mpq.getFileCount());
    }

    @Then("MPQ should have {int} known files")
    public void mpq_should_have_known_files(int count) {
        Assert.assertEquals(count, mpq.getKnownFileCount());
    }

    @Then("MPQ should have {int} unknown files")
    public void mpq_should_have_unknown_files(int count) {
        Assert.assertEquals(count, mpq.getUnknownFileCount());
    }

    @Then("File should exist: {string}")
    public void file_should_exist(String fileName) {
        Assert.assertTrue(mpq.fileExists(fileName));
    }

    @Then("File should have been extracted: {string}")
    public void file_should_have_been_extracted(String fileName) throws Exception {
        Mockito.verify(mockFileWriter, Mockito.times(1)).write(any(), any());

    }

    @Then("{int} files should have been extracted")
    public void files_should_have_been_extracted(int count) throws Exception {
        Mockito.verify(mockFileWriter, Mockito.times(count)).write(any(), any());
    }

    @Then("There should be {int} file names")
    public void there_should_be_file_names(int count) {
        Assert.assertEquals(count, fileNames.size());
    }

}
