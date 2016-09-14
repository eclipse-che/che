/*
 * Copyright 2008 Google Inc.
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

import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.util.browser.QuirksConstants;
import org.eclipse.che.ide.util.browser.UserAgent;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

import java.util.HashSet;
import java.util.Set;

/**
 * Attempts to bring sanity to the incredibly complex and inconsistent world of
 * browser events, especially with regards to key events.
 * <p/>
 * A new concept of the "signal" is introduced. A signal is basically an event,
 * but an event that we actually care about, with the information we care about.
 * Redundant events are merged into a single signal. For key events, a signal
 * corresponds to the key-repeat signal we get from the keyboard. For normal
 * typing input, this will always be the keypress event. For other types of key
 * events, it depends on the browser. For clipboard events, the "beforeXYZ" and
 * "XYZ" events are merged into a single one, the one that actually happens
 * right before the action (browser dependent). Key events are also classified
 * into subtypes identified by KeySignalType. This reflects the intended usage
 * of the event, not something to do with the event data itself.
 * <p/>
 * Currently the "filtering" needs to be done manually - simply construct a
 * signal from an event using {@link #create(Event, boolean)}, and if it returns null,
 * drop the event and do nothing with it (cancelling bubbling might be a good
 * idea though).
 * <p/>
 * NOTE(danilatos): getting the physical key pressed, even on a key down, is
 * inherently not possible without a big lookup table, because of international
 * input methods. e.g. press 'b' but in greek mode on safari on osx. nothing in
 * any of the events you receive will tell you it was a 'b', instead, you'll get
 * a beta for the keypress and 0 (zero) for the keydown. mmm, useful!
 * <p/>
 * TODO(danilatos): Hook this into the application's event plumbing in a more
 * invasive manner.
 *
 * @author danilatos@google.com (Daniel Danilatos)
 */
public class SignalEventImpl implements SignalEvent {

    public interface SignalEventFactory<T extends SignalEventImpl> {
        T create();
    }

    public static SignalEventFactory<SignalEventImpl> DEFAULT_FACTORY = new SignalEventFactory<SignalEventImpl>() {
        @Override
        public SignalEventImpl create() {
            return new SignalEventImpl();
        }
    };

    interface NativeEvent {
        String getType();

        int getButton();

        boolean getCtrlKey();

        boolean getMetaKey();

        boolean getAltKey();

        boolean getShiftKey();

        void preventDefault();

        void stopPropagation();
    }

    /**
     * @param event
     * @return True if the given event is a key event
     */
    public static boolean isKeyEvent(Event event) {
        return KEY_EVENTS.contains(event.getType());
    }

    private static final SignalKeyLogic.UserAgentType currentUserAgent =
            (UserAgent.isWebkit() ? SignalKeyLogic.UserAgentType.WEBKIT : (UserAgent
                                                                                   .isFirefox()
                                                                           ? SignalKeyLogic.UserAgentType.GECKO
                                                                           : SignalKeyLogic.UserAgentType.IE));

    private static final SignalKeyLogic.OperatingSystem currentOs =
            (UserAgent.isWin() ? SignalKeyLogic.OperatingSystem.WINDOWS : (UserAgent.isMac()
                                                                           ? SignalKeyLogic.OperatingSystem.MAC
                                                                           : SignalKeyLogic.OperatingSystem.LINUX));

    private static final SignalKeyLogic logic = new SignalKeyLogic(currentUserAgent, currentOs,
                                                                   QuirksConstants.COMMAND_COMBO_DOESNT_GIVE_KEYPRESS);

    /** This variable will be filled with mappings of unshifted keys to their shifted versions. */
    private static final int[] shiftMappings = new int[128];

    static {
        for (int a = 'A'; a <= 'Z'; a++) {
            shiftMappings[a] = a + 'a' - 'A';
        }
        // TODO(danilatos): Who knows what these mappings should be on other
        // keyboard layouts... e.g. pound signs? euros? etc? argh!
        shiftMappings['1'] = '!';
        shiftMappings['2'] = '@';
        shiftMappings['3'] = '#';
        shiftMappings['4'] = '$';
        shiftMappings['5'] = '%';
        shiftMappings['6'] = '^';
        shiftMappings['7'] = '&';
        shiftMappings['8'] = '*';
        shiftMappings['9'] = '(';
        shiftMappings['0'] = ')';
        shiftMappings['`'] = '~';
        shiftMappings['-'] = '_';
        shiftMappings['='] = '+';
        shiftMappings['['] = '{';
        shiftMappings[']'] = '}';
        shiftMappings['\\'] = '|';
        shiftMappings[';'] = ':';
        shiftMappings['\''] = '"';
        shiftMappings[','] = '<';
        shiftMappings['.'] = '>';
        shiftMappings['/'] = '?';
        // invalidate the inverse mappings
        for (int i = 1; i < shiftMappings.length; i++) {
            int m = shiftMappings[i];
            if (m > 0) {
                shiftMappings[m] = i;
            }
        }
    }

    private static final Set<String> KEY_EVENTS = new HashSet<>();

    private static final Set<String> COMPOSITION_EVENTS = new HashSet<>();

    private static final Set<String> MOUSE_EVENTS = new HashSet<>();

    private static final Set<String> MOUSE_BUTTON_EVENTS = new HashSet<>();

    private static final Set<String> MOUSE_BUTTONLESS_EVENTS = new HashSet<>();

    private static final Set<String> FOCUS_EVENTS = new HashSet<>();

    private static final Set<String> CLIPBOARD_EVENTS = new HashSet<>();

    /**
     * Events affected by
     * {@link QuirksConstants#CANCEL_BUBBLING_CANCELS_IME_COMPOSITION_AND_CONTEXTMENU}.
     */
    private static final Set<String> CANCEL_BUBBLE_QUIRKS = new HashSet<>();

    static {
        for (String e : new String[]{"keydown", "keypress", "keyup"}) {
            KEY_EVENTS.add(e);
        }
        for (String e : new String[]{"compositionstart", "compositionend", "compositionupdate", "text"}) {
            COMPOSITION_EVENTS.add(e);
            CANCEL_BUBBLE_QUIRKS.add(e);
        }
        COMPOSITION_EVENTS.add("textInput");
        CANCEL_BUBBLE_QUIRKS.add("contextmenu");
        for (String e : new String[]{"mousewheel", "DOMMouseScroll", "mousemove", "mouseover", "mouseout",
      /* not strictly a mouse event*/"contextmenu"}) {
            MOUSE_BUTTONLESS_EVENTS.add(e);
            MOUSE_EVENTS.add(e);
        }
        for (String e : new String[]{"mousedown", "mouseup", "click", "dblclick"}) {
            MOUSE_BUTTON_EVENTS.add(e);
            MOUSE_EVENTS.add(e);
        }
        for (String e : new String[]{"focus", "blur", "beforeeditfocus"}) {
            FOCUS_EVENTS.add(e);
        }
        for (String e : new String[]{"cut", "copy", "paste"}) {
            CLIPBOARD_EVENTS.add(e);
            CLIPBOARD_EVENTS.add("before" + e);
        }
    }

    protected NativeEvent nativeEvent;

    private KeySignalType keySignalType = null;

    private int cachedKeyCode = -1;

    private boolean hasBeenConsumed = false;

    protected SignalEventImpl() {
    }

    static class JsoNativeEvent extends Event implements NativeEvent {
        protected JsoNativeEvent() {
        }
    }

    /**
     * Create a signal from an event, possibly filtering the event
     * if it is deemed redundant.
     * <p/>
     * If the event is to be filtered, null is returned, and bubbling
     * is cancelled if cancelBubbleIfNullified is true.
     * (but the default is not prevented).
     * <p/>
     * NOTE(danilatos): So far, for key events, the following have been tested:
     * - Safari 3.1 OS/X (incl. num pad, with USB keyboard)
     * - Safari 3.0 OS/X, hosted mode only (so no ctrl+c, etc)
     * - Firefox 3, OS/X, WinXP
     * - IE7, WinXP
     * Needs testing:
     * - FF3 linux, Safari 3.0/3.1 Windows
     * - All kinds of weirdo keyboards (mac, international)
     * - Linux IME
     * <p/>
     * Currently, only key events have serious logic applied to them.
     * Maybe some logic for copy/paste, and mouse events?
     *
     * @param event
     *         Raw Event JSO
     * @param cancelBubbleIfNullified
     *         stops propagation if the event is nullified
     * @return SignalEvent mapping, or null, if the event is to be discarded
     */
    public static SignalEventImpl create(Event event, boolean cancelBubbleIfNullified) {
        return create(DEFAULT_FACTORY, event, cancelBubbleIfNullified);
    }

    public static <T extends SignalEventImpl> T create(SignalEventFactory<T> factory, Event event,
                                                       boolean cancelBubbleIfNullified) {
        if (hasBeenConsumed(event)) {
            return null;
        } else {
            T signal = createInner(factory, event);
            if (cancelBubbleIfNullified && signal == null) {
                event.stopPropagation();
            }
            return signal;
        }
    }

    private static boolean hasBeenConsumed(Event event) {
        SignalEventImpl existing = getFor(null, event);
        return existing != null && existing.hasBeenConsumed();
    }

    private static final String EVENT_PROP = "$se";

    @SuppressWarnings("unchecked")
    private static <T extends SignalEventImpl> T getFor(SignalEventFactory<T> factory, Event event) {
        return (T)(SignalEventImpl)event.<Jso>cast().getJavaObjectField(EVENT_PROP);
    }

    private static <T extends SignalEventImpl> T createFor(SignalEventFactory<T> factory, Event event) {

        T signal = factory.create();
        event.<Jso>cast().addField(EVENT_PROP, signal);
        return signal;
    }

    /** This would be a static local variable if java allowed it. Grouping it here. */
    private static final SignalKeyLogic.Result computeKeySignalTypeResult = new SignalKeyLogic.Result();

    private static <T extends SignalEventImpl> T createInner(SignalEventFactory<T> factory, Event event) {

        SignalKeyLogic.Result keySignalResult;
        if (isKeyEvent(event)) {
            keySignalResult = computeKeySignalTypeResult;

            String keyIdentifier = getKeyIdentifier(event);

            logic.computeKeySignalType(keySignalResult, event.getType(), getNativeKeyCode(event), getWhich(event),
                                       keyIdentifier, event.getMetaKey(), event.getCtrlKey(), event.getAltKey(), event.getShiftKey());

        } else {
            keySignalResult = null;
        }

        return createInner(createFor(factory, event), event.<JsoNativeEvent>cast(), keySignalResult);
    }

    /**
     * Populate a SignalEventImpl with the necessary information
     *
     * @param ret
     * @param keySignalResult
     *         only required if it's a key event
     * @return the signal, or null if it is to be ignored.
     */
    protected static <T extends SignalEventImpl> T createInner(T ret, NativeEvent event,
                                                               SignalKeyLogic.Result keySignalResult) {
        ret.nativeEvent = event;
        if (ret.isKeyEvent()) {
            KeySignalType type = keySignalResult.type;

            if (type != null) {
                ret.cacheKeyCode(keySignalResult.keyCode);
                ret.setup(type);
            } else {
                ret = null;
            }

        } else if ((UserAgent.isIE() ? "paste" : "beforepaste").equals(event.getType())) {
            // Only want 'beforepaste' for ie and 'paste' for everything else.
            // TODO(danilatos): Generalise clipboard events
            ret = null;
        }

        // TODO: return null if it's something we should ignore.
        return ret;
    }

    public static native int getNativeKeyCode(Event event) /*-{
        return event.keyCode || 0;
    }-*/;

    public static native int getWhich(Event event) /*-{
        return event.which || 0;
    }-*/;

    public static native String getKeyIdentifier(Event event) /*-{
        return event.key
    }-*/;

    /** @return Event type as a string, e.g. "keypress" */
    public final String getType() {
        return nativeEvent.getType();
    }

    /** @return The target element of the event */
    public Element getTarget() {
        return asEvent().getTarget();
    }

    /**
     * @return true if the event is a key event
     *         TODO(danilatos): Have a top level EventSignalType enum
     */
    public final boolean isKeyEvent() {
        return KEY_EVENTS.contains(nativeEvent.getType());
    }

    /** @return true if it is an IME composition event */
    public final boolean isCompositionEvent() {
        return COMPOSITION_EVENTS.contains(getType());
    }

    /**
     * Returns true if the key event is an IME input event.
     * Only makes sense to call this method if this is a key signal.
     * Does not work on FF. (TODO(danilatos): Can it be done? Tricks
     * with dom mutation events?)
     *
     * @return true if this is an IME input event
     */
    public final boolean isImeKeyEvent() {
        return getKeyCode() == SignalKeyLogic.IME_CODE;
    }

    /**
     * @return true if this is a mouse event
     *         TODO(danilatos): Have a top level EventSignalType enum
     */
    public final boolean isMouseEvent() {
        return MOUSE_EVENTS.contains(getType());
    }

    /**
     * TODO(danilatos): Click + drag? I.e. return true for mouse move, if the
     * button is pressed? (this might be useful for tracking changing selections
     * as the user holds & drags)
     *
     * @return true if this is an event involving some use of mouse buttons
     */
    public final boolean isMouseButtonEvent() {
        return MOUSE_BUTTON_EVENTS.contains(getType());
    }

    /** @return true if this is a mouse event but not {@link #isMouseButtonEvent()} */
    public final boolean isMouseButtonlessEvent() {
        return MOUSE_BUTTONLESS_EVENTS.contains(getType());
    }

    /** @return true if this is a "click" event */
    public final boolean isClickEvent() {
        return "click".equals(getType());
    }

    /** @return True if this is a dom mutation event */
    public final boolean isMutationEvent() {
        // What about DOMMouseScroll?
        return getType().startsWith("DOM");
    }

    /** @return true if this is any sort of clipboard event */
    public final boolean isClipboardEvent() {
        return CLIPBOARD_EVENTS.contains(getType());
    }

    /** @return If this is a focus event */
    public final boolean isFocusEvent() {
        return FOCUS_EVENTS.contains(getType());
    }

    /**
     * @return true if this is a paste event
     *         TODO(danilatos): Make a ClipboardSignalType enum instead
     */
    public final boolean isPasteEvent() {
        return (UserAgent.isIE() ? "beforepaste" : "paste").equals(nativeEvent.getType());
    }

    /**
     * @return true if this is a cut event
     *         TODO(danilatos): Make a ClipboardSignalType enum instead
     */
    public final boolean isCutEvent() {
        return (UserAgent.isIE() ? "beforecut" : "cut").equals(nativeEvent.getType());
    }

    /**
     * @return true if this is a copy event
     *         TODO(danilatos): Make a ClipboardSignalType enum instead
     */
    public final boolean isCopyEvent() {
        return "copy".equals(nativeEvent.getType());
    }

    /**
     * @return true if the command key is depressed
     * @see SignalKeyLogic#commandIsCtrl()
     */
    public final boolean getCommandKey() {
        return logic.commandIsCtrl() ? getCtrlKey() : getMetaKey();
    }

    public static boolean getCommandKey(com.google.gwt.dom.client.NativeEvent event) {
        return logic.commandIsCtrl() ? event.getCtrlKey() : event.getMetaKey();
    }

    /** @return true if the ctrl key is depressed */
    public final boolean getCtrlKey() {
        return nativeEvent.getCtrlKey();
    }

    /** @return true if the meta key is depressed */
    public final boolean getMetaKey() {
        return nativeEvent.getMetaKey();
    }

    /** @return true if the alt key is depressed */
    public final boolean getAltKey() {
        // TODO(danilatos): Handle Alt vs Option on OSX?
        return nativeEvent.getAltKey();
    }

    /** @return true if the shift key is depressed */
    public final boolean getShiftKey() {
        return nativeEvent.getShiftKey();
    }

    /** @return The underlying event view of this event */
    public final Event asEvent() {
        return (Event)nativeEvent;
    }

    /**
     * Only valid for key events.
     * Currently only implemented for deleting, not actual navigating.
     *
     * @return The move unit of this event
     */
    public final MoveUnit getMoveUnit() {
        if (getKeySignalType() == KeySignalType.DELETE) {
            if (UserAgent.isMac()) {
                if (getAltKey()) {
                    // Note: in practice, some combinations of bkspc/delete + modifier key
                    // have no effect. This is inconsistent across browsers. It's probably
                    // ok to normalise it here, as we will be manually implementing everything
                    // except character-sized deletes on collapsed selections, and so users
                    // would get a more consistent (and logical and symmetrical) experience.
                    return MoveUnit.WORD;
                } else if (getCommandKey()) {
                    return MoveUnit.LINE;
                } else {
                    return MoveUnit.CHARACTER;
                }
            } else {
                if (getCommandKey()) {
                    return MoveUnit.WORD;
                } else {
                    return MoveUnit.CHARACTER;
                }
            }
        } else {
            // TODO(danilatos): Also implement for mere navigation events?
            // Currently just for deleting... so we'll at least for now just pretend
            // everything else is of character magnitude. This is because we
            // probably won't be using the information anyway, instead letting
            // the browser just do its default navigation behaviour.

            return MoveUnit.CHARACTER;
        }
    }

    @Override
    public final boolean isUndoCombo() {
        return isCombo('Z', KeyModifier.COMMAND);
    }

    @Override
    public final boolean isRedoCombo() {
        if ((UserAgent.isMac() || UserAgent.isLinux()) && isCombo('Z', KeyModifier.COMMAND_SHIFT)) {
            // Mac and Linux accept command-shift-z for undo
            return true;
        }
        // NOTE(user): COMMAND + Y for redo, except for Mac OS X (for chrome,
        // default behaviour is browser history)
        return !UserAgent.isMac() && isCombo('Y', KeyModifier.COMMAND);
    }

    /**
     * Because we must use keypress events for FF, in order to get repeats,
     * but prefer keydowns for combo type events for the other browsers,
     * we need to convert the case here.
     *
     * @param letter
     */
    private final int comboInputKeyCode(char letter) {
        // TODO(danilatos): Check the compiled javascript to make sure it does simple
        // numerical operations and not string manipulations and conversions... char is
        // used all over this file
        return UserAgent.isFirefox() ? letter + 'a' - 'A' : letter;
    }

    /**
     * @param letter
     *         Treated case-insensitive, including things like '1' vs '!'
     *         User may provide either, but upper case for letters and unshifted for
     *         other keys is recommended
     * @param modifier
     * @return True if the given letter is pressed, and only the given modifiers.
     */
    public final boolean isCombo(int letter, KeyModifier modifier) {
        assert letter > 0 && letter < shiftMappings.length;
        int keyCode = getKeyCode();
        if (keyCode >= shiftMappings.length) {
            return false;
        }

        return (letter == keyCode || letter == shiftMappings[keyCode]) && modifier.check(this);
    }

    /**
     * @param letter
     * @return true, if the given letter was pressed without modifiers. Takes into
     *         account the caps lock key being pressed (it will be as if it
     *         weren't pressed)
     */
    public final boolean isOnly(int letter) {
        return isCombo(letter, KeyModifier.NONE);
    }

    @Override
    public final int getMouseButton() {
        return nativeEvent.getButton();
    }

    /**
     * @return The key signal type of this even, or null if it is not a key event
     * @see SignalEvent.KeySignalType
     */
    public KeySignalType getKeySignalType() {
        return this.keySignalType;
    }

    /**
     * @return The gwtKeyCode of this event, with some minor compatibility
     *         adjustments
     */
    public int getKeyCode() {
        return this.cachedKeyCode;
    }

    /**
     * Returns true if the event has effectively had its propagation stopped, since
     * we couldn't physically stop it due to browser quirkiness.  See {@link #stopPropagation()}.
     */
    private boolean hasBeenConsumed() {
        return hasBeenConsumed;
    }

    private void markAsConsumed() {
        hasBeenConsumed = true;
    }

    protected void cacheKeyCode(int keyCode) {
        this.cachedKeyCode = keyCode;
    }

    private boolean stopPropagationPreventsDefault() {
        if (QuirksConstants.CANCEL_BUBBLING_CANCELS_IME_COMPOSITION_AND_CONTEXTMENU) {
            return CANCEL_BUBBLE_QUIRKS.contains(getType());
        } else {
            return false;
        }
    }

    private boolean isPreventDefaultEffective() {
        if (QuirksConstants.PREVENT_DEFAULT_STOPS_CONTEXTMENT) {
            return true;
        } else {
            String type = nativeEvent.getType();
            return !type.equals("contextmenu");
        }
    }

    @Override
    public final void stopPropagation() {
        if (stopPropagationPreventsDefault()) {
            markAsConsumed();
        } else {
            nativeEvent.stopPropagation();
        }
    }

    protected final void setup(KeySignalType signalType) {
        this.keySignalType = signalType;
    }

    @Override
    public final void preventDefault() {
        nativeEvent.preventDefault();
        if (!isPreventDefaultEffective()) {
            // HACK(user): Really we would like the event to continue to propagate
            //   and stop it immediately before reaching the top, rather than at this
            //   point.
            nativeEvent.stopPropagation();
        }
    }
}
