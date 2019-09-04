package com.inzapp.sqlToJsonParser.core;

import com.inzapp.sqlToJsonParser.config.Config;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

public class SqlToJsonParser {
    public static void main(String[] args) {
        SqlToJsonParser sqlToJsonParser = new SqlToJsonParser();
        SqlVisitor sqlVisitor = new SqlVisitor();
        String sql = sqlToJsonParser.readSqlFromFile();
        System.out.println("input sql\n\n" + sql);

        try {
            JSONObject json = sqlVisitor.parse(sql);
            String jsonString = json.toString(4);
            System.out.println("output json\n\n" + jsonString);
            sqlToJsonParser.saveFile(jsonString);
        } catch (Exception e) {
            sqlToJsonParser.saveFile(Config.SQL_SYNTAX_ERROR);
        }
    }

    private String readSqlFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(Config.INPUT_FILE_NAME));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                sb.append(line.trim()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveFile(String jsonString) {
        try {
            FileOutputStream fos = new FileOutputStream(Config.OUTPUT_FILE_NAME);
            fos.write(jsonString.getBytes());
            if (jsonString.equals(Config.SQL_SYNTAX_ERROR))
                System.out.println("\nparse failure");
            else
                System.out.println("\nparse success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}