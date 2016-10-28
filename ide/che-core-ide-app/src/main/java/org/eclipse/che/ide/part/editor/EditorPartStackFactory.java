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
package org.eclipse.che.ide.part.editor;

import org.eclipse.che.ide.api.parts.EditorPartStack;

/**
 * The factory which creates instances of {@link EditorPartStack}.
 *
 * @author Roman Nikitenko
 * @deprecated use {@link com.google.inject.Provider} to get new instance
 */
@Deprecated
public interface EditorPartStackFactory {

    /**
     * Creates implementation of {@link EditorPartStack}.
     *
     * @return an instance of {@link EditorPartStack}
     */
    EditorPartStack create();
}
