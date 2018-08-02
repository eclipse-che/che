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
package org.eclipse.che.plugin.maven.client.comunnication.progressor.background;

import static com.google.gwt.dom.client.Style.Unit.PCT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Implementation of {@link BackgroundLoaderView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class BackgroundLoaderViewImpl implements BackgroundLoaderView {
  @UiField(provided = true)
  Resources resources;

  @UiField Label status;
  @UiField SimplePanel iconLoader;
  @UiField SimplePanel progressContainer;
  @UiField SimplePanel progress;
  @UiField SimplePanel iconClose;
  @UiField FlowPanel mainPanel;

  FlowPanel rootElement;

  private ActionDelegate delegate;

  @Inject
  public BackgroundLoaderViewImpl(LoaderViewImplUiBinder uiBinder, Resources resources) {
    this.resources = resources;

    LoaderCss styles = resources.css();
    styles.ensureInjected();

    rootElement = uiBinder.createAndBindUi(this);

    iconLoader.getElement().appendChild((resources.loaderIcon().getSvg().getElement()));
    iconClose.getElement().appendChild((resources.errorOperationIcon().getSvg().getElement()));
    iconClose.setVisible(false);

    status.addClickHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            delegate.showResolverInfo();
          }
        });
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void hide() {
    mainPanel.setVisible(false);
  }

  @Override
  public void show() {
    mainPanel.setVisible(true);
  }

  @Override
  public void setOperationLabel(String text) {
    status.setText(text);
  }

  @Override
  public void updateProgressBar(int percent) {
    progress.getElement().getStyle().setWidth(percent, PCT);
  }

  @Override
  public Widget asWidget() {
    return rootElement;
  }

  /** Styles for loader. */
  public interface LoaderCss extends CssResource {
    String statusLabel();

    String iconLoader();

    String iconClose();

    String progressContainer();

    String progressBar();
  }

  /** Resources for the loader. */
  public interface Resources extends ClientBundle {
    @Source({"Loader.css"})
    LoaderCss css();

    @Source("loaderIcon.svg")
    SVGResource loaderIcon();

    @Source("error.svg")
    SVGResource errorOperationIcon();
  }

  interface LoaderViewImplUiBinder extends UiBinder<FlowPanel, BackgroundLoaderViewImpl> {}
}
