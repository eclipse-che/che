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

package org.eclipse.che.ide.ui.menu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import elemental.css.CSSStyleDeclaration;
import elemental.css.CSSStyleDeclaration.Unit;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.ClientRect;
import org.eclipse.che.ide.runtime.Assert;
import org.eclipse.che.ide.util.CssUtils;
import org.eclipse.che.ide.util.RelativeClientRect;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * A controller which handles positioning an element relative to another element. This controller is
 * aware of the screen position and will attempt to keep an element on the screen if it would
 * otherwise run off. As an additional wrinkle, if flipping it does not produce a valid screen
 * position it will just set the offending dimension to 0 .
 */
/*
 * TODO: In the case where things don't fit on screen we perform a few extra layouts, this
 * could be fixed by offsetting the original rect until we find a position that works then
 * performing a second layout to actually move it.
 */
public class PositionController {

  private static final String GWT_ROOT = "gwt_root";

  static {
    if (Elements.getElementById(GWT_ROOT) == null) {
      com.google.gwt.user.client.Element div = DOM.createDiv();
      div.setId(GWT_ROOT);
      Elements.getBody().appendChild((Node) div);
    }
  }

  /**
   * A builder which specifies positioning options for a {@link PositionController}.It defaults to
   * using {@link VerticalAlign#TOP}, {@link HorizontalAlign#LEFT}, and {@link Position#OVERLAP}.
   */
  public static class PositionerBuilder {

    private VerticalAlign verticalAlignment = VerticalAlign.TOP;

    private HorizontalAlign horizontalAlignment = HorizontalAlign.LEFT;

    private Position position = Position.OVERLAP;

    public PositionerBuilder setVerticalAlign(VerticalAlign verticalAlignment) {
      this.verticalAlignment = verticalAlignment;
      return this;
    }

    public PositionerBuilder setHorizontalAlign(HorizontalAlign horizontalAlignment) {
      this.horizontalAlignment = horizontalAlignment;
      return this;
    }

    public PositionerBuilder setPosition(Position position) {
      this.position = position;
      return this;
    }

    /**
     * Creates a positioner which positions an element next to the provided anchor using gwt_root as
     * the container. This is the preferred method of positioning if the element is not going to
     * deal with scrolling in anyway and just needs to be positioned around an element.
     */
    public Positioner buildAnchorPositioner(Element anchor) {
      return new AnchorPositioner(anchor, verticalAlignment, horizontalAlignment, position);
    }

    /**
     * Creates a positioner which positions an element next to the provided anchor using the anchors
     * offsetParent as the container. This is the preferred method of positioning if the parent is a
     * scrollable container.
     */
    public Positioner buildOffsetFromParentPositioner(Element anchor) {
      return new OffsetPositioner(
          anchor,
          OffsetPositioner.ANCHOR_OFFSET_PARENT,
          verticalAlignment,
          horizontalAlignment,
          position);
    }

    /**
     * Creates a positioner which positions an element next to the provided anchor using the
     * supplied ancestor as the container. This is the preferred method of positioning if the
     * ancestor is a scrollable and anchor is not a direct child.
     */
    public Positioner buildOffsetFromAncestorPositioner(Element anchor, Element ancestor) {
      return new OffsetPositioner(
          anchor, ancestor, verticalAlignment, horizontalAlignment, position);
    }

    /**
     * Creates a positioner which appends elements to the body of the document allowing positioning
     * absolutely at a given point in the viewport (typically mouse coordinates).
     */
    public Positioner buildMousePositioner() {
      return new MousePositioner(verticalAlignment, horizontalAlignment, position);
    }
  }

  /** The base functionality of a positioner. */
  public abstract static class Positioner {
    private final VerticalAlign verticalAlignment;

    private final HorizontalAlign horizontalAlignment;

    private final Position position;

    private VerticalAlign curVerticalAlignment;

    private HorizontalAlign curHorizontalAlignment;

    public Positioner(
        VerticalAlign verticalAlignment, HorizontalAlign horizontalAlignment, Position position) {
      this.verticalAlignment = verticalAlignment;
      this.horizontalAlignment = horizontalAlignment;
      this.position = position;

      revert();
    }

    public VerticalAlign getVerticalAlignment() {
      return curVerticalAlignment;
    }

    public HorizontalAlign getHorizontalAlignment() {
      return curHorizontalAlignment;
    }

    public Position getPosition() {
      return position;
    }

    /** Appends an element to the appropriate place in the DOM for this positioner */
    abstract void appendElementToContainer(Element element);

    /**
     * Returns the minimum width that should be used by elements being positioned by this
     * positioner. The meaning of this value varies depending on the implementation.
     */
    public abstract int getMinimumWidth();

    abstract double getTop(ClientRect elementRect, int y);

    abstract double getLeft(ClientRect elementRect, int x);

    void flip(VerticalAlign verticalAlignment, HorizontalAlign horizontalAlignment) {
      curVerticalAlignment = verticalAlignment;
      curHorizontalAlignment = horizontalAlignment;
    }

    void revert() {
      curVerticalAlignment = verticalAlignment;
      curHorizontalAlignment = horizontalAlignment;
    }
  }

  /** A positioner which positions an element next to another element. */
  public static class AnchorPositioner extends Positioner {

    private final Element anchor;

    private AnchorPositioner(
        Element anchor,
        VerticalAlign verticalAlignment,
        HorizontalAlign horizontalAlignment,
        Position position) {
      super(verticalAlignment, horizontalAlignment, position);
      this.anchor = anchor;
    }

    /** @return the width of the anchor used by this {@link Positioner}. */
    @Override
    public int getMinimumWidth() {
      return (int) anchor.getBoundingClientRect().getWidth();
    }

    /** Appends the element to the body of the DOM */
    @Override
    void appendElementToContainer(Element element) {
      Element gwt = Elements.getElementById(GWT_ROOT);
      gwt.appendChild(element);
    }

    @Override
    double getTop(ClientRect elementRect, int offsetY) {
      Element gwt = Elements.getElementById(GWT_ROOT);
      ClientRect anchorRect =
          RelativeClientRect.relativeToRect(
              gwt.getBoundingClientRect(), anchor.getBoundingClientRect());

      switch (getVerticalAlignment()) {
        case TOP:
          return anchorRect.getTop() - elementRect.getHeight() - offsetY;
        case MIDDLE:
          double anchory = anchorRect.getTop() + anchorRect.getHeight() / 2;
          return anchory - elementRect.getHeight() / 2 - offsetY;
        case BOTTOM:
          return anchorRect.getBottom() + offsetY;
        case TOP_TOP:
          return anchorRect.getTop();
        default:
          return 0;
      }
    }

    @Override
    double getLeft(ClientRect elementRect, int offsetX) {
      Element gwt = Elements.getElementById(GWT_ROOT);
      ClientRect anchorRect =
          RelativeClientRect.relativeToRect(
              gwt.getBoundingClientRect(), anchor.getBoundingClientRect());

      switch (getHorizontalAlignment()) {
        case LEFT:
          if (getPosition() == Position.OVERLAP) {
            return anchorRect.getLeft() + offsetX;
          } else {
            return anchorRect.getLeft() - elementRect.getWidth() - offsetX;
          }
        case MIDDLE:
          double anchorx = anchorRect.getLeft() + anchorRect.getWidth() / 2;
          double left = anchorx - elementRect.getWidth() / 2 - offsetX;
          if (left < 0) {
            left = -1;
          }
          return left;
        case RIGHT:
          if (getPosition() == Position.OVERLAP) {
            return anchorRect.getRight() - elementRect.getWidth() - offsetX;
          } else {
            return anchorRect.getRight() + offsetX;
          }
        default:
          return 0;
      }
    }
  }

  public static class OffsetPositioner extends Positioner {

    /** Indicates that the offsetParent of the anchor should be used for positioning. */
    public static final Element ANCHOR_OFFSET_PARENT = Elements.createDivElement();

    private final Element anchor;

    private final Element offsetAncestor;

    private double anchorOffsetTop = -1;

    private double anchorOffsetLeft = -1;

    private OffsetPositioner(
        Element anchor,
        Element ancestor,
        VerticalAlign verticalAlignment,
        HorizontalAlign horizontalAlignment,
        Position position) {
      super(verticalAlignment, horizontalAlignment, position);
      this.anchor = anchor;
      this.offsetAncestor = ancestor;
    }

    /** @return the width of the anchor used by this {@link Positioner}. */
    @Override
    public int getMinimumWidth() {
      return (int) anchor.getBoundingClientRect().getWidth();
    }

    /** Appends the element to the specified ancestor of the anchor. */
    @Override
    void appendElementToContainer(Element element) {
      Element container = getOffsetAnchestorForAnchor();
      container.appendChild(element);
    }

    @Override
    double getTop(ClientRect elementRect, int offsetY) {
      // This rect is to only be used for width and height since the coordinates are relative to the
      // viewport and we are relative to an offsetParent.
      ClientRect anchorRect = anchor.getBoundingClientRect();

      ensureOffsetCalculated();
      switch (getVerticalAlignment()) {
        case TOP:
          return anchorOffsetTop - elementRect.getHeight() - offsetY;
        case MIDDLE:
          double anchory = anchorOffsetTop + anchorRect.getHeight() / 2;
          return anchory - elementRect.getHeight() / 2 - offsetY;
        case BOTTOM:
          double anchorBottom = anchorOffsetTop + anchorRect.getHeight();
          return anchorBottom + offsetY;
        default:
          return 0;
      }
    }

    @Override
    double getLeft(ClientRect elementRect, int offsetX) {
      // This rect is to only be used for width and height since the coordinates are relative to the
      // viewport and we are relative to an offsetParent.
      ClientRect anchorRect = anchor.getBoundingClientRect();

      ensureOffsetCalculated();
      switch (getHorizontalAlignment()) {
        case LEFT:
          if (getPosition() == Position.OVERLAP) {
            return anchorOffsetLeft + offsetX;
          } else {
            return anchorOffsetLeft - elementRect.getWidth() - offsetX;
          }
        case MIDDLE:
          double anchorx = anchorOffsetLeft + anchorRect.getWidth() / 2;
          return anchorx - elementRect.getWidth() / 2 - offsetX;
        case RIGHT:
          double anchorRight = anchorOffsetLeft + anchorRect.getWidth();
          if (getPosition() == Position.OVERLAP) {
            return anchorRight - elementRect.getWidth() - offsetX;
          } else {
            return anchorRight + offsetX;
          }
        default:
          return 0;
      }
    }

    private Element getOffsetAnchestorForAnchor() {
      Element e =
          offsetAncestor == ANCHOR_OFFSET_PARENT ? anchor.getOffsetParent() : offsetAncestor;
      return e == null ? Elements.getBody() : e;
    }

    private void ensureOffsetCalculated() {
      if (anchorOffsetTop >= 0 && anchorOffsetLeft >= 0) {
        return;
      }

      Element ancestor = getOffsetAnchestorForAnchor();
      anchorOffsetTop = anchorOffsetLeft = 0;
      for (Element e = anchor; e != ancestor; e = e.getOffsetParent()) {
        Assert.isNotNull(e, "Offset parent specified is not in ancestory chain");
        anchorOffsetTop += e.getOffsetTop();
        anchorOffsetLeft += e.getOffsetLeft();
      }
    }
  }

  /** A positioner which positions directly next to a point such as the mouse. */
  public static class MousePositioner extends Positioner {
    private MousePositioner(
        VerticalAlign verticalAlignment, HorizontalAlign horizontalAlignment, Position position) {
      super(verticalAlignment, horizontalAlignment, position);
    }

    /** @returns 0 since this is being positioned next to the mouse. */
    @Override
    public int getMinimumWidth() {
      return 0;
    }

    /** Appends the element to the document body */
    @Override
    void appendElementToContainer(Element element) {
      Elements.getBody().appendChild(element);
    }

    @Override
    double getTop(ClientRect elementRect, int mouseY) {
      double top;
      switch (getVerticalAlignment()) {
        case TOP:
          top = mouseY - elementRect.getHeight();
          break;
        case MIDDLE:
          top = mouseY - elementRect.getHeight() / 2;
          break;
        case BOTTOM:
        default:
          top = mouseY;
          break;
      }
      return top;
    }

    @Override
    double getLeft(ClientRect elementRect, int mouseX) {
      double left;
      switch (getHorizontalAlignment()) {
        case LEFT:
          left = mouseX;
          break;
        case MIDDLE:
          left = mouseX - elementRect.getWidth() / 2;
          break;
        case RIGHT:
        default:
          left = mouseX - elementRect.getWidth();
          break;
      }
      return left;
    }
  }

  public enum VerticalAlign {
    /** Aligns the bottom of the element to the top of the anchor. */
    TOP,
    /** Aligns the top of the element to the bottom of the anchor. */
    BOTTOM,
    /** Aligns the middle of the element to the middle of the anchor. */
    MIDDLE,

    /** Aligns the top of the element to the top of the anchor. */
    TOP_TOP,
  }

  public enum HorizontalAlign {
    /** Aligns to the left side of the anchor. */
    LEFT,
    /** Aligns to the horizontal middle of the anchor. */
    MIDDLE,
    /** Aligns to the right side of the anchor. */
    RIGHT,
  }

  /**
   * Changes the position of the element. If the element is aligned with the RIGHT or LEFT side of
   * the anchor, Position will determine whether or not the element overlaps the anchor.
   */
  public enum Position {
    OVERLAP,
    NO_OVERLAP
  }

  /**
   * Used to specify that a value should be ignored by {@link #setElementLeftAndTop(double,
   * double)}.
   */
  private static final double IGNORE = -1;

  private final Element element;

  private final Positioner elementPositioner;

  public PositionController(Positioner positioner, Element element) {
    this.elementPositioner = positioner;
    this.element = element;
  }

  /** Updates the element's position to move it to the correct location. */
  public void updateElementPosition() {
    updateElementPosition(0, 0);
  }

  /**
   * Updates the element's position. If this controller is aligning next to an anchor then x and y
   * will be offsets, otherwise they will be treated as the absolute x and y position to align to.
   *
   * <p>
   *
   * <p>Note: If used as offsets x and y are relative to the aligned edge i.e. if you are aligned to
   * the right then x moves you left vs aligning to the left where x moves you to the right.
   */
  public void updateElementPosition(int x, int y) {
    place(x, y);
    // check if we're at a valid place, if not temporarily flip the positioner
    // and place again.
    VerticalAlign originalVertical = elementPositioner.getVerticalAlignment();
    HorizontalAlign originalHorizontal = elementPositioner.getHorizontalAlignment();

    if (!checkPositionValidAndMaybeUpdatePositioner()) {
      place(x, y);

      boolean wasVerticalFlipped = originalVertical != elementPositioner.getVerticalAlignment();
      boolean wasHorizontalFlipped =
          originalHorizontal != elementPositioner.getHorizontalAlignment();

      // Check if the new position is valid,
      if (!checkPositionValidAndMaybeUpdatePositioner()) {
        /*
         * We try to make our best move here, if the element is off the screen in both dimensions
         * then the window is tiny and we try to move it to 0,0. if it's only one dimensions we move
         * it to either the top or left of the screen.
         */
        if (wasVerticalFlipped) {
          setElementLeftAndTop(IGNORE, 0);
        }
        if (wasHorizontalFlipped) {
          setElementLeftAndTop(0, IGNORE);
        }
      }
    }

    // revert any temporary changes made to our positioner
    elementPositioner.revert();
  }

  public Positioner getPositioner() {
    return elementPositioner;
  }

  /**
   * Checks if the element is completely visible on the screen, if not it will temporarily flip our
   * {@link #elementPositioner} with updated alignment values which might work to fix the problem.
   */
  private boolean checkPositionValidAndMaybeUpdatePositioner() {
    // recalculate the element's dimensions and check to see if any of the edges
    // of the element are outside the window
    ClientRect elementRect = ensureVisibleAndGetRect(element);

    VerticalAlign updatedVerticalAlign = elementPositioner.getVerticalAlignment();
    HorizontalAlign updatedHorizontalAlign = elementPositioner.getHorizontalAlignment();

    if (elementRect.getBottom() > Window.getClientHeight()) {
      updatedVerticalAlign = VerticalAlign.TOP;
    } else if (elementRect.getTop() < 0) {
      updatedVerticalAlign = VerticalAlign.BOTTOM;
    }

    if (elementRect.getRight() > Window.getClientWidth()) {
      updatedHorizontalAlign = HorizontalAlign.RIGHT;
    } else if (elementRect.getLeft() < 0) {
      updatedHorizontalAlign = HorizontalAlign.LEFT;
    }

    if (updatedVerticalAlign != elementPositioner.getVerticalAlignment()
        || updatedHorizontalAlign != elementPositioner.getHorizontalAlignment()) {
      elementPositioner.flip(updatedVerticalAlign, updatedHorizontalAlign);
      return false;
    }
    return true;
  }

  /**
   * Place the element based on the given information.
   *
   * @param x the offset or location depending on the underlying positioner.
   * @param y the offset or location depending on the underlying positioner.
   */
  private void place(int x, int y) {
    resetElementPosition();

    ClientRect elementRect = ensureVisibleAndGetRect(element);
    double left = elementPositioner.getLeft(elementRect, x);
    double top = elementPositioner.getTop(elementRect, y);

    setElementLeftAndTop(left, top);
  }

  /** Sets an elements left and top to the provided values. */
  private void setElementLeftAndTop(double left, double top) {
    CSSStyleDeclaration style = element.getStyle();
    if (left != IGNORE) {
      style.setLeft(left, Unit.PX);
    }
    if (top != IGNORE) {
      style.setTop(top, Unit.PX);
    }
  }

  /**
   * Resets an element's position by removing top/right/bottom/left and setting position to
   * absolute.
   */
  private void resetElementPosition() {
    CSSStyleDeclaration style = element.getStyle();
    style.setPosition("absolute");
    style.clearTop();
    style.clearRight();
    style.clearBottom();
    style.clearLeft();

    elementPositioner.appendElementToContainer(element);
  }

  /**
   * Ensures that an element is not display: none and is just visibility hidden so we can get an
   * accurate client rect.
   */
  private static ClientRect ensureVisibleAndGetRect(Element element) {
    // Try to get rect and see if it isn't all 0's
    ClientRect rect = element.getBoundingClientRect();
    double rectSum =
        rect.getBottom()
            + rect.getTop()
            + rect.getLeft()
            + rect.getRight()
            + rect.getHeight()
            + rect.getWidth();
    if (rectSum != 0) {
      return rect;
    }

    // We make an attempt to get an accurate measurement of the element
    CSSStyleDeclaration style = element.getStyle();
    String visibility = CssUtils.setAndSaveProperty(element, "visibility", "hidden");
    String display = style.getDisplay();

    // if display set to none we remove it and let its normal style show through
    if (style.getDisplay().equals("none")) {
      style.removeProperty("display");
    } else {
      // it's likely display: none in a css class so we just have to guess.
      // We guess display:block since that's common on containers.
      style.setDisplay("block");
    }
    rect = element.getBoundingClientRect();
    style.setDisplay(display);
    style.setVisibility(visibility);

    return rect;
  }
}
