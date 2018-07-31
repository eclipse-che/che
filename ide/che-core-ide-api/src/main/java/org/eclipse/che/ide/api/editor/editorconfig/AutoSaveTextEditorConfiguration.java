/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.editorconfig;

import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;

/** @author Evgen Vidolob */
public class AutoSaveTextEditorConfiguration extends DefaultTextEditorConfiguration {

  private ReconcilerWithAutoSave reconcilerWithAutoSave =
      new ReconcilerWithAutoSave(DocumentPartitioner.DEFAULT_CONTENT_TYPE, getPartitioner());

  @Override
  public Reconciler getReconciler() {
    return reconcilerWithAutoSave;
  }
}
