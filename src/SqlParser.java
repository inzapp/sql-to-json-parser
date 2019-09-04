import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;

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
    static final String JOIN = "JOIN ";
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
            sortJsonByKey();
            return this.json;
        } catch (Exception e) {
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
            select.getSelectBody().accept(new SelectVisitor() {
                @Override
                public void visit(PlainSelect plainSelect) {
                    // column
                    // TODO : select * column test
                    List<SelectItem> selectItems = plainSelect.getSelectItems();
                    if (selectItems != null)
//                        selectItems.forEach(selectItem -> selectItem.accept(selectItemVisitor));
                        selectItems.forEach(selectItem -> putToJson(JsonKey.COLUMN, selectItem.toString()));

                    // table
                    FromItem fromItem = plainSelect.getFromItem();
                    if (fromItem != null)
                        fromItem.accept(fromItemVisitor);

                    // where
                    Expression whereExpression = plainSelect.getWhere();
                    if (whereExpression != null) {
                        putToJson(JsonKey.WHERE, whereExpression.toString());
                        whereExpression.accept(whereExpressionVisitor);
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
                        joins.forEach(join -> putToJson(JsonKey.JOIN, 1, join.toString()));
                }

                @Override
                public void visit(SetOperationList setOperationList) {
                    // where sub query
                    List<SelectBody> selectBodies = setOperationList.getSelects();
                    if (selectBodies != null) {
                        selectBodies.forEach(selectBody -> {
                            putToJson(JsonKey.WHERE_SUB_QUERY, 1, selectBody.toString());
                            putToJson(JsonKey.WHERE_SUB_QUERY_ANALYSE, 1, new SqlToJsonParser().parse(selectBody.toString()));
                        });
                    }

                }

                @Override
                public void visit(WithItem withItem) {
                    System.out.println("[NEED DEBUG -> visit(WithItem withItem)]");
                }

                @Override
                public void visit(ValuesStatement valuesStatement) {
                    System.out.println("[NEED DEBUG -> visit(ValuesStatement valuesStatement)]");
                }
            });
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

            // where
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
                whereExpression.accept(whereExpressionVisitor);
            }

            super.visit(delete);
        }
    };

//    private final SelectItemVisitorAdapter selectItemVisitor = new SelectItemVisitorAdapter() {
//        @Override
//        public void visit(SelectExpressionItem item) {
//            putToJson(JsonKey.COLUMN, item.toString());
//            super.visit(item);
//        }
//    };

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

        // column for select, set
        @Override
        public void visit(Column column) {
            // TODO : does where column needed?
            putToJson(JsonKey.COLUMN, column.toString());
            super.visit(column);
        }
    };

    // used for only where expression (need no where column)
    private final ExpressionVisitorAdapter whereExpressionVisitor = new ExpressionVisitorAdapter(){
        @Override
        public void visit(SubSelect subSelect) {
            putToJson(JsonKey.WHERE_SUB_QUERY, 1, subSelect.toString());
            putToJson(JsonKey.WHERE_SUB_QUERY_ANALYSE, 1, new SqlToJsonParser().parse(subSelect.toString()));
            super.visit(subSelect);
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

    private void sortJsonByKey() {
        try {
            Iterator keys = json.keys();
            Map<String, Object> treeMap = new TreeMap<>(String::compareTo);
            while (keys.hasNext()) {
                String key = (String) keys.next();
                treeMap.put(key, json.get(key));
            }
            this.json = new JSONObject(treeMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class SqlParser {
    public static void main(String[] args) {
        String sql = readSqlFromFile();
        System.out.println("input sql\n\n" + sql);

        try {
            JSONObject json = new SqlToJsonParser().parse(sql);
            String jsonString = json.toString(4);
            System.out.println("output json\n\n" + jsonString);
            saveFile(jsonString);
        } catch (Exception e) {
            saveFile(pRes.SQL_SYNTAX_ERROR);
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