package com.yiba.core.collect;

import java.util.ArrayList;
import java.util.List;

import org.junit.platform.commons.util.ToStringBuilder;

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
	
	
	
	public static final boolean DEBUG_MSG = false;
	//-=-=-=-=-=-=-=-=-=-=-=-=GEOHash相关-=-=-=-=-=-=-=-=-=-=-=-=//
	//		左区间	        中值			      右区间		                 位数                    m error            左区间二进制0 右区间二进制1
	//	   -10000       0      		  10000				1         14143
	//        0        5000     	  10000				2         7072
	//        0        2500     	  5000				3         3536
	//        0        1250     	  2500				4		  1768
	//        0        625      	  1250				5         884
	//        0        312.5    	  625				6         442
	//        0        156.25   	  312.5				7		  221
	//        0        78.125         156.25			8		  111
	//        0        39.0625        78.125			9		  56
	//        0        19.53125       39.0625			10		  28
	//        0        9.765625       19.53125			11        14
	//        0        4.8828125	  9.765625			12		  7
	//        0        2.44140625     4.8828125			13        4
	//        0        1.220703125    2.44140625		14        2
	//        0        0.6103515625   1.220703125		15        1
	//        0        0.30517578125  0.6103515625		16        0.5
	private static final double GEO_X_MAX = 10000;
	private static final double GEO_X_MIN = -10000;
	private static final double GEO_Y_MAX = 10000;
	private static final double GEO_Y_MIN = -10000;
	
	/** GEO中为地球周长的一半，这里应该是矩形对角线的长，还是长宽中的长？ 
	 * 不同bits下可查找精度的范围。较小值，保证这个精度一定可找到。*/
	private static final double MERCATOR_MAX = 10000;
	
	/* 16*2 = 32 bits. */
	private static final int GEO_STEP_MAX = 16;
	
	/**
	 * 计算分值
	 * @return
	 */
	public static long calcScore(double x, double y) {
		long geohash = geohashEncode(x, y, GEO_STEP_MAX);
		return geohashAlign52Bits(geohash, GEO_STEP_MAX);
	}
	
	/**
	 * 根据step（bits精度）计算geohash值
	 * @param x
	 * @param y
	 * @param step
	 * @return
	 */
	public static long geohashEncode(double x, double y, int step) {
		if (step > 32 || step <= 0) {
			return 0;
		}
		if (x < GEO_X_MIN || x > GEO_X_MAX || y < GEO_Y_MIN || y > GEO_Y_MAX) {
			return 0;
		}
		
		double x_offset = (x - GEO_X_MIN) / (GEO_X_MAX - GEO_X_MIN);
		double y_offset = (y - GEO_Y_MIN) / (GEO_Y_MAX - GEO_Y_MIN);
		
		long xlo = (long) (x_offset * (1L << step));
		long ylo = (long) (y_offset * (1L << step));
		
		return interleave64(xlo, ylo);
	}
	
	/** 位移数组 */
	private static final long B[] = {0x5555555555555555L, 0x3333333333333333L,
            0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL,
            0x0000FFFF0000FFFFL};
	private static final int S[] = {1, 2, 4, 8, 16};
	
	private static long interleave64(long xlo, long ylo) {
		xlo = (xlo | (xlo << S[4])) & B[4];
		ylo = (ylo | (ylo << S[4])) & B[4];
		
		xlo = (xlo | (xlo << S[3])) & B[3];
		ylo = (ylo | (ylo << S[3])) & B[3];
		
		xlo = (xlo | (xlo << S[2])) & B[2];
		ylo = (ylo | (ylo << S[2])) & B[2];
		
		xlo = (xlo | (xlo << S[1])) & B[1];
		ylo = (ylo | (ylo << S[1])) & B[1];
		
		xlo = (xlo | (xlo << S[0])) & B[0];
		ylo = (ylo | (ylo << S[0])) & B[0];
		
		return xlo | (ylo << 1);
	}
	
	private static final long DB[] = {0x5555555555555555L, 0x3333333333333333L,
            0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL,
            0x0000FFFF0000FFFFL, 0x00000000FFFFFFFFL};
	private static final int DS[] = {0, 1, 2, 4, 8, 16};
	
	/**
	 * hash = [LAT][LONG]
	 * @param interleaved
	 * @return
	 */
	private static long deinterleave64(long interleaved) {
		long x = interleaved;
		long y = interleaved >> 1;
		
		x = (x | (x >> DS[0])) & DB[0];
	    y = (y | (y >> DS[0])) & DB[0];

	    x = (x | (x >> DS[1])) & DB[1];
	    y = (y | (y >> DS[1])) & DB[1];

	    x = (x | (x >> DS[2])) & DB[2];
	    y = (y | (y >> DS[2])) & DB[2];

	    x = (x | (x >> DS[3])) & DB[3];
	    y = (y | (y >> DS[3])) & DB[3];

	    x = (x | (x >> DS[4])) & DB[4];
	    y = (y | (y >> DS[4])) & DB[4];

	    x = (x | (x >> DS[5])) & DB[5];
	    y = (y | (y >> DS[5])) & DB[5];
	    
	    return x | (y << 32);
	}
	
	public static double[] geohashDecode(long hash, int step) {
		double[] area = new double[4];
		
		long hash_sep = deinterleave64(hash);
		long ixo = hash_sep & 0x00000000FFFFFFFFL; 	// 原X的hash
		long iyo = hash_sep >> 32;					// 原Y的hash
	    
		double x_scale = GEO_X_MAX - GEO_X_MIN;
		double y_scale = GEO_Y_MAX - GEO_Y_MIN;
		
		area[0] = GEO_X_MIN + (ixo * 1.0d / (1L << step)) * x_scale;		//x min
		area[1] = GEO_X_MIN + ((ixo + 1) * 1.0d / (1L << step)) * x_scale;	//x max
		area[2] = GEO_Y_MIN + (iyo * 1.0d / (1L << step)) * y_scale;		//y min
		area[3] = GEO_Y_MIN + ((iyo + 1) * 1.0d / (1L << step)) * y_scale;	//y max
		return area;
	}
	
	/**
	 * 通过半径估算step
	 * @param range_meters
	 * @return
	 */
	public static int geohashEstimateStepsByRadius(double range_meters) {
//		if (Math.abs(range_meters - 0) < 1.0e-6) {
		if (range_meters == 0) {
			return GEO_STEP_MAX;
		}
		
		int step = 0;
		while (range_meters < MERCATOR_MAX && step < GEO_STEP_MAX) {
			range_meters *= 2;
			step++;
		}
		
		if (step < 1) {
			step = 1;
		}
		return step;
	}
	
	/**
	 * 获取临近8个方向+自己的geohash
	 * -------------
	 * | 2 | 5 | 8 |
	 * -------------
	 * | 1 | 4 | 7 |
	 * -------------
	 * | 0 | 3 | 6 |
	 * -------------
	 * @param geohash
	 * @param step
	 * @return
	 */
	public static long[] geohashNeighbors(long geohash, int step) {
		// SW, W, NW, S, CENTER, N, SE, E, NE
		long[] neighbors = new long[9];
		int index = 0;
		for (int x = -1; x <= 1; ++x) {
			for (int y = -1; y <= 1; ++y) {
				if (x == 0 && y == 0) {
					neighbors[index++] = geohash;
				}
				else {
					long hash = geohash;
					hash = geohash_move_x(hash, x, step);
					hash = geohash_move_y(hash, y, step);
					neighbors[index++] = hash;
				}
			}
		}
		
		return neighbors;
	}
	
	private static long geohash_move_x(long geohash, int d, int step) {
		if (d == 0)
			return geohash;
		
		long x = geohash & 0x5555555555555555L;
		long y = geohash & 0xaaaaaaaaaaaaaaaaL;
		long zz = 0xaaaaaaaaaaaaaaaaL >> (64 - step * 2);
		
		if (d > 0) {
			x = x + (zz + 1);
		}
		else {
			x = x | zz;
	        x = x - (zz + 1);
		}
		
		x &= (0x5555555555555555L >> (64 - step * 2));
		
		geohash = (x | y);
		return geohash;
	}
	
	private static long geohash_move_y(long geohash, int d, int step) {
		if (d == 0)
			return geohash;
		
		long x = geohash & 0x5555555555555555L;
		long y = geohash & 0xaaaaaaaaaaaaaaaaL;
		long zz = 0x5555555555555555L >> (64 - step * 2);
		
		if (d > 0) {
			y = y + (zz + 1);
		}
		else {
			y = y | zz;
	        y = y - (zz + 1);
		}
		
		y &= (0xaaaaaaaaaaaaaaaaL >> (64 - step * 2));
		
		geohash = (x | y);
		return geohash;
	}
	
	/**
	 * 计算两点间的距离
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double geohashGetDistance(double x1, double y1, double x2, double y2) {
		double x_offset = x2 - x1;
		double y_offset = y2 - y1;
		return Math.sqrt(x_offset * x_offset + y_offset * y_offset);
	}
	
	/**
	 * 中心点，半径包围盒的大小
	 * @param x
	 * @param y
	 * @param radius_meters
	 * @return
	 */
	private static double[] geohashBoundingBox(double x, double y, double radius_meters) {
		double[] bounds = new double[4];
		bounds[0] = x - radius_meters;
		bounds[1] = x + radius_meters;
		bounds[2] = y - radius_meters;
		bounds[3] = y + radius_meters;
		return bounds;
	}
	
	/**
	 * 通过半径获取搜索区域
	 * @param x
	 * @param y
	 * @param radius_meters
	 */
	public static GeoArea geohashGetAreasByRadius(double x, double y, double radius_meters) {
		// 半径对应的包围盒
		double[] bounds = geohashBoundingBox(x, y, radius_meters);
		// 预估bits step
		int steps = geohashEstimateStepsByRadius(radius_meters);
		
		// 中心点对应的hash
		long geohash = geohashEncode(x, y, steps);
		// 临近的hash
		long[] geohashNeighbors = geohashNeighbors(geohash, steps);
		// 中心点对应的区域
		double[] area = geohashDecode(geohash, steps);
		
		/* 检查预估的steps是否覆盖到了要搜索的区域。
		 * 向东南西北四个方向检查距离是否满足，如果中心点临近区域的边界，预估的steps不能覆盖要搜索的区域。
		 */
		boolean descrease_step = false;
		{
			double[] areaN = geohashDecode(geohashNeighbors[5], steps);
			double[] areaS = geohashDecode(geohashNeighbors[3], steps);
			double[] areaE = geohashDecode(geohashNeighbors[7], steps);
			double[] areaW = geohashDecode(geohashNeighbors[1], steps);
			
			if (!descrease_step && geohashGetDistance(x, y, x, areaN[3]) < radius_meters) 
				descrease_step = true;
			if (!descrease_step && geohashGetDistance(x, y, x, areaS[2]) < radius_meters) 
				descrease_step = true;
			if (!descrease_step && geohashGetDistance(x, y, areaE[1], y) < radius_meters) 
				descrease_step = true;
			if (!descrease_step && geohashGetDistance(x, y, areaW[0], y) < radius_meters) 
				descrease_step = true;
		}
		
		// 需要减少step，即增大搜索范围
		if (steps > 1 && descrease_step) {
			steps--;
			geohash = geohashEncode(x, y, steps);
			geohashNeighbors = geohashNeighbors(geohash, steps);
			area = geohashDecode(geohash, steps);
		}
		
		// 排除外围超出范围不需要搜索的区域
		if (steps >= 2) {
			// x_min < bounds_x
			if (area[0] < bounds[0]) {
				geohashNeighbors[0] = -1;
				geohashNeighbors[1] = -1;
				geohashNeighbors[2] = -1;
			}
			// x_max > bounds_x
			if (area[1] > bounds[1]) {
				geohashNeighbors[6] = -1;
				geohashNeighbors[7] = -1;
				geohashNeighbors[8] = -1;
			}
			// y_min < bounds_y
			if (area[2] < bounds[2]) {
				geohashNeighbors[0] = -1;
				geohashNeighbors[3] = -1;
				geohashNeighbors[6] = -1;
			}
			// y_max > bounds_y
			if (area[3] > bounds[3]) {
				geohashNeighbors[2] = -1;
				geohashNeighbors[5] = -1;
				geohashNeighbors[8] = -1;
			}
		}
		
		return new GeoArea(steps, geohash, area, geohashNeighbors);
	}
	
	/**
	 * geohash->score_min,score_max
	 * @param geohash
	 * @param step
	 * @return
	 */
	public static long[] scoresOfGeoHashBox(long geohash, int step) {
		long[] scores = new long[2];
		// min
		scores[0] = geohashAlign52Bits(geohash, step);
		geohash++;
		// max
		scores[1] = geohashAlign52Bits(geohash, step);
		return scores;
	}
	
	/**
	 * 自定义的geohash为16+16=32位，转化为zset的score，用52位表示。
	 * @param geohash
	 * @param step
	 * @return
	 */
	public static long geohashAlign52Bits(long geohash, int step) {
		geohash <<= (52 - step * 2);
		return geohash;
	}
	
	public static class GeoArea {
		public int steps;
		public long geohash;
		public double[] area;
		public long[] geohashNeighbors;
		
		public GeoArea(int steps, long geohash, double[] area, long[] geohashNeighbors) {
			this.steps = steps;
			this.geohash = geohash;
			this.area = area;
			this.geohashNeighbors = geohashNeighbors;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("steps", steps)
					.append("geohash", geohash)
					.append("area", area)
					.append("geohashNeighbors", geohashNeighbors)
					.toString();
		}
	}

}
