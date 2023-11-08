package Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

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
        StringBuilder currentRecord = new StringBuilder();
        boolean inRecord = false;

        // Use a StringBuilder to accumulate the CSV lines
        StringBuilder csvOutput = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("<EOR>")) {
                inRecord = false;
                processRecord(currentRecord.toString(), fields, csvOutput);
                currentRecord.setLength(0);
            } else if (line.startsWith("<EOH>")) {
                continue;
            } else if (inRecord) {
                currentRecord.append(line);
            } else if (line.startsWith("<") && line.contains(":")) {
                inRecord = true;
                currentRecord.append(line);
            }
        }

        // Print CSV header
        System.out.println("QSO_DATE,TIME_ON," + String.join(",", fields.keySet()));

        // Print the accumulated CSV output
        System.out.println(csvOutput.toString());
    }

    private static void processRecord(String recordData, Map<String, Integer> fields, StringBuilder csvOutput) {
        String[] lines = recordData.split("\\s+");
        Map<String, String> recordMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Pattern pattern = Pattern.compile("<([A-Z_]+):(\\d+)>([^<]+)");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String fieldName = matcher.group(1);
                int fieldLength = Integer.parseInt(matcher.group(2));
                String fieldValue = matcher.group(3);
                recordMap.put(fieldName, fieldValue.substring(0, fieldLength));
                fields.put(fieldName, 1);
            }
        }

        // Fill in missing fields with empty strings
        for (String fieldName : fields.keySet()) {
            if (!recordMap.containsKey(fieldName)) {
                recordMap.put(fieldName, "");
            }
        }

        // Prepare the record as a CSV line
        StringBuilder csvLine = new StringBuilder();
        csvLine.append(recordMap.get("QSO_DATE")).append(",").append(recordMap.get("TIME_ON"));
        for (String fieldName : fields.keySet()) {
            if (!fieldName.equalsIgnoreCase("QSO_DATE") && !fieldName.equalsIgnoreCase("TIME_ON")) {
                csvLine.append(",").append(recordMap.get(fieldName));
            }
        }
        csvLine.append("\n");

        // Append the CSV line to the output
        csvOutput.append(csvLine.toString());
    }
}

