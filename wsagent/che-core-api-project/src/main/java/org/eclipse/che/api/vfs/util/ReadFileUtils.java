/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;

import static java.lang.Character.toChars;
import static java.lang.System.lineSeparator;

/**
 * @author Vitalii Parfonov
 */

public class ReadFileUtils {


    /**
     * Read given file and return {@link Line} by given offset
     * @param file
     * @param offset
     * @return
     * @throws IOException
     */
    public static Line getLine(File file, int offset) throws IOException {
        if (file.length() < offset) {
            throw new IOException("File is not long enough");
        }
        try (LineNumberReader r = new LineNumberReader(new FileReader(file))) {

            int count = 0;
            int read = 0;
            int startPosition = 0;
            while (read != -1 && count < offset) {
                read = r.read();
                final char[] chars = toChars(read);
                if (lineSeparator().equals(new String(chars))) {
                    startPosition = count;
                }
                count++;
            }

            if (count == offset) {
                int lineNumber = r.getLineNumber();
                String s = r.readLine();
                int t = offset + s.length();
                int len = t - startPosition;
                r.close();

                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                if (lineNumber > 0) { //skip previous new line symbol
                    startPosition++;
                    len--;
                }
                byte [] chars = new byte[len];
                randomAccessFile.seek(startPosition);
                randomAccessFile.read(chars, 0, len);
                return new Line(lineNumber, new String(chars));
            } else {
                throw new IOException("File is not long enough");
            }
        }

    }

    /**
     * Describe line number and it content
     */
    public static class Line {
        private int lineNumber;
        private String lineContent;

        Line(int lineNumber, String lineContent) {
            this.lineNumber = lineNumber;
            this.lineContent = lineContent;
        }


        /**
         * @return line content
         */
        public String getLineContent() {
            return lineContent;
        }

        /**
         * @return number of line in the file keep in mind first line will be 0 not 1
         */
        public int getLineNumber() {
            return lineNumber;
        }
    }
}
