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
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;

/** @author Evgen Vidolob */
public class OrionLinkedModeOverlay extends JavaScriptObject implements LinkedMode {
  protected OrionLinkedModeOverlay() {}

  /**
   * Starts Linked Mode, selects the first position and registers the listeners.
   *
   * @param linkedModel linkedModeModel An object describing the model to be used by linked mode.
   *     Contains one or more position groups. If a position in a group is edited, the other
   *     positions in the same group are edited the same way. The model structure is as follows:
   *     <pre>{
   * 	groups: [{
   * 		data: {},
   * 		positions: [{
   * 			offset: 10, // Relative to the text buffer
   * 			length: 3
   * 		}]
   * 	}],
   * 	escapePosition: 19, // Relative to the text buffer
   * }</pre>
   *     Each group in the model has an optional <code>data</code> property which can be used to
   *     provide additional content assist for the group. The <code>type</code> in data determines
   *     what kind of content assist is provided. These are the support structures for the <code>
   *     data</code> property.
   *     <pre>{
   * 	type: "link"
   * 	values: ["proposal0", "proposal1", ...]
   * }</pre>
   *     The "link" data struture provides static content assist proposals stored in the <code>
   *     values</code> property.
   */
  public final native void enterLinkedMode(OrionLinkedModelOverlay linkedModel) /*-{
        this.enterLinkedMode(linkedModel);
    }-*/;

  /**
   * Exits Linked Mode. Optionally places the caret at linkedMode escapePosition.
   *
   * @param escapePosition if true, place the caret at the escape position.
   */
  @Override
  public final native void exitLinkedMode(boolean escapePosition) /*-{
        this.exitLinkedMode(escapePosition);
    }-*/;

  /**
   * Exits Linked Mode. Optionally places the caret at linkedMode escapePosition.
   *
   * @param escapePosition if true, place the caret at the escape position.
   * @param successful successful or not exit linked mode
   */
  @Override
  public final native void exitLinkedMode(boolean escapePosition, boolean successful) /*-{
        this.exitLinkedMode(escapePosition, successful);
    }-*/;

  @Override
  public final native void selectLinkedGroup(int index) /*-{
        this.selectLinkedGroup(index);
    }-*/;

  @Override
  public final native void addListener(LinkedModeListener listener) /*-{
        if ($wnd.che_handels === undefined) {
            $wnd.che_handels = {};
        }
        var start, end;
        var model = this._annotationModel
        var annotationListener = function () {
            var annotations = model.getAnnotations(), annotation;
            while (annotations.hasNext()) {
                annotation = annotations.next();
                switch (annotation.type) {

                    case "orion.annotation.linkedGroup":
                    case "orion.annotation.selectedLinkedGroup":
                    case "orion.annotation.currentLinkedGroup":
                    {
                        start = annotation.start;
                        end = annotation.end;
                    }
                }
            }

        };
        this._annotationModel.addEventListener("Changed", annotationListener, true);
        this.annotationListener = annotationListener;
        var func = function (param) {
            listener.@org.eclipse.che.ide.api.editor.link.LinkedMode.LinkedModeListener::onLinkedModeExited(*)(param.isSuccessful,
                start || -1, end || -1);
        };
        $wnd.che_handels[listener] = func;
        this.addEventListener("LinkedModeExit", func, true);
    }-*/;

  @Override
  public final native void removeListener(LinkedModeListener listener) /*-{
        this._annotationModel.removeEventListener("Changed", this.annotationListener, true);
        this.removeEventListener("LinkedModeExit", $wnd.che_handels[listener], true);
    }-*/;

  @Override
  public final void enterLinkedMode(LinkedModel model) {
    if (model instanceof OrionLinkedModelOverlay) {
      enterLinkedMode((OrionLinkedModelOverlay) model);
    } else {
      throw new IllegalArgumentException(
          "This implementation supports only OrionLinkedModelOverlay model");
    }
  }
}
