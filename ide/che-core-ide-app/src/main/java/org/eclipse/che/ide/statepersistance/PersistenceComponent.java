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
package org.eclipse.che.ide.statepersistance;

import org.eclipse.che.ide.api.statepersistance.dto.ActionDescriptor;

import java.util.List;

/**
 * Defines requirements for a component which would like to persist some state of workspace across sessions.
 * <p/>Implementations of this interface need to be registered using
 * a multibinder in order to be picked-up on IDE start-up.
 *
 * @deprecated use {@link org.eclipse.che.ide.api.component.StateComponent} instead.
 *
 * @author Artem Zatsarynnyi
 */
@Deprecated
public interface PersistenceComponent {

    /**
     * Returns sequence of actions which should be performed each
     * time when IDE is loaded in order to restore workspace state.
     *
     * @return actions with it's parameters that should be performed on each loading IDE
     */
    List<ActionDescriptor> getActions();
}
