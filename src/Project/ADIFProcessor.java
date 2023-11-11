package Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADIFProcessor {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        Map<String, Integer> fields = new TreeMap<>();
        Map<String, Map<String, String>> records = new TreeMap<>();
        StringBuilder currentRecord = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.toUpperCase().contains("<EOR>")) {
                currentRecord.append(line, 0, line.toUpperCase().indexOf("<EOR>")).append("\n");
                processRecord(currentRecord.toString(), fields, records);
                currentRecord.setLength(0);
                currentRecord.append(line.substring(line.toUpperCase().indexOf("<EOR>") + 5)).append("\n");
            } else if (line.toUpperCase().contains("<EOH>")) {
                currentRecord.setLength(0);
                currentRecord.append(line.substring(line.toUpperCase().indexOf("<EOH>") + 5)).append("\n");
            } else {
                currentRecord.append(line).append("\n");
            }

        }

        // Sort fields alphabetically
        fields = new TreeMap<>(fields);

        // Print CSV header, when a key in fields has ",", we add "" to the key
        StringBuilder csvHeader = new StringBuilder();
        boolean firstField = true; // judge whether it is the first field
        for (String fieldName : fields.keySet()) {
            if (firstField) {
                if(fieldName.contains(","))
                    csvHeader.append("\"").append(fieldName).append("\"");
                else
                    csvHeader.append(fieldName);
                firstField = false;
            } else {
                if(fieldName.contains(","))
                    csvHeader.append(",").append("\"").append(fieldName).append("\"");
                else
                    csvHeader.append(",").append(fieldName);
            }
        }
        System.out.println(csvHeader.toString());

        // Print records in sorted order
        for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
            StringBuilder csvLine = new StringBuilder();
            firstField = true; // judge whether it is the first field

            for (String fieldName : fields.keySet()) {
                if (firstField) {
                    String fieldValue = entry.getValue().get(fieldName);
                    if (fieldValue!=null) {
                        if(fieldValue.contains(","))
                            csvLine.append("\"").append(fieldValue).append("\"");
                        else
                            csvLine.append(fieldValue);
                    }
                    firstField = false;
                } else {
                    String fieldValue = entry.getValue().get(fieldName);
                    if (fieldValue!=null) {
                        if(fieldValue.contains(","))
                            csvLine.append(",").append("\"").append(fieldValue).append("\"");
                        else
                            csvLine.append(",").append(fieldValue);
                    }
                    else {
                        csvLine.append(",");
                    }
                }
            }
            System.out.println(csvLine.toString());
        }
    }

    private static void processRecord(String recordData, Map<String, Integer> fields, Map<String, Map<String, String>> records) {
        String[] lines = recordData.split("\n");
        int index = 0;

        Map<String, String> recordMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Pattern pattern = Pattern.compile("<([^>\\u4E00-\\u9FA5]+):(\\d+)>");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            index = 0;
            while (index<line.length() && matcher.find(index)) {
                String fieldName = matcher.group(1).toUpperCase(); // Convert to uppercase
                int fieldLength = Integer.parseInt(matcher.group(2));
                String original_fieldValue = line.substring(matcher.end(), Math.min((matcher.end() + fieldLength), line.length()));
                String fieldValue = getFieldValue(original_fieldValue, fieldLength);
                index = matcher.end() + Math.min(fieldValue.length(), fieldLength);
                recordMap.put(fieldName, fieldValue.substring(0, Math.min(fieldValue.length(), fieldLength)));
                fields.put(fieldName, 1);
            }
        }

        // Fill in missing fields with empty strings
//        for (String fieldName : fields.keySet()) {
//            if (!recordMap.containsKey(fieldName)) {
//                recordMap.put(fieldName, "");
//            }
//        }

        // Store the record in the records map
        records.put(recordMap.get("QSO_DATE") + recordMap.get("TIME_ON"), recordMap);
    }

    private static String getFieldValue(String original, int fieldLength) {
        String fieldValue = "";
        while(fieldLength>0) {
            char c = original.charAt(0);
            if(isChineseCharacter(c)) {
                if(fieldLength==1) {
                    break;
                }
                fieldValue += c;
                fieldLength -= 2;
            } else {
                fieldValue += c;
                fieldLength -= 1;
            }
            original = original.substring(1);
        }
        return fieldValue;
    }

    private static boolean isChineseCharacter(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }

}
