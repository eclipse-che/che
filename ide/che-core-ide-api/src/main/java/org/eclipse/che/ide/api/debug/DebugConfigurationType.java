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
package org.eclipse.che.ide.api.debug;

/**
 * The type of a debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
public interface DebugConfigurationType {

    /** Returns unique identifier for this debug configuration type. */
    String getId();

    /** Returns the display name of this debug configuration type. */
    String getDisplayName();

    /** Returns the {@link DebugConfigurationPage} that allows to edit debug configuration of this type. */
    DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage();
}
