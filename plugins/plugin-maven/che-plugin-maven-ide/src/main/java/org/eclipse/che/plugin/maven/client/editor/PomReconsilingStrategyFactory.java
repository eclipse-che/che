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
package org.eclipse.che.plugin.maven.client.editor;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;

/**
 * Factory class for creating PomReconsiligStrategy
 *
 * @author Evgen Vidolob
 */
public interface PomReconsilingStrategyFactory {

    PomReconsilingStrategy create(AnnotationModel annotationModel, TextEditorPresenter<?> editor);
}
