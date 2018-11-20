package org.jow.core.collect;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code ZSet}的工具类。
 * 非线程安全
 * 先只实现单个score(int)/member版本。
 * @author U-Demon
 * @date 2018年11月19日 上午11:51:43
 */
public class ZSets {
	
	/**
	 * zaddCommand
	 */
	public static <E extends ZSetEle<K>, K> void zadd(String key, long score, E member) {
		member.setScore(score);
		zadd(key, member);
	}
	
	public static <E extends ZSetEle<K>, K> void zadd(String key, E member) {
		zadd(key, false, false, false, false, member);
	}
	
	public static <E extends ZSetEle<K>, K> void zadd(String key, boolean nx, E member) {
		zadd(key, nx, false, false, false, member);
	}
	
	public static <E extends ZSetEle<K>, K> void zadd(String key, boolean nx, boolean xx, E member) {
		zadd(key, nx, xx, false, false, member);
	}
	
	public static <E extends ZSetEle<K>, K> void zadd(String key, boolean nx, boolean xx, boolean ch, E member) {
		zadd(key, nx, xx, ch, false, member);
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends ZSetEle<K>, K> void zadd(String key, boolean nx, boolean xx, boolean ch, boolean incr, E member) {
		ZSetEle<K>[] members = new ZSetEle[] {member};
		zaddGenericCommand(key, nx, xx, ch, incr, members);
	}
	
	public static <E extends ZSetEle<K>, K> void zadd(String key, E[] members) {
		zaddGenericCommand(key, false, false, false, false, members);
	}
	
	/**
	 * @param key
	 * @param flags		枚举标志位合集
	 * @param score
	 * @param member
	 */
	public static <E extends ZSetEle<K>, K> void zadd(String key, int flags, E[] member) {
		zaddGenericCommand(key, flags, member);
	}
	
	/**
	 * zincrbyCommand
	 */
	public static <E extends ZSetEle<K>, K> void zincrby(String key, E[] members) {
		zaddGenericCommand(key, false, false, false, true, members);
	}
	
	private static <E extends ZSetEle<K>, K> int zaddGenericCommand(String key, int flags, E[] member) {
		boolean nx = EnumModuleZSet.contain(flags, EnumModuleZSet.ZADD_NX);
		boolean xx = EnumModuleZSet.contain(flags, EnumModuleZSet.ZADD_XX);
		boolean ch = EnumModuleZSet.contain(flags, EnumModuleZSet.ZADD_CH);
		boolean incr = EnumModuleZSet.contain(flags, EnumModuleZSet.ZADD_INCR);
		return zaddGenericCommand(key, nx, xx, ch, incr, member);
	}
	
	/**
	 * Ehis generic command implements both ZADD and ZINCRBY.
	 * @param key
	 * 			ZSet的key
	 * @param nx
	 * @param xx
	 * @param ch
	 * @param incr
	 * @param scores
	 * @param members
	 * @return
	 */
	private static <E extends ZSetEle<K>, K> int zaddGenericCommand(String key, boolean nx, boolean xx, 
			boolean ch, boolean incr, E[] members) {
		// 获取对应的集合
		ZSet<E, K> zset = ZSet.lookupKey(key);
		if (zset == null) {
			if (xx) return -3;
			// 这里线程不安全
			zset = new ZSet<E, K>(key);
			ZSet.addZSet(zset);
		}
		
		int result = 0;
		// 依次加入ZSet
		for (int i = 0; i < members.length; ++i) {
			E ele = members[i];
			if (zset.add(ele.getScore(), ele, nx, xx, incr) > 0) {
				++result;
			}
		}
		return result;
	}
	
	/**
	 * 通过分数区间获取列表
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static <E extends ZSetEle<K>, K> List<E> zrangebyscore(String key, long min, long max) {
		ZSet<E, K> zset = ZSet.lookupKey(key);
		if (zset == null) {
			return new ArrayList<>();
		}
		
		return zset.rangeByScore(min, max);
	}
	
	/**
	 * 移除
	 * @param key
	 * @param id
	 */
	public static <E extends ZSetEle<K>, K> boolean zrem(String key, K id) {
		ZSet<E, K> zset = ZSet.lookupKey(key);
		if (zset == null) {
			return true;
		}
		
		return zset.remove(id);
	}

}
