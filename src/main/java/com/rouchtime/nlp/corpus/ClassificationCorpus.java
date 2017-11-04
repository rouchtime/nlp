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
import com.rouchtime.persistence.dao.NlpClassificationRawMapper;
import com.rouchtime.persistence.model.NlpClassificationRaw;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;

import tk.mybatis.mapper.entity.Example;

@Component
@Qualifier("classificationCorpus")
public class ClassificationCorpus extends AbstractBaseCorpus<NlpClassificationRaw> {
	@Autowired
	private NlpClassificationRawMapper nlpClassificationRawMapper;

	public List<String> fileids() {
		List<NlpClassificationRaw> list = nlpClassificationRawMapper.selectNewsKeys();
		List<String> fileids = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			fileids.add(nlpClassificationRaw.getNewsKey());
		}
		return fileids;
	}

	public String rawFromfileids(String fileids) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getContent();
	}

	public List<String> fileidFromLabel(String label) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("label=", label);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			fileids.add(nlpClassificationRaw.getNewsKey());
		}
		return fileids;
	}

	public List<String> fileidFromThirLabelAndFirstLabel(String label, String first_label) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", first_label).andCondition("third_label=", label);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			fileids.add(nlpClassificationRaw.getNewsKey());
		}
		return fileids;
	}

	public List<String> fileidFromSecondLabelAndFirstLabel(String second_label, String first_label) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", first_label).andCondition("second_label=", second_label);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			fileids.add(nlpClassificationRaw.getNewsKey());
		}
		return fileids;
	}

	public List<Term> wordFromfileids(String fileids, TokenizerFactory factory) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
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
		List<NlpClassificationRaw> list = nlpClassificationRawMapper.selectLabels();
		List<String> set = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			set.add(nlpClassificationRaw.getLabel());
		}
		return set;
	}

	public List<String> secondLabels() {
		List<NlpClassificationRaw> list = nlpClassificationRawMapper.selectSecondLabels();
		List<String> set = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			set.add(nlpClassificationRaw.getSecondLabel());
		}
		return set;
	}

	public List<String> thirdLabels() {
		List<NlpClassificationRaw> list = nlpClassificationRawMapper.selectThirdLabels();
		List<String> set = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			set.add(nlpClassificationRaw.getThirdLabel());
		}
		return set;
	}

	public List<String> fileidFromSecondLabel(String secondLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("second_label=", secondLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			fileids.add(nlpClassificationRaw.getNewsKey());
		}
		return fileids;
	}

	public Set<String> labelsFromFirstlabel(String firstLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", firstLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		Set<String> labels = new HashSet<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			labels.add(nlpClassificationRaw.getLabel());
		}
		return labels;
	}

	public Set<String> secondlabelsFromFirstlabel(String firstLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", firstLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		Set<String> labels = new HashSet<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			labels.add(nlpClassificationRaw.getSecondLabel());
		}
		return labels;
	}

	public Set<String> thridlabelsFromFirstlabel(String firstLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", firstLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		Set<String> labels = new HashSet<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			labels.add(nlpClassificationRaw.getThirdLabel());
		}
		return labels;
	}

	public Set<String> thridlabelsFromFirstlabelAndSecondLabel(String firstLabel, String secondLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", firstLabel).andCondition("second_label=", secondLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		Set<String> labels = new HashSet<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			labels.add(nlpClassificationRaw.getThirdLabel());
		}
		return labels;
	}

	public Set<String> labelsFromSecondlabelAndFirstLabel(String secondLabel,String firstLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("second_label=", secondLabel).andCondition("first_label=", firstLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		Set<String> labels = new HashSet<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			labels.add(nlpClassificationRaw.getLabel());
		}
		return labels;
	}

	public Set<String> thirdlabelsFromSecondlabel(String secondLabel,String firstLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("second_label=", secondLabel).andCondition("first_label=", firstLabel);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		Set<String> labels = new HashSet<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			labels.add(nlpClassificationRaw.getThirdLabel());
		}
		return labels;
	}
	
	public List<String> fileidFromFirstLabel(String first) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", first);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (NlpClassificationRaw nlpClassificationRaw : list) {
			fileids.add(nlpClassificationRaw.getNewsKey());
		}
		return fileids;
	}

	public String labelFromfileid(String fileid) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("news_key=", fileid);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getLabel();
	}

	public String titleFromfileid(String fileids) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("news_key=", fileids);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getTitle();
	}

	public String thirdLabelFromFileid(String fileid) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("news_key=", fileid);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getThirdLabel();
	}

	public String secondLabelFromThirdLabel(String label, String firstLabel) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("first_label=", firstLabel).andCondition("third_label=", label);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getSecondLabel();
	}

	public String secondLabelByFileid(String newsKey) {
		Example example = new Example(NlpClassificationRaw.class);
		example.createCriteria().andCondition("news_key=", newsKey);
		List<NlpClassificationRaw> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getSecondLabel();
	}
}
