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
package org.eclipse.che.ide.jseditor.client.editorconfig;

import org.eclipse.che.ide.jseditor.client.partition.DocumentPartitioner;
import org.eclipse.che.ide.jseditor.client.reconciler.Reconciler;
import org.eclipse.che.ide.jseditor.client.reconciler.ReconcilerWithAutoSave;

/**
 * @author Evgen Vidolob
 */
public class AutoSaveTextEditorConfiguration extends DefaultTextEditorConfiguration{

    private ReconcilerWithAutoSave reconcilerWithAutoSave =
            new ReconcilerWithAutoSave(DocumentPartitioner.DEFAULT_CONTENT_TYPE, getPartitioner());

    @Override
    public Reconciler getReconciler() {
        return reconcilerWithAutoSave;
    }
}
