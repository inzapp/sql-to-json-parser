# sql-to-json-parser
SQL to org.json.JSONObject parser using JsqlParser

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
