package com.rouchtime.persistence.dao;

import com.rouchtime.persistence.custom.mapper.OtherCommonMapper;
import com.rouchtime.persistence.model.NlpClassificationRaw;

import tk.mybatis.mapper.common.Mapper;

public interface NlpClassificationRawMapper extends Mapper<NlpClassificationRaw>,OtherCommonMapper<NlpClassificationRaw> {
}