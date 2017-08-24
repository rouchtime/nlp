package com.rouchtime.nlp.corpus;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.common.Term;
import com.rouchtime.persistence.model.NlpFinanceNewsNonRaw;

@Component
@Qualifier("financeNewsOrNonCorpus")
public class FinanceNewsOrNonCorpus extends AbstractBaseCorpus<NlpFinanceNewsNonRaw>{

	@Override
	public List<String> fileids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Term> wordFromfileids(String fileids, TokenizerFactory factory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String labelFromfileid(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
