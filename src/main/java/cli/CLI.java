package cli;

import mpq.Mpq;
import org.apache.commons.io.FileUtils;
import settings.GlobalSettings;
import settings.MpqSettings;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Set;

public class CLI {

    private String LISTFILE_PATH = "listfile.txt";

    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("------------------");
        System.out.println("Frost v " + GlobalSettings.VERSION);
        System.out.println();
        System.out.println(GlobalSettings.GITHUB_LINK);
        System.out.println("------------------");
        System.out.print("Enter filename: ");
        String inFile = scanner.nextLine();
        if (!inFile.contains(".")) {
            inFile = inFile + ".w3x";
        }
        MpqSettings settings = new MpqSettings(MpqSettings.LogSettings.DEBUG, MpqSettings.MpqOpenSettings.CRITICAL);

        Mpq mpq = new Mpq(inFile, settings);
        System.out.print("Enter action type (extract/list/extractAllKnown/count/countKnown/save/quit/import): ");

        String actionType = scanner.nextLine();
        executeAction(scanner, mpq, actionType);
    }

    private void executeAction(Scanner scanner, Mpq mpq, String actionType) {
        while(true) {
            if (actionType.equalsIgnoreCase("extract")) {
                extract(scanner, mpq);
            } else if (actionType.equalsIgnoreCase("list")) {
                list(scanner, mpq);
            } else if (actionType.equalsIgnoreCase("extractAllKnown")) {
                extractAllKnown(scanner, mpq);
            } else if (actionType.equalsIgnoreCase("count")) {
                System.out.println("Total files: " + mpq.getFileCount());
            } else if (actionType.equalsIgnoreCase("countKnown")) {
                System.out.println("Known files: " + mpq.getFileCount());
            } else if (actionType.equalsIgnoreCase("save")) {
                mpq.save(new File("saved.w3x"));
                System.out.println("File saved successfully");
            } else if(actionType.equalsIgnoreCase("import")) {
                runImport(scanner, mpq);
            } else if (actionType.equalsIgnoreCase("quit")) {
                System.exit(0);
            }
        }
    }

    private void runImport(Scanner scanner, Mpq mpq) {
        try {
            File inputFile = new File("");
            do {
                System.out.print("Enter file to import: ");
                inputFile = new File(scanner.nextLine());
            } while (!inputFile.exists());
            mpq.importFile(inputFile.getName(), FileUtils.readFileToByteArray(inputFile));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void extractAllKnown(Scanner scanner, Mpq mpq) {
        File listfilePath = getListfile(scanner);
        if(listfilePath.exists()) {
            mpq.extractAllKnown(listfilePath);
        } else {
            mpq.extractAllKnown();
        }
    }

    private void extract(Scanner scanner, Mpq mpq) {
        System.out.print("Enter filename to extract: ");
        String fileName = scanner.nextLine();
        if (mpq.fileExists(fileName)) {
            mpq.extractFile(fileName);
        } else {
            System.out.println("File does not exist.");
        }
    }

    private void list(Scanner scanner, Mpq mpq) {
        File listfilePath = getListfile(scanner);
        if(listfilePath.exists()) {
            mpq.addExternalListfile(listfilePath);
        }
        Set<String> entries = mpq.getFileNames();
        System.out.println("-------");
        System.out.println("Files:");
        for (String entry : entries) {
            System.out.println(entry);
        }
        System.out.println("-------");
    }

    private File getListfile(Scanner scanner) {
        File listfilePath = new File(LISTFILE_PATH);
        if (!listfilePath.exists()) {
            System.out.print("Enter listfile path: ");
            listfilePath = new File(scanner.nextLine());
        }
        return listfilePath;
    }

    public static void main(String[] args) {
        new CLI().run();
    }
}
