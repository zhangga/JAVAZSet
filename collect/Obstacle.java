package org.jow.worldsrv.support.pathFinding;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jow.common.support.Vector2D;
import org.jow.common.support.Vector3D;
import org.jow.core.collect.ZSet;
import org.jow.core.collect.ZSetEle;
import org.jow.core.collect.ZSets;
import org.jow.worldsrv.support.area.AbstractArea;
import org.jow.worldsrv.support.area.EnumAreaType;

/**
 * 障碍物
 *
 * @author U-Demon
 */
public class Obstacle extends ZSetEle<Long> {
	
	/** 对应WorldObject的ID */
	private long id;
	
	/** 区域 */
	private AbstractArea area;
	
	/**
	 * 构造函数
	 * @param id		worldObj.id
	 * @param areaType	EnumAreaType
	 * @param param		float[]
	 * @param pos		Vector3D
	 */
	public Obstacle(long id, int areaType, float[] param, Vector3D pos) {
		this.id = id;
		this.area = AbstractArea.createArea(areaType, param, pos, -10087);
		Vector2D center = this.area.getCenter();
		this.score = ZSets.calcScore(center.x, center.y);
	}
	
	@Override
	public Long getKey() {
		return this.id;
	}
	
	/**
	 * 判断点是否在障碍物内
	 * @param pos
	 * @return
	 */
	public boolean inObstacle(Vector3D pos) {
		return false;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Obstacle other = (Obstacle) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("score", score)
				.append("area", area)
				.toString();
	}

	public long getId() {
		return id;
	}

	public AbstractArea getArea() {
		return area;
	}
	
	/**
	 * 性能测试
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("logFileName", "world" + 0);
//		ZSet<Obstacle, Long> obstacle = new ZSet<>(null);
//		for (int i = 0; i < 1000000; ++i) {
//			Obstacle ob = new Obstacle(i, EnumAreaType.AREA_CIRCLE.getIndex(), new float[] {1}, Vector3D.ZERO);
//			obstacle.add(ob);
//		}
//		System.out.println("总个数："+obstacle.dict.size());
//		System.out.println("分数总值："+obstacle.zsl.size());
//		long curr = System.currentTimeMillis();
//		for (int i = 0; i < 10000; ++i) {			
//			List<Obstacle> list = obstacle.rangeByScore(1000, 3000);
//		}
//		System.out.println("用时：" + (System.currentTimeMillis() - curr));
//		long hash = ZSets.geohashEncode(8750, 10, 16);
//		System.out.println("hash == " + hash);
//		long[] s = ZSets.geohashNeighbors(hash, 16);
//		for (int i = 0; i < s.length; i++) {
//			System.out.println(s[i]);
//		}
		
//		long hash = ZSets.geohashEncode(8750, 10, 16);
//		double[] d = ZSets.geohashDecode(hash, 16);
//		for (double dd:d) {
//			System.out.println(dd);
//		}
		
//		ZSets.GeoArea areas = ZSets.geohashGetAreasByRadius(8750, 10, 8000);
//		System.out.println(areas);
//		
//		long geohash = ZSets.geohashEncode(8750, 10, 16);
//		System.out.println(geohash);
//		long[] ss = ZSets.scoresOfGeoHashBox(geohash, 16);
//		System.out.println(ss[0] + ", " + ss[1]);
		
		ZSet<Obstacle, Long> zset = new ZSet<>(null);
		float[] param = new float[] {1};
		Obstacle ob1 = new Obstacle(1, EnumAreaType.AREA_CIRCLE.getIndex(), param, new Vector3D(0, 0, 0));
		zset.add(ob1);
		System.out.println(ob1);
		
		
		List<Obstacle> list = zset.georadius(2, 2, 1);
		System.out.println(list);
		list.forEach(e -> System.out.println(e.getArea().getCenter()));
	}

}
