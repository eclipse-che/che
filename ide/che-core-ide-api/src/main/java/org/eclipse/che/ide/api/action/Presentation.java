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
package org.eclipse.che.ide.api.action;

import com.google.gwt.dom.client.Element;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.ListenerManager;

/**
 * The presentation of an action in a specific place in the user interface.
 *
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public final class Presentation {

  /** Defines tool tip for button at tool bar or text for element at menu value: String */
  public static final String PROP_TEXT = "text";
  /** value: String */
  public static final String PROP_DESCRIPTION = "description";
  /** value: Icon */
  public static final String PROP_ICON = "icon";
  /** value: Boolean */
  public static final String PROP_VISIBLE = "visible";
  /** The actual value is a Boolean. */
  public static final String PROP_ENABLED = "enabled";

  private Map<String, Object> userMap = new HashMap<>();
  private ListenerManager<PropertyChangeListener> myChangeSupport;
  private String text;
  private String myDescription;

  private Element imageElement;
  private String htmlResource;

  private boolean visible;
  private boolean enabled;

  public Presentation() {
    myChangeSupport = ListenerManager.create();
    visible = true;
    enabled = true;
  }

  public Presentation(final String text) {
    this();
    this.text = text;
  }

  public void addPropertyChangeListener(PropertyChangeListener l) {
    myChangeSupport.add(l);
  }

  public void removePropertyChangeListener(PropertyChangeListener l) {
    myChangeSupport.remove(l);
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    String oldText = this.text;
    this.text = text;
    firePropertyChange(PROP_TEXT, oldText, text);
  }

  public String getDescription() {
    return myDescription;
  }

  public void setDescription(String description) {
    String oldDescription = myDescription;
    myDescription = description;
    firePropertyChange(PROP_DESCRIPTION, oldDescription, myDescription);
  }

  /**
   * Returns SVG image resource.
   *
   * @return svg image resource
   */
  public Element getImageElement() {
    return imageElement == null ? null : (Element) imageElement.cloneNode(true);
  }

  /**
   * Sets icon image element.
   *
   * @param imageElement icon image element
   */
  public void setImageElement(Element imageElement) {
    Element oldElement = this.imageElement;
    this.imageElement = imageElement;
    firePropertyChange(PROP_ICON, oldElement, imageElement);
  }

  /**
   * Returns icon HTML resource.
   *
   * @return html resource
   */
  public String getHTMLResource() {
    return htmlResource;
  }

  /**
   * Sets icon HTML resource.
   *
   * @param htmlResource html resource
   */
  public void setHTMLResource(String htmlResource) {
    String oldHTMLResource = htmlResource;
    this.htmlResource = htmlResource;
    firePropertyChange(PROP_ICON, oldHTMLResource, htmlResource);
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    if (this.visible == visible) {
      return;
    }

    this.visible = visible;

    if (visible) {
      firePropertyChange(PROP_VISIBLE, Boolean.FALSE, Boolean.TRUE);
    } else {
      firePropertyChange(PROP_VISIBLE, Boolean.TRUE, Boolean.FALSE);
    }
  }

  /**
   * Returns the state of this action.
   *
   * @return <code>true</code> if action is enabled, <code>false</code> otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether the action enabled or not. If an action is disabled, {@link
   * Action#actionPerformed} won't be called. In case when action represents a button or a menu
   * item, the representing button or item will be greyed out.
   *
   * @param enabled <code>true</code> if you want to enable action, <code>false</code> otherwise
   */
  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }

    this.enabled = enabled;

    if (enabled) {
      firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    } else {
      firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
  }

  public final void setEnabledAndVisible(boolean enabled) {
    setEnabled(enabled);
    setVisible(enabled);
  }

  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    final PropertyChangeEvent event =
        new PropertyChangeEvent(this, propertyName, oldValue, newValue);
    myChangeSupport.dispatch(listener -> listener.onPropertyChange(event));
  }

  public Presentation clone() {
    Presentation presentation = new Presentation(getText());
    presentation.myDescription = myDescription;
    presentation.enabled = enabled;
    presentation.visible = visible;
    presentation.imageElement = imageElement;
    if (imageElement != null) {
      presentation.imageElement = imageElement.cloneNode(true).cast();
    }
    presentation.htmlResource = htmlResource;
    return presentation;
  }

  public void putClientProperty(@NotNull String key, @Nullable Object value) {
    Object oldValue = userMap.get(key);
    userMap.put(key, value);
    firePropertyChange(key, oldValue, value);
  }

  /**
   * Return user client property by specified key.
   *
   * @param key user client property key
   * @return object, stored by property key
   */
  public Object getClientProperty(String key) {
    if (key == null) {
      return null;
    }

    return userMap.get(key);
  }

  @Override
  public String toString() {
    return text + " (" + myDescription + ")";
  }
}
