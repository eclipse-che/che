/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Connection utilities.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendConnectionUtils {

	public static final void writeString(DataOutputStream out, String line) throws IOException {
		byte[] byteArray = line.getBytes(Charset.defaultCharset());
		out.writeInt(byteArray.length);
		out.write(byteArray);
	}

	public static final void writeStringAsBytes(DataOutputStream out, byte[] byteArray) throws IOException {
		out.writeInt(byteArray.length);
		out.write(byteArray);
	}

	public static final String readString(DataInputStream in) throws IOException {
		return new String(readStringAsBytes(in), Charset.defaultCharset());
	}

	public static final String readEncodedString(DataInputStream in, String encoding) throws IOException {
		byte[] byteArray = readStringAsBytes(in);
		String rv = getTextFromBytes(byteArray, encoding);
		return rv;
	}

	public static final void writeEncodedString(DataOutputStream out, String line, String encoding) throws IOException {
		byte[] byteArray = getBytesFromText(line, encoding);
		out.writeInt(byteArray.length);
		out.write(byteArray);
	}

	public static final byte[] getBytesFromText(String text, String encoding) {
		try {
			return text.getBytes(encoding);
		} catch (Exception e) {
		}
		return text.getBytes(Charset.defaultCharset());
	}

	public static final String getTextFromBytes(byte[] theBytes, String encoding) {
		try {
			return new String(theBytes, encoding);
		} catch (Exception e) {
		}
		return new String(theBytes, Charset.defaultCharset());
	}

	public static final byte[] readStringAsBytes(DataInputStream in) throws IOException {
		int size = in.readInt();
		byte[] byteArray = new byte[size];
		in.readFully(byteArray);
		return byteArray;
	}

}
