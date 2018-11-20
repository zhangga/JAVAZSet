package org.jow.core.collect;

/**
 * Sorted set API flags.
 * @author U-Demon
 * @date 2018年11月19日 上午11:46:40
 */
public enum EnumModuleZSet {
	
	/* Input flags. */
	ZADD_NONE(0),
	// INCR表示在原来的score基础上加上新的score，而不是替换。
	ZADD_INCR(1<<0),	/* Increment the score instead of setting it. */
	// XX表示只操作已存在的元素。
	ZADD_NX(1<<1),		/* Don't touch elements not already existing. */
	// NX表示如果元素存在，则不执行替换操作直接返回。
	ZADD_XX(1<<2),		/* Only touch elements already existing. */
	
	/* Output flags. */
	ZADD_NOP(1<<3),		/* Operation not performed because of conditionals.*/
	ZADD_NAN(1<<4),		/* Only touch elements already existing. */
	ZADD_ADDED(1<<5),	/* The element was new and was added. */
	ZADD_UPDATED(1<<6),	/* The element already existed, score updated. */
	
	/* Flags only used by the ZADD command but not by zsetAdd() API: */
	// CH表示返回修改（包括添加，更新）元素的数量，只能被ZADD命令使用。
	ZADD_CH(1<<16),		/* Return num of elements added or updated. */
	;
	
	private int flag;
	
	EnumModuleZSet(int flag) {
		this.flag = flag;
	}

	public int getFlag() {
		return flag;
	}
	
	public static boolean contain(int flags, EnumModuleZSet module) {
		return (flags & module.flag) == module.flag;
	}

}
