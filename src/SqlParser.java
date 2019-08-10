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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
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
    static final String INPUT_FILE_NAME = "input.txt";
    static final String OUTPUT_FILE_NAME = "output.txt";
    static final String SQL_SYNTAX_ERROR = "sql syntax error";
}

public class SqlParser {
    public static void main(String[] args) {
        String sql = readSqlFromFile();
        System.out.println("input sql\n\n" + sql);

        String jsonString = sqlToJsonString(sql);
        System.out.println("output json string\n\n" + jsonString);
        saveFile(jsonString);
    }

    private static String readSqlFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(pRes.INPUT_FILE_NAME));
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

    private static String sqlToJsonString(String sql) {
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            return pRes.SQL_SYNTAX_ERROR;
        }

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

        try {
            return json.toString(4);
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject parseDelete(Delete delete) {
        Table table = delete.getTable();
        String tableName = table.getName();

        Expression where = delete.getWhere();
        String whereCondition = where.toString();

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
        List<Column> columnList = insert.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null)
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));

        String tableName = insert.getTable().getName();

        List<Expression> insertValueExpressionList = ((ExpressionList) insert.getItemsList()).getExpressions();
        List<String> insertValueList = new ArrayList<>();
        insertValueExpressionList.forEach(expression -> insertValueList.add(expression.toString()));

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
        List<Column> columnList = update.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null)
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));

        List<Table> tableList = update.getTables();
        List<String> tableNameList = new ArrayList<>();
        tableList.forEach(table -> tableNameList.add(table.getName()));

        List<Expression> expressions = update.getExpressions();
        List<String> valueList = new ArrayList<>();
        expressions.forEach(expression -> valueList.add(expression.toString()));

        Expression whereExpression = update.getWhere();
        String whereCondition = whereExpression.toString();

        JSONObject json = new JSONObject();
        try {
            json.put(Attribute.CRUD, Attribute.UPDATE);
            json.put(Attribute.COLUMN, columnNameList);
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
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems = plain.getSelectItems();
        List<String> columnList = new ArrayList<>();
        if (selectItems != null) {
            for (SelectItem selectItem : selectItems)
                columnList.add(selectItem.toString());
        }

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);

        Expression whereExpression = plain.getWhere();
        String whereCondition = "";
        try {
            whereExpression.toString();
        }catch(Exception e) {
            // empty
        }

        GroupByElement groupByElement = plain.getGroupBy();
        List<String> groupByList = new ArrayList<>();
        if (groupByElement != null) {
            List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
            if (groupByExpressions != null)
                groupByExpressions.forEach(groupByExpression -> groupByList.add(groupByExpression.toString()));
        }

        List<OrderByElement> orderByElementList = plain.getOrderByElements();
        List<String> orderByList = new ArrayList<>();
        if (orderByElementList != null)
            orderByElementList.forEach(orderByElement -> orderByList.add(orderByElement.getExpression().toString()));

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

    public static JSONObject parseSelectJoin(Statement statement) {
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        List<String> joinList = new ArrayList<>();
        List<String> joinExpressionList = new ArrayList<>();
        if (joins != null) {
            for (Join join : joins) {
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

    private static void saveFile(String jsonString) {
        try {
            FileOutputStream fos = new FileOutputStream(pRes.OUTPUT_FILE_NAME);
            fos.write(jsonString.getBytes());
            if (jsonString.equals(pRes.SQL_SYNTAX_ERROR))
                System.out.println("\nparse failure");
            else
                System.out.println("\nparse success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}