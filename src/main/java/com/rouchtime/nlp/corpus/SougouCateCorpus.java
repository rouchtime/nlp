package com.rouchtime.nlp.corpus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.persistence.dao.NlpSougouPublicRawMapper;
import com.rouchtime.persistence.model.NlpGuojiRaw;
import com.rouchtime.persistence.model.NlpSougouPublicRaw;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;

import tk.mybatis.mapper.entity.Example;

@Component
@Qualifier("sougouCateCorpus")
public class SougouCateCorpus extends AbstractBaseCorpus<NlpSougouPublicRaw> {

	@Autowired
	private NlpSougouPublicRawMapper nlpSougouPublicRawMapper;

	public List<String> fileids() {
		List<NlpSougouPublicRaw> list = nlpSougouPublicRawMapper.selectNewsKeys();
		List<String> fileids = new ArrayList<String>();
		for (NlpSougouPublicRaw nlpSougouPublicRaw : list) {
			fileids.add(nlpSougouPublicRaw.getNewsKey());
		}
		return fileids;
	}

	public String rawFromfileids(String fileids) {
		Example example = new Example(NlpSougouPublicRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpSougouPublicRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getContent();
	}

	public String titleFromfileids(String fileids) {
		Example example = new Example(NlpSougouPublicRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpSougouPublicRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getTitle();
	}
	
	public List<Term> wordFromfileids(String fileids, TokenizerFactory factory) {
		Example example = new Example(NlpSougouPublicRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpSougouPublicRaw> list = mapper.selectByExample(example);
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
		List<NlpSougouPublicRaw> list = nlpSougouPublicRawMapper.selectLabels();
		List<String> set = new ArrayList<String>();
		for (NlpSougouPublicRaw nlpSougouPublicRaw : list) {
			set.add(nlpSougouPublicRaw.getLabel());
		}
		return set;
	}

	public String labelFromfileid(String fileid) {
		Example example = new Example(NlpSougouPublicRaw.class);
		example.createCriteria().andCondition("news_key=", fileid);
		List<NlpSougouPublicRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getLabel();
	}
}
