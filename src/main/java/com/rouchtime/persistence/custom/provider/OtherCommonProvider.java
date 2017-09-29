package com.rouchtime.persistence.custom.provider;

import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

public class OtherCommonProvider extends MapperTemplate {

	public OtherCommonProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
		super(mapperClass, mapperHelper);
	}

	public String selectNewsKeys(MappedStatement ms) {
		Class<?> entityClass = getEntityClass(ms);
		setResultType(ms, entityClass);
		StringBuilder sql = new StringBuilder();
		sql.append("select news_key");
		sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
		return sql.toString();
	}

	public String selectLabels(MappedStatement ms) {
		Class<?> entityClass = getEntityClass(ms);
		setResultType(ms, entityClass);
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct label");
		sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
		return sql.toString();
	}

	public String selectSecondLabels(MappedStatement ms) {
		Class<?> entityClass = getEntityClass(ms);
		setResultType(ms, entityClass);
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct second_label");
		sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
		return sql.toString();
	}

	public String selectThirdLabels(MappedStatement ms) {
		Class<?> entityClass = getEntityClass(ms);
		setResultType(ms, entityClass);
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct third_label");
		sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
		return sql.toString();

	}
}
