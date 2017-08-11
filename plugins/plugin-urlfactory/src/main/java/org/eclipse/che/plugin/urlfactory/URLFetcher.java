/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.urlfactory;

import org.apache.commons.compress.utils.BoundedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * Allow to grab content from URL
 *
 * @author Florent Benoit
 */
@Singleton
public class URLFetcher {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(URLFetcher.class);

    /**
     * Maximum size of allowed data. (30KB)
     */
    protected static final long MAXIMUM_READ_BYTES = 30 * 1000;

    /**
     * Fetch the url provided and return its content
     * To prevent DOS attack, limit the amount of the collected data
     *
     * @param url
     *         the URL to fetch
     * @return the content of the file
     */
    public String fetch(@NotNull final String url) {
        requireNonNull(url, "url parameter can't be null");
        try {
            return fetch(new URL(url).openConnection());
        } catch (IOException e) {
            // we shouldn't fetch if check is done before
            LOG.debug("Invalid URL", e);
            return null;
        }
    }


    /**
     * Fetch the urlConnection stream by using the urlconnection and return its content
     * To prevent DOS attack, limit the amount of the collected data
     *
     * @param urlConnection
     *         the URL connection to fetch
     * @return the content of the file
     */
    public String fetch(@NotNull URLConnection urlConnection) {
        requireNonNull(urlConnection, "urlConnection parameter can't be null");
        final String value;
        try (InputStream inputStream = urlConnection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(new BoundedInputStream(inputStream, getLimit()), UTF_8))) {
            value = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            // we shouldn't fetch if check is done before
            LOG.debug("Invalid URL", e);
            return null;
        }
        return value;
    }

    /**
     * Maximum size that can be read.
     * @return maximum size.
     */
    protected long getLimit() {
        return MAXIMUM_READ_BYTES;
    }
}
