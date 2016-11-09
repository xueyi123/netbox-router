package com.iih5.router.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;

public class LogUtil {
	static {
		try {
			Properties prop = new Properties();
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("log4j.properties"));
			PropertyConfigurator.configure(prop);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Logger logger = Logger.getLogger(LogUtil.class);
	public static void init() {
		 logger.info("Logger Init");
	}
}
