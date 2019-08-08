import java.sql.PreparedStatement;
import java.util.*;

public class SqlParser {
    private static String SQL = "SELECT firstCol, secondCol, thirdCol FROM table1 as a, ";
    private static final String[] SYNTAX = {
            "SELECT", "SELECT DISTINCT", "INSERT", "UPDATE", "DELETE", "FROM", "WHERE",
            "ORDER BY", "GROUP BY", "UNION", "UNION ALL"
    };

    public static void main(String[] args) {
        List<String> strList = new LinkedList<>(Arrays.asList(split(SQL, " ")));
        switch (strList.get(0).toUpperCase()) {
            case "SELECT":
                strList.remove(0);
                for(String cur : getStringUntil(getString(strList), new String[]{"FROM"}).split(" "))
                    System.out.println(cur);
                break;

            default:
                break;
        }
    }

    private static String getString(List<String> strList) {
        StringBuilder builder = new StringBuilder();
        for(String cur : strList)
            builder.append(cur).append(" ");
        return builder.toString().trim();
    }

    private static String getStringUntil(String str, String[] criteria) {
        List<String> strList = Arrays.asList(str.split(" "));
        List<String> untilStrList = new ArrayList<>();
        boolean breakFlag = false;
        for(String cur : strList) {
            for(String criterion : criteria) {
                if(cur.trim().equals(criterion.trim())) {
                    breakFlag = true;
                    break;
                }
            }
            if(breakFlag)
                break;
            untilStrList.add(cur.trim());
        }
        return getString(untilStrList);
    }

    private static String[] split(String str, String regex) {
        String[] splits = str.split(regex);
        List<String> splitList = new ArrayList<>();
        List<String> quoteWordList = new ArrayList<>();
        boolean quote = false;
        for (String cur : splits) {
            char[] iso = cur.toCharArray();
            if (iso[0] == '\'' && iso[iso.length - 1] == '\'') {
                splitList.add(cur);
            } else if (iso[0] == '\'' || iso[0] == '\"') {
                // 'asd asd asd asd' 나 "asd asd" 와 같은 쿼테이션으로 둘러쌓인 단어의 시작부분을 감지
                quoteWordList.add(cur);
                quote = true;
            } else if (iso[iso.length - 1] == '\'' || iso[iso.length - 1] == '\"') {
                // 'asd asd asd asd' 나 "asd asd" 와 같은 쿼테이션으로 둘러쌓인 단어의 끝부분을 감지
                quoteWordList.add(cur);
                StringBuilder builder = new StringBuilder();
                for (String curQuote : quoteWordList)
                    builder.append(curQuote + " ");
                splitList.add(builder.toString().trim());
                quoteWordList.clear();
                quote = false;
            } else {
                if (quote) {
                    // 쿼테이션 플래그가 활성화되어 3번째 else if 문에 들어가기 전까지 모두를 담아둠
                    quoteWordList.add(cur);
                } else {
                    // 쿼테이션으로 둘러쌓이지 않은 일반 단어 감지
                    splitList.add(cur);
                }
            }
        }

        String[] newSplits = new String[splitList.size()];
        for (int i = 0; i < splitList.size(); ++i)
            newSplits[i] = splitList.get(i);
        return newSplits;
    }
}