// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.ui.tree;

import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.KeyboardEvent;
import elemental.dom.Element;
import elemental.html.InputElement;

import org.eclipse.che.ide.util.dom.DomUtils;
import org.eclipse.che.ide.util.dom.Elements;
import com.google.gwt.resources.client.CssResource;

/**
 * Utility for mutating a tree data model object backing a
 * {@link TreeNodeElement}, in place in a {@link Tree}.
 */
public class TreeNodeMutator<D> {

    /** Encapsulates all needed data to perform a mutation action. */
    public interface MutationAction<D> {

        Element getElementForMutation(TreeNodeElement<D> node);

        void onBeforeMutation(TreeNodeElement<D> node);

        void onMutationCommit(TreeNodeElement<D> node, String oldLabel, String newLabel);

        boolean passValidation(TreeNodeElement<D> node, String newLabel);
    }

    /** Css for the mutator. */
    public interface Css extends CssResource {
        String nodeNameInput();
    }

    private final EventListener keyListener = new EventListener() {
        @Override
        public void handleEvent(Event evt) {
            KeyboardEvent keyEvent = (KeyboardEvent)evt;
            switch (keyEvent.getKeyCode()) {
                case KeyboardEvent.KeyCode.ENTER:
                    commitIfValid(false);
                    break;
                case KeyboardEvent.KeyCode.ESC:
                    cancel();
                    break;
                default:
                    return;
            }
            evt.stopPropagation();
        }
    };

    private final EventListener blurListener = new EventListener() {
        @Override
        public void handleEvent(Event evt) {
            commitIfValid(false);
        }
    };

    private static class State<D> {
        final TreeNodeElement<D> node;
        final MutationAction<D>  callback;
        final InputElement       input;
        final String             oldLabel;

        State(TreeNodeElement<D> node, MutationAction<D> callback, InputElement input,
              String oldLabel) {
            this.node = node;
            this.callback = callback;
            this.input = input;
            this.oldLabel = oldLabel;
        }
    }

    private State<D> state;
    private Css      css;

    /**
     * @param css
     *         can be null
     */
    public TreeNodeMutator(Css css) {
        this.css = css;
    }

    public boolean isMutating() {
        return state != null;
    }

    /**
     * Replaces the nodes text label with an input box to allow the user to
     * rename the node.
     */
    public void enterMutation(TreeNodeElement<D> node, MutationAction<D> callback) {
        // If we are already mutating, return.
        if (isMutating()) {
            return;
        }

        Element element = callback.getElementForMutation(node);
        if (element == null) {
            return;
        }

        String oldLabel = element.getTextContent();
        callback.onBeforeMutation(node);

        // Make a temporary text input box to grab user input, and place it inside
        // the label element.
        InputElement input = Elements.createInputElement();
        if (css != null) {
            input.setClassName(css.nodeNameInput());
        }
        input.setType("text");
        input.setValue(oldLabel);

        // Wipe the content from the element.
        element.setTextContent("");

        // Attach the temporary input box.
        element.appendChild(input);
        input.focus();
        input.select();

        // If we hit enter, commit the action.
        input.addEventListener(Event.KEYUP, keyListener, false);

        // If we lose focus, commit the action.
        input.addEventListener(Event.BLUR, blurListener, false);

        state = new State<D>(node, callback, input, oldLabel);
    }

    /** Cancels the mutation, if any. */
    public void cancel() {
        if (!isMutating()) {
            return;
        }
        state.input.setValue(state.oldLabel);
        forceCommit();
    }

    /** Commits the current text if it passes validation, or cancels the mutation. */
    public void forceCommit() {
        commitIfValid(true);
    }

    private void commitIfValid(boolean forceCommit) {
        if (!isMutating()) {
            return;
        }

        State<D> oldState = state;
        state = null;

        // Update the node label and commit the change if it passes validation.
        boolean passedValidation =
                oldState.callback.passValidation(oldState.node, oldState.input.getValue());
        if (passedValidation || forceCommit) {

            // Disconnect the handlers first!
            oldState.input.removeEventListener(Event.KEYUP, keyListener, false);
            oldState.input.removeEventListener(Event.BLUR, blurListener, false);

            // Detach the input box. Note that on Chrome this synchronously dispatches
            // a blur event. The guard above saves us.
            DomUtils.removeFromParent(oldState.input);

            String newLabel = passedValidation ? oldState.input.getValue() : oldState.oldLabel;
            oldState.callback.onMutationCommit(oldState.node, oldState.oldLabel, newLabel);
        } else {
            state = oldState;
            state.input.focus();
            state.input.select();
        }
    }
}
