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
package org.eclipse.che.api.core.notification;

import org.eclipse.che.commons.lang.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author andrew00x
 */
@Singleton
public class EventOriginClientPropagationPolicy implements ClientEventPropagationPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(EventOriginClientPropagationPolicy.class);

    private final Map<URI, Set<String>> forPropagation;

    @Inject
    EventOriginClientPropagationPolicy(@Named("notification.client.propagate_events") Pair<String, String>[] forPropagation) {
        final Map<URI, Set<String>> cfg = new HashMap<>();
        for (Pair<String, String> service : forPropagation) {
            try {
                final URI key = new URI(service.first);
                Set<String> values = cfg.get(key);
                if (values == null) {
                    cfg.put(key, values = new LinkedHashSet<>());
                }
                if (service.second != null) {
                    values.add(service.second);
                }
            } catch (URISyntaxException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        this.forPropagation = cfg;
    }

    @Override
    public boolean shouldPropagated(URI uri, Object event) {
        final EventOrigin eventOrigin = event.getClass().getAnnotation(EventOrigin.class);
        final Set<String> set = forPropagation.get(uri);
        return set != null && eventOrigin != null && set.contains(eventOrigin.value());
    }
}
