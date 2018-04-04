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
package org.eclipse.che.ide.ui.smartTree.event.internal;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Event;
import org.eclipse.che.ide.util.browser.UserAgent;

public class NativeTreeEvent extends Event {

  protected NativeTreeEvent() {}

  /**
   * Returns true if the control or meta key was depressed.
   *
   * @return true if control or meta
   */
  public final boolean getCtrlOrMetaKey() {
    return getCtrlKey() || getMetaKey();
  }

  /**
   * Returns the event target element.
   *
   * @return the target element
   */
  public final Element getEventTargetEl() {
    return getEventTarget().cast();
  }

  /**
   * Returns <code>true</code> if the event is a right click.
   *
   * @return the right click state
   */
  public final boolean isRightClick() {
    return getButton() == Event.BUTTON_RIGHT || (UserAgent.isMac() && getCtrlKey());
  }

  /**
   * Returns <code>true</code> if the target of this event equals or is a child of the given
   * element.
   *
   * @param element the element
   * @return the within state
   */
  public final boolean within(Element element) {
    return within(element, false);
  }

  /**
   * Returns <code>true</code> if the target of this event equals or is a child of the given
   * element.
   *
   * @param element the element
   * @param toElement true to use {@link com.google.gwt.user.client.Event#getRelatedEventTarget()}
   * @return the within state
   */
  public final boolean within(Element element, boolean toElement) {
    if (Element.is(element)) {
      EventTarget target = toElement ? getRelatedEventTarget() : getEventTarget();
      if (Element.is(target)) {
        return element.isOrHasChild((Element) target.cast());
      }
    }
    return false;
  }
}
