package com.inzapp.sqlToJsonParser.core;

import com.inzapp.sqlToJsonParser.config.JsonKey;
import com.inzapp.sqlToJsonParser.core.json.JsonManager;
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
import org.json.JSONObject;

import java.util.List;

class SqlVisitor extends JsonManager {
    /**
     * parse sql string to org.json.JSONObject
     * @param sql
     * read from file in com.inzapp.sqlToJsonParser.SqlToJsonParser.readFromFile()
     * @return
     * converted json object
     * return null if conversion failed
     */
    JSONObject parse(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            statement.accept(statementVisitor);
            sortJsonByKey();
            return this.json;
        } catch (Exception e) {
            // sql parse failure
//            e.printStackTrace();
            return null;
        }
    }

    private final StatementVisitorAdapter statementVisitor = new StatementVisitorAdapter() {
        /**
         * insert
         * @param insert
         * visitor event listened
         */
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

        /**
         * select
         * @param select
         * visitor event listened
         */
        @Override
        public void visit(Select select) {
            // crud
            putToJson(JsonKey.CRUD, JsonKey.SELECT);
            select.getSelectBody().accept(new SelectVisitorAdapter() {
                @Override
                public void visit(PlainSelect plainSelect) {
                    // column
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

                // union
                @Override
                public void visit(SetOperationList setOperationList) {
                    // where sub query
                    List<SelectBody> selectBodies = setOperationList.getSelects();
                    if (selectBodies != null) {
                        selectBodies.forEach(selectBody -> {
                            putToJson(JsonKey.WHERE_SUB_QUERY, 1, selectBody.toString());
                            putToJson(JsonKey.WHERE_SUB_QUERY_ANALYSE, 1, new SqlVisitor().parse(selectBody.toString()));
                        });
                    }
                }
            });
            super.visit(select);
        }

        /**
         * update
         * @param update
         * visitor event listened
         */
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

        /**
         * delete
         * @param delete
         * visitor event listened
         */
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

    private final SelectItemVisitorAdapter selectItemVisitor = new SelectItemVisitorAdapter() {
        /**
         * for testing
         * @param item
         * visitor event listened
         */
        @Override
        public void visit(SelectExpressionItem item) {
            putToJson(JsonKey.COLUMN, item.toString());
            super.visit(item);
        }
    };

    private final FromItemVisitorAdapter fromItemVisitor = new FromItemVisitorAdapter() {
        /**
         * search table name
         * @param table
         * visitor event listened
         */
        @Override
        public void visit(Table table) {
            putToJson(JsonKey.TABLE, table.toString());
            super.visit(table);
        }

        /**
         * search sub query in from statement
         * @param subSelect
         * visitor event listened
         */
        @Override
        public void visit(SubSelect subSelect) {
            putToJson(JsonKey.TABLE_SUB_QUERY, 1, subSelect.toString());
            putToJson(JsonKey.TABLE_SUB_QUERY_ANALYSE, 1, new SqlVisitor().parse(subSelect.toString()));
            super.visit(subSelect);
        }
    };

    private final ExpressionVisitorAdapter expressionVisitor = new ExpressionVisitorAdapter() {
        /**
         * search sub query in where statement
         * @param subSelect
         * visitor event listened
         */
        @Override
        public void visit(SubSelect subSelect) {
            putToJson(JsonKey.WHERE_SUB_QUERY, 1, subSelect.toString());
            putToJson(JsonKey.WHERE_SUB_QUERY_ANALYSE, 1, new SqlVisitor().parse(subSelect.toString()));
            super.visit(subSelect);
        }

        /**
         * column for select, set
         * @param column
         * visitor event listened
         */
        @Override
        public void visit(Column column) {
            putToJson(JsonKey.COLUMN, column.toString());
            super.visit(column);
        }
    };

    private final ExpressionVisitorAdapter whereExpressionVisitor = new ExpressionVisitorAdapter() {
        /**
         * used for only where expression (need no where column)
         * @param subSelect
         * visitor event listened
         */
        @Override
        public void visit(SubSelect subSelect) {
            putToJson(JsonKey.WHERE_SUB_QUERY, 1, subSelect.toString());
            putToJson(JsonKey.WHERE_SUB_QUERY_ANALYSE, 1, new SqlVisitor().parse(subSelect.toString()));
            super.visit(subSelect);
        }

        // do not override column visit method here
    };

    private GroupByVisitor groupByVisitor = new GroupByVisitor() {
        /**
         * search group by
         * @param groupByElement
         * visitor event listened
         */
        @Override
        public void visit(GroupByElement groupByElement) {
            putToJson(JsonKey.GROUP_BY, groupByElement.toString());
        }
    };

    private final OrderByVisitorAdapter orderByVisitor = new OrderByVisitorAdapter() {
        /**
         * search order by
         * @param orderBy
         * visitor event listened
         */
        @Override
        public void visit(OrderByElement orderBy) {
            putToJson(JsonKey.ORDER_BY, orderBy.toString());
            super.visit(orderBy);
        }
    };
}