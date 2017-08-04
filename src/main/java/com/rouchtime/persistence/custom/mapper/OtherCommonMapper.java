package com.rouchtime.persistence.custom.mapper;

import java.util.List;

import org.apache.ibatis.annotations.SelectProvider;

import com.rouchtime.persistence.custom.provider.NlpSougouPublicRawProvider;

public interface OtherCommonMapper<T> {
	@SelectProvider(type = NlpSougouPublicRawProvider.class, method = "dynamicSQL")
	List<T> selectNewsKeys();
	
	@SelectProvider(type = NlpSougouPublicRawProvider.class, method = "dynamicSQL")
	List<T> selectLabels();
}
