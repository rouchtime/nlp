package com.rouchtime.nlp.corpus;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.persistence.custom.mapper.OtherCommonMapper;
import com.rouchtime.persistence.model.AbstractRaw;
import com.rouchtime.persistence.model.NlpGuojiRaw;
import com.rouchtime.persistence.model.NlpSougouPublicRaw;
import com.rouchtime.util.Contants;
import com.rouchtime.util.RegexUtils;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

/**
 * 语料库的通用类，通过通用mapper得到相应数据
 * 
 * @author 龚帅宾
 *
 * @param <T>
 */
public abstract class AbstractBaseCorpus<T extends AbstractRaw> implements ICorpus {

	@Autowired
	protected Mapper<T> mapper;

	@Override
	public List<String> titles() {
		return null;
	}

	@Override
	public List<String> titlesFromLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> labels() {
		return null;
	}

	@Override
	public Set<String> labelsFromFirstlabel(String firstLabel) {
		return null;
	}
	
	@Override
	public String labelFromTitles(String title) {
		Example example = new Example(getSuperClassGenricType(this.getClass(), 0));
		example.createCriteria().andCondition("title=", title);
		List<T> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getLabel();
	}

	@Override
	public List<String> raw() {
		return null;
	}

	@Override
	public String rawFromTitle(String title) {
		Example example = new Example(getSuperClassGenricType(this.getClass(), 0));
		example.createCriteria().andCondition("title=", title);
		List<T> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		return list.get(0).getContent();
	}

	@Override
	public List<String> rawFromLabel(String label) {
		Example example = new Example(getSuperClassGenricType(this.getClass(), 0));
		example.createCriteria().andCondition("label=", label);
		List<T> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		List<String> returnContentList = new ArrayList<String>();
		for (T t : list) {
			returnContentList.add(t.getContent());
		}
		return returnContentList;
	}

	@Override
	public List<Term> words(TokenizerFactory factory) {
		return null;
	}

	@Override
	public List<Term> wordsFromTitle(String title, TokenizerFactory factory) {
		Example example = new Example(getSuperClassGenricType(this.getClass(), 0));
		example.createCriteria().andCondition("title=", title);
		List<T> list = mapper.selectByExample(example);
		if (null == list || 0 == list.size()) {
			return null;
		}
		String content = list.get(0).getContent();
		List<Term> terms = new ArrayList<Term>();
		for (String t : factory.tokenizer(content.toCharArray(), 0, content.length())) {
			String[] wn = t.split(Contants.SLASH);
			if (wn.length != 2) {
				continue;
			}
			Term term = new Term(wn[0], wn[1]);
			terms.add(term);
		}
		// TODO Auto-generated method stub
		return terms;
	}

	@Override
	public List<Term> wordsFromLabel(String label, TokenizerFactory factory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> sents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> sentsFromTitle(String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> sentsFromLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> fileidFromLabel(String label) {
		Example example = new Example(getSuperClassGenricType(this.getClass(), 0));
		example.createCriteria().andCondition("label=", label);
		List<T> list = mapper.selectByExample(example);
		List<String> fileids = new ArrayList<String>();
		for (T t : list) {
			fileids.add(t.getNewsKey());
		}
		return fileids;
	}
	
	
	public Map<String,List<String>> produceTrainAndTestByRate(double rate,Long seed) {
		List<String> trainFileids = new ArrayList<String>();
		List<String> testFileids = new ArrayList<String>();
		for(String label : labels()) {
			List<String> fileids = fileidFromLabel(label);
			LinkedList<String> list = new LinkedList<String>(fileids);
			int totalSize = fileids.size();
			int trainSize = (int) (totalSize * rate);
			for (int i = 0; i < trainSize; i++) {
				Random r = new Random(seed);
				int rIndex = r.nextInt(totalSize);
				trainFileids.add(list.get(rIndex));
				list.remove(rIndex);
				totalSize--;
			}
			for(String fileid : list) {
				testFileids.add(fileid);
			}
		}
		Map<String,List<String>> mapTrainAndTest = new HashMap<String,List<String>>();
		mapTrainAndTest.put("train", trainFileids);
		mapTrainAndTest.put("test", testFileids);
		return mapTrainAndTest;
	}
	
	public Map<String,List<String>> produceTrainAndTestByRateByLabel(double rate,Long seed,String firstlabel) {
		List<String> trainFileids = new ArrayList<String>();
		List<String> testFileids = new ArrayList<String>();
		for(String label : labelsFromFirstlabel(firstlabel)) {
			List<String> fileids = fileidFromLabel(label);
			LinkedList<String> list = new LinkedList<String>(fileids);
			int totalSize = fileids.size();
			int trainSize = (int) (totalSize * rate);
			for (int i = 0; i < trainSize; i++) {
				Random r = new Random(seed);
				int rIndex = r.nextInt(totalSize);
				trainFileids.add(list.get(rIndex));
				list.remove(rIndex);
				totalSize--;
			}
			for(String fileid : list) {
				testFileids.add(fileid);
			}
		}
		Map<String,List<String>> mapTrainAndTest = new HashMap<String,List<String>>();
		mapTrainAndTest.put("train", trainFileids);
		mapTrainAndTest.put("test", testFileids);
		return mapTrainAndTest;
	}
	
	/**
	 * 根据类别，返回泛型T的类型，提供给通用mapper使用
	 * 
	 * @param clazz
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<Object> getSuperClassGenricType(final Class clazz, final int index) {

		// 返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		// 返回表示此类型实际类型参数的 Type 对象的数组。
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}

		return (Class) params[index];
	}
}
