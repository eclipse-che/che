/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 ******************************************************************************/
package org.eclipse.che.ide.api.multisplitpanel;

/**
 * Factory for {@link SubPanel}.
 *
 * @author Artem Zatsarynnyi
 */
public interface SubPanelFactory {

    /**
     * Create new instance of {@link SubPanel}.
     *
     * @param focusListener
     *         listener that should be called on focusing {@link SubPanel}
     * @param parentPanel
     * @return new split panel that contains {@code widget}
     */
    SubPanel newPanel(FocusListener focusListener, SubPanel parentPanel);
}
