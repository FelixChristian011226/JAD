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
        boolean inRecord = false;

        while ((line = reader.readLine()) != null) {
            currentRecord.append(line).append("\n");
            if (line.contains("<EOR>") || line.contains("<eor>")) {
                inRecord = false;
                processRecord(currentRecord.toString(), fields, records);
                currentRecord.setLength(0);
            } else if (line.contains("<EOH>") || line.contains("<eoh>")) {
                currentRecord.setLength(0);
                continue;
            } else if (line.startsWith("<") && line.contains(":")) {
                inRecord = true;
            }
        }
        if(currentRecord.length() > 0) {
            processRecord(currentRecord.toString(), fields, records);
        }

        // Sort fields alphabetically
        fields = new TreeMap<>(fields);

        // When a key in fields has ",", we add "" to the key
        for (String fieldName : fields.keySet()) {
            if (fieldName.contains(",")) {
                fields.put("\"" + fieldName + "\"", 1);
                fields.remove(fieldName);
            }
        }

        // Print CSV header
        System.out.println(String.join(",", fields.keySet()));

        // Print records in sorted order
        for (Map.Entry<String, Map<String, String>> entry : records.entrySet()) {
            StringBuilder csvLine = new StringBuilder();
            boolean firstField = true; // judge whether it is the first field

            for (String fieldName : fields.keySet()) {
                if (firstField) {
                    String fieldValue = entry.getValue().get(fieldName);
                    csvLine.append(fieldValue != null ? fieldValue : "");
                    firstField = false;
                } else {
                    String fieldValue = entry.getValue().get(fieldName);
                    csvLine.append(",").append(fieldValue != null ? fieldValue : "");
                }
            }
            System.out.println(csvLine.toString());
        }
    }

    private static void processRecord(String recordData, Map<String, Integer> fields, Map<String, Map<String, String>> records) {
        String[] lines = recordData.split("\n");

        Map<String, String> recordMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Pattern pattern = Pattern.compile("<([^>]+):(\\d+)>([^<]+)");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String fieldName = matcher.group(1).toUpperCase(); // Convert to uppercase
                int fieldLength = Integer.parseInt(matcher.group(2));
                String fieldValue = matcher.group(3);
                recordMap.put(fieldName, fieldValue.substring(0, fieldLength));
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
}
