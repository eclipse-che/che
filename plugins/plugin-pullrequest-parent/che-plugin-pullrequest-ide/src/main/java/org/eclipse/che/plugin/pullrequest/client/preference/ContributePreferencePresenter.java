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
package org.eclipse.che.plugin.pullrequest.client.preference;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.valueOf;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.parts.PartStack.State.HIDDEN;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.plugin.pullrequest.client.ContributeMessages;

/**
 * Preference page presenter for the Contribute Part.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ContributePreferencePresenter extends AbstractPreferencePagePresenter
    implements ContributePreferenceView.ActionDelegate, PartStackStateChangedEvent.Handler {
  public static final String ACTIVATE_BY_PROJECT_SELECTION =
      "git.contribute.activate.projectSelection";

  private ContributePreferenceView view;
  private ContributeMessages localizationConstants;
  private PreferencesManager preferencesManager;

  private PartStack toolingPartStack;
  private NotificationManager notificationManager;
  private boolean isActivateByProjectSelection;

  @Inject
  public ContributePreferencePresenter(
      EventBus eventBus,
      WorkspaceAgent workspaceAgent,
      ContributePreferenceView view,
      ContributeMessages localizationConstants,
      PreferencesManager preferencesManager,
      NotificationManager notificationManager) {
    super(
        localizationConstants.contributePreferencesTitle(),
        localizationConstants.contributePreferencesCategory());

    this.view = view;
    this.localizationConstants = localizationConstants;
    this.preferencesManager = preferencesManager;
    this.toolingPartStack = workspaceAgent.getPartStack(TOOLING);
    this.notificationManager = notificationManager;

    view.setDelegate(this);
    eventBus.addHandler(PartStackStateChangedEvent.TYPE, this);
    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> init());
  }

  private void init() {
    String preference = preferencesManager.getValue(ACTIVATE_BY_PROJECT_SELECTION);
    if (isNullOrEmpty(preference)) {
      preference = "true";
      preferencesManager.setValue(ACTIVATE_BY_PROJECT_SELECTION, preference);
    }

    isActivateByProjectSelection = parseBoolean(preference);
  }

  @Override
  public boolean isDirty() {
    String preference = preferencesManager.getValue(ACTIVATE_BY_PROJECT_SELECTION);
    boolean storedValue = isNullOrEmpty(preference) || parseBoolean(preference);

    return isActivateByProjectSelection != storedValue;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setActivateByProjectSelection(isActivateByProjectSelection);
  }

  @Override
  public void storeChanges() {
    preferencesManager.setValue(
        ACTIVATE_BY_PROJECT_SELECTION, valueOf(isActivateByProjectSelection));
  }

  @Override
  public void revertChanges() {
    boolean storedValue = parseBoolean(preferencesManager.getValue(ACTIVATE_BY_PROJECT_SELECTION));

    isActivateByProjectSelection = storedValue;
    view.setActivateByProjectSelection(storedValue);
  }

  @Override
  public void onActivateByProjectSelectionChanged(boolean isActivated) {
    isActivateByProjectSelection = isActivated;
    delegate.onDirtyChanged();
  }

  @Override
  public void onPartStackStateChanged(PartStackStateChangedEvent event) {
    if (!isActivateByProjectSelection || !event.isUserInteraction()) {
      return;
    }

    if (toolingPartStack != null
        && toolingPartStack.equals(event.getPartStack())
        && toolingPartStack.getPartStackState() == HIDDEN) {
      isActivateByProjectSelection = false;
      view.setActivateByProjectSelection(false);

      preferencesManager.setValue(ACTIVATE_BY_PROJECT_SELECTION, "false");
      preferencesManager.flushPreferences();
      notificationManager.notify(
          "",
          localizationConstants.contributePreferencesNotificationActivatePartText(),
          SUCCESS,
          EMERGE_MODE);
    }
  }
}
