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
package org.eclipse.che.api.vfs.impl.file.event;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category;

import javax.inject.Singleton;
import java.util.List;

import static java.lang.Long.compare;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category.UNDEFINED;
import static org.eclipse.che.api.vfs.impl.file.event.HiEvent.Category.values;

/**
 * Splits high level event list according to their categories (see {@link Category})
 * and then broadcast categorized events with the highest priorities.
 * <p>
 *     Note: broadcasters are secured for each {@link HiEvent} instance during
 *     creation, so it is out of scope of this class to define broadcasters, but simply
 *     to call them.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
class HiEventBroadcasterManager {
    void manageEvents(List<HiEvent> hiEvents) {
        if (hiEvents.isEmpty()) {
            return;
        }
        for (Category category : values()) {

            final List<HiEvent> events = hiEvents.stream()
                                                 .filter(o -> o.getCategory().equals(category))
                                                 .sorted((o1, o2) -> compare(o1.getCategory().getPriority(),
                                                                             o2.getCategory().getPriority()))
                                                 .collect(toList());

            if (UNDEFINED.equals(category)) {
                events.stream().forEach(HiEvent::broadcast);
            } else if (!events.isEmpty()) {
                // getting the event with the highest priority
                events.get(0).broadcast();
            }
        }
    }

}
