/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.keybinding;

import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.AreaElement;
import com.google.gwt.dom.client.InputElement;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.ide.util.input.SignalEventUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Implementation of the {@link KeyBindingAgent}.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class KeyBindingManager implements KeyBindingAgent {

    private final PresentationFactory          presentationFactory;
    private final Provider<PerspectiveManager> perspectiveManager;

    private SchemeImpl    globalScheme;
    private SchemeImpl    activeScheme;
    private SchemeImpl    eclipseScheme;
    private ActionManager actionManager;

    @Inject
    public KeyBindingManager(ActionManager actionManager, Provider<PerspectiveManager> perspectiveManager) {
        this.actionManager = actionManager;
        this.perspectiveManager = perspectiveManager;
        globalScheme = new SchemeImpl("ide.ui.keyBinding.global", "Global");
        eclipseScheme = new SchemeImpl("ide.ui.keyBinding.eclipse", "Eclipse Scheme");
        //TODO check user settings
        activeScheme = eclipseScheme;

        presentationFactory = new PresentationFactory();

        // Attach the listeners.
        final Element documentElement = Elements.getDocument().getDocumentElement();
        EventListener downListener = new EventListener() {
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
                final JavaScriptObject jso = (JavaScriptObject)event.getTarget();
                if (InputElement.is(jso) || AreaElement.is(jso)) {
                    return;
                }

                //handle event in active scheme
                int digest = CharCodeWithModifiers.computeKeyDigest(signalEvent);
                preventDefaultBrowserAction((KeyboardEvent)event, digest);

                List<String> actionIds = activeScheme.getActionIds(digest);

                if (!actionIds.isEmpty()) {
                    runActions(actionIds, event);
                }
                //else handle event in global scheme
                else if (!(actionIds = globalScheme.getActionIds(digest)).isEmpty()) {
                    runActions(actionIds, event);
                }

                //default, lets this event handle other part of the IDE
            }
        };
        if (UserAgent.isFirefox()) {
            // firefox fires keypress events
            documentElement.addEventListener(Event.KEYPRESS, downListener, true);
        } else {
            //webkit fires keydown events
            documentElement.addEventListener(Event.KEYDOWN, downListener, true);
        }
    }

    private void preventDefaultBrowserAction(KeyboardEvent keyboardEvent, int digest) {
        //prevent browser default action on Ctrl + S
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
            ActionEvent e = new ActionEvent(presentationFactory.getPresentation(action), actionManager, perspectiveManager.get());
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
        return globalScheme;
    }

    /** {@inheritDoc} */
    @Override
    public Scheme getEclipse() {
        return eclipseScheme;
    }

    @Override
    public Scheme getActive() {
        return activeScheme;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public CharCodeWithModifiers getKeyBinding(@NotNull String actionId) {
        CharCodeWithModifiers keyBinding = activeScheme.getKeyBinding(actionId);
        if (keyBinding != null)
            return keyBinding;
        else {
            return globalScheme.getKeyBinding(actionId);
        }
    }
}
