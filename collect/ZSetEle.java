package com.yiba.core.collect;

/**
 * ZSet元素的基类
 * @author U-Demon
 * @date 2018年11月20日 上午11:25:24
 * @param <K>
 */
public abstract class ZSetEle<K> {
	
	/** 分值 */
	protected long score;
	
	/**
	 * 元素的key值，唯一标识
	 * @return
	 */
	public abstract K getKey();
	
	/**
	 * 分值
	 * @return
	 */
	public long getScore() {
		return this.score;
	}
	
	public void setScore(long score) {
		this.score = score;
	}

}
