package com.fileup.bitsyntax.function;

/**© 2015-2017 Chenxj Copyright
 * 类    名：ToBytes
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年9月16日-下午2:19:01
 */
@FunctionalInterface
public interface ToBytes{
	public byte[] apply(Object o,Integer l);
}
