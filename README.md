# JAVAZSet
自己实现的java版zset数据结构。

##实现了ZSet的主要功能，可以直接使用。
##实现了二维x,y坐标查找附近的人功能，代码中坐标范围是±10000。x,y分别使用16位二进制表示。geohash位YX组合，一共为32位。geohash转化为zset的score为52位。
可扩展。不要达到64位，因为用long表示，java数据结构都是有符号的。
##Obstacle.java为游戏中使用的一个示例，性能和可靠性完全OJBK。放心食用。
