/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server.search;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.MountPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * SearcherProvider for Lucene based Searchers.
 *
 * @author andrew00x
 */
public abstract class LuceneSearcherProvider implements SearcherProvider {

    @Override
    public abstract Searcher getSearcher(MountPoint mountPoint, boolean create) throws ServerException;

    /** Get list of media type of virtual files which must be indexed. */
    protected Set<String> getIndexedMediaTypes() {
        Set<String> forIndex = null;
        final URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/indices_types.txt");
        if (url != null) {
            InputStream in = null;
            BufferedReader reader = null;
            try {
                in = url.openStream();
                reader = new BufferedReader(new InputStreamReader(in));
                forIndex = new LinkedHashSet<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    int c = line.indexOf('#');
                    if (c >= 0) {
                        line = line.substring(0, c);
                    }
                    line = line.trim();
                    if (line.length() > 0) {
                        forIndex.add(line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to get list of media types for indexing. %s", e.getMessage()));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        if (forIndex == null || forIndex.isEmpty()) {
            throw new RuntimeException(
                    "Failed to get list of media types for indexing. File 'META-INF/indices_types.txt not found or empty. ");
        }
        return forIndex;
    }

}
