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
package org.eclipse.che.ide.extension.maven.client.editor;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.ide.ext.java.client.editor.JavaAnnotationModelFactory;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.editorconfig.AutoSaveTextEditorConfiguration;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPositionMap;

/**
 * @author Evgen Vidolob
 */
public class PomEditorConfiguration extends AutoSaveTextEditorConfiguration {

    private AnnotationModel annotationModel;

    @Inject
    public PomEditorConfiguration(Provider<DocumentPositionMap> docPositionMapProvider, JavaAnnotationModelFactory javaAnnotationModelFactory) {
        annotationModel = javaAnnotationModelFactory.create(docPositionMapProvider.get());
    }

    @Override
    public AnnotationModel getAnnotationModel() {
        return annotationModel;
    }
}
