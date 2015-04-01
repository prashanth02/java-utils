/**
 * 
 */
package com.prashanth02.org.hex;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

/**
 * @author PrashanthB
 * 
 */
public class HexString {

	@Test
	public void testGetHexString() throws Exception {
//		F4CB48BC4B220A4F8AC3E60D8E58E087
		byte[] temp = {(byte) 12345678};
//		System.out.println(getHexString(temp));
		assert("4E" == getHexString(temp));
	}

	// Converts the HexaDecimal to a String
	public static String getHexString(byte[] raw) throws UnsupportedEncodingException {

		byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2',
				(byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
				(byte) '8', (byte) '9', (byte) 'A', (byte) 'B', (byte) 'C',
				(byte) 'D', (byte) 'E', (byte) 'F' };

		int index = 0;
		if (raw == null || raw.length <= 0) {
			return null;
		}

		byte[] hex = new byte[2 * raw.length];
		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex, "ASCII");
	}

}
