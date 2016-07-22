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

/**
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@FunctionalInterface
public interface HiEventBroadcaster {
    void broadcast(HiEvent event);
}
