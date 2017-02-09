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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.texteditor.EditorHandle;

/**
 * Factory for {@link JavaAnnotationModel} instances.
 */
public interface JavaAnnotationModelFactory {
    /**
     * Builds an instance of {@link JavaAnnotationModel}.
     * 
     * @param editorHandle an {@link EditorHandle}
     * @param docPositionMap a doc position map model
     * @return a java annotation model
     */
    JavaAnnotationModel create(DocumentPositionMap docPositionMap);
}
