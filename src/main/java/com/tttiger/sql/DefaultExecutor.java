package com.tttiger.sql;

import com.tttiger.sql.constant.SqlResultType;
import com.tttiger.sql.handler.DefaultResultHandler;
import com.tttiger.sql.handler.ResultHandler;

import javax.sql.DataSource;
import java.sql.*;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/10 17:11
 */
public class DefaultExecutor<T> implements Executor<T> {

    private DataSource dataSource;

    private Class<?> executeType;


    private ResultHandler<T> resultHandler;

    public DefaultExecutor(DataSource dataSource, Class<?> executeType) {
        this.dataSource = dataSource;
        this.executeType = executeType;

        this.resultHandler = new DefaultResultHandler<>(executeType);
    }

    private Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/test"
                    , "root", "123456");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        try {
//            return dataSource.getConnection();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }

    @Override
    public Result execute(SqlMethod sqlMethod) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sqlMethod.getSql());
            preparedStatement.execute();
            if(sqlMethod.getExpectResultType() == SqlResultType.EXPECT_RESULT_LIST){
                return resultHandler.handleListResult(preparedStatement.getResultSet());
            }else if(sqlMethod.getExpectResultType() == SqlResultType.EXPECT_RESULT_INT){
                Result result = new Result();
                result.setResult(preparedStatement.getUpdateCount());
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
