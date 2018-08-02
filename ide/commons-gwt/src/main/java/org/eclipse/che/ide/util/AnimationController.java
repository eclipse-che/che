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
package org.eclipse.che.ide.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import elemental.css.CSSStyleDeclaration;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import org.eclipse.che.ide.collections.Jso;

/*
 * TODO: Here's the list of short-term TODOs:
 *
 * - Make sure our client measurements are accurate if the element has padding,
 * margins, border, etc. Luckily, the current clients don't have any of these
 *
 * - So far, clients have used pixels for height, etc., I need to figure out
 * whether other units are kosher with CSS transition.
 *
 * - There's a bug in Chrome that I need to file, to repro click four times
 * quickly on an the expander arrow, and notice that element is no longer
 * expandable (the element doesn't accept changes with setClassName anymore).
 *
 * - A way to give the controller multiple mutually exclusive elements and
 * transition smoothly (e.g. I am doing this with the
 * CollaborationNavigationSection, but manually with a show/hide. I think the
 * animation aesthetics can be improved if this controller knows/manages both
 * together as one.)
 *
 * - For things like fixed height, we can figure it out based on the element.
 * But, we don't want to do it at time of animation because we have to walk the
 * CSSStyleRules. Instead, allow the builder to take in a "template" element
 * that will look like the elements passed to show/hide.
 *
 * - Add support for non-animated initial state.
 */

/**
 * Controller to aid in animating elements.
 *
 * <p>Rules:
 *
 * <ul>
 *   <li>Initialize the element by calling {@link
 *       AnimationController#hideWithoutAnimating(Element)}. Don't set "display: none" in your CSS
 *       since the animation controller cannot undo it.
 *   <li>Do not modify the {@link Builder} after calling its {@link Builder#build()}.
 *   <li>Padding and margins must be specified in px units.
 * </ul>
 */
public class AnimationController {

  public interface AnimationStateListener {
    void onAnimationStateChanged(Element element, State state);
  }

  /** Expands and collapses an element into and out of view. */
  public static final AnimationController COLLAPSE_ANIMATION_CONTROLLER =
      new AnimationController.Builder().setCollapse(true).build();

  /** Fades an element into and out of view. */
  public static final AnimationController FADE_ANIMATION_CONTROLLER =
      new AnimationController.Builder().setFade(true).build();

  public static final AnimationController COLLAPSE_FADE_ANIMATION_CONTROLLER =
      new AnimationController.Builder().setCollapse(true).setFade(true).build();

  /** Does not animate. */
  public static final AnimationController NO_ANIMATION_CONTROLLER =
      new AnimationController.Builder().build();

  /** Builder for the {@link AnimationController}. Do not modify after calling {@link #build()}. */
  public static class Builder {
    private boolean collapse;
    private boolean fade;
    private boolean fixedHeight;

    public AnimationController build() {
      return new AnimationController(this);
    }

    // TODO: shrink height or width?

    /** Defaults to false */
    public Builder setCollapse(boolean collapse) {
      this.collapse = collapse;
      return this;
    }

    /** Defaults to false */
    public Builder setFade(boolean fade) {
      this.fade = fade;
      return this;
    }

    /** Defaults to false */
    public Builder setFixedHeight(boolean fixedHeight) {
      this.fixedHeight = fixedHeight;
      return this;
    }
  }

  /** Handles the end of a CSS transition. */
  private abstract class AbstractTransitionEndHandler implements EventListener {
    public void handleEndFor(Element elem) {
      // TODO: Keep an eye on whether or not webkit supports the
      // vendor prefix free version. If they ever do we should remove this.
      elem.addEventListener(Event.WEBKITTRANSITIONEND, this, false);
      // For FF4 when we are ready.
      elem.addEventListener("transitionend", this, false);
    }

    public void unhandleEndFor(Element elem) {
      elem.removeEventListener(Event.WEBKITTRANSITIONEND, this, false);
      // For FF4 when we are ready.
      elem.removeEventListener("transitionend", this, false);
    }

    /*
     * GWT complains that AbstractTransitionEndHandler doesn't define
     * handleEvent() if we do not include this abstract method to override the
     * interface method.
     */
    @Override
    public abstract void handleEvent(Event evt);
  }

  /** Handles the end of the show transition. */
  private class ShowTransitionEndHandler extends AbstractTransitionEndHandler {

    @Override
    public void handleEvent(Event evt) {
      /*
       * Transition events propagate, so the event target could be a child of
       * the element that we are controlling. For example, the child could be a
       * button (with transitions enabled) within a form that is being animated.
       *
       * We verify that the target is actually being animated by the
       * AnimationController by checking its current state. It will only have a
       * state if the AnimationController added the state attribute to the
       * target.
       */
      Element target = (Element) evt.getTarget();
      if (isAnyState(target, State.SHOWING)) {
        showWithoutAnimating(target); // Puts element in SHOWN state
      }
    }
  }

  /** Handles the end of the hide transition. */
  private class HideTransitionEndHandler extends AbstractTransitionEndHandler {

    @Override
    public void handleEvent(Event evt) {
      /*
       * Transition events propagate, so the event target could be a child of
       * the element that we are controlling. For example, the child could be a
       * button (with transitions enabled) within a form that is being animated.
       *
       * We verify that the target is actually being animated by the
       * AnimationController by checking its current state. It will only have a
       * state if the AnimationController added the state attribute to the
       * target.
       */
      Element target = (Element) evt.getTarget();
      if (isAnyState(target, State.HIDING)) {
        hideWithoutAnimating(target); // Puts element in HIDDEN state
      }
    }
  }

  /** An attribute added to an element to indicate its state. */
  private static final String ATTR_STATE = "__animControllerState";

  /** An attribute added to an element to stash its animation state listener. */
  private static final String ATTR_STATE_LISTENER = "__animControllerStateListener";

  /**
   * The states that an element can be in.
   *
   * <p>The only method we call on the state is {@link State#ordinal()}, which allows the GWT
   * compiler to ordinalize the enums into integer constants.
   */
  public static enum State {
    /** The element is completely hidden. */
    HIDDEN,

    /** The element is transitioning to the hidden state. */
    HIDING,

    /** The element is completely shown. */
    SHOWN,

    /** The element is transitioning to the shown state. */
    SHOWING
  }

  final boolean isAnimated; // Visible for testing.
  private final Builder options;
  private final ShowTransitionEndHandler showEndHandler;
  private final HideTransitionEndHandler hideEndHandler;

  private AnimationController(Builder builder) {
    this.options = builder;
    this.showEndHandler = new ShowTransitionEndHandler();
    this.hideEndHandler = new HideTransitionEndHandler();

    /*
     * If none of the animated properties are being animated, then the CSS
     * transition end listener may not execute at all. In that case, we
     * show/hide the element immediately.
     */
    this.isAnimated = options.collapse || options.fade;
  }

  /**
   * Animate the element out of view. Do not enable transitions in the CSS for this element, or the
   * animations may not work correctly. AnimationController will enable animations automatically.
   *
   * @see #hideWithoutAnimating(Element)
   */
  public void hide(final Element element) {
    // Early exit if the element is hidden or hiding.
    if (isAnyState(element, State.HIDDEN, State.HIDING)) {
      return;
    }

    if (!isAnimated) {
      hideWithoutAnimating(element);
      return;
    }

    // Cancel pending transition event listeners.
    showEndHandler.unhandleEndFor(element);

    final CSSStyleDeclaration style = element.getStyle();

    if (options.collapse) {
      // Set height because the CSS transition requires one
      int height = getCurrentHeight(element);
      style.setHeight(height + CSSStyleDeclaration.Unit.PX);
    }

    // Give the browser a chance to accept the height set above
    setState(element, State.HIDING);
    schedule(
        element,
        new ScheduledCommand() {
          @Override
          public void execute() {
            // The user changed the state before this command executed.
            if (!clearLastCommand(element, this) || !isAnyState(element, State.HIDING)) {
              return;
            }

            if (options.collapse) {
              /*
               * Hide overflow if changing height, or the overflow will be visible
               * even as the element collapses.
               */
              AnimationUtils.backupOverflow(style);
            }
            AnimationUtils.enableTransitions(style);

            if (options.collapse) {
              // Animate all properties that could affect height if collapsing.
              style.setHeight("0");
              style.setMarginTop("0");
              style.setMarginBottom("0");
              style.setPaddingTop("0");
              style.setPaddingBottom("0");
              CssUtils.setBoxShadow(element, "0 0");
            }

            if (options.fade) {
              style.setOpacity(0);
            }
          }
        });

    // For webkit based browsers.
    hideEndHandler.handleEndFor(element);
  }

  /**
   * Animates the element into view. Do not enable transitions in the CSS for this element, or the
   * animations may not work correctly. AnimationController will enable animations automatically.
   */
  public void show(final Element element) {
    // Early exit if the element is shown or showing.
    if (isAnyState(element, State.SHOWN, State.SHOWING)) {
      return;
    }

    if (!isAnimated) {
      showWithoutAnimating(element);
      return;
    }

    // Cancel pending transition event listeners.
    hideEndHandler.unhandleEndFor(element);

    /*
     * Make this "visible" again so we can measure its eventual height (required
     * for CSS transitions). We will set its initial state in this event loop,
     * so the element will not be fully visible.
     */
    final CSSStyleDeclaration style = element.getStyle();
    element.getStyle().removeProperty("display");
    final int measuredHeight = getCurrentHeight(element);

    /*
     * Set the initial state, but not if the element is in the process of
     * hiding.
     */
    if (!isAnyState(element, State.HIDING)) {
      if (options.collapse) {
        // Start the animation at a height of zero.
        style.setHeight("0");

        // We want to animate from total height of 0
        style.setMarginTop("0");
        style.setMarginBottom("0");
        style.setPaddingTop("0");
        style.setPaddingBottom("0");
        CssUtils.setBoxShadow(element, "0 0");

        /*
         * Hide overflow if expanding the element, or the entire element will be
         * instantly visible. Do not do this by default, because it could hide
         * absolutely positioned elements outside of the root element, such as
         * the arrow on a tooltip.
         */
        AnimationUtils.backupOverflow(style);
      }

      if (options.fade) {
        style.setOpacity(0);
      }
    }

    // Give the browser a chance to accept the properties set above
    setState(element, State.SHOWING);
    schedule(
        element,
        new ScheduledCommand() {
          @Override
          public void execute() {
            // The user changed the state before this command executed.
            if (!clearLastCommand(element, this) || !isAnyState(element, State.SHOWING)) {
              return;
            }

            // Enable animations before setting the end state.
            AnimationUtils.enableTransitions(style);

            // Set the end state.
            if (options.collapse) {
              if (options.fixedHeight) {
                // The element's styles have a fixed height set, so we just want to
                // clear our override
                style.setHeight("");
              } else {
                // Give it an explicit height to animate to, because the element's
                // height is auto otherwise
                style.setHeight(measuredHeight + CSSStyleDeclaration.Unit.PX);
              }

              style.removeProperty("margin-top");
              style.removeProperty("margin-bottom");
              style.removeProperty("padding-top");
              style.removeProperty("padding-bottom");
              CssUtils.removeBoxShadow(element);
            }

            if (options.fade) {
              style.setOpacity(1);
            }
          }
        });

    // For webkit based browsers.
    showEndHandler.handleEndFor(element);
  }

  /**
   * Checks if the specified element is logically hidden, which is true if it is hidden or in the
   * process of hiding.
   */
  public boolean isHidden(Element element) {
    return isAnyState(element, State.HIDDEN, State.HIDING);
  }

  /** Returns the height as would be set on the CSS "height" property. */
  private int getCurrentHeight(final Element element) {
    // TODO: test to see if horizontal scroll plays nicely
    CSSStyleDeclaration style = CssUtils.getComputedStyle(element);
    return element.getClientHeight()
        - CssUtils.parsePixels(style.getPaddingTop())
        - CssUtils.parsePixels(style.getPaddingBottom());
  }

  public void setVisibilityWithoutAnimating(Element element, boolean visibile) {
    if (visibile) {
      showWithoutAnimating(element);
    } else {
      hideWithoutAnimating(element);
    }
  }

  /**
   * Hide the element without animating it out of view. Use this method to set the initial state of
   * the element.
   */
  public void hideWithoutAnimating(Element element) {
    if (isAnyState(element, State.HIDDEN)) {
      return;
    }
    cancel(element);
    element.getStyle().setDisplay(CSSStyleDeclaration.Display.NONE);
    setState(element, State.HIDDEN);
  }

  /** Show the element without animating it into view. */
  public void showWithoutAnimating(Element element) {
    if (isAnyState(element, State.SHOWN)) {
      return;
    }
    cancel(element);
    element.getStyle().removeProperty("display");
    setState(element, State.SHOWN);
  }

  /**
   * Sets the listener for animation state change events.
   *
   * <p>
   *
   * <p>If an element is not visible in the UI when an animation is applied, the animation will
   * never complete and the element will stay in the state HIDING until some other animation is
   * applied.
   */
  public void setAnimationStateListener(Element element, AnimationStateListener listener) {
    ((Jso) element).addField(ATTR_STATE_LISTENER, listener);
  }

  public AnimationStateListener getAnimationStateListener(Element element) {
    return (AnimationStateListener) ((Jso) element).getJavaObjectField(ATTR_STATE_LISTENER);
  }

  /** Cancel the currently executing animation without completing it. */
  private void cancel(Element element) {
    // Cancel all handlers.
    setLastCommandImpl(element, null);
    hideEndHandler.unhandleEndFor(element);
    showEndHandler.unhandleEndFor(element);

    // Disable animations.
    CSSStyleDeclaration style = element.getStyle();
    AnimationUtils.removeTransitions(style);
    if (options.collapse) {
      AnimationUtils.restoreOverflow(style);
    }

    // Remove the height and properties we set.
    if (options.collapse) {
      style.removeProperty("height");
      style.removeProperty("margin-top");
      style.removeProperty("margin-bottom");
      style.removeProperty("padding-top");
      style.removeProperty("padding-bottom");
    }
    if (options.fade) {
      style.removeProperty("opacity");
    }
    CssUtils.removeBoxShadow(element);
  }

  private void setState(Element element, State state) {
    element.setAttribute(ATTR_STATE, Integer.toString(state.ordinal()));

    AnimationStateListener listener = getAnimationStateListener(element);
    if (listener != null) {
      listener.onAnimationStateChanged(element, state);
    }
  }

  /**
   * Check if the element is in any of the specified states.
   *
   * @param states the states to check, null is not allowed
   * @return true if in any one of the states
   */
  // Visible for testing.
  boolean isAnyState(Element element, State... states) {
    // Get the state ordinal from the attribute.
    String ordinalStr = element.getAttribute(ATTR_STATE);

    // NOTE: The following NULL check makes a dramatic performance impact!
    if (ordinalStr == null) {
      return false;
    }

    int ordinal = -1;
    try {
      ordinal = Integer.parseInt(ordinalStr);
    } catch (NumberFormatException e) {
      // The element's state has not been initialized yet.
      return false;
    }

    for (State state : states) {
      if (ordinal == state.ordinal()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Schedule a command to execute on the specified element. Use {@link #clearLastCommand(Element,
   * ScheduledCommand)} to verify that the command is still the most recent command scheduled for
   * the element.
   */
  private void schedule(Element element, ScheduledCommand command) {
    setLastCommandImpl(element, command);
    Scheduler.get().scheduleDeferred(command);
  }

  /**
   * Clear the last command from the specified element if the last command scheduled equals the
   * specified command.
   *
   * @return true if the last command equals the specified command, false if no
   */
  private native boolean clearLastCommand(Element element, ScheduledCommand command) /*-{
        if (element.__gwtLastCommand == command) {
            element.__gwtLastCommand = null; // Clear the last command if it is about to execute.
            return true;
        }
        return false;
    }-*/;

  private native void setLastCommandImpl(Element element, ScheduledCommand command) /*-{
        element.__gwtLastCommand = command;
    }-*/;
}
