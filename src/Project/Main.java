package Project;

import java.io.*;
import java.util.*;

class MyRecord {
    private HashMap<String, String> records;

    public HashMap<String, String> getRecords() {
        return records;
    }

    public void setRecords(HashMap<String, String> records) {
        this.records.putAll(records);
    }

    public MyRecord() {
        this.records = new HashMap<>();
    }
}

public class Main {
    public static final int BUFF_SIZE = 1000000;
    public static ArrayList<String> cols = new ArrayList<>();
    public static ArrayList<MyRecord> myRecords = new ArrayList<>();
    public static HashMap<String, String> tmpRecords = new HashMap<>();

    public static void parseLine(String str) {
        if(str.indexOf('<') == -1)
            return;
        else{
            int start = str.indexOf('<');
            int end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
            while (true){
                if(start == -1 || end == -1)
                    break;
                else{
                    String tmpfield = str.substring(start + 1, end);

                    if(!isValidTag(tmpfield)){
                        start = str.indexOf('<' , end + 1);
                        end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
                    }

                    else{
                        String[] fieldInfo = parseOneField(tmpfield);
                        if(fieldInfo[0].equals("B")){
                            if(fieldInfo[1].equals("EOH")){
                                start = str.indexOf('<' , end + 1);
                                end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
                                tmpRecords.clear();
                            }
                            else{
                                if(tmpRecords.isEmpty()){
                                    start = str.indexOf('<' , end + 1);
                                    end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
                                    continue;
                                }
                                Set<String> tmpcols = tmpRecords.keySet();
                                for(String s : tmpcols){
                                    if(!cols.contains(s))
                                        cols.add(s);
                                }
                                MyRecord m = new MyRecord();
                                m.setRecords(tmpRecords);
                                myRecords.add(m);
                                tmpRecords.clear();
                                start = str.indexOf('<' , end + 1);
                                end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
                            }
                        }
                        else{
                            int fieldLength = Integer.parseInt(fieldInfo[2]);
                            String possiFieldContent = str.substring(end + 1 , (Math.min(end + 1 + fieldLength, str.length())));
                            if(!isContainsChinese(possiFieldContent)){
                                tmpRecords.put(fieldInfo[1] , possiFieldContent);
                                start = str.indexOf('<' , end + 1 + fieldLength);
                                end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
                                continue;
                            }
                            int sub = end + 1;
                            int totalLength = fieldLength;
                            int jflag = 0;
                            StringBuilder hasHanziFieldContent = new StringBuilder();
                            while(totalLength > 0){
                                char c = str.charAt(sub);
                                if(isChineseChar(c)){
                                    if(totalLength == 1){
                                        jflag = 1;
                                        break;
                                    }
                                    hasHanziFieldContent.append(c);
                                    totalLength -= 2;
                                }
                                else{
                                    hasHanziFieldContent.append(c);
                                    totalLength -= 1;
                                }
                                sub++;
                            }
                            tmpRecords.put(fieldInfo[1] , hasHanziFieldContent.toString());
                            start = str.indexOf('<' , (jflag == 0 ? sub : sub + 1));
                            end = ( start != -1 ? str.indexOf('>' , start + 1) : -1 );
                        }

                    }
                }
            }
        }
    }

    public static boolean isValidTag(String str){
        int indexOfColon = str.indexOf(':');
        if(indexOfColon == -1){
            String tmp = str.toUpperCase();
            return tmp.equals("EOH") || tmp.equals("EOR");
        }
        else{
            String fieldname = str.substring(0,indexOfColon);
            String fieldlength = str.substring(indexOfColon + 1);
            return fieldname.length() > 0 && fieldlength.length() > 0;
        }
    }

    public static String[] parseOneField(String str){
        int indexOfColon = str.indexOf(':');
        if(indexOfColon == -1){
            String fieldname = str.toUpperCase();
            return new String[]{"B" , fieldname};
        }
        else{
            String fieldname = str.substring(0,indexOfColon).toUpperCase();
            String fieldlength = str.substring(indexOfColon + 1);
            return new String[]{"A" , fieldname , fieldlength};

        }
    }

    public static void printPart(){
        cols.sort(Comparator.naturalOrder());
        int colflag = 0;
        for(String s : cols){
            if(s.indexOf(',') != -1)
                s = "\"" + s + "\"";
            if(colflag == 0){
                System.out.print(s);
                colflag = 1;
            }
            else{
                System.out.print("," + s);
            }
        }
        System.out.print('\n');
        Collections.sort(myRecords, new Comparator<MyRecord>() {
            @Override
            public int compare(MyRecord o1, MyRecord o2) {
                String o1_key = "(" + o1.getRecords().get("QSO_DATE") + "," + o1.getRecords().get("TIME_ON");
                String o2_key = "(" + o2.getRecords().get("QSO_DATE") + "," + o2.getRecords().get("TIME_ON");
                return o1_key.compareTo(o2_key);
            }
        });
        for(MyRecord m: myRecords){
            int flag = 0;
            for(String s:cols){
                if(m.getRecords().containsKey(s)){
                    String res = m.getRecords().get(s);
                    if(res.indexOf(',') != -1)
                        res = "\"" + res + "\"";
                    if(flag == 0){
                        System.out.print(res);
                        flag = 1;
                    }
                    else
                        System.out.print("," + res);
                }
                else{
                    if(flag == 0){
                        flag = 1;
                    }
                    else
                        System.out.print(",");
                }

            }
            System.out.print('\n');
        }
    }

    private static boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static boolean isContainsChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChineseChar(c)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in), BUFF_SIZE);
        String text = null;
        while ((text = buff.readLine()) != null) {
            parseLine(text);
        }
        printPart();
    }

}
