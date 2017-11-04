package com.rouchtime.nlp.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.persistence.dao.NlpGuojiRawMapper;
import com.rouchtime.persistence.model.NlpGuojiRaw;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;

import tk.mybatis.mapper.entity.Example;

@Component
@Qualifier("guojiCorpus")
public class GuojiCorpus extends AbstractBaseCorpus<NlpGuojiRaw> {

	@Autowired
	private NlpGuojiRawMapper nlpGuojiRawMapper;

	public List<String> fileids() {
		List<NlpGuojiRaw> list = nlpGuojiRawMapper.selectNewsKeys();
		List<String> fileids = new ArrayList<String>();
		for (NlpGuojiRaw nlpGuojiRaw : list) {
			fileids.add(nlpGuojiRaw.getNewsKey());
		}
		return fileids;
	}

	public String rawFromfileids(String fileids) {
		Example example = new Example(NlpGuojiRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpGuojiRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getContent();
	}

	public List<String> fileidFromLabel(String label) {
		Example example = new Example(NlpGuojiRaw.class);
		example.createCriteria().andCondition("label=", label);
		List<NlpGuojiRaw> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (NlpGuojiRaw nlpGuojiRaw : list) {
			fileids.add(nlpGuojiRaw.getNewsKey());
		}
		return fileids;
	}
	
	public List<Term> wordFromfileids(String fileids, TokenizerFactory factory) {
		Example example = new Example(NlpGuojiRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpGuojiRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		String content = RegexUtils.cleanSpecialWord(list.get(0).getContent());
		List<Term> terms = new ArrayList<Term>();
		for (String t : factory.tokenizer(content.toCharArray(), 0, content.length())) {
			String[] wn = t.split(Contants.SLASH);
			if (wn.length != 2) {
				continue;
			}
			Term term = new Term(wn[0], wn[1]);
			terms.add(term);
		}
		return terms;
	}

	@Override
	public List<String> labels() {
		List<NlpGuojiRaw> list = nlpGuojiRawMapper.selectLabels();
		List<String> set = new ArrayList<String>();
		for (NlpGuojiRaw nlpGuojiRaw : list) {
			set.add(nlpGuojiRaw.getLabel());
		}
		return set;
	}

	public String labelFromfileid(String fileid) {
		Example example = new Example(NlpGuojiRaw.class);
		example.createCriteria().andCondition("news_key=", fileid);
		List<NlpGuojiRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getLabel();
	}
	
	public String titleFromfileid(String fileids) {
		Example example = new Example(NlpGuojiRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpGuojiRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getTitle();
	}
}
