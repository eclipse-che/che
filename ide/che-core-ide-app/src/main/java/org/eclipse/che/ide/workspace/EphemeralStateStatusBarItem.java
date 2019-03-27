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
package org.eclipse.che.ide.workspace;

import static java.lang.Boolean.parseBoolean;

import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.model.WorkspaceConfigImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

/**
 * Status bar action that indicates ephemeral state of the pvc volume. Ephemeral means that pvc
 * volume is not persisted after workspace stop.
 *
 * @author Vlad Zhukovskyi
 * @since 7.0.0-beta-3.0
 */
@Singleton
public class EphemeralStateStatusBarItem extends BaseAction implements CustomComponentAction {

  private final AppContext appContext;
  private final Locale locale;
  private final HorizontalPanel panel;

  private static final String PERSIST_VOLUMES_ATTRIBUTE = "persistVolumes";

  @Inject
  public EphemeralStateStatusBarItem(AppContext appContext, Locale locale) {
    super();
    this.appContext = appContext;
    this.locale = locale;

    panel = new HorizontalPanel();
    panel.ensureDebugId("statusBarEphemeralStatePanel");
  }

  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    return panel;
  }

  @Override
  public void update(ActionEvent e) {
    super.update(e);
    panel.clear();

    WorkspaceImpl workspace = appContext.getWorkspace();
    if (workspace == null) {
      return;
    }

    WorkspaceConfigImpl workspaceConfig = workspace.getConfig();
    if (workspaceConfig == null) {
      return;
    }

    Map<String, String> attributes = workspaceConfig.getAttributes();
    if (attributes.containsKey(PERSIST_VOLUMES_ATTRIBUTE)
        && !parseBoolean(attributes.get(PERSIST_VOLUMES_ATTRIBUTE))) {
      Widget icon = new HTML(FontAwesome.EXCLAMATION_TRIANGLE);
      icon.getElement().getStyle().setMarginRight(5., Style.Unit.PX);
      icon.getElement().getStyle().setColor("#fcc13d");
      panel.add(icon);

      Label ephemeralMode = new Label(locale.label());
      ephemeralMode.setTitle(locale.description());
      ephemeralMode.ensureDebugId("statusBarEphemeralStatePanelEphemeralMode");
      ephemeralMode.getElement().getStyle().setMarginRight(5., Style.Unit.PX);
      ephemeralMode.getElement().getStyle().setColor("#fcc13d");
      panel.add(ephemeralMode);
    }
  }

  interface Locale extends Messages {
    @DefaultMessage("Ephemeral Mode")
    String label();

    @DefaultMessage(
        "All changes to the source code will be lost when the workspace is stopped unless they are pushed to a source code repository.")
    String description();
  }
}
