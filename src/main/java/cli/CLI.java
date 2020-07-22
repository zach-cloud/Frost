package cli;

import mpq.Mpq;

import java.util.Scanner;

public class CLI {

    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter filename: ");
        String inFile = scanner.nextLine();
        if(!inFile.endsWith(".w3x")) {
            inFile = inFile + ".w3x";
        }
        Mpq mpq = new Mpq(inFile);
        System.out.println("Using extract action type...");
        System.out.print("Enter filename to extract: ");
        String fileName = scanner.nextLine();
        if(mpq.fileExists(fileName)) {
            mpq.extractFile(fileName);
        } else {
            System.out.println("File does not exist.");
        }
    }

    public static void main(String[] args) {
        new CLI().run();
    }
}
