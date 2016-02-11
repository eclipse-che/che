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
package org.eclipse.che.ide.api.texteditor;

public interface IsConfigurable<T> {
    /**
     * Configures the source viewer using the given configuration. Prior to 3.0 this method can only be called once.
     *
     * @param configuration
     *         the source viewer configuration to be used
     */
    void configure(T configuration);
}
