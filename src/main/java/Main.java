import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {

        try {
            Scanner scanner = new Scanner(System.in);
            JSONArray rules = getConfigFile(scanner);
            List<Integer> inputValues = getInputFile(scanner);

            checkRules(rules, inputValues);
        } catch (Exception exception) {
            System.out.println(exception);
            System.out.println("Issue with file, please ensure proper path and formatting");
        }
    }

    public static String readFile(String string, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(string));
        return new String(encoded, encoding);
    }

    public static JSONArray jsonStringToArray(String jsonString) throws JSONException {
        JSONArray returnArray = new JSONArray(jsonString);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            return jsonArray;
        } catch(JSONException e) {
            e.printStackTrace();
            System.out.println("Issue with file, please ensure proper path and formatting");
        }

        return returnArray;
    }

    public static JSONArray getConfigFile(Scanner scanner) {
        System.out.print("Config file path? ");
        String configPath = scanner.next();
        Path pathToFile = Paths.get(configPath).toAbsolutePath();

        try {
            String configContent = readFile(pathToFile.normalize().toString(), StandardCharsets.US_ASCII);
            return jsonStringToArray(configContent);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Issue with file, please ensure proper path and formatting");
        }

        return null;
    }

    public static List<Integer> getInputFile(Scanner scanner) {
        System.out.print("Input file path? ");
        String inputPath = scanner.next();
        Path pathToFile = Paths.get(inputPath).toAbsolutePath();
        File file = new File(pathToFile.normalize().toString());

        List<List<String>> lines = new ArrayList<>();
        Scanner inputStream;

        try{
            inputStream = new Scanner(file);

            while(inputStream.hasNext()){
                String line= inputStream.next();
                String[] values = line.split(",");
                lines.add(Arrays.asList(values));
            }

            inputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Issue with file, please ensure proper path and formatting");
        }

        List<String> inputs = lines.iterator().next();
        List<Integer> inputValues = new ArrayList<>();

        for(String input : inputs) {
            inputValues.add(Integer.valueOf(input));
        }

        return inputValues;
    }

    public static void checkRules(JSONArray rules, List<Integer> inputValues){
        for (int i = 0, size = rules.length(); i < size; i++) {
            JSONObject rule = rules.getJSONObject(i);
            String ruleId = Integer.toString((int) rule.get("id"));
            int iterator = 0;

            for (Integer value : inputValues) {
                String ruleType = (String) rule.get("type");
                if (ruleType.equals("comparison")) {
                    doComparisonCheck(value, rule, ruleId, iterator);
                } else if (ruleType.equals("delta")) {
                    if (iterator == 0) {
                        ++iterator;
                        continue;
                    }

                    doDeltaCheck(rule, iterator, ruleId, inputValues);
                }

                ++iterator;
            }
        }
    }

    public static void doComparisonCheck(Integer value, JSONObject rule, String ruleId, int iterator) {
        int ruleValue = (int) rule.get("value");

        if (rule.get("check").equals(">")  && value > ruleValue) {
            System.out.println(ruleId + "@" + Integer.toString(iterator));
        } else if (rule.get("check").equals("<")  && value < ruleValue) {
            System.out.println(ruleId + "@" + Integer.toString(iterator));
        } else if(rule.get("check").equals("=") && value.equals(ruleValue)) {
            System.out.println(ruleId + "@" + Integer.toString(iterator));
        }
    }

    public static void doDeltaCheck(JSONObject rule, int iterator, String ruleId, List<Integer> inputValues) {
        String ruleCheck = (String) rule.get("check");
        Integer delta = (Integer) rule.get("over") - (Integer) rule.get("change");
        Integer currentNumber = inputValues.get(iterator);
        Integer pastNumber = inputValues.get(iterator - 1);
        Integer thisDelta = currentNumber - pastNumber;

        if (ruleCheck.equals("=") && delta.equals(thisDelta)) {
            System.out.println(ruleId + "@" + Integer.toString(iterator));
        } else if (ruleCheck.equals(">") && thisDelta > delta) {
            System.out.println(ruleId + "@" + Integer.toString(iterator));
        } else if (ruleCheck.equals("<") && thisDelta < delta) {
            System.out.println(ruleId + "@" + Integer.toString(iterator));
        }
    }
}