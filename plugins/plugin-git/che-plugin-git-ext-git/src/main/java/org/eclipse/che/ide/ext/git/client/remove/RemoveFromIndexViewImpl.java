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
package org.eclipse.che.ide.ext.git.client.remove;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link RemoveFromIndexView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class RemoveFromIndexViewImpl extends Window implements RemoveFromIndexView {
  interface RemoveFromIndexViewImplUiBinder extends UiBinder<Widget, RemoveFromIndexViewImpl> {}

  private static RemoveFromIndexViewImplUiBinder ourUiBinder =
      GWT.create(RemoveFromIndexViewImplUiBinder.class);

  @UiField Label message;
  @UiField CheckBox remove;
  Button btnRemove;
  Button btnCancel;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private ActionDelegate delegate;

  /**
   * Create view.
   *
   * @param resources
   * @param locale
   */
  @Inject
  protected RemoveFromIndexViewImpl(GitResources resources, GitLocalizationConstant locale) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-removeFromIndex-window");

    Widget widget = ourUiBinder.createAndBindUi(this);

    this.setTitle(locale.removeFromIndexTitle());
    this.setWidget(widget);

    btnRemove =
        createButton(
            locale.buttonRemove(),
            "git-removeFromIndex-remove",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onRemoveClicked();
              }
            });
    btnRemove.addStyleName(Window.resources.windowCss().primaryButton());
    addButtonToFooter(btnRemove);

    btnCancel =
        createButton(
            locale.buttonCancel(),
            "git-removeFromIndex-cancel",
            new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
              }
            });
    addButtonToFooter(btnCancel);
  }

  @Override
  protected void onEnterClicked() {
    if (isWidgetFocused(btnRemove)) {
      delegate.onRemoveClicked();
      return;
    }

    if (isWidgetFocused(btnCancel)) {
      delegate.onCancelClicked();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setMessage(@NotNull String message) {
    this.message.getElement().setInnerHTML(message);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isRemoved() {
    return remove.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public void setRemoved(boolean isUpdated) {
    remove.setValue(isUpdated);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.show(btnRemove);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
