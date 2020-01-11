package com.tttiger.sql;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/11 20:43
 */
public class SqlMethod {

    private String sql;

    private int sqlType;

    private int expectResultType;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getExpectResultType() {
        return expectResultType;
    }

    public void setExpectResultType(int expectResultType) {
        this.expectResultType = expectResultType;
    }

    public SqlMethod(String sql, int sqlType, int expectResultType) {
        this.sql = sql;
        this.sqlType = sqlType;
        this.expectResultType = expectResultType;
    }
}
