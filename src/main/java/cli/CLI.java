package cli;

import mpq.Mpq;
import settings.MpqSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CLI {

    private String LISTFILE_PATH = "listfile.txt";

    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter filename: ");
        String inFile = scanner.nextLine();
        if(!inFile.contains(".")) {
            inFile = inFile + ".w3x";
        }
        MpqSettings settings = new MpqSettings(MpqSettings.LogSettings.DEBUG);

        Mpq mpq = new Mpq(inFile, settings);
        System.out.print("Enter action type (extract/list/extractAll)");

        String actionType = scanner.nextLine();
        executeAction(scanner, mpq, actionType);
    }

    private void executeAction(Scanner scanner, Mpq mpq, String actionType) {
        if(actionType.equalsIgnoreCase("extract")) {
            extract(scanner, mpq);
        } else if(actionType.equalsIgnoreCase("list")) {
            list(scanner, mpq);
        } else if(actionType.equalsIgnoreCase("extractAll")) {
            extractAll(scanner, mpq);
        }
    }

    private void extractAll(Scanner scanner, Mpq mpq) {
        File listfilePath = getListfile(scanner);
        mpq.extractAll(listfilePath);
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
        List<String> entries = mpq.listFiles(listfilePath);
        System.out.println("-------");
        System.out.println("Files:");
        for(String entry : entries) {
            System.out.println(entry);
        }
        System.out.println("-------");
    }

    private File getListfile(Scanner scanner) {
        File listfilePath = new File(LISTFILE_PATH);
        while(!listfilePath.exists()) {
            System.out.print("Enter listfile path: ");
            listfilePath = new File(scanner.nextLine());
        }
        return listfilePath;
    }

    public static void main(String[] args) {
        new CLI().run();
    }
}
