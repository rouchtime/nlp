package com.rouchtime.nlp.common;

import java.util.Arrays;
import java.util.Set;

public class NewsSig extends News {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3982437137025631621L;

	public NewsSig() {
		super();
	}

	@Override
	public String toString() {
		return "NewsSig [hash=" + Arrays.toString(hash) + ", vector=" + vector + ", toString()=" + super.toString()
				+ "]";
	}

	private int[] hash;
	private Set<Integer> vector;
	
	public int[] getHash() {
		return hash;
	}

	public Set<Integer> getVector() {
		return vector;
	}

	public void setHash(int[] hash) {
		this.hash = hash;
	}

	public void setVector(Set<Integer> vector) {
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
        private News news;

        public NewsSigBuilder(int[] hash, Set<Integer> vector)
        {
            this.hash = hash;
            this.vector = vector;
        }

        public NewsSigBuilder news(News news) {
            this.news = news;
            return this;
        }
        
        public NewsSigBuilder id(String id)
        {
            this.id = id;
            return this;
        }

        public NewsSigBuilder article(String article)
        {
            this.article = article;
            return this;
        }

        public NewsSigBuilder title(String title)
        {
            this.title = title;
            return this;
        }

        public NewsSigBuilder url(String url)
        {
            this.url = url;
            return this;
        }

		@Override
		public NewsSig builder() {
			return new NewsSig(this);
		}
    }
	
    private NewsSig(NewsSigBuilder builder)
    {
    	hash = builder.hash;
    	vector = builder.vector;
    	if(builder.article!=null) {
    		super.setArticle(builder.article);
    	}
    	if(builder.title!=null) {
    		super.setArticle(builder.title);
    	}
    	if(builder.url!=null) {
    		super.setArticle(builder.url);
    	}
    	if(builder.id!=null) {
    		super.setArticle(builder.id);
    	}
    }
}
