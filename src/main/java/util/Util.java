package util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


/**
 * Class to collect different technical methods
 */
public class Util {

    public static void writeFile(String filePath, String content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] strToBytes = content.getBytes();
            outputStream.write(strToBytes);
        }

    }

    public static String getStringFromClipboard() throws IOException, UnsupportedFlavorException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        return (String) clipboard.getData(DataFlavor.stringFlavor);
    }

    public static HashMap<String, String> convertToHash(String... key_value) {
        HashMap<String, String> keys = new HashMap<>();
        if (key_value.length > 0) {
            for (int i = 0; i < key_value.length - 1; i += 2) {
                keys.put(key_value[i], key_value[i + 1]);
            }
        }
        return keys;
    }

    public static boolean isDuplicateExistInArray(ArrayList<String> input) {
        Set<String> inputSet = new HashSet<>(input);
        return inputSet.size() < input.size();
    }

    /**
     * Normilize any phone number value to a simple format (e.g. +1201333444)
     */
    public static String normilizePhone(String initial) {
        StringBuilder out = new StringBuilder();
        out.append("+");
        for (char i :
                initial.toCharArray()) {
            if (i >= '0' && i <= '9') {
                out.append(i);
            }
        }
        return out.toString();
    }

    /**
     * @param v        - initial value;
     * @param currency - currency for the value
     * @return value with currency and decimal format. for example: $52.00. The format has been taken from Purchases report.
     */
    public static String toMoney(int v, Currency currency) {
        return currency.getSymbol() + v + ".00";
    }

}