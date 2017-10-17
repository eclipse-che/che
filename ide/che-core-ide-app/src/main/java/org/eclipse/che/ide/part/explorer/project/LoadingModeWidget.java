/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.explorer.project;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;

/** Represents loading mode widget for Project explorer */
public class LoadingModeWidget extends Composite {

  interface LoadingModeWidgetUiBinder extends UiBinder<Widget, LoadingModeWidget> {}

  @Inject
  public LoadingModeWidget(LoadingModeWidgetUiBinder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }
}
