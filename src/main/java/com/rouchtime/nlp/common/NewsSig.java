package com.rouchtime.nlp.common;

import java.util.Set;

public class NewsSig extends News {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3982437137025631621L;

	public NewsSig() {
		super();
	}

	public NewsSig(int[] hash, Set<Integer> vector, News news) {
		super(news.getId(), news.getTitle(), news.getArticle(), news.getUrl());
		this.hash = hash;
		this.vector = vector;
	}
	
	public static class NewsSigBuilder implements Builder<NewsSig>
    {
        // 必须参数
        private final int[] hash;// required
        private final Set<Integer> vector;// required

        // 可选参数
        private String id;
        private String article;
        private String title;
        private String url;

        // 必须参数必须通过通过构造参数传递
        public Builder(int[] hash, Set<Integer> vector)
        {
            this.hash = hash;
            this.vector = vector;
        }

        // 构建calories,返回本身，以便可以把调用连接起来
        public Builder calories(String id)
        {
            this.id = calories;
            return this;
        }

        // 构建sodium
        public Builder sodium(String article)
        {
            this.article = sodium;
            return this;
        }

        // 构建fat
        public Builder fat(String title)
        {
            this.title = fat;
            return this;
        }

        // 构建carbohydrate
        public Builder carbohydrate(String url)
        {
            this.carbohydrate = carbohydrate;
            return this;
        }

        //build,返回NutritionFact3
        public NutritionFact3 build()
        {
            return new NutritionFact3(this);
        }

		@Override
		public NewsSig builder() {
			return null;
		}
    }
	
	
	private int[] hash;
	private Set<Integer> vector;

	public Set<Integer> getVector() {
		return vector;
	}

	public void setVector(Set<Integer> vector) {
		this.vector = vector;
	}

	public int[] getHash() {
		return hash;
	}

	public void setHash(int[] hash) {
		this.hash = hash;
	}
}
