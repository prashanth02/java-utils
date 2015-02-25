package com.prashanth02.org.json;

import java.io.FileInputStream;
import java.util.Properties;

import org.junit.Test;

public class TestCompareJSONs {
	/**
	 * This test case compares 2 JSON's, first the xml file is converted to JSON, then compares the JSON Array 
	 * @throws Exception
	 */
	@Test
	public void testCompareJSONFiles() throws Exception {
		Properties prop = new Properties();
		FileInputStream in = new FileInputStream(TestCompareJSONs.class.getClassLoader()
				.getResource("com/prashanth02/org/json/config-test.properties").getPath());
		prop.load(in);
		JSONUtil jUtil = new JSONUtil();
		jUtil.compareJSONs(prop);// == Boolean.TRUE;
		in.close();
//		System.out.println(TestCompareJSONs.class.getClassLoader()
//				.getResource("com/prashanth02/org/json/config-test.properties").getPath());
	}
}
