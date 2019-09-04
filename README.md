# sql-to-json-parser
SQL to org.json.JSONObject parser using JsqlParser

## Video
https://www.youtube.com/watch?v=a24L_8GETLY

## Download
https://github.com/inzapp/sql-to-json-parser/releases

## Usage
```
$ java -jar SqlToJsonParser.jar
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
