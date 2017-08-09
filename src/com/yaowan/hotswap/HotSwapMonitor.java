package com.yaowan.hotswap;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.internal.org.objectweb.asm.ClassReader;

/**
 * 热替换任务
 * 
 * @author zhuyuanbiao
 *
 * @date 2017年6月1日 上午10:03:21
 */
public class HotSwapMonitor implements Runnable {

	/** 要监视的目录 */
	private String classPath;

	private Instrumentation instrumentation;

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private int interval;

	private static final Logger logger = Logger.getLogger(HotSwapMonitor.class.getName());

	public HotSwapMonitor(Instrumentation instrumentation, String classPath, int interval) {
		this.instrumentation = instrumentation;
		this.classPath = classPath;
		this.interval = interval;
	}

	public void start() {
		logger.info("HotSwapMonitor start...");
		executor.scheduleAtFixedRate(this, 0, interval, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			scanClassFile();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "HotSwapMonitor error", e);
		}
	}

	/**
	 * 扫描class文件
	 */
	public void scanClassFile() throws Exception {
		File path = new File(classPath);
		File[] files = path.listFiles();
		if (files == null) {
			return;
		}
		String classFilePath = null;
		boolean success = false;
		long now = System.currentTimeMillis();
		for (File file : files) {
			if (!isClassFile(file)) {
				continue;
			}
			classFilePath = file.getPath();
			reloadClass(classFilePath);
			logger.fine(String.format("Reload %s success", classFilePath));
			file.delete();
			success = true;
		}
		if (success) {
			logger.fine(String.format("Reload success, cost time:%sms", (System.currentTimeMillis() - now)));
		}
	}

	/**
	 * 重新加载class
	 * 
	 * @param classFilePath
	 */
	private void reloadClass(String classFilePath) throws Exception {
		DataInputStream in = null;
		try {
			File file = new File(classFilePath);
			byte[] buff = new byte[(int) file.length()];
			in = new DataInputStream(new FileInputStream(file));
			in.readFully(buff);
			ClassReader cr = new ClassReader(buff);
			ClassDefinition definition = new ClassDefinition(Class.forName(cr.getClassName()), buff);
			instrumentation.redefineClasses(new ClassDefinition[] { definition });
		} catch (Exception e) {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * 是否class文件
	 * 
	 * @param file
	 * @return
	 */
	private boolean isClassFile(File file) {
		return file.getName().contains(".class");
	}

}
