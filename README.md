# SQL to JSON parser
SQL to org.json.JSONObject parser using [**JsqlParser**](https://github.com/JSQLParser/JSqlParser)

## Download
https://github.com/inzapp/sql-to-json-parser/releases

## Usage
Run as default file name<br>
Default input file name : input.txt<br>
Default output file name : output.json<br>
```bash
$ java -jar SqlToJsonParser.jar
```

Run as specified file name
```bash
$ java -jar SqlToJsonParser.jar yourInputFileName yourOutputFileName
```

In Java
```java
SqlToJsonParser sqlToJsonParser = new SqlToJsonParser();
String jsonString = sqlToJsonParse.parse("SELECT * FROM TAB");
```

## Select
input
```sql
SELECT * FROM TAB
```
output
```json
{
    "CRUD": ["SELECT"],
    "COLUMN": ["*"],
    "TABLE": ["TAB"]
}
```

## Insert
input
```sql
INSERT INTO TABLENAME VALUE ('TESTVALUE')
```
output
```json
{
    "CRUD": ["INSERT"],
    "TABLE": ["TABLENAME"],
    "VALUE": ["'TESTVALUE'"]
}
```

## Update
input
```sql
UPDATE TABLENAME
SET COLNAME = 1
WHERE CONDITION = 2
```
output
```json
{
    "CRUD": ["UPDATE"],
    "TABLE": ["TABLENAME"],
    "COLUMN": ["COLNAME"],
    "VALUE": ["1"],
    "WHERE": ["CONDITION = 2"]
}
```

## Delete
input
```sql
DELETE FROM TABLE
WHERE CONDITION = 'ALL'
```
output
```json
{
    "CRUD": ["DELETE"],
    "TABLE": ["TABLE"],
    "WHERE": ["CONDITION = 'ALL'"]
}
```

## Sub Query
input
```sql
SELECT A, B FROM (SELECT A, B FROM FROMTABLE WHERE FROMCONDITION = 'FROMCONDITION')
WHERE C = (SELECT C FROM WHERETABLE WHERE WHERECONDITION = 'WHERECONDITION')
ORDER BY A
```

output
```json
{
    "CRUD": ["SELECT"],
    "COLUMN": [
      "A",
      "B"
    ],
    "TABLE": ["(SELECT A, B FROM FROMTABLE WHERE SUBCONDITION = 'SUBCONDITION')"],
    "TABLE SUB QUERY 1": ["(SELECT A, B FROM FROMTABLE WHERE FROMCONDITION = 'FROMCONDITION')"],
    "TABLE SUB QUERY ANALYSE 1": {
        "CRUD": ["SELECT"],
        "COLUMN": [
          "A",
          "B"
        ],
        "TABLE": ["FROMTABLE"],
        "WHERE": ["FROMCONDITION = 'FROMCONDITION'"]
    },
    "WHERE": ["C = (SELECT C FROM WHERETABLE WHERE WHERECONDITION = 'WHERECONDITION')"],
    "WHERE SUB QUERY 1": ["(SELECT C FROM WHERETABLE WHERE WHERECONDITION = 'WHERECONDITION')"],
    "WHERE SUB QUERY ANALYSE 1": {
        "CRUD": ["SELECT"],
        "COLUMN": ["C"],
        "TABLE": ["WHERETABLE"],
        "WHERE": ["WHERECONDITION = 'WHERECONDITION'"]
    },
    "ORDER_BY": ["A"]
}
```

## Join and Alias
input
```sql
SELECT A.a, C.b, E.c
FROM
(
    SELECT A.a, A.select_id, B.id
    FROM table A
    INNER JOIN joinTable B ON A.id = B.id
    INNER JOIN joinTable2 C ON B.id2 = C.id2
    WHERE A.yn = 'Y' AND C.id2 = 'id' AND A.select_id =
    (
        SELECT select_id
        FROM selector_table
        WHERE c_name = 'con_name' AND gateway = 'gateway' AND CONTAINER = 'container'
    )
) A
LEFT OUTER JOIN table_resource C ON A.select_id = C.select_id
INNER JOIN item D ON A.id = D.id
INNER JOIN table_item E
ON D.c = E.c
```

output
```json
{
    "CRUD": ["SELECT"],
    "COLUMN": [
        "A.a",
        "C.b",
        "E.c"
    ],
    "TABLE": ["(SELECT A.a, A.select_id, B.id FROM table A INNER JOIN joinTable B ON A.id = B.id INNER JOIN joinTable2 C ON B.id2 = C.id2 WHERE A.yn = 'Y' AND C.id2 = 'id' AND A.select_id = (SELECT select_id FROM selector_table WHERE c_name = 'con_name' AND gateway = 'gateway' AND CONTAINER = 'container'))"],
    "TABLE ALIAS": ["A"],
    "TABLE SUB QUERY 1": ["(SELECT A.a, A.select_id, B.id FROM table A INNER JOIN joinTable B ON A.id = B.id INNER JOIN joinTable2 C ON B.id2 = C.id2 WHERE A.yn = 'Y' AND C.id2 = 'id' AND A.select_id = (SELECT select_id FROM selector_table WHERE c_name = 'con_name' AND gateway = 'gateway' AND CONTAINER = 'container'))"],
    "TABLE SUB QUERY ANALYSE 1": {
        "COLUMN": [
            "A.a",
            "A.select_id",
            "B.id"
        ],
        "CRUD": ["SELECT"],
        "JOIN 1": ["INNER JOIN joinTable ON A.id = B.id"],
        "JOIN 2": ["INNER JOIN joinTable2 ON B.id2 = C.id2"],
        "JOIN ALIAS 1": ["B"],
        "JOIN ALIAS 2": ["C"],
        "TABLE": ["table"],
        "TABLE ALIAS": ["A"],
        "WHERE": ["A.yn = 'Y' AND C.id2 = 'id' AND A.select_id = (SELECT select_id FROM selector_table WHERE c_name = 'con_name' AND gateway = 'gateway' AND CONTAINER = 'container')"],
        "WHERE SUB QUERY 1": ["(SELECT select_id FROM selector_table WHERE c_name = 'con_name' AND gateway = 'gateway' AND CONTAINER = 'container')"],
        "WHERE SUB QUERY ANALYSE 1": {
            "COLUMN": ["select_id"],
            "CRUD": ["SELECT"],
            "TABLE": ["selector_table"],
            "WHERE": ["c_name = 'con_name' AND gateway = 'gateway' AND CONTAINER = 'container'"]
        }
    },
    "JOIN 1": ["LEFT OUTER JOIN table_resource ON A.select_id = C.select_id"],
    "JOIN 2": [
        "INNER JOIN item ON A.id = D.id",
        "INNER JOIN table_item ON D.c = E.c"
    ],
    "JOIN ALIAS 1": ["C"],
    "JOIN ALIAS 2": [
        "D",
        "E"
    ]
}
```
