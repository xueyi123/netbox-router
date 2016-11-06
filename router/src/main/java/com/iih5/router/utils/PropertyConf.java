/**
* ---------------------------------------------------------------------------
* 类名称   ：PropertyConf.java
* 类描述   ： 配置文件操作类
* 创建人   ： 薛一
* 创建时间： 2016年5月22日 下午4:00:11
* 版权拥有：银信网银科技有限公司
* ---------------------------------------------------------------------------
*/
package com.iih5.router.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyConf {
	private static Properties prop;
	static {
		try {
			InputStream in=Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");
			prop = new Properties();
			prop.load(in);
			PropertyConfigurator.configure(prop);
		} catch (IOException e) {
			Logger.getLogger(PropertyConf.class).error("操作失败",e);
		}
	}
	public static String get(String key) {
		return prop.getProperty(key);
	}
	public static Integer getInt(String key){
		return Integer.valueOf(get(key));
	}



}
