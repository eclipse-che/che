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
package org.eclipse.che.ide.keybinding;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.AreaElement;
import com.google.gwt.dom.client.InputElement;
import com.google.inject.Inject;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.api.keybinding.SchemeImpl;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.input.SignalEventUtils;

/**
 * Implementation of the {@link KeyBindingAgent}.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public class KeyBindingManager implements KeyBindingAgent {

  public static final String SCHEME_ECLIPSE_ID = "ide.ui.keyBinding.eclipse";
  public static final String SCHEME_GLOBAL_ID = "ide.ui.keyBinding.global";

  private final PresentationFactory presentationFactory;

  private final Map<String, Scheme> schemes = new HashMap<>();

  private String activeScheme;
  private ActionManager actionManager;

  @Inject
  public KeyBindingManager(ActionManager actionManager) {
    this.actionManager = actionManager;

    addScheme(new SchemeImpl(SCHEME_GLOBAL_ID, "Global"));
    addScheme(new SchemeImpl(SCHEME_ECLIPSE_ID, "Eclipse Scheme"));

    // TODO check user settings
    activeScheme = SCHEME_GLOBAL_ID;

    presentationFactory = new PresentationFactory();

    // Attach the listeners.
    final Element documentElement = Elements.getDocument().getDocumentElement();
    EventListener downListener =
        new EventListener() {
          @Override
          public void handleEvent(Event event) {
            SignalEvent signalEvent = SignalEventUtils.create(event, false);
            if (signalEvent == null) {
              return;
            }

            /*
            Temporary solution to prevent calling actions if focus is in input element.
            The problem in that, some actions, may be bound to Ctrl+C/X/V/Z or Delete so
            We should allow browser to process event natively instead of calling actions.
            Need to be reworked in nearest future. */
            final JavaScriptObject jso = (JavaScriptObject) event.getTarget();
            if (InputElement.is(jso) || AreaElement.is(jso)) {
              return;
            }

            // handle event in active scheme
            int digest = CharCodeWithModifiers.computeKeyDigest(signalEvent);
            preventDefaultBrowserAction((KeyboardEvent) event, digest);

            List<String> actionIds = getActive().getActionIds(digest);

            if (!actionIds.isEmpty()) {
              runActions(actionIds, event);
            }
            // else handle event in global scheme
            else if (!(actionIds = getGlobal().getActionIds(digest)).isEmpty()) {
              runActions(actionIds, event);
            }

            // default, lets this event handle other part of the IDE
          }
        };
    if (UserAgent.isFirefox()) {
      // firefox fires keypress events
      documentElement.addEventListener(Event.KEYPRESS, downListener, true);
    } else {
      // webkit fires keydown events
      documentElement.addEventListener(Event.KEYDOWN, downListener, true);
    }
  }

  private void preventDefaultBrowserAction(KeyboardEvent keyboardEvent, int digest) {
    // prevent browser default action on Ctrl + S
    if (digest == 65651) {
      keyboardEvent.preventDefault();
    }
  }

  /**
   * Finds and runs an action cancelling original key event
   *
   * @param actionIds list containing action ids
   * @param keyEvent original key event
   */
  private void runActions(List<String> actionIds, Event keyEvent) {
    for (String actionId : actionIds) {
      Action action = actionManager.getAction(actionId);
      if (action == null) {
        continue;
      }
      ActionEvent e = new ActionEvent(presentationFactory.getPresentation(action), actionManager);
      action.update(e);

      if (e.getPresentation().isEnabled() && e.getPresentation().isVisible()) {
        /** Stop handling the key event */
        keyEvent.preventDefault();
        keyEvent.stopPropagation();

        /** Perform the action */
        action.actionPerformed(e);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public Scheme getGlobal() {
    return getScheme(SCHEME_GLOBAL_ID);
  }

  /** {@inheritDoc} */
  @Override
  public Scheme getEclipse() {
    return getScheme(SCHEME_ECLIPSE_ID);
  }

  /** {@inheritDoc} */
  @Override
  public Scheme getActive() {
    return getScheme(activeScheme);
  }

  public void setActive(String scheme) {
    if (schemes.containsKey(scheme)) {
      activeScheme = scheme;
    } else {
      // Fallback on global scheme
      activeScheme = SCHEME_GLOBAL_ID;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void addScheme(Scheme scheme) {
    schemes.put(scheme.getSchemeId(), scheme);
  }

  /** {@inheritDoc} */
  @Override
  public List<Scheme> getSchemes() {
    return new ArrayList<>(this.schemes.values());
  }

  /** {@inheritDoc} */
  @Override
  public Scheme getScheme(String id) {
    if (schemes.containsKey(id)) {
      return schemes.get(id);
    }
    return null;
  }

  /** {@inheritDoc} */
  @Nullable
  @Override
  public CharCodeWithModifiers getKeyBinding(@NotNull String actionId) {
    CharCodeWithModifiers keyBinding = getActive().getKeyBinding(actionId);
    if (keyBinding != null) return keyBinding;
    else {
      return getGlobal().getKeyBinding(actionId);
    }
  }
}
