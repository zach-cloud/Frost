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
    }

    public static void main(String[] args) {
        new CLI().run();
    }
}
