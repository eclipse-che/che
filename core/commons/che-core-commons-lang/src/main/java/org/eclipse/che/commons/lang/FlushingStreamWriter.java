/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2015] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.eclipse.che.commons.lang;

/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Stream writer which flushes after each write operation.
 *
 * @author Guillaume Laforge
 */
public class FlushingStreamWriter extends OutputStreamWriter {

    public FlushingStreamWriter(OutputStream out) {
        super(out);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        flush();
    }

    public void write(int c) throws IOException {
        super.write(c);
        flush();
    }

    public void write(String str, int off, int len) throws IOException {
        super.write(str, off, len);
        flush();
    }
}