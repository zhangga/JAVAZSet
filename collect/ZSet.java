package org.jow.core.collect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Redis zset主要功能的JAVA实现
 * 非线程安全
 * <b>注意：E必须重写hashCode和equals方法
 * @author U-Demon
 * @date 2018年11月19日 上午11:41:39
 */
public class ZSet<E extends ZSetEle<K>, K> {
	
	/** 所有的ZSet集合 */
	private static final Map<String, ZSet<?, ?>> all = new HashMap<>();
	
	/**
	 * 通过key获取当前的ZSet
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends ZSetEle<K>, K> ZSet<E, K> lookupKey(String key) {
		return (ZSet<E, K>) all.get(key);
	}
	
	/**
	 * putIfAbsent
	 * @param zset
	 * @return
	 */
	public static ZSet<?, ?> addZSet(ZSet<?, ?> zset) {
		return all.putIfAbsent(zset.key, zset);
	}
	
	/** ZSet的key */
	private String key;
	
	/** 元素和分值的映射 */
	public Map<K, E> dict;
	
	/** 
	 * 线程安全的SkipList。
	 * 由于场景分线一定是线程安全的特性，也可以使用TreeMap。
	 * 另外根据同一分值下元素的多少可以使用List或TreeMap。
	 */
//	private ConcurrentSkipListMap<Long, List<E>> zsl;
	public TreeMap<Long, List<E>> zsl;
	
	public ZSet(String key) {
		this.key = key;
		this.dict = new HashMap<>();
//		this.zsl = new ConcurrentSkipListMap<>();
		this.zsl = new TreeMap<>();
	}
	
	/**
	 * 添加元素
	 * @param ele
	 * @return
	 */
	public int add(E ele) {
		return add(ele.getScore(), ele, false, false, false);
	}
	
	/**
	 * 添加元素
	 * @param score
	 * @param ele
	 * @param nx
	 * @param xx
	 * @param incr
	 * @return
	 */
	public int add(Long score, E ele, boolean nx, boolean xx, boolean incr) {
		E de = dict.get(ele.getKey());
		if (de != null) {
			if (nx) {
				/* NX? Return, same element already exists. */
				return -2;
			}
			
			/* Prepare the score for the increment if needed. */
			if (incr) {
				score += de.getScore();
			}
			
			long oldScore = de.getScore();
			/** 更新skiplist */
			if (score != oldScore) {
				de.setScore(score);
				zslUpdateScore(de, oldScore);
			}
			return 1;
		}
		else if (!xx) {
			dict.put(ele.getKey(), ele);
			List<E> list = zsl.computeIfAbsent(score, v -> new ArrayList<>());
			list.add(ele);
			return 1;
		}
		else {
			return -3;
		}
	}
	
	private void zslUpdateScore(E ele, Long oldScore) {
		zsl.get(oldScore).remove(ele);
		List<E> list = zsl.computeIfAbsent(ele.getScore(), v -> new ArrayList<>());
		list.add(ele);
	}
	
	/**
	 * 通过score区间获取
	 * @param min
	 * @param max
	 * @return
	 */
	public List<E> rangeByScore(long min, long max) {
		List<E> list = new ArrayList<>();
		if (min > max) {
			return list;
		}
		
		List<E> mins = zsl.get(min);
		if (mins != null) {
			list.addAll(mins);
		}
		Long score = zsl.higherKey(min);
		while (score != null && score <= max) {
			List<E> ret = zsl.get(score);
			if (ret != null) {
				list.addAll(ret);
			}
			score = zsl.higherKey(score);
		}
		return list;
	}
	
	/**
	 * 移除
	 * @param id
	 * @return
	 */
	public boolean remove(K id) {
		E ele = dict.remove(id);
		if (ele == null) {
			return true;
		}
		List<E> list = zsl.get(ele.getScore());
		if (list == null) {
			return true;
		}
		return list.remove(ele);
	}

}
