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
package org.eclipse.che.ide.jseditor.client.codeassist;

import org.eclipse.che.ide.jseditor.client.partition.DocumentPartitioner;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;

/**
 * Factory for {@link CodeAssistant} objects.
 */
public interface CodeAssistantFactory {

    /**
     * Create a {@link CodeAssistant} for the given editor.
     * 
     * @param textEditor  the editor
     * @param partitioner the partitioner
     * @return a {@link CodeAssistant}
     */
    CodeAssistant create(TextEditor textEditor, DocumentPartitioner partitioner);
}
