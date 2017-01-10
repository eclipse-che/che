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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author andrew00x
 */
@Singleton
public class EventOriginServerPropagationPolicy implements ServerEventPropagationPolicy {
    private final Set<String> forPropagation;

    @Inject
    EventOriginServerPropagationPolicy(@Named("notification.server.propagate_events") String[] forPropagation) {
        this.forPropagation = new LinkedHashSet<>(Arrays.asList(forPropagation));
    }

    @Override
    public boolean shouldPropagated(Object event) {
        final EventOrigin eventOrigin = event.getClass().getAnnotation(EventOrigin.class);
        return eventOrigin != null && forPropagation.contains(eventOrigin.value());
    }
}
