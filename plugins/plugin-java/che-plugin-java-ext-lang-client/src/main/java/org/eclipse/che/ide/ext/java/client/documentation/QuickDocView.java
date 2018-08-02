/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.documentation;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/** @author Evgen Vidolob */
@ImplementedBy(QuickDocViewImpl.class)
public interface QuickDocView extends View<QuickDocView.ActionDelegate> {
  void show(String url, int x, int y);

  interface ActionDelegate {

    void onCloseView();
  }
}
