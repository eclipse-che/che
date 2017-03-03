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

package org.eclipse.che.plugin.openshift.client.kubernetes;

import io.fabric8.kubernetes.client.Callback;
import io.fabric8.kubernetes.client.utils.InputStreamPumper;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;

/**
 * Adapter class for passing data from a {@code kubernetes-client} output stream (e.g.
 * for an exec call) to {@link MessageProcessor}. This class should be passed to a
 * {@link InputStreamPumper} along with the output of the exec call.
 *
 * <p> Output passed in via the {@link #call(byte[])} method is parsed into lines,
 * (respecting {@code '\n'} and {@code CRLF} as line separators), and
 * passed to the {@link MessageProcessor} as {@link LogMessage}s.
 */
public class KubernetesOutputAdapter implements Callback<byte[]> {

    private LogMessage.Type type;
    private MessageProcessor<LogMessage> execOutputProcessor;
    private StringBuilder lineBuffer;

    /**
     * Create a new KubernetesOutputAdapter
     *
     * @param type
     *         the type of LogMessages being passed to the MessageProcessor
     * @param processor
     *         the processor receiving LogMessages. If null, calling {@link #call(byte[])}
     *         will return immediately.
     */
    public KubernetesOutputAdapter(LogMessage.Type type,
                                   @Nullable MessageProcessor<LogMessage> processor) {
        this.type = type;
        this.execOutputProcessor = processor;
        this.lineBuffer = new StringBuilder();
    }

    @Override
    public void call(byte[] data) {
        if (data == null || data.length == 0 || execOutputProcessor == null) {
            return;
        }
        int start = 0;
        int offset = 0;

        for (int pos = 0; pos < data.length; pos++) {
            if (data[pos] == '\n' || data[pos] == '\r') {
                offset = pos - start;
                String line = new String(data, start, offset);
                lineBuffer.append(line);
                execOutputProcessor.process(new LogMessage(type, lineBuffer.toString()));
                lineBuffer.setLength(0);
                if (data[pos] == '\r') {
                    pos += 1;
                }
                start = pos + 1;
            }
        }
        String trailingChars = new String(data, start, data.length - start);
        lineBuffer.append(trailingChars);
    }
}
