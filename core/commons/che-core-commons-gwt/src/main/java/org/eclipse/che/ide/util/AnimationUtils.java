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

package org.eclipse.che.ide.util;

import elemental.css.CSSStyleDeclaration;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.dom.Element;

import org.eclipse.che.ide.util.browser.UserAgent;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/** Utility class for using CSS3 transitions. */
public class AnimationUtils {

    public static final double ALERT_TRANSITION_DURATION  = 2.0;
    public static final double LONG_TRANSITION_DURATION   = 0.7;
    public static final double MEDIUM_TRANSITION_DURATION = 0.3;
    public static final double SHORT_TRANSITION_DURATION  = 0.2;

    private static final String OLD_OVERFLOW_STYLE_KEY = "__old_overflow";
    private static final String TRANSITION_PROPERTIES  = "all";

    /** Handles transition ends and optionally invokes an animation callback. */
    private static class TransitionEndHandler implements EventListener {
        private EventListener animationCallback;

        private TransitionEndHandler(EventListener animationCallback) {
            this.animationCallback = animationCallback;
        }

        private void handleEndFor(Element elem, String type) {

            // An element should only have 1 transition end handler at a time. We
            // remove in the handle callback, but we cannot depend on the handle
            // callback being correctly invoked. Over eager removal is OK.
            TransitionEndHandler oldListener = getOldListener(elem, type);
            if (oldListener != null) {
                elem.removeEventListener(type, oldListener, false);
                oldListener.maybeDispatchAnimationCallback(null);
            }
            elem.addEventListener(type, this, false);
            replaceOldListener(elem, type, this);
        }

        private native void replaceOldListener(
                Element elem, String type, TransitionEndHandler transitionEndHandler) /*-{
            elem["__" + type + "_h"] = transitionEndHandler;
        }-*/;

        private native TransitionEndHandler getOldListener(Element elem, String type) /*-{
            return elem["__" + type + "_h"];
        }-*/;

        private void maybeDispatchAnimationCallback(Event evt) {
            if (animationCallback != null) {
                animationCallback.handleEvent(evt);
                animationCallback = null;
            }
        }

        @Override
        public void handleEvent(Event evt) {
            Element target = (Element)evt.getTarget();
            target.removeEventListener(evt.getType(), this, false);
            removeTransitions(target.getStyle());
            maybeDispatchAnimationCallback(evt);
        }
    }

    /**
     * @see: {@link #animatePropertySet(Element, String, String, double,
     * EventListener)}.
     */
    public static void animatePropertySet(
            final Element elem, String property, String value, double duration) {
        animatePropertySet(elem, property, value, duration, null);
    }

    /**
     * Enables animations prior to setting the value for the specified style
     * property on the supplied element. The end result is that there property is
     * transitioned to.
     *
     * @param elem
     *         the {@link Element} we want to set the style property on.
     * @param property
     *         the name of the style property we want to set.
     * @param value
     *         the target value of the style property.
     * @param duration
     *         the time in seconds we want the transition to last.
     * @param animationCallback
     *         callback that is invoked when the animation
     *         completes. It will be passed a {@code null} event if the animation
     *         was pre-empted by some other animation on the same element.
     */
    public static void animatePropertySet(final Element elem, String property, String value,
                                          double duration, final EventListener animationCallback) {
        final CSSStyleDeclaration style = elem.getStyle();
        enableTransitions(style, duration);

        if (UserAgent.isFirefox()) {
            // For FF4.
            new TransitionEndHandler(animationCallback).handleEndFor(elem, "transitionend");
        } else {
            // For webkit based browsers.
            // TODO: Keep an eye on whether or not webkit supports the
            // vendor prefix free version. If they ever do we should remove this.
            new TransitionEndHandler(animationCallback).handleEndFor(elem, Event.WEBKITTRANSITIONEND);
        }

        style.setProperty(property, value);
    }

    public static void backupOverflow(CSSStyleDeclaration style) {
        style.setProperty(OLD_OVERFLOW_STYLE_KEY, style.getOverflow());
        style.setOverflow("hidden");
    }

    /**
     * Disables CSS3 transitions.
     * <p/>
     * If you want to reset transitions after enabling them, use
     * {@link #removeTransitions(CSSStyleDeclaration)} instead.
     */
    public static void disableTransitions(CSSStyleDeclaration style) {
        style.setProperty("-webkit-transition-property", "none");
        style.setProperty("-moz-transition-property", "none");
    }

    /** @see: {@link #enableTransitions(CSSStyleDeclaration, double)} */
    public static void enableTransitions(CSSStyleDeclaration style) {
        enableTransitions(style, MEDIUM_TRANSITION_DURATION);
    }

    /**
     * Enables CSS3 transitions for a given element.
     * <p/>
     * If you want to reset transitions after disabling them, use
     * {@link #removeTransitions(CSSStyleDeclaration)} instead.
     *
     * @param style
     *         the style object belonging to the element we want to animate.
     * @param duration
     *         the length of time we want the animation to last.
     */
    public static void enableTransitions(CSSStyleDeclaration style, double duration) {
        style.setProperty("-webkit-transition-property", TRANSITION_PROPERTIES);
        style.setProperty("-moz-transition-property", TRANSITION_PROPERTIES);
        style.setProperty("-webkit-transition-duration", duration + "s");
        style.setProperty("-moz-transition-duration", duration + "s");
    }

    /** Removes CSS3 transitions, returning them to their original state. */
    public static void removeTransitions(CSSStyleDeclaration style) {
        style.removeProperty("-webkit-transition-property");
        style.removeProperty("-moz-transition-property");
        style.removeProperty("-webkit-transition-duration");
        style.removeProperty("-moz-transition-duration");
    }

    public static void fadeIn(final Element elem) {
        elem.getStyle().setDisplay(CSSStyleDeclaration.Display.BLOCK);

        // TODO: This smells like a chrome bug to me that we need to do a
        // deferred command here.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                animatePropertySet(elem, "opacity", "1.0", SHORT_TRANSITION_DURATION);
            }
        });
    }

    public static void fadeOut(final Element elem) {
        animatePropertySet(elem, "opacity", "0", SHORT_TRANSITION_DURATION, new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                elem.getStyle().setDisplay("none");
            }
        });
    }

    /** Flashes an element to highlight that it has recently changed. */
    public static void flash(final Element elem) {
    /*
     * If we interrupt a flash with another flash, we need to disable animations
     * so the initial background color takes effect immediately. Animations are
     * reenabled in animatePropertySet.
     */
        removeTransitions(elem.getStyle());

        elem.getStyle().setBackgroundColor("#f9edbe");

        // Give the start color a chance to take effect.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                animatePropertySet(elem, "background-color", "", ALERT_TRANSITION_DURATION);
            }
        });
    }

    public static void restoreOverflow(CSSStyleDeclaration style) {
        style.setOverflow(style.getPropertyValue(OLD_OVERFLOW_STYLE_KEY));
    }

}
