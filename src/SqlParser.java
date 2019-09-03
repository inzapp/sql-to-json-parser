import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
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
    static final String INSERT = "INSERT";
    static final String SELECT = "SELECT";
    static final String UPDATE = "UPDATE";
    static final String DELETE = "DELETE";
    static final String CRUD = "CRUD";
    static final String COLUMN = "COLUMN";
    static final String TABLE = "TABLE";
    static final String WHERE = "WHERE";
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

//class ParserTest {
//    public static void main__() throws JSQLParserException {
//        String sql = "SELECT * FROM myTable, (select * from myTable2) as data1, (select b from myTable3) as data2";
//        Select select = (Select) CCJSqlParserUtil.parse(sql);
//        System.out.println(select.toString());
//
//        System.out.println("Type 1: Visitor processing");
//        select.getSelectBody().accept(new SelectVisitorAdapter() {
//            @Override
//            public void visit(PlainSelect plainSelect) {
//                plainSelect.getFromItem().accept(fromVisitor);
//                if (plainSelect.getJoins() != null)
//                    plainSelect.getJoins().forEach(join -> join.getRightItem().accept(fromVisitor));
//            }
//        });
//
//        System.out.println("Type 2: simple method calls");
//        processFromItem(((PlainSelect) select.getSelectBody()).getFromItem());
//        if (((PlainSelect) select.getSelectBody()).getJoins() != null)
//            ((PlainSelect) select.getSelectBody()).getJoins().forEach(join -> processFromItem(join.getRightItem()));
//
//        System.out.println("Type 3: hierarchically process all subselects");
//        select.getSelectBody().accept(new SelectDeParser() {
//            @Override
//            public void visit(SubSelect subSelect) {
//                System.out.println("  found subselect=" + subSelect.toString());
//                super.visit(subSelect);
//            }
//        });
//    }
//
//    private final static FromItemVisitorAdapter fromVisitor = new FromItemVisitorAdapter() {
//        @Override
//        public void visit(SubSelect subSelect) {
//            System.out.println("subselect=" + subSelect);
//        }
//
//        @Override
//        public void visit(Table table) {
//            System.out.println("table=" + table);
//        }
//    };
//
//    private static void processFromItem(FromItem fromItem) {
//        System.out.println("fromItem=" + fromItem);
//    }
//}

class Visitor {
    static final StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
        @Override
        public void visit(Insert insert) {
            System.out.println("insert : " + insert);
            super.visit(insert);
        }

        @Override
        public void visit(Select select) {
            System.out.println("select : " + select);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            plainSelect.accept(plainSelectVisitor);
            super.visit(select);
        }

        @Override
        public void visit(Update update) {
            System.out.println("update : " + update);
            super.visit(update);
        }

        @Override
        public void visit(Delete delete) {
            System.out.println("delete : " + delete);
            super.visit(delete);
        }
    };

    private static final SelectVisitorAdapter plainSelectVisitor = new SelectVisitorAdapter() {
        @Override
        public void visit(PlainSelect plainSelect) {
            plainSelect.getFromItem().accept(fromVisitor);
            plainSelect.getSelectItems().forEach(selectItem -> selectItem.accept(selectItemVisitor));

            // order by
            List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
            if (orderByElements != null)
                orderByElements.forEach(orderByElement -> orderByElement.accept(orderByVisitor));

            // joins
            List<Join> joins = plainSelect.getJoins();
            if (joins != null)
                joins.forEach(join -> join.getRightItem().accept(fromVisitor));

            super.visit(plainSelect);
        }
    };

    private static final FromItemVisitorAdapter fromVisitor = new FromItemVisitorAdapter() {
        @Override
        public void visit(Table table) {
            System.out.println("table : " + table);
            super.visit(table);
        }

        @Override
        public void visit(SubSelect subSelect) {
            System.out.println("sub select : " + subSelect);
            super.visit(subSelect);
        }
    };

    private static final SelectItemVisitorAdapter selectItemVisitor = new SelectItemVisitorAdapter() {
        @Override
        public void visit(SelectExpressionItem item) {
            System.out.println("column : " + item);
            super.visit(item);
        }
    };

    private static final OrderByVisitorAdapter orderByVisitor = new OrderByVisitorAdapter() {
        @Override
        public void visit(OrderByElement orderBy) {
            System.out.println("order by : " + orderBy);
            super.visit(orderBy);
        }
    };
}

public class SqlParser {
    public static void main(String[] args) {
//        try {
//            ParserTest.main__();
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//        System.exit(9);

        String sql = readSqlFromFile();
//        System.out.println("input sql\n\n" + sql);

        String jsonString = sqlToJsonString(sql);
        System.out.println("output json\n\n" + jsonString);
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
        // parse sql
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            // sql parse failure
            e.printStackTrace();
            return pRes.SQL_SYNTAX_ERROR;
        }

        statement.accept(Visitor.statementVisitor);

        // convert sql string to json
        JSONObject json = new JSONObject();
//        if (statement instanceof Insert) {
//            Insert insert = (Insert) statement;
////            json = parseInsert(insert);
//        } else if (statement instanceof Select) {
//            Select select = (Select) statement;
////            json = parseSelect(select);
//        } else if (statement instanceof Update) {
//            Update update = (Update) statement;
////            json = parseUpdate(update);
//        } else if (statement instanceof Delete) {
//            Delete delete = (Delete) statement;
////            json = parseDelete(delete);
//        }


        // return json string with indent
        try {
            return json.toString(4);
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject parseInsert(Insert insert) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, Attribute.CRUD, Attribute.INSERT);

        // add column
        List<Column> columnList = insert.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null) {
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));
            putToJson(json, Attribute.COLUMN, columnNameList.toString());
        }

        // add table
        String tableName = insert.getTable().getName();
        if (tableName != null)
            putToJson(json, Attribute.TABLE, tableName);

        // add value
        List<Expression> insertValueExpressionList = ((ExpressionList) insert.getItemsList()).getExpressions();
        List<String> insertValueList = new ArrayList<>();
        insertValueExpressionList.forEach(expression -> insertValueList.add(expression.toString()));
        if (!insertValueList.isEmpty())
            putToJson(json, Attribute.VALUE, insertValueList.toString());

        return json;
    }

    private static JSONObject parseSelect(Select select) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, Attribute.CRUD, Attribute.SELECT);

        // convert plain select
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

        // add column
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        List<String> columnList = new ArrayList<>();
        if (selectItems != null) {
            for (SelectItem selectItem : selectItems)
                columnList.add(selectItem.toString());
            putToJson(json, Attribute.COLUMN, columnList.toString());
        }

        // add table
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        if (!tableList.isEmpty())
            putToJson(json, Attribute.TABLE, tableList.toString());

        // add where
        Expression whereExpression = plainSelect.getWhere();
        if (whereExpression != null) {
            String whereString = whereExpression.toString();
            putToJson(json, Attribute.WHERE, whereString);
        }

        // add group by
        GroupByElement groupByElement = plainSelect.getGroupBy();
        List<String> groupByList = new ArrayList<>();
        if (groupByElement != null) {
            List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
            if (groupByExpressions != null) {
                groupByExpressions.forEach(groupByExpression -> groupByList.add(groupByExpression.toString()));
                putToJson(json, Attribute.GROUP_BY, groupByList.toString());
            }
        }

        // add order by
        List<OrderByElement> orderByElementList = plainSelect.getOrderByElements();
        List<String> orderByList = new ArrayList<>();
        if (orderByElementList != null) {
            orderByElementList.forEach(orderByElement -> orderByList.add(orderByElement.getExpression().toString()));
            putToJson(json, Attribute.ORDER_BY, orderByList.toString());
        }

        return json;
    }

    private static JSONObject parseUpdate(Update update) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, Attribute.CRUD, Attribute.UPDATE);

        // add column
        List<Column> columnList = update.getColumns();
        List<String> columnNameList = new ArrayList<>();
        if (columnList != null) {
            columnList.forEach(column -> columnNameList.add(column.getColumnName()));
            putToJson(json, Attribute.COLUMN, columnNameList.toString());
        }

        // add table
        List<Table> tableList = update.getTables();
        List<String> tableNameList = new ArrayList<>();
        tableList.forEach(table -> tableNameList.add(table.getName()));
        putToJson(json, Attribute.TABLE, tableNameList.toString());

        // add value
        List<Expression> expressions = update.getExpressions();
        List<String> valueList = new ArrayList<>();
        expressions.forEach(expression -> valueList.add(expression.toString()));
        putToJson(json, Attribute.VALUE, valueList.toString());

        // add where
        Expression whereExpression = update.getWhere();
        if (whereExpression != null)
            putToJson(json, Attribute.WHERE, whereExpression.toString());

        return json;
    }

    private static JSONObject parseDelete(Delete delete) {
        // add crud
        JSONObject json = new JSONObject();
        putToJson(json, Attribute.CRUD, Attribute.DELETE);

        // add table
        Table table = delete.getTable();
        putToJson(json, Attribute.TABLE, table.toString());

        // add where
        Expression whereExpression = delete.getWhere();
        if (whereExpression != null)
            putToJson(json, Attribute.WHERE, whereExpression.toString());

        return json;
    }

    private static boolean hasSubQuery(String whereString) {
        return (whereString.toUpperCase().contains(Attribute.SELECT.toUpperCase()) || whereString.contains(Attribute.SELECT));
    }

    private static String extractSubQuery(String whereString) {
        boolean upperCase = false;
        String[] splits = whereString.split(Attribute.SELECT);
        if (splits.length == 0)
            return "";

        if (splits.length == 1) {
            splits = whereString.split(Attribute.SELECT.toUpperCase());
            upperCase = true;
        }

        String beforeSelect = splits[0];
        String select = upperCase ? Attribute.SELECT.toUpperCase() : Attribute.SELECT;
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
        putToJson(json, Attribute.CRUD, Attribute.SELECT);

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
            putToJson(json, Attribute.JOIN, joinList.toString());
            putToJson(json, Attribute.JOIN_EXPRESSION, joinExpressionList.toString());
        }

        return json;
    }

    private static void putToJson(JSONObject json, String key, String value) {
        try {
            if (hasSubQuery(value) && !key.equals(Attribute.CRUD)) {
                String subQuery = extractSubQuery(value);
                json.put(key, new JSONObject(sqlToJsonString(subQuery)));
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