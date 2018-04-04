/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
