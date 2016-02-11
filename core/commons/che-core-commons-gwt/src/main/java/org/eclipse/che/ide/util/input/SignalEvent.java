/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.che.ide.util.input;

import org.eclipse.che.ide.util.browser.UserAgent;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;


/**
 * Attempts to bring sanity to the incredibly complex and inconsistent world of
 * browser events, especially with regards to key events.
 * <p/>
 * {@link SignalEventImpl}
 *
 * @author danilatos@google.com (Daniel Danilatos)
 */
public interface SignalEvent {

    /**
     * The "type" of key signal. The type is defined by us, based on a convenient
     * classification of these events.
     */
    public enum KeySignalType {
        /**
         * Typing in the form of inputting text
         * <p/>
         * NOTE: "TAB" is treated as INPUT.
         */
        INPUT,
        /** Moving around */
        NAVIGATION,
        /** Deleting text (Backspace + Delete) */
        DELETE,
        /** Other, like ESC or F3, that by themselves will do nothing to the document */
        NOEFFECT,
        /** Sentinal for debugging purposes only */
        SENTINAL
    }

    /**
     * The "movement unit" of this event. For example, on windows and linux,
     * holding down CTRL whilst navigating left and right with the arrow
     * keys moves a word at a time. Even more importantly, move units
     * are used for deleting text.
     */
    public enum MoveUnit {
        /** A character at a time */
        CHARACTER,
        /** A word at a time */
        WORD,
        /** To the start or end of the line */
        LINE,
        /** A "page" at a time */
        PAGE,
        /** To the start or end of the document */
        DOCUMENT
    }

    /**
     * These are the currently supported modifier combinations
     * <p/>
     * Supporting more requires tricky, browser-specific code. Go to the experiment harness
     * and have a look at the wonders....
     */
    public enum KeyModifier {
        /** No modifiers are pressed */
        NONE {
            @Override
            public boolean check(SignalEvent event) {
                return !event.getShiftKey() && !event.getAltKey()
                       && !event.getCtrlKey() && !event.getMetaKey();
            }
        },
        /** Only shift is pressed */
        SHIFT {
            @Override
            public boolean check(SignalEvent event) {
                return event.getShiftKey() && !event.getAltKey()
                       && !event.getCtrlKey() && !event.getMetaKey();
            }
        },
        /** Only ctrl is pressed */
        CTRL {
            @Override
            public boolean check(SignalEvent event) {
                return !event.getShiftKey() && !event.getAltKey()
                       && event.getCtrlKey() && !event.getMetaKey();
            }
        },
        /** Only alt is pressed */
        ALT {
            @Override
            public boolean check(SignalEvent event) {
                return !event.getShiftKey() && event.getAltKey()
                       && !event.getCtrlKey() && !event.getMetaKey();
            }
        },
        /** Only the meta key is pressed */
        META {
            @Override
            public boolean check(SignalEvent event) {
                return !event.getShiftKey() && !event.getAltKey()
                       && !event.getCtrlKey() && event.getMetaKey();
            }
        },
        /**
         * Only the "command" key is pressed
         *
         * @see SignalEvent#COMMAND_IS_CTRL
         */
        COMMAND {
            @Override
            public boolean check(SignalEvent event) {
                return COMMAND_IS_CTRL ? CTRL.check(event) : META.check(event);
            }
        },
        /** Both ctrl and alt, but no others, are pressed */
        CTRL_ALT {
            @Override
            public boolean check(SignalEvent event) {
                return !event.getShiftKey() && event.getAltKey()
                       && event.getCtrlKey() && !event.getMetaKey();
            }
        },
        /** Both "command" and shift, but no others, are pressed */
        COMMAND_SHIFT {
            @Override
            public boolean check(SignalEvent event) {
                // The ctrl/meta key which is NOT the command key
                boolean notCommandKey = COMMAND_IS_CTRL ? event.getMetaKey() : event.getCtrlKey();
                return event.getShiftKey() && !event.getAltKey()
                       && event.getCommandKey() && !notCommandKey;
            }
        };

        /** Whether the "command" key is the control key (as opposed to the meta key). */
        // TODO(danilatos): Reconcile this with the value in SignalKeyLogic.
        private static final boolean COMMAND_IS_CTRL = !UserAgent.isMac();

        /**
         * Check if the given event has the enum value's modifiers pressed.
         *
         * @param event
         * @return true if they are pressed
         */
        public abstract boolean check(SignalEvent event);
    }


    /** @return Event type as a string, e.g. "keypress" */
    String getType();

    /** @return The target element of the event */
    Element getTarget();

    /**
     * @return true if the event is a key event
     *         TODO(danilatos): Have a top level EventSignalType enum
     */
    boolean isKeyEvent();

    /** @return true if it is an IME composition event */
    boolean isCompositionEvent();

    /**
     * Returns true if the key event is an IME input event.
     * Only makes sense to call this method if this is a key signal.
     * Does not work on FF. (TODO(danilatos): Can it be done? Tricks
     * with dom mutation events?)
     *
     * @return true if this is an IME input event
     */
    boolean isImeKeyEvent();

    /**
     * @return true if this is a mouse event
     *         TODO(danilatos): Have a top level EventSignalType enum
     */
    boolean isMouseEvent();

    /**
     * TODO(danilatos): Click + drag? I.e. return true for mouse move, if the
     * button is pressed? (this might be useful for tracking changing selections
     * as the user holds & drags)
     *
     * @return true if this is an event involving some use of mouse buttons
     */
    boolean isMouseButtonEvent();

    /** @return true if this is a mouse event but not {@link #isMouseButtonEvent()} */
    boolean isMouseButtonlessEvent();

    /** @return true if this is a "click" event */
    boolean isClickEvent();

    /** @return True if this is a dom mutation event */
    boolean isMutationEvent();

    /** @return true if this is any sort of clipboard event */
    boolean isClipboardEvent();

    /** @return If this is a focus event */
    boolean isFocusEvent();

    /**
     * @return true if this is a paste event
     *         TODO(danilatos): Make a ClipboardSignalType enum instead
     */
    boolean isPasteEvent();

    /**
     * @return true if this is a cut event
     *         TODO(danilatos): Make a ClipboardSignalType enum instead
     */
    boolean isCutEvent();

    /**
     * @return true if this is a copy event
     *         TODO(danilatos): Make a ClipboardSignalType enum instead
     */
    boolean isCopyEvent();

    /**
     * @return true if the command key is depressed
     * @see #COMMAND_IS_CTRL
     */
    boolean getCommandKey();

    /** @return true if the ctrl key is depressed */
    boolean getCtrlKey();

    /** @return true if the meta key is depressed */
    boolean getMetaKey();

    /** @return true if the alt key is depressed */
    boolean getAltKey();

    /** @return true if the shift key is depressed */
    boolean getShiftKey();

    /**
     * TODO(user): Deprecate this, as it breaks abstraction. Also prevent people
     * from casting back and forth.
     *
     * @return The underlying event view of this event
     */
    Event asEvent();

    /**
     * Only valid for key events.
     * Currently only implemented for deleting, not actual navigating.
     *
     * @return The move unit of this event
     */
    MoveUnit getMoveUnit();

    /**
     * @return True if the event is the key combo for "undo"
     *         NOTE(danilatos): This is the best we have at detecting undo events :(
     *         We need even more special handling for undo done via the menu :( :(
     */
    boolean isUndoCombo();

    /** @return True if the event is the key combo for "redo" */
    boolean isRedoCombo();

    /**
     * @param letter
     *         Treated case-insensitive, including things like '1' vs '!'
     *         User may provide either, but upper case for letters and unshifted for
     *         other keys is recommended
     * @param modifier
     * @return True if the given letter is pressed, and only the given modifiers.
     */
    boolean isCombo(int letter, KeyModifier modifier);

    /**
     * @param letter
     * @return true, if the given letter was pressed without modifiers. Takes into
     *         account the caps lock key being pressed (it will be as if it
     *         weren't pressed)
     */
    boolean isOnly(int letter);

    /**
     * @return The gwtKeyCode of this event, with some minor compatibility
     *         adjustments
     */
    int getKeyCode();

    /**
     * @return the mouse button bit field for this event.  The masks used to extract
     *         individual buttons from the field are {@link NativeEvent#BUTTON_LEFT},
     *         {@link NativeEvent#BUTTON_MIDDLE}, and {@link NativeEvent#BUTTON_RIGHT}.
     */
    int getMouseButton();

    /**
     * @return The key signal type of this even, or null if it is not a key event
     * @see KeySignalType
     */
    KeySignalType getKeySignalType();

    /**
     * Prevent this event from propagating or, if that is impossible, ensures that
     * {@link SignalEventImpl#create(Event, boolean)} will subsequently return null
     * for the same corresponding event object.
     */
    void stopPropagation();

    /**
     * Prevents the browser from taking its default action for the given event.
     * Under some circumstances (see {@link SignalEventImpl#isPreventDefaultEffective}
     * this may also stop propagation.
     * <p/>
     * See also {@link #stopPropagation()}.
     */
    public void preventDefault();
}
