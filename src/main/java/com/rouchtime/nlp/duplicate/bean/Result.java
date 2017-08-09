package com.rouchtime.nlp.duplicate.bean;

public class Result {
	private DuplicateBean duplicateBean;
	

	public DuplicateBean getDuplicateBean() {
		return duplicateBean;
	}

	public void setDuplicateBean(DuplicateBean duplicateBean) {
		this.duplicateBean = duplicateBean;
	}

	private double similariy;

	public double getSimilariy() {
		return similariy;
	}

	public void setSimilariy(double similariy) {
		this.similariy = similariy;
	}

	@Override
	public String toString() {
		return "Result [Id=" + duplicateBean.getId() + ", similariy=" + similariy + "]";
	}

	
	
}
