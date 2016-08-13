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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@Singleton
class HiEventDetectorManager {
    private final Set<HiEventDetector<?>> hiEventDetectors;

    @Inject
    public HiEventDetectorManager(Set<HiEventDetector<?>> hiEventDetectors) {
        this.hiEventDetectors = hiEventDetectors;
    }

    List<HiEvent> getDetectedEvents(EventTreeNode root) {
        return hiEventDetectors.stream()
                               .map(o -> o.detect(root))
                               .filter(Optional::isPresent)
                               .map(Optional::get)
                               .collect(toList());
    }

}
