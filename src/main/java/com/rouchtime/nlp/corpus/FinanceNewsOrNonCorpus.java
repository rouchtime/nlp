package com.rouchtime.nlp.corpus;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rouchtime.persistence.model.NlpFinanceNewsNonRaw;

@Component
@Qualifier("financeNewsOrNonCorpus")
public class FinanceNewsOrNonCorpus extends AbstractBaseCorpus<NlpFinanceNewsNonRaw>{
	
}
