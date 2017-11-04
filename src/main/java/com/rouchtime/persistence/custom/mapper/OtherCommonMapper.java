package com.rouchtime.persistence.custom.mapper;

import java.util.List;

import org.apache.ibatis.annotations.SelectProvider;
import com.rouchtime.persistence.custom.provider.OtherCommonProvider;
public interface OtherCommonMapper<T> {
	@SelectProvider(type = OtherCommonProvider.class, method = "dynamicSQL")
	List<T> selectNewsKeys();
	
	@SelectProvider(type = OtherCommonProvider.class, method = "dynamicSQL")
	List<T> selectLabels();
	
	@SelectProvider(type = OtherCommonProvider.class, method = "dynamicSQL")
	List<T> selectSecondLabels();
	
	@SelectProvider(type = OtherCommonProvider.class, method = "dynamicSQL")
	List<T> selectThirdLabels();
}
