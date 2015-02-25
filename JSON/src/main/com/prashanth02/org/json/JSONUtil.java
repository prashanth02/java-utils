package com.prashanth02.org.json;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class JSONUtil {
	
	
	/**
	 * Runs a stored proc & converts the result to JSON Array
	 * @param prop
	 * @return
	 */
	private JSONArray getJSONForStoredProc(Properties prop) {
		try {
			Class.forName(prop.getProperty("driverName"));
			Connection conn = DriverManager.getConnection(
					prop.getProperty("jdbcConnUrl"),
					prop.getProperty("username"), prop.getProperty("password"));
			CallableStatement cs = conn.prepareCall(prop
					.getProperty("storedProc"));
			ResultSet rs = cs.executeQuery();
			return convertResultSetToJSON(rs);
		} catch (ClassNotFoundException e) {
			System.err.println("Could not find the driver to connect to DB.");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("Error executing the stored proc.");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the result set & builds the JSON Array
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	private JSONArray convertResultSetToJSON(ResultSet rs) throws SQLException,
			JSONException {
		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();

		while (rs.next()) {
			JSONObject obj = new JSONObject();

			for (int i = 1; i < rsmd.getColumnCount(); i++) {
				String column_name = rsmd.getColumnName(i);

				switch (rsmd.getColumnType(i)) {
				case java.sql.Types.ARRAY:
					obj.put(toCamelCase(column_name, false),
							rs.getArray(column_name));
					break;
				case java.sql.Types.BIGINT:
					obj.put(toCamelCase(column_name, false),
							rs.getInt(column_name));
					break;
				case java.sql.Types.BOOLEAN:
					obj.put(toCamelCase(column_name, false),
							rs.getBoolean(column_name));
					break;
				case java.sql.Types.BLOB:
					obj.put(toCamelCase(column_name, false),
							rs.getBlob(column_name));
					break;
				case java.sql.Types.DOUBLE:
					obj.put(toCamelCase(column_name, false),
							rs.getDouble(column_name));
					break;
				case java.sql.Types.FLOAT:
					obj.put(toCamelCase(column_name, false),
							rs.getFloat(column_name));
					break;
				case java.sql.Types.INTEGER:
					obj.put(toCamelCase(column_name, false),
							rs.getLong(column_name));
					break;
				case java.sql.Types.NVARCHAR:
					obj.put(toCamelCase(column_name, false),
							rs.getNString(column_name));
					break;
				case java.sql.Types.VARCHAR:
					obj.put(toCamelCase(column_name, false),
							rs.getString(column_name));
					break;
				case java.sql.Types.TINYINT:
					obj.put(toCamelCase(column_name, false),
							rs.getInt(column_name));
					break;
				case java.sql.Types.SMALLINT:
					obj.put(toCamelCase(column_name, false),
							rs.getInt(column_name));
					break;
				case java.sql.Types.DATE:
					obj.put(toCamelCase(column_name, false),
							rs.getDate(column_name));
					break;
				case java.sql.Types.TIMESTAMP:
					obj.put(toCamelCase(column_name, false),
							rs.getString(column_name));
					break;
				case java.sql.Types.NUMERIC:
					obj.put(toCamelCase(column_name, false),
							rs.getBigDecimal(column_name).toPlainString());
					break;
				default:
					obj.put(toCamelCase(column_name, false),
							rs.getObject(column_name));
					break;
				}
			}
			json.put(obj);
		}
		// System.out.println(json);
		return json;
	}

	/**
	 * Generates XML string from the file
	 * @param node
	 * @param encoding
	 * @return
	 */
	public static String toString(Node node, String encoding) {
		try {
			OutputFormat format = new OutputFormat();
			if (encoding != null) {
				format.setEncoding(encoding);
			}
			StringWriter result = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(result, format);
			switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE:
				serializer.serialize((Document) node);
				break;
			case Node.ELEMENT_NODE:
				serializer.serialize((Element) node);
				break;
			case Node.DOCUMENT_FRAGMENT_NODE:
				serializer.serialize((DocumentFragment) node);
				break;
			}
			return result.toString();
		} catch (IOException e) {
			throw new RuntimeException("XML Parsing Failure", e);
		}
	}

	/**
	 * Reads the response xml file defined in config-test.properties & returns
	 * the JSON Array
	 * 
	 * @param prop
	 * @param file 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private JSONArray getJSONForResponseXML(Properties prop, File file) {
		try {
//			File file = new File(prop.getProperty("responseXmlFilePath"));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			String parsedXML = toString(doc, "UTF-8");
			JSONObject jsonParent = XML.toJSONObject(parsedXML);
			JSONObject jsonResp = jsonParent.getJSONObject("Response");
			JSONObject jsonBody = jsonResp.getJSONObject("Body");

			JSONObject jsonArr = jsonBody.has(prop
					.getProperty("responseListName")) ? jsonBody
					.getJSONObject(prop.getProperty("responseListName")) : null;

			if (jsonArr != null
					&& jsonArr.has(prop.getProperty("responseListElementName"))) {
				if (jsonArr.get(prop.getProperty("responseListElementName")) instanceof JSONObject) {
					JSONArray arr = new JSONArray();
					arr.put(jsonArr.getJSONObject(prop
							.getProperty("responseListElementName")));
					// System.out.println(arr);
					return arr;
				} else {
					// System.out.println(jsonArr.getJSONArray(prop.getProperty("responseListElementName")));
					return jsonArr.getJSONArray(prop
							.getProperty("responseListElementName"));
				}
			}
		} catch (ParserConfigurationException | SAXException e) {
			System.err.println("Error while parsing XML file.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not open the XML file.");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Compares JSON's, either reading from 2 different files or from db & file
	 * @param prop
	 * @return
	 * @throws Exception
	 */
	public Boolean compareJSONs(Properties prop) throws Exception {
		File file = new File(TestCompareJSONs.class.getClassLoader()
				.getResource(prop.getProperty("responseXmlFilePathEmployee1")).getPath());
		File file1 = new File(TestCompareJSONs.class.getClassLoader()
				.getResource(prop.getProperty("responseXmlFilePathEmployee2")).getPath());
		return jsonsEqual(getJSONForResponseXML(prop, file),
				getJSONForResponseXML(prop, file1),
				prop.getProperty("ignoreElementCount"));
	}

	/**
	 * This is a recursive method which recursively compare JSON Arrays
	 * @param objDb
	 * @param objXML
	 * @param ignoreElementCount
	 * @return
	 */
	private Boolean jsonsEqual(Object objDb, Object objXML,
			String ignoreElementCount) {
		if ((objDb == null) || (objXML == null)) {
			System.out.println("Null object found::objDb=>" + objDb
					+ "\nobjXML=>" + objXML);
			return Boolean.valueOf(false);
		}
		if (!objDb.getClass().equals(objXML.getClass())) {
			System.out.println("JSON element classes are different::objDb=>"
					+ objDb.getClass() + "::objXML=>" + objXML.getClass());
			return Boolean.valueOf(false);
		}
		try {
			if ((objDb instanceof JSONObject)) {
				JSONObject jsonDbObj = (JSONObject) objDb;
				JSONObject jsonXMLObj = (JSONObject) objXML;

				String[] namesDb = JSONObject.getNames(jsonDbObj);
				String[] namesXML = JSONObject.getNames(jsonXMLObj);
				String[] namesToLoop = namesDb;
				if ((!ignoreElementCount.equalsIgnoreCase("YES"))
						&& (namesDb.length != namesXML.length)) {
					System.out
							.println("Total names in the JSON's doesn't match.");
					return Boolean.valueOf(false);
				}
				if (ignoreElementCount.equalsIgnoreCase("YES")) {
					namesToLoop = namesXML;
				}
				for (String fieldName : namesToLoop) {
					Object objDbFieldValue = jsonDbObj.has(fieldName) ? jsonDbObj
							.get(fieldName) : null;
					Object objXMLFieldValue = jsonXMLObj.has(fieldName) ? jsonXMLObj
							.get(fieldName) : null;
					if (!jsonsEqual(objDbFieldValue, objXMLFieldValue,
							ignoreElementCount).booleanValue()) {
						System.out.println("JSON element=> " + fieldName
								+ " ::are not equal::ElementDb=>"
								+ objDbFieldValue + "::ElementXML=>"
								+ objXMLFieldValue);
						return Boolean.valueOf(false);
					}
				}
			} else if ((objDb instanceof JSONArray)) {
				JSONArray objDbArray = (JSONArray) objDb;
				JSONArray objXMLArray = (JSONArray) objXML;
				if (objDbArray.length() != objXMLArray.length()) {
					System.out.println("Total Record count differs");
					return Boolean.valueOf(false);
				}
				for (int i = 0; i < objDbArray.length(); i++) {
					if (!jsonsEqual(objDbArray.get(i), objXMLArray.get(i),
							ignoreElementCount).booleanValue()) {
						return Boolean.valueOf(false);
					}
				}
			} else if (!objDb.equals(objXML)) {
				return Boolean.valueOf(false);
			}
		} catch (JSONException e) {
			System.err.println("JSON comparision error!!!");
			e.printStackTrace();
		}
		return Boolean.valueOf(true);
	}

	private String toCamelCase(String value, boolean startWithLowerCase) {
		String[] strings = StringUtils.split(value.toLowerCase(), "_");
		for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++) {
			strings[i] = StringUtils.capitalize(strings[i]);
		}
		return StringUtils.join(strings);
	}
}