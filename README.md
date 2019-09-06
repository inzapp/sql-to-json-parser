# sql-to-json-parser
SQL to org.json.JSONObject parser using JsqlParser

## Download
https://github.com/inzapp/sql-to-json-parser/releases

## Usage
Run as default file name<br>
Input file name : input.txt<br>
Output file name : output.json<br>
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
input.txt
```sql
SELECT * FROM TAB
```
output.json
```json
{
    "CRUD": ["SELECT"],
    "COLUMN": ["*"],
    "TABLE": ["TAB"]
}
```
## Insert
input.txt
```sql
INSERT INTO TABLENAME VALUE ('TESTVALUE')
```
output.json
```json
{
    "CRUD": ["INSERT"],
    "TABLE": ["TABLENAME"],
    "VALUE": ["'TESTVALUE'"]
}
```
## Update
input.txt
```sql
UPDATE TABLENAME
SET COLNAME = 1
WHERE CONDITION = 2
```
output.json
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
input.txt
```sql
DELETE FROM TABLE
WHERE CONDITION = 'ALL'
```
output.json
```json
{
    "CRUD": ["DELETE"],
    "TABLE": ["TABLE"],
    "WHERE": ["CONDITION = 'ALL'"]
}
```
## Sub Query

input.txt
```sql
SELECT A, B FROM (SELECT A, B FROM FROMTABLE WHERE FROMCONDITION = 'FROMCONDITION')
WHERE C = (SELECT C FROM WHERETABLE WHERE WHERECONDITION = 'WHERECONDITION')
ORDER BY A
```

output.json
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
