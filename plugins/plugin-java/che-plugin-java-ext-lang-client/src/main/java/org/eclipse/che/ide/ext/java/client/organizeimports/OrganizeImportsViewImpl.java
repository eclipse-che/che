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
package org.eclipse.che.ide.ext.java.client.organizeimports;

import static com.google.gwt.dom.client.Style.Cursor.POINTER;
import static org.eclipse.che.ide.api.theme.Style.getEditorSelectionColor;
import static org.eclipse.che.ide.api.theme.Style.getMainFontColor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implements of {@link OrganizeImportsView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
final class OrganizeImportsViewImpl extends Window implements OrganizeImportsView {
  interface OrganizeImportsViewImplUiBinder extends UiBinder<Widget, OrganizeImportsViewImpl> {}

  private static OrganizeImportsViewImplUiBinder UI_BINDER =
      GWT.create(OrganizeImportsViewImplUiBinder.class);

  @UiField VerticalPanel container;

  @UiField(provided = true)
  final JavaLocalizationConstant locale;

  private ActionDelegate delegate;
  private Button next;
  private Button back;
  private Button finish;

  private String selectedImport;
  private Label selectedLabel;

  @Inject
  public OrganizeImportsViewImpl(JavaLocalizationConstant locale) {
    this.locale = locale;
    setTitle(locale.organizeImportsName());
    setWidget(UI_BINDER.createAndBindUi(this));

    createButtons(locale);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** {@inheritDoc} */
  @Override
  public void show(ConflictImportDTO match) {
    container.clear();
    List<String> matches = match.getTypeMatches();
    for (String fqn : matches) {
      final Label label = new Label(fqn);
      if (fqn.equals(selectedImport)) {
        selectedLabel = label;
        selectedLabel.getElement().getStyle().setBackgroundColor(getEditorSelectionColor());
      }
      label.getElement().getStyle().setColor(getMainFontColor());
      label.getElement().getStyle().setCursor(POINTER);
      label.addClickHandler(
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
              selectedLabel.getElement().getStyle().setBackgroundColor("initial");
              selectedLabel = label;
              label.getElement().getStyle().setBackgroundColor(getEditorSelectionColor());
            }
          });

      container.add(label);
    }

    super.show();
  }

  /** {@inheritDoc} */
  public String getSelectedImport() {
    return selectedLabel.getText();
  }

  /** {@inheritDoc} */
  @Override
  public void setSelectedImport(String fqn) {
    this.selectedImport = fqn;
  }

  /** {@inheritDoc} */
  @Override
  public void changePage(ConflictImportDTO match) {
    show(match);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableFinishButton(boolean isEnable) {
    finish.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableNextButton(boolean isEnable) {
    next.setEnabled(isEnable);
  }

  /** {@inheritDoc} */
  @Override
  public void setEnableBackButton(boolean isEnable) {
    back.setEnabled(isEnable);
  }

  private void createButtons(JavaLocalizationConstant locale) {
    back =
        createButton(
            locale.organizeImportsButtonBack(),
            "imports-back-button",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onBackButtonClicked();
              }
            });

    next =
        createButton(
            locale.organizeImportsButtonNext(),
            "imports-next-button",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onNextButtonClicked();
              }
            });

    Button cancel =
        createButton(
            locale.organizeImportsButtonCancel(),
            "imports-cancel-button",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                hide();
                delegate.onCancelButtonClicked();
              }
            });

    finish =
        createPrimaryButton(
            locale.organizeImportsButtonFinish(),
            "imports-finish-button",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                delegate.onFinishButtonClicked();
              }
            });

    addButtonToFooter(finish);
    addButtonToFooter(cancel);
    addButtonToFooter(next);
    addButtonToFooter(back);
  }
}
