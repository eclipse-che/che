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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.plugin.languageserver.ide.filestructure.ElementSelectionDelegate;
import org.eclipse.che.plugin.languageserver.ide.filestructure.FileStructureView;
import org.eclipse.che.plugin.languageserver.ide.filestructure.NodeFactory;

/**
 * A window showing a java specific file structure
 *
 * @author Thomas Mäder
 */
@Singleton
public final class FileStructureWindow extends Window
    implements View<ElementSelectionDelegate<ExtendedSymbolInformation>> {
  interface FileStructureWindowUiBinder extends UiBinder<Widget, FileStructureWindow> {}

  private static FileStructureWindowUiBinder UI_BINDER =
      GWT.create(FileStructureWindowUiBinder.class);
  private final FileStructureView view;

  @UiField DockLayoutPanel treeContainer;
  @UiField Label showInheritedLabel;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  @Inject
  public FileStructureWindow(NodeFactory nodeFactory, JavaLocalizationConstant locale) {
    super(false);
    this.locale = locale;
    setWidget(UI_BINDER.createAndBindUi(this));
    view = new FileStructureView(nodeFactory);

    treeContainer.add(view);
  }

  public void setShowInherited(boolean on) {
    showInheritedLabel.setText(
        on ? locale.hideInheritedMembersLabel() : locale.showInheritedMembersLabel());
  }

  public void setInput(List<ExtendedSymbolInformation> input) {
    view.setInput(input);
  }

  /** {@inheritDoc} */
  @Override
  public void show() {
    super.show(view);
    view.onShow();
  }

  @Override
  public void setDelegate(ElementSelectionDelegate<ExtendedSymbolInformation> delegate) {
    view.setDelegate(delegate);
  }
}
