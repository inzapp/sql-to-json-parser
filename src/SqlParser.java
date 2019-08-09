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

import java.util.List;

public class SqlParser {

    public static final String SELECT_SQL = "select user_name,age,email from t_user " +
            "where user_id > 16546 group by age order by user_name desc";

    public static final String INSERT_SQL = "insert into t_order (id,user_id,sum) values ('EF1243',12,23.6)";

    public static final String UPDATE_SQL = "update person set first_name = 'Fred' where last_name ='Wilson'";

    public static final String DELETE_SQL = "delete from t_item where id = 'AF3434'";

    public static void main(String[] args) throws Exception {
        parseSQL(SELECT_SQL);
        parseSQL(INSERT_SQL);
        parseSQL(UPDATE_SQL);
        parseSQL(DELETE_SQL);
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
        System.out.print("\ntable: ");
        Table table = delete.getTable();
        System.out.print(table.getName());

        Expression where = delete.getWhere();
        System.out.print("\nWhere: " + where.toString());
    }

    private static void parseInsert(Insert insert) {
        System.out.print("\ncolumn: ");
        List<Column> columns = insert.getColumns();
        if (columns != null) {
            columns.forEach(column -> System.out.print(column.getColumnName() + " "));
        }

        System.out.print("\ntable: ");
        String tableName = insert.getTable().getName();
        System.out.print(tableName);

        System.out.print("\nvalue:");
        List<Expression> insertValueExpressionList = ((ExpressionList) insert.getItemsList())
                .getExpressions();
        insertValueExpressionList.forEach(expression -> System.out.print(expression.toString() + " "));
        System.out.println();
    }

    private static void parseUpdate(Update update) {
        System.out.print("\ncolumn: ");
        List<Column> columns = update.getColumns();
        if (columns != null) {
            columns.forEach(column -> System.out.print(column.getColumnName() + " "));
        }

        System.out.print("\ntable: ");
        List<Table> tables = update.getTables();
        tables.forEach(table -> System.out.print(table.getName() + " "));

        System.out.print("\nvalue: ");
        List<Expression> expressions = update.getExpressions();
        expressions.forEach(expression -> System.out.print(expression.toString() + " "));

        Expression whereExpression = update.getWhere();
        System.out.print("\nwhere: " + whereExpression);
        System.out.println();
    }

    private static void parseSelect(Select select) {
        System.out.print("\ncolumn: ");
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectItems = plain.getSelectItems();
        if (selectItems != null) {
            for (int i = 0; i < selectItems.size(); i++) {
                SelectItem selectItem = selectItems.get(i);
                System.out.print(selectItem.toString() + " ");
            }
        }

        System.out.print("\ntable: ");
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        tableList.forEach(tableName -> System.out.print(tableName));


        Expression whereExpression = plain.getWhere();
        System.out.print("\nwhere: " + whereExpression.toString());

        System.out.print("\ngroup by: ");
        GroupByElement groupByElement = plain.getGroupBy();
        if (groupByElement != null) {
            List<Expression> groupByExpressions = groupByElement.getGroupByExpressions();
            if (groupByExpressions != null) {
                groupByExpressions
                        .forEach(groupByExpression -> System.out.print(groupByExpression.toString()));
            }
        }

        System.out.print("\norder by: ");
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
                System.out.println("join: " + join.toString());
                System.out.println("join expression: " + join.getOnExpression().toString());
            }
        }
    }
}