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
package org.eclipse.che.plugin.docker.client;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;

/**
 * Docker daemon sends chunked data in response. One chunk isn't always one JSON object so need to read full chunk at once to be able
 * restore JSON object. This reader merges (if needs) few chunks until get full JSON object that we can parse.
 * Parameter of this class is class where JSON message should be parsed.
 *
 * @author Alexander Garagatyi
 */
public class JsonMessageReader<T> {
    private static final Gson GSON = new Gson();

    private final JsonStreamParser streamParser;
    private final Class<T>         messageClass;
    private final PushbackReader   reader;

    private boolean firstRead = true;

    /**
     * @param source source of messages in JSON format
     * @param messageClass class of the message object where JSON messages should be parsed.
     *                     Because of erasure of generic information in runtime in some cases
     *                     we can't get parameter class of current class.
     */
    public JsonMessageReader(InputStream source, Class<T> messageClass) {
        // we need to push back only 1 char, read more further
        this.reader = new PushbackReader(new InputStreamReader(source), 1);
        this.streamParser = new JsonStreamParser(reader);
        this.messageClass = messageClass;
    }

    /**
     * Returns message parsed from JSON stream.
     *
     * @return object of class passed as parameter of constructor or null if stream is empty
     * @throws IOException if error occurs on reading stream
     */
    public T next() throws IOException {
        // on first read we check if this stream is empty with reading of the first byte of stream
        // if so we do not call JsonStreamParser.hasNext() because it will throw exception
        // if not we return read byte to stream using PushbackInputStream
        if (firstRead) {
            int firstChar = reader.read();
            if (firstChar == -1) {
                return null;
            } else {
                reader.unread(firstChar);
                firstRead = false;
            }
        }
        try {
            if (streamParser.hasNext()) {
                return GSON.fromJson(streamParser.next(), messageClass);
            }
        } catch (JsonIOException e) {
            throw new IOException(e);
        } catch (JsonParseException ignore) {
        }
        return null;
    }
}
