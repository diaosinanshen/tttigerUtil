package com.tttiger.sql;

import java.util.List;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/10 16:51
 */
public interface Executor<T> {

    /**
     * 根据条件查询
     *
     * @param sql 查询条件
     * @return 查询结果
     */
    List<T> select(String sql);

    /**
     * 根据主键进行查询
     *
     * @param sql 执行语句
     * @return 查询结果
     */
    T selectById(String sql);

    /**
     * 数据插入
     *
     * @param sql 执行语句
     * @return 是否成功
     */
    boolean insert(String sql);

    /**
     * 根据主键更新实体非空字段
     *
     * @param sql 执行语句
     * @return 是否成
     */
    boolean updateById(String sql);

    /**
     * 根据条件更新，实体非空字段
     *
     * @param sql 执行语句
     * @return 影响行数
     */
    int update(String sql);

    /**
     * 进行真实删除或逻辑删除
     *
     * @param sql 执行语句
     * @return 是否成功
     */
    boolean deleteById(String sql);

    /**
     * 根据条件进行真实删除或逻辑删除
     *
     * @param sql 执行语句
     * @return 影响行数
     */
    int delete(String sql);
}
