import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


class Attribute {
    static final String INSERT = "insert";
    static final String SELECT = "select";
    static final String UPDATE = "update";
    static final String DELETE = "delete";
    static final String CRUD = "crud";
    static final String COLUMN = "column";
    static final String TABLE = "table";
    static final String WHERE = "where";
    static final String VALUE = "value";
    static final String GROUP_BY = "group_by";
    static final String ORDER_BY = "order_by";
    static final String JOIN = "join";
    static final String JOIN_EXPRESSION = "join_expression";
}

class pRes {
    static final String OUTPUT_FILE_NAME = "parsed.txt";
}

public class SqlParser {
//    public static String SELECT_SQL = "select user_name,age,email from t_user " +
//            "where user_id > 16546 group by age order by user_name desc";
//    public static String INSERT_SQL = "insert into t_order (id,user_id,sum) values ('EF1243',12,23.6)";
//    public static String UPDATE_SQL = "update person set first_name = 'Fred' where last_name ='Wilson'";
//    public static String DELETE_SQL = "delete from t_item where id = 'AF3434'";

//    private static String SELECT_SQL = "SELECT\n" +
//            "EC_CLPS_DV_CD as STAFF_GBN,\n" +
//            "EC_NM as STAFF_CLASS,\n" +
//            "EC_GRP as ORG_STAFF_GBN,\n" +
//            "ODR as ORDER_SEQ,\n" +
//            "ORGZ_ID as ORG_ID,\n" +
//            "EMP_NO as STAFF_EMP_ID,\n" +
//            "USE_AYN as USE_YN,\n" +
//            "REG_DTM,\n" +
//            "RGR_ID,\n" +
//            "UPDT_DTM,\n" +
//            "UTUR_ID\n" +
//            "FROM ETCH005M\n" +
//            "WHERE EC_CLPS_DV_CD IN(\n" +
//            "SELECT EC_DV as staff_gbn\n" +
//            "FROM ETCH004M\n" +
//            "WHERE(TO_DATE(SCH_REG_DT,'YYYY-MM-DD')\n" +
//            "BETWEEN TO_DATE(SYSDATE,'YYYY-MM-DD')\n" +
//            "AND TO_DATE(SYSDATE,'YYYY-MM-DD')\n" +
//            ")\n" +
//            "GROUP BY EC_DV\n" +
//            "UNION\n" +
//            "SELECT EC_CLPS_DV_CD as staff_gbn\n" +
//            "FROM ETCH005M\n" +
//            "WHERE NVL(USE_AYN,'Y')<>'N')\n" +
//            "ORDER BY ODR";

    private static String SELECT_SQL = "SELECT\n" +
            "REG_NO AS REGIS_SEQNO,\n" +
            "to_char(REG_DTM,'yyyy-MM-dd') AS REGIS_DT,\n" +
            "to_char(REG_DTM,'HH24miss') AS REGIS_TM,\n" +
            "to_char(UPDT_DTM,'yyyyMMdd') AS MODIFY_DT,\n" +
            "to_char(UPDT_DTM,'HH24miss') AS MODIFY_TIME,\n" +
            "RGR_ID AS REGIS_ID,\n" +
            "RGR_NM AS REGIS_NAME,\n" +
            "ANW_NM AS REPLY_NAME,\n" +
            "ANW_ID AS REPLY_ID,\n" +
            "ANS_DT AS REPLY_DT,\n" +
            "TXT AS CONTENTS,\n" +
            "NVL(ATC_FL, '') AS ACCTFILE,\n" +
            "NTT_TYP AS BOARD_TYPE,\n" +
            "TITL AS TITLE,\n" +
            "INQ_CNT AS CNT,\n" +
            "COUNT(*) OVER() AS TOTAL_CNT\n" +
            "FROM ETCH002M\n" +
            "WHERE MASKING_YB = 'Y'";

    private static String INSERT_SQL = "INSERT INTO etob004m\n" +
            "(tsk_no,dept_cd,tsk_dv_cd,tsk_nm,oppb_gd_cd,txt,stt_dtm,\n" +
            "end_dtm,rgr_id,reg_dtm,utur_id,updt_dtm\n" +
            ")\n" +
            "VALUES('2019-00436','015000','001','test','001','w',TO_DATE('2019-08-08 09:00:00','yyyy-MM-dd HH24:mi:ss'),\n" +
            "TO_DATE('2019-08-08 18:00:00','yyyy-MM-dd HH24:mi:ss'),'1507030',SYSDATE,'1507030',SYSDATE\n" +
            ")";

    private static String UPDATE_SQL = "UPDATE etch002m\n" +
            "SET updt_dtm=SYSDATE,\n" +
            "utur_id=0912026,\n" +
            "anw_nm='',\n" +
            "anw_id='',\n" +
            "ans_dt='',\n" +
            "txt = '<p>asdfaasd~!@#%@W%sdfsdf</p>',\n" +
            "atc_fl='',\n" +
            "titl='테스트'\n" +
            "WHERE reg_no=24831;";

//    private static final String UPDATE_SQL = "UPDATE etch002m\n" +
//            "SET updt_dtm = SYSDATE,\n" +
//            "utur_id = 0912026,\n" +
//            "anw_nm = '',\n" +
//            "anw_id = '',\n" +
//            "ans_dt = '',\n" +
//            "txt = '<p>테ㅔㅔ테테ㅔ테테테테트ㅡ스스스트트트ㅏ</p><p><br></p><p>ㄴㅇㄹㅁㄴㅇㄹ,asdfaasd~!@#%@W%sdfsdfsㄴㅇㄹㄴㄹ\"ㄴㅇㄹ\"ㄴㅇㄹ\"ㄴ\"\"\"\"'''''</p>',\n" +
//            "atc_fl ='',\n" +
//            "titl ='테스트'\n" +
//            "WHERE reg_no = 24831;";

//    private static String DELETE_SQL = "DELETE FROM\n" +
//            "ETCH009M\n" +
//            "WHERE PK_SEQ = '2019-00370'";

    private static String DELETE_SQL = "DELETE FROM ETCH006M \n" +
            "WHERE EMP_NO IN ('123', '124', '125', '126', '127', '128')";

    public static void main(String[] args) {
//        parseSQL(SELECT_SQL);
//        parseSQL(INSERT_SQL);
//        parseSQL(UPDATE_SQL);
        parseSQL(DELETE_SQL);
    }

    private static void parseSQL(String sql) {
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            System.out.println("\n==============sql:\n" + "syntax error");
            return;
        }

        System.out.println("\n==============sql:\n" + sql);
        JSONObject json = new JSONObject();
        if (statement instanceof Select) {
            Select select = (Select) statement;
            json = parseSelect(select);
        } else if (statement instanceof Update) {
            Update update = (Update) statement;
            json = parseUpdate(update);
        } else if (statement instanceof Insert) {
            Insert insert = (Insert) statement;
            json = parseInsert(insert);
        } else if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            json = parseDelete(delete);
        }
        saveJsonToFile(json);
    }

    private static JSONObject parseDelete(Delete delete) {
        System.out.println("\ntable: ");
        Table table = delete.getTable();
        String tableName = table.getName();
        System.out.print(tableName);

        Expression where = delete.getWhere();
        String whereCondition = where.toString();
        System.out.println("\nWhere: " + whereCondition);

        JSONObject json = new JSONObject();
        try {
            json.put(Attribute.CRUD, Attribute.DELETE);
            json.put(Attribute.TABLE, tableName);
            json.put(Attribute.WHERE, whereCondition);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private static JSONObject parseInsert(Insert insert) {
        System.out.println("\ncolumn: ");
        List<Column> columnList = insert.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null) {
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));
            columnList.forEach(column -> System.out.println(column.getColumnName() + " "));
        }

        System.out.println("\ntable: ");
        String tableName = insert.getTable().getName();
        System.out.print(tableName);

        System.out.println("\nvalue:");
        List<Expression> insertValueExpressionList = ((ExpressionList) insert.getItemsList()).getExpressions();
        List<String> insertValueList = new ArrayList<>();
        insertValueExpressionList.forEach(expression -> insertValueList.add(expression.toString()));
        insertValueExpressionList.forEach(expression -> System.out.println(expression.toString() + " "));
        System.out.println();

        JSONObject json = new JSONObject();
        try {
            json.put(Attribute.CRUD, Attribute.INSERT);
            json.put(Attribute.TABLE, tableName);
            json.put(Attribute.VALUE, insertValueList);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private static JSONObject parseUpdate(Update update) {
        System.out.println("\ncolumn: ");
        List<Column> columnList = update.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null) {
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));
            columnList.forEach(column -> System.out.println(column.getColumnName() + " "));
        }

        System.out.println("\ntable: ");
        List<Table> tableList = update.getTables();
        List<String> tableNameList = new ArrayList<>();
        tableList.forEach(table -> tableNameList.add(table.getName()));
        tableList.forEach(table -> System.out.println(table.getName() + " "));

        System.out.println("\nvalue: ");
        List<Expression> expressions = update.getExpressions();
        List<String> valueList = new ArrayList<>();
        expressions.forEach(expression -> valueList.add(expression.toString()));
        expressions.forEach(expression -> System.out.println(expression.toString() + " "));

        Expression whereExpression = update.getWhere();
        String whereCondition = whereExpression.toString();
        System.out.println("\nwhere:\n " + whereCondition);
        System.out.println();

        JSONObject json = new JSONObject();
        try {
            json.put(Attribute.CRUD, Attribute.UPDATE);
            json.put(Attribute.TABLE, tableNameList);
            json.put(Attribute.VALUE, valueList);
            json.put(Attribute.WHERE, whereCondition);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private static JSONObject parseSelect(Select select) {
        System.out.print("\ncolumn: ");
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems = plain.getSelectItems();
        List<String> columnList = new ArrayList<>();
        if (selectItems != null) {
            for (SelectItem selectItem : selectItems) {
                columnList.add(selectItem.toString());
                System.out.println(selectItem.toString() + " ");
            }
        }

        System.out.print("\ntable: ");
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        tableList.forEach(System.out::println);

        Expression whereExpression = plain.getWhere();
        String whereCondition = whereExpression.toString();
        System.out.print("\nwhere: " + whereCondition);

        System.out.print("\ngroup by: ");
        GroupByElement groupByElement = plain.getGroupBy();
        List<String> groupByList = new ArrayList<>();
        if (groupByElement != null) {
            List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
            if (groupByExpressions != null) {
                groupByExpressions.forEach(groupByExpression -> System.out.println(groupByExpression.toString()));
                groupByExpressions.forEach(groupByExpression -> groupByList.add(groupByExpression.toString()));
            }
        }

        System.out.print("\norder by: ");
        List<OrderByElement> orderByElementList = plain.getOrderByElements();
        List<String> orderByList = new ArrayList<>();
        if (orderByElementList != null) {
            orderByElementList.forEach(orderByElement -> System.out.println(orderByElement.getExpression().toString()));
            orderByElementList.forEach(orderByElement -> orderByList.add(orderByElement.getExpression().toString()));
        }
        System.out.println();

        JSONObject json = new JSONObject();
        try {
            json.put(Attribute.CRUD, Attribute.SELECT);
            json.put(Attribute.COLUMN, columnList);
            json.put(Attribute.TABLE, tableList);
            json.put(Attribute.WHERE, whereCondition);
            json.put(Attribute.GROUP_BY, groupByList);
            json.put(Attribute.ORDER_BY, orderByList);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public static JSONObject parseSelectJoin(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        List<String> joinList = new ArrayList<>();
        List<String> joinExpressionList = new ArrayList<>();
        if (joins != null) {
            for (Join join : joins) {
                System.out.println("join: " + join.toString());
                System.out.println("join expression: " + join.getOnExpression().toString());
                joinList.add(join.toString());
                joinExpressionList.add(join.getOnExpression().toString());
            }
        }

        JSONObject json = new JSONObject();
        try {
            json.put(Attribute.CRUD, Attribute.SELECT);
            json.put(Attribute.JOIN, joinList);
            json.put(Attribute.JOIN_EXPRESSION, joinExpressionList);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private static void saveJsonToFile(JSONObject json) {
        try {
            FileOutputStream fos = new FileOutputStream(pRes.OUTPUT_FILE_NAME);
            fos.write(json.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}