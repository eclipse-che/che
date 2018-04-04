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
package org.eclipse.che.ide.ext.git.client.outputconsole;

import com.google.gwt.dom.client.PreElement;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ui.button.ConsoleButton;
import org.eclipse.che.ide.ui.button.ConsoleButtonFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Implements {@link GitOutputPartView}.
 *
 * @author Andrey Plotnikov
 */
public class GitOutputPartViewImpl extends Composite implements GitOutputPartView {

  private final ConsoleButtonFactory consoleButtonFactory;

  interface GitOutputPartViewImplUiBinder extends UiBinder<Widget, GitOutputPartViewImpl> {}

  private ActionDelegate delegate;

  @UiField FlowPanel buttons;

  @UiField ScrollPanel scrollPanel;

  @UiField FlowPanel consoleLines;

  @Inject
  public GitOutputPartViewImpl(
      GitLocalizationConstant constant,
      PartStackUIResources resources,
      GitOutputPartViewImplUiBinder uiBinder,
      ConsoleButtonFactory consoleButtonFactory) {
    this.consoleButtonFactory = consoleButtonFactory;
    initWidget(uiBinder.createAndBindUi(this));

    ConsoleButton.ActionDelegate scrollBottomDelegate =
        new ConsoleButton.ActionDelegate() {
          @Override
          public void onButtonClicked() {
            delegate.onScrollClicked();
          }
        };
    createButton(resources.arrowBottom(), constant.buttonScroll(), scrollBottomDelegate);

    ConsoleButton.ActionDelegate cleanDelegate =
        new ConsoleButton.ActionDelegate() {
          @Override
          public void onButtonClicked() {
            delegate.onClearClicked();
          }
        };
    createButton(resources.erase(), constant.buttonClear(), cleanDelegate);
  }

  /** {@inheritDoc} */
  @Override
  public void print(String text) {
    PreElement pre = DOM.createElement("pre").cast();
    pre.setInnerText(text.isEmpty() ? "&nbsp;" : text);
    consoleLines.getElement().appendChild(pre);
  }

  @Override
  public void print(String text, String color) {
    PreElement pre = DOM.createElement("pre").cast();
    pre.setInnerText(text.isEmpty() ? "&nbsp;" : text);

    try {
      pre.getStyle().setColor(SimpleHtmlSanitizer.sanitizeHtml(color).asString());
    } catch (Exception e) {
      Log.error(getClass(), "Unable to set color [" + color + "]", e);
    }

    consoleLines.getElement().appendChild(pre);
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    consoleLines.clear();
  }

  /** {@inheritDoc} */
  @Override
  public void scrollBottom() {
    scrollPanel.getElement().setScrollTop(scrollPanel.getElement().getScrollHeight());
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @NotNull
  private void createButton(
      @NotNull SVGResource icon,
      @NotNull String prompt,
      @NotNull ConsoleButton.ActionDelegate delegate) {
    ConsoleButton button = consoleButtonFactory.createConsoleButton(prompt, icon);
    button.setDelegate(delegate);

    buttons.add(button);
  }
}
