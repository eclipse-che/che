/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import java.util.function.Consumer;

/**
 * A Ruler is a graphical element that is placed either on the left or on the right side of the
 * view. It can be used to provide the view with per line decoration such as line numbering,
 * bookmarks, breakpoints, folding disclosures, etc. There are two types of rulers: page and
 * document. A page ruler only shows the content for the lines that are visible, while a document
 * ruler always shows the whole content.
 */
public class OrionExtRulerOverlay extends JavaScriptObject {

  /** Instantiates a new Orion ext ruler overlay. */
  protected OrionExtRulerOverlay() {}

  /**
   * Factory to create a ruler.
   *
   * @param annotationModel the annotation model for the ruler
   * @param style the style for the ruler
   * @param location the location for the ruler {@link
   *     org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay.RulerLocation}
   * @param overview the overview for the ruler {@link
   *     org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay.RulerOverview}
   */
  public static native void create(
      OrionAnnotationModelOverlay annotationModel,
      OrionStyleOverlay style,
      String location,
      String overview,
      Consumer<OrionExtRulerOverlay> callback) /*-{
        $wnd.require(['orion/editor/rulers'], function (rulers) {
            var rul = new rulers.Ruler(annotationModel, location, overview, style);
            callback.@java.util.function.Consumer::accept(*)(rul);
        });
    }-*/;

  /** @return style of the ruler */
  public final native OrionStyleOverlay getStyle() /*-{
        return this.getRulerStyle();
    }-*/;

  /** @return location of the ruler. Possible values: "left", "right" */
  public final native String getLocation() /*-{
        return this.getLocation();
    }-*/;

  /**
   * Add annotation type to the receiver. Only annotations of the specified types will be shown by
   * the receiver. If the priority is not specified, the annotation type will be added to the end of
   * the receiver's list (lowest priority).
   *
   * @param type the annotation type to be shown
   * @param priority the priority for the annotation type
   */
  public final native void addAnnotationType(String type, int priority) /*-{
        this.addAnnotationType(type, priority);
    }-*/;

  /**
   * Returns the annotation model.
   *
   * @return the ruler annotation model
   */
  public final native OrionAnnotationModelOverlay getAnnotationModel() /*-{
        return this.getAnnotationModel();
    }-*/;

  /**
   * Returns the annotations for a given line range merging multiple annotations when necessary.
   * This method is called by the text view when the ruler is redrawn.
   *
   * @param startLine the start line index
   * @param endLine the end line index
   * @return the annotations for the line range. The array might be sparse.
   */
  public final native OrionAnnotationOverlay[] getAnnotations(int startLine, int endLine) /*-{
        return this.getAnnotations(startLine, endLine);
    }-*/;

  /**
   * Returns an array of annotations in the specified annotation model for the given range of text
   * sorted by type. Defined in:
   * </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
   *
   * @param annotationModel the annotation model
   * @param start the start offset of the range
   * @param end he end offset of the range
   * @return an annotation array
   */
  public final native OrionAnnotationOverlay[] getAnnotationsByType(
      OrionAnnotationModelOverlay annotationModel, int start, int end) /*-{
        return this.getAnnotationsByType(annotationModel, start, end);
    }-*/;

  /**
   * Gets the annotation type priority. The priority is determined by the order the annotation type
   * is added to the receiver. Annotation types added first have higher priority. Returns 0 if the
   * annotation type is not added. Defined in:
   * </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
   *
   * @param type the annotation type
   * @return the annotation type priority
   */
  public final native int getAnnotationTypePriority(JavaScriptObject type) /*-{
        return this.getAnnotationTypePriority(type);
    }-*/;

  /**
   * Returns the widest annotation which determines the width of the ruler. If the ruler does not
   * have a fixed width it should provide the widest annotation to avoid the ruler from changing
   * size as the view scrolls. This method is called by the text view when the ruler is redrawn.
   *
   * @return the widest annotation
   */
  public final native OrionAnnotationOverlay getWidestAnnotation() /*-{
        return this.getWidestAnnotation();
    }-*/;

  /**
   * Returns whether the receiver shows annotations of the specified type. Defined in:
   * </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
   *
   * @param type the annotation type
   * @return whether the specified annotation type is shown
   */
  public final native boolean isAnnotationTypeVisible(String type) /*-{
        return this.isAnnotationTypeVisible(type);
    }-*/;

  /**
   * Removes an annotation type from the receiver. Defined in:
   * </jobs/genie.orion/orion-client-stable/workspace/bundles/org.eclipse.orion.client.editor/web/orion/editor/annotations.js>.
   *
   * @param type the annotation type to be removed
   */
  public final native void removeAnnotationType(String type) /*-{
        this.removeAnnotationType(type);
    }-*/;

  /**
   * Sets the annotation model for the ruler.
   *
   * @param annotationModel the annotation model
   */
  public final native void setAnnotationModel(OrionAnnotationModelOverlay annotationModel) /*-{
        this.setAnnotationModel(annotationModel);
    }-*/;

  /**
   * Sets the annotation that is displayed when a given line contains multiple annotations. This
   * annotation is used when there are different types of annotations in a given line.
   *
   * @param annotation the annotation for lines with multiple annotations
   */
  public final native void setMultiAnnotation(OrionAnnotationOverlay annotation) /*-{
        this.setMultiAnnotation(annotation);
    }-*/;

  /**
   * Sets the annotation that overlays a line with multiple annotations. This annotation is
   * displayed on top of the computed annotation for a given line when there are multiple
   * annotations of the same type in the line. It is also used when the multiple annotation is not
   * set.
   *
   * @param annotation the annotation overlay for lines with multiple annotations
   */
  public final native void setMultiAnnotationOverlay(OrionAnnotationOverlay annotation) /*-{
        this.setMultiAnnotationOverlay(annotation);
    }-*/;

  /**
   * Add event listener. To get advantage of this method the current ruler must be extended by
   * adding Event Target interface.
   *
   * @param <T> the type parameter
   * @param eventType the event type
   * @param handler the handler
   * @param useCapture the use capture
   */
  public final native <T extends OrionEventOverlay> void addEventListener(
      String eventType, EventHandler<T> handler, boolean useCapture) /*-{
        var func = function (param) {
            handler.@org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay.EventHandler::onEvent(*)(param);
        };
        this.addEventListener(eventType, func, useCapture);
    }-*/;

  /**
   * Overrides OnClick function to dispatch event. Current ruler must be extended by adding Event
   * Target interface.
   */
  public final native void overrideOnClickEvent() /*-{
        this.onClick = function (lineIndex, e) {
            this.dispatchEvent({type: "RulerClick", lineIndex: lineIndex});
        };
    }-*/;

  /**
   * The interface Event handler.
   *
   * @param <T> the type parameter
   */
  public interface EventHandler<T extends OrionEventOverlay> {
    /**
     * On event.
     *
     * @param parameter the parameter
     */
    void onEvent(T parameter);
  }

  /** The ruler location, which is either "left" or "right" or "margin". */
  public enum RulerLocation {
    LEFT("left"),
    MARGIN("margin"),
    RIGHT("right");

    final String location;

    RulerLocation(String location) {
      this.location = location;
    }

    public String getLocation() {
      return location;
    }
  }

  /** The overview type, which is either "page" or "document" or "fixed". */
  public enum RulerOverview {
    PAGE("page"),
    DOCUMENT("document"),
    FIXED("fixed");

    final String overview;

    RulerOverview(String overview) {
      this.overview = overview;
    }

    public String getOverview() {
      return overview;
    }
  }
}
