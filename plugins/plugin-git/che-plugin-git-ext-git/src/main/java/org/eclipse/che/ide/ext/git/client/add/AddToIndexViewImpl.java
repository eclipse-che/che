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
package org.eclipse.che.ide.ext.git.client.add;

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
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link AddToIndexView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class AddToIndexViewImpl extends Window implements AddToIndexView {
  interface AddToIndexViewImplUiBinder extends UiBinder<Widget, AddToIndexViewImpl> {}

  private static AddToIndexViewImplUiBinder ourUiBinder =
      GWT.create(AddToIndexViewImplUiBinder.class);

  @UiField Label message;
  @UiField CheckBox update;
  Button btnAdd;
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
  protected AddToIndexViewImpl(GitResources resources, GitLocalizationConstant locale) {
    this.res = resources;
    this.locale = locale;

    ensureDebugId("git-addToIndex-window");
    setTitle(locale.addToIndexTitle());
    setWidget(ourUiBinder.createAndBindUi(this));

    btnAdd =
        createButton(
            locale.buttonAdd(),
            "git-addToIndex-btnAdd",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onAddClicked();
              }
            });
    btnAdd.addStyleName(Window.resources.windowCss().primaryButton());
    addButtonToFooter(btnAdd);

    btnCancel =
        createButton(
            locale.buttonCancel(),
            "git-addToIndex-btnCancel",
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
    if (isWidgetFocused(btnAdd)) {
      delegate.onAddClicked();
      return;
    }

    if (isWidgetFocused(btnCancel)) {
      delegate.onCancelClicked();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setMessage(String htmlMessage) {
    this.message.getElement().setInnerHTML(htmlMessage);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isUpdated() {
    return update.getValue();
  }

  /** {@inheritDoc} */
  @Override
  public void setUpdated(boolean isUpdated) {
    update.setValue(isUpdated);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    this.hide();
  }

  /** {@inheritDoc} */
  @Override
  public void showDialog() {
    this.show(btnAdd);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }
}
