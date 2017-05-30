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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Interface for components which handle {@link ViewPortChangeEvent}.
 *
 * @author "Mickaël Leduque"
 */
public interface HasViewPortChangeHandlers extends HasHandlers {
    HandlerRegistration addViewPortChangeHandler(ViewPortChangeHandler handler);
}
