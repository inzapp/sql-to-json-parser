package com.inzapp.sqlToJsonParser.core;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

public class SqlToJsonParser {
    /**
     * used for java code
     *
     * @param sql raw sql query
     * @return parsed json string
     * return null if exception was caught
     */
    public String parse(String sql) {
        try {
            return new SqlVisitor().parse(sql).toString(4);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * read sql from com.inzapp.SqlToJsonParser.config.Config.INPUT_FILE_NAME
     * used for executable jar
     *
     * @param fileName main methods first args, user specified input file name
     * @return sql from file
     * return null if not exist file
     */
    private String readSqlFromFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
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
     * save json string as specified file name
     *
     * @param jsonString org.json.JSONObject().toString()
     *                   parsed json object
     * @param fileName   main methods second args, user specified output file name
     */
    private void saveFile(String jsonString, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(jsonString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}