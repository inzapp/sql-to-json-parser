import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;

public class SqlParser {
    private static String SQL = "SELECT * FROM TAB where a = 1 and (b = 2 and c = 3) or d = 4";
//    private static String SQL = "SELECT * FROM TAB where a between c and d";

    private static final String[] SYNTAX = {
            "SELECT", "DISTINCT", "INSERT", "UPDATE", "DELETE", "FROM", "WHERE",
            "BY", "UNION", "ALL"
    };

    private static List<String> getSelectColumnList(Statement statement) {
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<String> columnList = new ArrayList<>();
        for (SelectItem item : plainSelect.getSelectItems()) {
            System.out.println(item.toString());
            columnList.add(item.toString());
        }
        return columnList;
    }

    private static List<String> getSelectTableList(Statement statement) {
        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
        return tableList;
    }

    private static List<String> getSelectWhereList(Statement statement) {
        Select selectStatement = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        Expression whereExpression = plainSelect.getWhere();
        whereExpression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(MultiExpressionList multiExprList) {
                for(ExpressionList expression : multiExprList.getExprList()) {
                    System.out.println(expression.toString());
                }
                super.visit(multiExprList);
            }

            @Override
            protected void visitBinaryExpression(BinaryExpression expr) {
                if(expr instanceof ComparisonOperator) {
                    System.out.println(expr.getLeftExpression() + expr.getStringExpression() + expr.getRightExpression());
                }
                super.visitBinaryExpression(expr);
            }
        });
//        System.out.println(plainSelect.getWhere().toString());
//        net.sf.jsqlparser.expression.operators.relational.
//        if(whereExpression instanceof AndExpression) {
//            System.out.println("and");
//        } else if(whereExpression instanceof OrExpression) {
//            System.out.println("or");
//        } else if(whereExpression instanceof Between){
//            System.out.println("between");
//        } else {
//            System.out.println("else");
//        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        try {
            ParseTest.main(new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
        Select statement;
        try {
            statement = (Select) CCJSqlParserUtil.parse(SQL);
        } catch (JSQLParserException e) {
            e.printStackTrace();
            return;
        }

        getSelectWhereList(statement);
//        PlainSelect ps = (PlainSelect) statement.getSelectBody();
//        System.out.println(ps.getWhere().toString());
//        System.out.println(ps.getSelectItems().get(0).toString());

        // here you have to check what kind of expression it is and execute your actions individualy for every expression implementation
//        AndExpression e = (AndExpression) ps.getWhere();
//        System.out.println(e.getLeftExpression());

//        for (String cur : getSelectColumnList(statement))
//            System.out.println(cur);
//
//        for (String cur : getSelectTableList(statement))
//            System.out.println(cur);

        System.exit(1);

        List<String> strList = new LinkedList<>(Arrays.asList(SQL.split(" ")));
        switch (strList.get(0).toUpperCase()) {
            case "SELECT":
                System.out.printf("method : %s\n\n", strList.get(0).toUpperCase());
                removeUntil(strList, new String[]{"SELECT"});
                if (strList.isEmpty())
                    return;

                String[] strings = getStringsUntil(strList, new String[]{"FROM"}, ",");
                System.out.println("col : ");
                for (String cur : strings)
                    System.out.println(cur.trim());
                System.out.println();

                removeUntil(strList, new String[]{"FROM"});
                if (strList.isEmpty())
                    return;

                strings = getStringsUntil(strList, new String[]{"WHERE"}, ",");
                System.out.println("table :");
                for (String cur : strings)
                    System.out.println(cur.trim());
                System.out.println();

                removeUntil(strList, new String[]{"WHERE"});
                if (strList.isEmpty())
                    return;

                strings = getStringsUntil(strList, new String[]{""}, "AND|OR| ");
                System.out.println("condition :");
                for (String cur : strings)
                    System.out.println(cur.trim());
                System.out.println();
                break;

            default:
                break;
        }
    }

    private static void removeUntil(List<String> strList, String[] criteria) {
        boolean breakFlag = false;
        while (!breakFlag) {
            for (String criterion : criteria) {
                if (strList.isEmpty())
                    return;

                if (strList.get(0).equals(criterion)) {
                    strList.remove(0);
                    breakFlag = true;
                    break;
                } else {
                    strList.remove(0);
                }
            }
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

/**
 * Created by 2YSP on 2019/7/6.
 */
class ParseTest {

    public static final String SELECT_SQL = "select user_name,age,email from t_user " +
            "where user_id > 16546 group by age order by user_name desc";

    public static final String INSERT_SQL = "insert into t_order (id,user_id,sum) values ('EF1243',12,23.6)";

    public static final String UPDATE_SQL = "update person set first_name = 'Fred' where last_name ='Wilson'";

    public static final String DELETE_SQL = "delete from t_item where id = 'AF3434'";

    public static void main(String[] args) throws Exception {
        ParseTest.parseSQL(SELECT_SQL);
        ParseTest.parseSQL(INSERT_SQL);
        ParseTest.parseSQL(UPDATE_SQL);
        ParseTest.parseSQL(DELETE_SQL);

    }

    public static void parseSQL(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        System.out.println("\n==============sql: " + sql);
        if (statement instanceof Select) {
            Select select = (Select) statement;
            parseSelect(select);
        }
        if (statement instanceof Update) {
            Update update = (Update) statement;
            parseUpdate(update);
        }
        if (statement instanceof Insert) {
            Insert insert = (Insert) statement;
            parseInsert(insert);
        }
        if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            parseDelete(delete);
        }
    }

    private static void parseDelete(Delete delete) {
        //解析SQL中的表名
        System.out.print("\n表名: ");
        Table table = delete.getTable();
        System.out.print(table.getName());
        //解析SQL中的Where部分
        Expression where = delete.getWhere();
        System.out.print("\nWhere部分: " + where.toString());
    }

    private static void parseInsert(Insert insert) {
        // 获取更新的列名
        System.out.print("\n列名: ");
        List<Column> columns = insert.getColumns();
        if (columns != null) {
            columns.forEach(column -> System.out.print(column.getColumnName() + " "));
        }
        // 解析表名
        System.out.print("\n表名: ");
        String tableName = insert.getTable().getName();
        System.out.print(tableName);

        // 解析insert语句中的插入记录的各个列值
        System.out.print("\n列值:");
        List<Expression> insertValueExpressionList = ((ExpressionList) insert.getItemsList())
                .getExpressions();
        insertValueExpressionList.forEach(expression -> System.out.print(expression.toString() + " "));
        System.out.println();
    }

    private static void parseUpdate(Update update) {
        //解析列名
        System.out.print("\n列名: ");
        List<Column> columns = update.getColumns();
        if (columns != null) {
            columns.forEach(column -> System.out.print(column.getColumnName() + " "));
        }
        // 解析表名
        System.out.print("\n表名: ");
        List<Table> tables = update.getTables();
        tables.forEach(table -> System.out.print(table.getName() + " "));
        // 解析 列值
        System.out.print("\n列值: ");
        List<Expression> expressions = update.getExpressions();
        expressions.forEach(expression -> System.out.print(expression.toString() + " "));
        // 解析where部分
        Expression whereExpression = update.getWhere();
        System.out.print("\nWhere部分: " + whereExpression);
        System.out.println();
    }

    private static void parseSelect(Select select) {
        // 获取select语句查询的列
        System.out.print("\n列名: ");
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems = plain.getSelectItems();
        if (selectItems != null) {
            for (int i = 0; i < selectItems.size(); i++) {
                SelectItem selectItem = selectItems.get(i);
                System.out.print(selectItem.toString() + " ");
            }
        }
        // 解析Select语句中的表名
        System.out.print("\n表名: ");
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        tableList.forEach(tableName -> System.out.print(tableName));

        // 解析SQL语句中的where 部分
        Expression whereExpression = plain.getWhere();
        System.out.print("\nWhere部分: " + whereExpression.toString());
        // 解析SQL语句中的group by 部分
        System.out.print("\nGroup by 部分的列名: ");
        GroupByElement groupByElement = plain.getGroupBy();
        List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
        if (groupByExpressions != null) {
            groupByExpressions
                    .forEach(groupByExpression -> System.out.print(groupByExpression.toString()));
        }

        // 解析SQL语句中的 order by 部分的列名
        System.out.print("\norder by 部分的列名: ");
        List<OrderByElement> orderByElementList = plain.getOrderByElements();
        if (orderByElementList != null) {
            orderByElementList
                    .forEach(orderByElement -> System.out.print(orderByElement.getExpression().toString()));
        }
        System.out.println();
    }

    public static void parseSelectJoin(String sql) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joinList = plainSelect.getJoins();
        if (joinList != null) {
            for (int i = 0; i < joinList.size(); i++) {
                Join join = joinList.get(i);
                System.out.println("JOIN部分: " + join.toString());
                System.out.println("链接表达式: " + join.getOnExpression().toString());
            }
        }

    }
}