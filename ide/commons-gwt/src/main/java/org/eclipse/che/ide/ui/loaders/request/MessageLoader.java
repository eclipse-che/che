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
package org.eclipse.che.ide.ui.loaders.request;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Loader that contains configurable message. To obtain the instance of this loader you need to
 * inject {@link LoaderFactory} and call one from two methods. First one creates loader with default
 * message. Second one allow to pass own loader initial message. Loader will be showed to user if
 * total operation time is more that 0.5 second.
 *
 * @author Vlad Zhukovskiy
 */
public class MessageLoader implements AsyncRequestLoader {

  public static final String DEF_MESSAGE = "Loading...";
  public static final String MESSAGE_DBG_ID = "loader-message";
  public static final String GLASS_DBG_ID = "glass-panel";
  public static final String LOADER_DBG_ID = "loader-panel";
  public static final int SHOW_DELAY = 500; // 500ms

  private boolean showing = false;

  private final Label label = new Label();
  private final FlowPanel glass = new FlowPanel();
  private final FlowPanel loader = new FlowPanel();

  // show loader if it works more than 0.5 second
  private final DelayedTask showLoader =
      new DelayedTask() {
        @Override
        public void onExecute() {
          RootPanel.get().add(glass);
          showing = true;
        }
      };

  @AssistedInject
  public MessageLoader(MessageLoaderResources resources) {
    this(DEF_MESSAGE, resources);
  }

  @AssistedInject
  public MessageLoader(@Assisted String message, MessageLoaderResources resources) {
    label.ensureDebugId(MESSAGE_DBG_ID);
    label.setText(message == null || message.isEmpty() ? DEF_MESSAGE : message);
    label.setStyleName(resources.Css().label());
    loader.setStyleName(resources.Css().loader());

    FlowPanel loaderSvg = new FlowPanel();
    loaderSvg.setStyleName(resources.Css().loaderSvg());
    loaderSvg.add(new SVGImage(resources.loader()));

    loader.ensureDebugId(LOADER_DBG_ID);
    loader.add(loaderSvg);
    loader.add(label);

    glass.ensureDebugId(GLASS_DBG_ID);
    glass.add(loader);
    glass.setStyleName(resources.Css().glass());
  }

  /** {@inheritDoc} */
  @Override
  public void show() {
    show(null);
  }

  /** {@inheritDoc} */
  @Override
  public void show(String message) {
    setMessage(message);

    if (!showing) {
      showLoader.delay(SHOW_DELAY);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void hide() {
    if (showing) {
      glass.removeFromParent();
      showing = false;
      return;
    }

    showLoader.cancel();
  }

  /** {@inheritDoc} */
  @Override
  public void setMessage(String message) {
    if (message != null && !message.isEmpty()) {
      label.setText(message);
    }
  }
}
