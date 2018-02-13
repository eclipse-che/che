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
package org.eclipse.che.ide.preferences;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;

/**
 * PreferencesPresenter is presentation of preference pages. It manages preference pages. It's
 * responsible for the communication user and wizard page. In typical usage, the client instantiates
 * this class with list of preferences. The presenter serves as the preference page container and
 * orchestrates the presentation of its pages.
 *
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
@Singleton
public class PreferencesPresenter
    implements PreferencesView.ActionDelegate, PreferencePagePresenter.DirtyStateListener {

  private final PreferencesView view;
  private final Set<PreferencePagePresenter> preferences;
  private final Set<PreferencesManager> managers;
  private final Provider<NotificationManager> notificationManagerProvider;

  private Map<String, Set<PreferencePagePresenter>> preferencesMap;

  private DialogFactory dialogFactory;

  private CoreLocalizationConstant locale;

  /**
   * Create presenter.
   *
   * <p>For tests.
   *
   * @param view
   * @param preferences
   * @param dialogFactory
   * @param locale
   * @param managers
   */
  @Inject
  protected PreferencesPresenter(
      PreferencesView view,
      Set<PreferencePagePresenter> preferences,
      DialogFactory dialogFactory,
      CoreLocalizationConstant locale,
      Set<PreferencesManager> managers,
      Provider<NotificationManager> notificationManagerProvider) {
    this.view = view;
    this.preferences = preferences;
    this.dialogFactory = dialogFactory;
    this.locale = locale;
    this.managers = managers;
    this.notificationManagerProvider = notificationManagerProvider;
    this.view.setDelegate(this);
    for (PreferencePagePresenter preference : preferences) {
      preference.setUpdateDelegate(this);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onDirtyChanged() {
    for (PreferencePagePresenter p : preferences) {
      if (p.isDirty()) {
        view.enableSaveButton(true);
        return;
      }
    }
    view.enableSaveButton(false);
  }

  /** {@inheritDoc} */
  @Override
  public void onPreferenceSelected(PreferencePagePresenter preference) {
    preference.go(view.getContentPanel());
  }

  /** Shows preferences. */
  public void showPreferences() {
    if (preferencesMap != null) {
      view.showDialog();
      return;
    }

    preferencesMap = new HashMap<>();
    for (PreferencePagePresenter preference : preferences) {
      Set<PreferencePagePresenter> prefsList = preferencesMap.get(preference.getCategory());
      if (prefsList == null) {
        prefsList = new HashSet<PreferencePagePresenter>();
        preferencesMap.put(preference.getCategory(), prefsList);
      }

      prefsList.add(preference);
    }
    view.setPreferences(preferencesMap);

    view.enableSaveButton(false);
    view.selectPreference(preferencesMap.entrySet().iterator().next().getValue().iterator().next());
    view.showDialog();
  }

  @Override
  public void onSaveClicked() {
    for (PreferencePagePresenter preference : preferences) {
      if (preference.isDirty()) {
        preference.storeChanges();
      }
    }

    Promise<Void> promise = Promises.resolve(null);
    final List<PromiseError> promiseErrorList = new ArrayList<>();

    for (final PreferencesManager preferencesManager : managers) {
      promise =
          promise.thenPromise(
              new Function<Void, Promise<Void>>() {
                @Override
                public Promise<Void> apply(Void arg) throws FunctionException {
                  return preferencesManager
                      .flushPreferences()
                      .catchError(
                          new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError error) throws OperationException {
                              notificationManagerProvider
                                  .get()
                                  .notify(
                                      locale.unableToSavePreference(),
                                      error.getMessage(),
                                      FAIL,
                                      FLOAT_MODE);
                              promiseErrorList.add(error);
                            }
                          });
                }
              });
    }

    promise.then(
        new Operation<Void>() {
          @Override
          public void apply(Void aVoid) throws OperationException {
            if (promiseErrorList.isEmpty()) {
              view.enableSaveButton(false);
            }
          }
        });
  }

  @Override
  public void onRefreshClicked() {
    Promise<Map<String, String>> promise = Promises.resolve(null);
    for (final PreferencesManager preferencesManager : managers) {
      promise =
          promise.thenPromise(
              new Function<Map<String, String>, Promise<Map<String, String>>>() {
                @Override
                public Promise<Map<String, String>> apply(Map<String, String> arg)
                    throws FunctionException {
                  return preferencesManager
                      .loadPreferences()
                      .catchError(
                          new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError error) throws OperationException {
                              notificationManagerProvider
                                  .get()
                                  .notify(
                                      locale.unableToLoadPreference(),
                                      error.getMessage(),
                                      FAIL,
                                      FLOAT_MODE);
                            }
                          });
                }
              });
    }

    /** Revert changes on every preference page */
    promise.then(
        new Operation<Map<String, String>>() {
          @Override
          public void apply(Map<String, String> arg) throws OperationException {
            for (PreferencePagePresenter p : PreferencesPresenter.this.preferences) {
              p.revertChanges();
            }
          }
        });
  }

  /** {@inheritDoc} */
  @Override
  public void onCloseClicked() {
    boolean haveUnsavedData = false;
    for (PreferencePagePresenter preference : preferences) {
      if (preference.isDirty()) {
        haveUnsavedData = true;
        break;
      }
    }
    if (haveUnsavedData) {
      dialogFactory
          .createConfirmDialog(
              "",
              locale.messagesPromptSaveChanges(),
              locale.yesButtonTitle(),
              locale.noButtonTitle(),
              getConfirmCallback(),
              getCancelCallback())
          .show();
    } else {
      view.close();
    }
  }

  private ConfirmCallback getConfirmCallback() {
    return new ConfirmCallback() {
      @Override
      public void accepted() {
        for (PreferencePagePresenter preference : preferences) {
          if (preference.isDirty()) {
            preference.storeChanges();
          }
        }
        view.enableSaveButton(false);
        view.close();
      }
    };
  }

  private CancelCallback getCancelCallback() {
    return new CancelCallback() {
      @Override
      public void cancelled() {
        for (PreferencePagePresenter preference : preferences) {
          if (preference.isDirty()) {
            preference.revertChanges();
          }
        }
        view.close();
      }
    };
  }

  /** {@inheritDoc} */
  @Override
  public void onCloseWindow() {
    onCloseClicked();
  }
}
