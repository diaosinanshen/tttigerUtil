package com.tttiger.sql.handler;

import com.tttiger.sql.Result;

import java.sql.ResultSet;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/11 18:59
 */
public interface ResultHandler<T> {


    Result handleListResult(ResultSet resultSet);

    Result handleIntResult(ResultSet resultSet);

    Result getResult();
}
