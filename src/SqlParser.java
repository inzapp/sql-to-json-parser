import java.util.*;

public class SqlParser {
    private static String SQL = "SELECT  \n" +
            "                REG_NO AS REGIS_SEQNO,\n" +
            "               to_char(REG_DTM,'yyyy-MM-dd') AS REGIS_DT,   \n" +
            "               to_char(REG_DTM,'HH24miss') AS REGIS_TM,   \n" +
            "               to_char(UPDT_DTM,'yyyyMMdd') AS MODIFY_DT,\n" +
            "               to_char(UPDT_DTM,'HH24miss') AS MODIFY_TIME,\n" +
            "               RGR_ID AS REGIS_ID,   \n" +
            "               RGR_NM AS REGIS_NAME, \n" +
            "               ANW_NM AS REPLY_NAME, \n" +
            "               ANW_ID AS REPLY_ID,   \n" +
            "               ANS_DT AS REPLY_DT,   \n" +
            "               TXT AS CONTENTS,   \n" +
            "               NVL(ATC_FL, '') AS ACCTFILE,   \n" +
            "               NTT_TYP AS BOARD_TYPE, \n" +
            "               TITL AS TITLE,      \n" +
            "               INQ_CNT AS CNT,\n" +
            "               COUNT(*) OVER() AS TOTAL_CNT\n" +
            "               FROM ETCH002M\n" +
            "               WHERE MASKING_YB = 'Y'";

    private static final String[] SYNTAX = {
            "SELECT", "SELECT DISTINCT", "INSERT", "UPDATE", "DELETE", "FROM", "WHERE",
            "ORDER BY", "GROUP BY", "UNION", "UNION ALL"
    };

    public static void main(String[] args) {
        List<String> strList = new LinkedList<>(Arrays.asList(SQL.split(" ")));
        switch (strList.get(0).toUpperCase()) {
            case "SELECT":
                strList.remove(0);
                for (String cur : getStringsUntil(strList, new String[]{"FROM"}, ","))
                    System.out.println(cur.trim());
                break;

            default:
                break;
        }
    }

    private static String[] getStringsUntil(List<String> strList, String[] criteria, String regex) {
        String[] strings = getStringUntil(getString(strList), criteria).split(regex);
        List<String> totalList = new ArrayList<>();
        List<String> listForCombine = new ArrayList<>();

        Stack<Boolean> bracketStack = new Stack<>();
        for (String cur : strings) {
            char[] iso = cur.toCharArray();
            for (char c : iso) {
                if (c == '(')
                    bracketStack.push(true);
                else if (c == ')')
                    bracketStack.pop();
            }

            listForCombine.add(cur);
            if (bracketStack.empty()) {
                totalList.add(getString(listForCombine));
                listForCombine.clear();
            } else {
                listForCombine.add(regex);
            }
        }

        String[] arr = new String[totalList.size()];
        for (int i = 0; i < totalList.size(); ++i)
            arr[i] = totalList.get(i);
        return arr;
    }

    private static String getString(List<String> strList) {
        StringBuilder builder = new StringBuilder();
        for (String cur : strList)
            builder.append(cur).append(" ");
        return builder.toString().trim();
    }

    private static String getStringUntil(String str, String[] criteria) {
        String[] strings = str.split(" ");
        List<String> untilStrList = new ArrayList<>();
        boolean breakFlag = false;
        for (String cur : strings) {
            for (String criterion : criteria) {
                if (cur.trim().equals(criterion.trim())) {
                    breakFlag = true;
                    break;
                }
            }
            if (breakFlag)
                break;
            untilStrList.add(cur.trim());
        }
        return getString(untilStrList);
    }
}