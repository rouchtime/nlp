package com.rouchtime.nlp.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rouchtime.nlp.model.NlpFinanceNewsNonRaw;
import com.rouchtime.nlp.service.AbstractBaseCorpusService;

@Component
@Qualifier("financeNewsOrNonCorpus")
public class FinanceNewsOrNonCorpus extends AbstractBaseCorpusService<NlpFinanceNewsNonRaw> implements ICorpus {

	private NlpFinanceNewsNonRaw nlpFinanceNewsNonRaw;

	@Override
	public List<String> titles() {
		List<String> list = new ArrayList<String>();
		for(NlpFinanceNewsNonRaw raw :super.getRawList()) {
			list.add(raw.getTitle());
		}
		return list;
	}

	@Override
	public List<String> titlesFromLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> labels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String labelFromTitles(String Title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> raw() {
		List<String> list = new ArrayList<String>();
		for(NlpFinanceNewsNonRaw raw :super.getRawList()) {
			list.add(raw.getContent());
		}
		return list;
	}

	@Override
	public String rawFromTitle(String title) {
		nlpFinanceNewsNonRaw = new NlpFinanceNewsNonRaw();
		nlpFinanceNewsNonRaw.setTitle(title);
		NlpFinanceNewsNonRaw result = super.getRawFromTitle(nlpFinanceNewsNonRaw);
		return result.getContent();
	}

	@Override
	public List<String> rawFromLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> words() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> wordsFromTitle(String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> wordsFromLabel(String label) {
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

}
