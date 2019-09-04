# sql-to-json-parser
SQL to org.json.JSONObject parser using JsqlParser

## Video
https://www.youtube.com/watch?v=a24L_8GETLY

## Usage
```
java -jar SqlToJsonParser.jar
```
## Select
input.txt
```
SELECT * FROM TAB
```
output.json
```
{
    "CRUD": ["SELECT"],
    "COLUMN": ["*"],
    "TABLE": ["TAB"]
}
```
## Insert
input.txt
```
INSERT INTO TABLENAME VALUE ('TESTVALUE')
```
output.json
```
{
    "CRUD": ["INSERT"],
    "TABLE": ["TABLENAME"],
    "VALUE": ["'TESTVALUE'"]
}
```
## Update
input.txt
```
UPDATE TABLENAME
SET COLNAME = 1
WHERE CONDITION = 2
```
output.json
```
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
```
DELETE FROM TABLE
WHERE CONDITION = 'ALL'
```
output.json
```
{
    "CRUD": ["DELETE"],
    "TABLE": ["TABLE"],
    "WHERE": ["CONDITION = 'ALL'"]
}
```
## Sub Query
input.txt
```
SELECT A, B FROM (SELECT A, B FROM FROMTABLE WHERE SUBCONDITION = 'SUBCONDITION')
WHERE C = (SELECT C FROM WHERETABLE WHERE WHERECONDITION = 'WHERECONDITION')
ORDER BY A
```
output.json
```
{
    "CRUD": ["SELECT"],
    "COLUMN": [
      "A",
      "B"
    ],
    "TABLE SUB QUERY 1": ["(SELECT A, B FROM FROMTABLE WHERE SUBCONDITION = 'SUBCONDITION')"],
    "TABLE SUB QUERY ANALYSE 1": {
        "CRUD": ["SELECT"],
        "COLUMN": [
          "A",
          "B"
        ],
        "TABLE": ["FROMTABLE"],
        "WHERE": ["SUBCONDITION = 'SUBCONDITION'"]
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
