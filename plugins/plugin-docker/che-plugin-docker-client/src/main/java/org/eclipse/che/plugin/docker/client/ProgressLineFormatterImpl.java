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

import org.eclipse.che.plugin.docker.client.json.ProgressStatus;

/**
 * Beatify {@link ProgressStatus} messages.
 *
 * @author Alexander Garagatyi
 */
public class ProgressLineFormatterImpl implements MessageFormatter<ProgressStatus> {
    @Override
    public String format(ProgressStatus progressStatus) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[DOCKER] ");
        final String stream = progressStatus.getStream();
        final String status = progressStatus.getStatus();
        final String error = progressStatus.getError();
        if (error != null) {
            sb.append("[ERROR] ");
            sb.append(error);
        } else if (stream != null) {
            sb.append(stream.trim());
        } else if (status != null) {
            final String id = progressStatus.getId();
            if (id != null) {
                sb.append(id);
                sb.append(':');
                sb.append(' ');
            }
            sb.append(status);
            sb.append(' ');
            sb.append(parseProgressText(progressStatus));
        }
        return sb.toString();
    }

    /**
     * Parses text data from progress string. Typical progress string from docker API:
     * {@code [==================================&gt;                ]  9.13 MB/13.38 MB 19s}
     * This method gets text part from progress string, e.g. {@code 9.13 MB/13.38 MB 19s}
     *
     * @return text data from progress string or empty string if progress string is {@code null} or doesn't contains text date
     */
    protected String parseProgressText(ProgressStatus progressStatus) {
        // "
        final String rawProgress = progressStatus.getProgress();
        if (rawProgress == null) {
            return "";
        }
        // skip progress indicator: ==================================>                ]
        final int l = rawProgress.length();
        int n = 0;
        while (n < l && rawProgress.charAt(n) != '[') {
            n++;
        }
        int p = n;
        while (p < l && rawProgress.charAt(p) != ']') {
            p++;
        }
        if (p == n) {
            // unexpected string
            return "";
        }
        ++p;
        while (p < l && Character.isWhitespace(rawProgress.charAt(p))) {
            p++;
        }
        if (p == l) {
            // unexpected string
            return "";
        }
        return rawProgress.substring(p);
    }
}
