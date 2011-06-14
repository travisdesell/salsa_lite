package salsa_lite.compiler.definitions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;


public class CompilerErrors {

    public static File currentFile;
    public static ArrayList<String> lines;

    public static void initialize(String filename) {
        currentFile = new File(filename);

        try {
            BufferedReader br = new BufferedReader(new FileReader(currentFile));

            lines = new ArrayList<String>();

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            br.close();
        } catch (Exception e) {
            System.out.println("ERROR: problem reading file [" + filename + "] for compiler errors.");
            e.printStackTrace();
        }
    }

    public static String getLine(int beginLine) {
        return lines.get(beginLine - 1);
    }

    public static String getWhitespace(int beginColumn) {
        StringBuffer sb = new StringBuffer(beginColumn);

        for (int i = 0; i < beginColumn - 1; i++) sb.append(' ');

        return sb.toString();
    }


    public static void printErrorMessage(String message, CErrorInformation ei) {
        System.out.println("COMPILER ERROR: " + message + " [" + currentFile.getName() + "], line [" + ei.beginLine + "], column [" + ei.beginColumn + "]:");
        System.out.println(getLine(ei.beginLine));
        System.out.println(getWhitespace(ei.beginColumn) + "^");
        System.out.println();
    }
}
