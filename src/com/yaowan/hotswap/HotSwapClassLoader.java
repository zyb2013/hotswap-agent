package com.yaowan.hotswap;

/**
 * class热替换ClassLoader
 * 
 * @author zhuyuanbiao
 *
 * @date 2017年6月1日 上午10:00:06
 */
public class HotSwapClassLoader extends ClassLoader {

	public HotSwapClassLoader() {
		super(Thread.currentThread().getContextClassLoader());
	}

	public Class<?> findClass(byte[] b) throws ClassNotFoundException {
		return defineClass(null, b, 0, b.length);
	}

}
