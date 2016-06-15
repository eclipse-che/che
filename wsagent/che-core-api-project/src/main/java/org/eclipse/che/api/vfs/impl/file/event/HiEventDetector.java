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

import java.util.Optional;

/**
 * Event detectors are the key components of the whole VFS event handling subsystem.
 * <p>
 *     The main purpose of each event detector is to analyze instances of
 *     {@link EventTreeNode} and to find out if a specific conditions are met.
 *     If yes - corresponding high level event should be generated. Besides the
 *     obvious (event detection), event detectors are also responsible for high
 *     level event generation. Those events should correspond to a specific event
 *     tree state and populated with relevant data.
 * </p>
 * <p>
 *     Perhaps those functions will be shared between detectors and event builders
 *     in future implementations.
 * </p>
 * <p>
 *     To omit details, it defines a way a set of low level events transforms into a
 *     specific high level event or a list of events.
 * </p>
 * <p>
 *     Note: Though it is not restricted to generate different high level events in
 *     correspondence to different event trees by a single detector, it is not
 *     recommended to do so to avoid complications in understanding and handling of
 *     events. Adhere to a rule: one detector - one event. Unless you know what you
 *     are doing.
 * </p>
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@Beta
@FunctionalInterface
public interface HiEventDetector<T> {
     Optional<HiEvent<T>> detect(EventTreeNode eventTreeNode);
}
