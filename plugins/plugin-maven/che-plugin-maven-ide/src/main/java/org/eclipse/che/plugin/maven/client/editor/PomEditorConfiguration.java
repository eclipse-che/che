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
package org.eclipse.che.plugin.maven.client.editor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.editorconfig.AutoSaveTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;
import org.eclipse.che.ide.ext.java.client.editor.JavaAnnotationModelFactory;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.editor.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

/**
 * @author Evgen Vidolob
 */
public class PomEditorConfiguration extends AutoSaveTextEditorConfiguration {

    private AnnotationModel annotationModel;

    @Inject
    public PomEditorConfiguration(Provider<DocumentPositionMap> docPositionMapProvider,
                                  JavaAnnotationModelFactory javaAnnotationModelFactory,
                                  PomReconcilingStrategyFactory reconcilingStrategyFactory,
                                  @Assisted @NotNull final OrionEditorPresenter editor) {
        annotationModel = javaAnnotationModelFactory.create(docPositionMapProvider.get());
        PomReconcilingStrategy reconsilingStrategy = reconcilingStrategyFactory.create(annotationModel, editor);
        Reconciler reconciler = getReconciler();
        reconciler.addReconcilingStrategy(DEFAULT_CONTENT_TYPE, reconsilingStrategy);
    }

    @Override
    public AnnotationModel getAnnotationModel() {
        return annotationModel;
    }
}
