package com.rouchtime.persistence.custom.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import tk.mybatis.mapper.entity.EntityTable;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

public class NlpSougouPublicRawProvider extends MapperTemplate{

	public NlpSougouPublicRawProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
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
	
}
