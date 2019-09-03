import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

class JsonKey {
    static final String CRUD = "CRUD";
    static final String INSERT = "INSERT";
    static final String SELECT = "SELECT";
    static final String UPDATE = "UPDATE";
    static final String DELETE = "DELETE";
    static final String COLUMN = "COLUMN";
    static final String TABLE = "TABLE";
    static final String TABLE_SUB_QUERY = "TABLE SUB QUERY ";
    static final String TABLE_SUB_QUERY_ANALYSE = "TABLE SUB QUERY ANALYSE ";
    static final String WHERE = "WHERE";
    static final String WHERE_SUB_QUERY = "WHERE SUB QUERY ";
    static final String WHERE_SUB_QUERY_ANALYSE = "WHERE SUB QUERY ANALYSE ";
    static final String VALUE = "VALUE";
    static final String GROUP_BY = "GROUP_BY";
    static final String ORDER_BY = "ORDER_BY";
    static final String JOIN = "JOIN";
    static final String JOIN_EXPRESSION = "JOIN_EXPRESSION";
}

class pRes {
    static final String INPUT_FILE_NAME = "input.txt";
    static final String OUTPUT_FILE_NAME = "output.txt";
    static final String SQL_SYNTAX_ERROR = "sql syntax error";
}

class SqlToJsonParser {
    private JSONObject json = new JSONObject();

    JSONObject parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            statement.accept(statementVisitor);
            return json;
        } catch (JSQLParserException e) {
            // sql parse failure
            e.printStackTrace();
            return null;
        }
    }

    private final StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
        @Override
        public void visit(Insert insert) {
            // crud
            putToJson(JsonKey.CRUD, JsonKey.INSERT);

            // table
            Table table = insert.getTable();
            if (table != null)
                table.accept(fromItemVisitor);

            // columns
            List<Column> columns = insert.getColumns();
            if (columns != null)
                columns.forEach(column -> column.accept(expressionVisitor));

            // values
            List<Expression> expressions = ((ExpressionList) insert.getItemsList()).getExpressions();
            if (expressions != null)
                expressions.forEach(expression -> putToJson(JsonKey.VALUE, expression.toString()));

            super.visit(insert);
        }

        @Override
        public void visit(Select select) {
            // crud
            putToJson(JsonKey.CRUD, JsonKey.SELECT);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            // column
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            if (selectItems != null)
                selectItems.forEach(selectItem -> selectItem.accept(selectItemVisitor));

            // table
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem != null)
                fromItem.accept(fromItemVisitor);

            // where
            Expression whereExpression = plainSelect.getWhere();
            if (whereExpression != null) {
                putToJson(JsonKey.WHERE, whereExpression.toString());
                whereExpression.accept(expressionVisitor);
            }

            // group by
            GroupByElement groupByElement = plainSelect.getGroupBy();
            if (groupByElement != null)
                groupByElement.accept(groupByVisitor);

            // order by
            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
            if (orderByElements != null)
                orderByElements.forEach(orderByElement -> orderByElement.accept(orderByVisitor));

            // joins
            List<Join> joins = plainSelect.getJoins();
            if (joins != null)
                joins.forEach(join -> join.getRightItem().accept(fromItemVisitor));

            super.visit(select);
        }

        @Override
        public void visit(Update update) {
            // crud
            putToJson(JsonKey.CRUD, JsonKey.UPDATE);

            // columns
            List<Column> columns = update.getColumns();
            if (columns != null)
                columns.forEach(column -> column.accept(expressionVisitor));

            // tables
            List<Table> tables = update.getTables();
            if (tables != null)
                tables.forEach(table -> table.accept(fromItemVisitor));

            // values
            List<Expression> expressions = update.getExpressions();
            if (expressions != null)
                expressions.forEach(expression -> putToJson(JsonKey.VALUE, expression.toString()));

            // add where
            Expression whereExpression = update.getWhere();
            if (whereExpression != null)
                putToJson(JsonKey.WHERE, whereExpression.toString());

            super.visit(update);
        }

        @Override
        public void visit(Delete delete) {
            // crud
            putToJson(JsonKey.CRUD, JsonKey.DELETE);

            // table
            Table table = delete.getTable();
            if (table != null)
                table.accept(fromItemVisitor);

            // where
            Expression whereExpression = delete.getWhere();
            if (whereExpression != null) {
                putToJson(JsonKey.WHERE, whereExpression.toString());
                whereExpression.accept(expressionVisitor);
            }

            super.visit(delete);
        }
    };

    private final SelectItemVisitorAdapter selectItemVisitor = new SelectItemVisitorAdapter() {
        @Override
        public void visit(SelectExpressionItem item) {
            putToJson(JsonKey.COLUMN, item.toString());
            super.visit(item);
        }
    };

    private final FromItemVisitorAdapter fromItemVisitor = new FromItemVisitorAdapter() {
        @Override
        public void visit(Table table) {
            putToJson(JsonKey.TABLE, table.toString());
            super.visit(table);
        }

        @Override
        public void visit(SubSelect subSelect) {
            putToJson(JsonKey.TABLE_SUB_QUERY, 1, subSelect.toString());
            putToJson(JsonKey.TABLE_SUB_QUERY_ANALYSE, 1, new SqlToJsonParser().parse(subSelect.toString()));
            super.visit(subSelect);
        }
    };

    private final ExpressionVisitorAdapter expressionVisitor = new ExpressionVisitorAdapter() {
        @Override
        public void visit(SubSelect subSelect) {
            putToJson(JsonKey.WHERE_SUB_QUERY, 1, subSelect.toString());
            putToJson(JsonKey.WHERE_SUB_QUERY_ANALYSE, 1, new SqlToJsonParser().parse(subSelect.toString()));
            super.visit(subSelect);
        }

        @Override
        public void visit(Column column) {
            putToJson(JsonKey.COLUMN, column.toString());
            super.visit(column);
        }
    };

    private GroupByVisitor groupByVisitor = new GroupByVisitor() {
        @Override
        public void visit(GroupByElement groupByElement) {
            putToJson(JsonKey.GROUP_BY, groupByElement.toString());
        }
    };

    private final OrderByVisitorAdapter orderByVisitor = new OrderByVisitorAdapter() {
        @Override
        public void visit(OrderByElement orderBy) {
            putToJson(JsonKey.ORDER_BY, orderBy.toString());
            super.visit(orderBy);
        }
    };

    private void putToJson(String key, String value) {
        List<String> list = getConvertedJsonArray(key);
        if (list == null)
            list = new ArrayList<>();

        list.add(value);
        try {
            this.json.put(key, list);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void putToJson(String key, int idx, String value) {
        List<String> list = getConvertedJsonArray(key + idx);
        if (list != null) {
            putToJson(key + (idx + 1), value);
        } else {
            putToJson(key + idx, value);
        }
    }

    private void putToJson(String key, JSONObject json) {
        try {
            this.json.put(key, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void putToJson(String key, int idx, JSONObject json) {
        try {
            this.json.getJSONObject(key + idx);
            putToJson(key, (idx + 1), json);
        } catch (Exception e) {
            putToJson(key + idx, json);
        }
    }

    private List<String> getConvertedJsonArray(String key) {
        try {
            JSONArray jsonArray = this.json.getJSONArray(key);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i)
                list.add((String) jsonArray.get(i));
            return list;
        } catch (Exception e) {
            return null;
        }
    }
}

public class SqlParser {
    public static void main(String[] args) {
        String sql = readSqlFromFile();
        System.out.println("input sql\n\n" + sql);

        JSONObject json = new SqlToJsonParser().parse(sql);
        try {
            String jsonString = json.toString(4);
            System.out.println("output json\n\n" + jsonString);
            saveFile(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

//    private static String sqlToJsonString(String sql) {
//        // parse sql
//        Statement statement;
//        try {
//            statement = CCJSqlParserUtil.parse(sql);
//        } catch (JSQLParserException e) {
//            // sql parse failure
//            e.printStackTrace();
//            return pRes.SQL_SYNTAX_ERROR;
//        }
//
//        statement.accept(statementVisitor);
//
//        // convert sql string to json
//        JSONObject json = new JSONObject();
////        if (statement instanceof Insert) {
////            Insert insert = (Insert) statement;
//////            json = parseInsert(insert);
////        } else if (statement instanceof Select) {
////            Select select = (Select) statement;
//////            json = parseSelect(select);
////        } else if (statement instanceof Update) {
////            Update update = (Update) statement;
//////            json = parseUpdate(update);
////        } else if (statement instanceof Delete) {
////            Delete delete = (Delete) statement;
//////            json = parseDelete(delete);
////        }
//
//
//        // return json string with indent
//        try {
//            return json.toString(4);
//        } catch (JSONException e) {
//            return null;
//        }
//    }

    private static JSONObject parseInsert(Insert insert) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, JsonKey.CRUD, JsonKey.INSERT);

        // add column
        List<Column> columnList = insert.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null) {
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));
            putToJson(json, JsonKey.COLUMN, columnNameList.toString());
        }

        // add table
        String tableName = insert.getTable().getName();
        if (tableName != null)
            putToJson(json, JsonKey.TABLE, tableName);

        // add value
        List<Expression> insertValueExpressionList = ((ExpressionList) insert.getItemsList()).getExpressions();
        List<String> insertValueList = new ArrayList<>();
        insertValueExpressionList.forEach(expression -> insertValueList.add(expression.toString()));
        if (!insertValueList.isEmpty())
            putToJson(json, JsonKey.VALUE, insertValueList.toString());

        return json;
    }

    private static JSONObject parseSelect(Select select) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, JsonKey.CRUD, JsonKey.SELECT);

        // convert plain select
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        // add column
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        List<String> columnList = new ArrayList<>();
        if (selectItems != null) {
            for (SelectItem selectItem : selectItems)
                columnList.add(selectItem.toString());
            putToJson(json, JsonKey.COLUMN, columnList.toString());
        }

        // add table
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        if (!tableList.isEmpty())
            putToJson(json, JsonKey.TABLE, tableList.toString());

        // add where
        Expression whereExpression = plainSelect.getWhere();
        if (whereExpression != null) {
            String whereString = whereExpression.toString();
            putToJson(json, JsonKey.WHERE, whereString);
        }

        // add group by
        GroupByElement groupByElement = plainSelect.getGroupBy();
        List<String> groupByList = new ArrayList<>();
        if (groupByElement != null) {
            List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
            if (groupByExpressions != null) {
                groupByExpressions.forEach(groupByExpression -> groupByList.add(groupByExpression.toString()));
                putToJson(json, JsonKey.GROUP_BY, groupByList.toString());
            }
        }

        // add order by
        List<OrderByElement> orderByElementList = plainSelect.getOrderByElements();
        List<String> orderByList = new ArrayList<>();
        if (orderByElementList != null) {
            orderByElementList.forEach(orderByElement -> orderByList.add(orderByElement.getExpression().toString()));
            putToJson(json, JsonKey.ORDER_BY, orderByList.toString());
        }

        return json;
    }

    private static JSONObject parseUpdate(Update update) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, JsonKey.CRUD, JsonKey.UPDATE);

        // add column
        List<Column> columnList = update.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null) {
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));
            putToJson(json, JsonKey.COLUMN, columnNameList.toString());
        }

        // add table
        List<Table> tableList = update.getTables();
        List<String> tableNameList = new ArrayList<>();
        tableList.forEach(table -> tableNameList.add(table.getName()));
        putToJson(json, JsonKey.TABLE, tableNameList.toString());

        // add value
        List<Expression> expressions = update.getExpressions();
        List<String> valueList = new ArrayList<>();
        expressions.forEach(expression -> valueList.add(expression.toString()));
        putToJson(json, JsonKey.VALUE, valueList.toString());

        // add where
        Expression whereExpression = update.getWhere();
        if (whereExpression != null)
            putToJson(json, JsonKey.WHERE, whereExpression.toString());

        return json;
    }

    private static JSONObject parseDelete(Delete delete) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, JsonKey.CRUD, JsonKey.DELETE);

        // add table
        Table table = delete.getTable();
        putToJson(json, JsonKey.TABLE, table.toString());

        // add where
        Expression whereExpression = delete.getWhere();
        if (whereExpression != null)
            putToJson(json, JsonKey.WHERE, whereExpression.toString());

        return json;
    }

    private static boolean hasSubQuery(String whereString) {
        return (whereString.toUpperCase().contains(JsonKey.SELECT.toUpperCase()) || whereString.contains(JsonKey.SELECT));
    }

    private static String extractSubQuery(String whereString) {
        boolean upperCase = false;
        String[] splits = whereString.split(JsonKey.SELECT);
        if (splits.length == 0)
            return "";

        if (splits.length == 1) {
            splits = whereString.split(JsonKey.SELECT.toUpperCase());
            upperCase = true;
        }

        String beforeSelect = splits[0];
        String select = upperCase ? JsonKey.SELECT.toUpperCase() : JsonKey.SELECT;
        StringBuilder sb = new StringBuilder();
        sb.append(select);
        for (int i = 1; i < splits.length; ++i) {
            sb.append(splits[i]);
            if (i == splits.length - 1)
                break;
            sb.append(select);
        }

        // validate bracket
        int cnt = 0;
        int bracketStack = 0;
        char[] iso = sb.toString().toCharArray();
        for (char c : iso) {
            if (c == '(')
                ++bracketStack;
            else if (c == ')')
                --bracketStack;

            if (bracketStack < 0)
                break;

            ++cnt;
        }

        // remove end ')' if bracket stack is not empty
        if (cnt != iso.length) {
            sb = new StringBuilder(sb.toString());
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static JSONObject parseSelectJoin(Statement statement) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, JsonKey.CRUD, JsonKey.SELECT);

        // add join, join expression
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
            putToJson(json, JsonKey.JOIN, joinList.toString());
            putToJson(json, JsonKey.JOIN_EXPRESSION, joinExpressionList.toString());
        }

        return json;
    }

    private static void putToJson(JSONObject json, String key, String value) {
        try {
            if (hasSubQuery(value) && !key.equals(JsonKey.CRUD)) {
                String subQuery = extractSubQuery(value);
//                json.put(key, new JSONObject(sqlToJsonString(subQuery)));
            } else
                json.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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