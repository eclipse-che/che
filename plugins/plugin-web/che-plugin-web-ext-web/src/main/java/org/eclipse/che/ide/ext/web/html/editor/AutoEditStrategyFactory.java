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
package org.eclipse.che.ide.ext.web.html.editor;

import org.eclipse.che.ide.api.editor.changeintercept.TextChangeInterceptor;

/**
 * Allows to define a new AutoEditStrategy based on text editor and content type.
 * 
 * @author Florent Benoit
 */
public interface AutoEditStrategyFactory {

    /**
     * Build a new instance
     * 
     * @return a new strategy
     */
    TextChangeInterceptor build(String contentType);
}
