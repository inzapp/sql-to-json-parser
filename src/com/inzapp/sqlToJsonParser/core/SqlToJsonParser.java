package com.inzapp.sqlToJsonParser.core;

import com.inzapp.sqlToJsonParser.config.Config;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

public class SqlToJsonParser {
    /**
     * main method
     *
     * @param args not used
     */
    public static void main(String[] args) {
        SqlToJsonParser sqlToJsonParser = new SqlToJsonParser();
        SqlVisitor sqlVisitor = new SqlVisitor();
        try {
            String sql = sqlToJsonParser.readSqlFromFile();
            if (sql == null)
                throw new Exception("input file does not exist");

            JSONObject json = sqlVisitor.parse(sql);
            if (json == null)
                throw new Exception("sql syntax error");

            String jsonString = json.toString(4);
            System.out.println("input sql\n\n" + sql);
            System.out.println("output json\n\n" + jsonString);

            sqlToJsonParser.saveFile(jsonString);
            System.out.println("parse success");
        } catch (Exception e) {
            sqlToJsonParser.saveFile(Config.SQL_SYNTAX_ERROR);
            System.out.println(e.getMessage());
        }
    }

    /**
     * read sql from com.inzapp.SqlToJsonParser.config.Config.INPUT_FILE_NAME
     *
     * @return sql from file
     * return null if not exist file
     */
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
            return null;
        }
    }

    /**
     * save json string as file
     * file name is com.inzapp.SqlToJsonParser.config.Config.OUTPUT_FILE_NAME
     *
     * @param jsonString org.json.JSONObject().toString()
     *                   parsed json object
     */
    private void saveFile(String jsonString) {
        try {
            FileOutputStream fos = new FileOutputStream(Config.OUTPUT_FILE_NAME);
            fos.write(jsonString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}