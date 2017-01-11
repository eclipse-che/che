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

package org.eclipse.che.ide.ui;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.EventRemover;
import elemental.events.EventTarget;
import elemental.events.MouseEvent;
import elemental.util.Timer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.eclipse.che.ide.ui.menu.AutoHideComponent;
import org.eclipse.che.ide.ui.menu.AutoHideView;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.util.AnimationController;
import org.eclipse.che.ide.util.HoverController;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single tooltip instance attached to any element, activated by
 * hovering.
 */
/*
 * TODO: oh, my god this thing has become a monster. Might be nice to
 * get a list of requirements and start from the top... especially if we need
 * some coach marks as well for the landing page.
 */
public class Tooltip extends AutoHideComponent<AutoHideView<Void>, AutoHideComponent.AutoHideModel> {

    private static final Resources RESOURCES  = GWT.create(Resources.class);

    private static final int       SHOW_DELAY = 600;
    private static final int       HIDE_DELAY = 600;

    /** The singleton view instance that all tooltips use. */
    private static AutoHideView<Void>            tooltipViewInstance;

    /** The currently active tooltip that is bound to the view. */
    private static Tooltip                       activeTooltip;

    /** Holds a reference to the css. */
    private final  Css                           css;

    private final  List<Element>                 targetElements;
    private final  Timer                         showTimer;
    private final  TooltipRenderer               renderer;
    private final  PositionController            positionController;
    private final  List<EventRemover>            eventRemovers;
    private final  PositionController.Positioner positioner;
    private        Element                       contentElement;
    private        String                        title;
    private        String                        maxWidth;
    private        boolean                       isEnabled = true;
    private        boolean                       isShowDelayDisabled;

    static {
        RESOURCES.tooltipCss().ensureInjected();
    }

    private Tooltip(AutoHideView<Void> view,
                    Resources res,
                    List<Element> targetElements,
                    PositionController.Positioner positioner,
                    TooltipRenderer renderer,
                    boolean shouldShowOnHover) {
        super(view, new AutoHideModel());
        this.positioner = positioner;
        this.renderer = renderer;
        this.css = res.tooltipCss();
        this.targetElements = targetElements;

        this.eventRemovers =
                shouldShowOnHover ? attachToTargetElement() : new ArrayList<EventRemover>();

        getView().setAnimationController(AnimationController.FADE_ANIMATION_CONTROLLER);

        positionController = new PositionController(positioner, getView().getElement());

        showTimer = new Timer() {
            @Override
            public void run() {
                show();
            }
        };
        setDelay(HIDE_DELAY);
        setCaptureOutsideClickOnClose(false);

        getHoverController().setHoverListener(new HoverController.HoverListener() {
            @Override
            public void onHover() {
                if (isEnabled && !isShowing()) {
                    deferredShow();
                }
            }
        });
    }

    /** Static factory method for creating a simple tooltip. */
    public static Tooltip create(Element targetElement, PositionController.VerticalAlign vAlign,
                                 PositionController.HorizontalAlign hAlign, String... tooltipText) {
        return new Builder(targetElement, new TooltipPositionerBuilder().setVerticalAlign(vAlign)
                                                                        .setHorizontalAlign(hAlign)
                                                                        .buildAnchorPositioner(targetElement)).setTooltipRenderer(
                new SimpleStringRenderer(tooltipText)).build();
    }

    /** Static factory method for creating a simple tooltip with given element as content. */
    public static Tooltip create(Element targetElement, PositionController.VerticalAlign vAlign,
                                 PositionController.HorizontalAlign hAlign, final Element tooltipContent) {
        return new Builder(targetElement, new TooltipPositionerBuilder().setVerticalAlign(vAlign)
                .setHorizontalAlign(hAlign)
                .buildAnchorPositioner(targetElement)).setTooltipRenderer(

                new TooltipRenderer() {
                    @Override
                    public Element renderDom() {
                        return tooltipContent;
                    }
                }

        ).build();
    }

    /** The Tooltip is a flyweight that uses a singleton View base element. */
    private static AutoHideView<Void> getViewInstance(Css css) {
        if (tooltipViewInstance == null) {
            tooltipViewInstance = new AutoHideView<Void>(Elements.createDivElement());
            Elements.addClassName(css.tooltipPosition(), tooltipViewInstance.getElement());
        }
        return tooltipViewInstance;
    }

    @Override
    public void show() {
        // Nothing to do if it is showing.
        if (isShowing()) {
            return;
        }

        /*
         * Hide the old Tooltip. This will not actually hide the View because we set
         * activeTooltip to null.
         */
        if (activeTooltip != null) {
            activeTooltip.hide();
        }

        ensureContent();

        // Bind to the singleton view.
        getView().getElement().setInnerHTML("");
        getView().getElement().appendChild(contentElement);
        positionController.updateElementPosition();
        activeTooltip = this;

        super.show();
    }

    @Override
    public void forceHide() {
        super.forceHide();
        activeTooltip = null;
    }

    @Override
    protected void hideView() {
        // If another tooltip is being shown, do not hide the shared view.
        if (activeTooltip == this) {
            super.hideView();
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMaxWidth(String maxWidth) {
        this.maxWidth = maxWidth;

        // Update the content element if it is already created.
        if (contentElement != null) {
            if (maxWidth == null) {
                contentElement.getStyle().removeProperty("max-width");
            } else {
                contentElement.getStyle().setProperty("max-width", maxWidth);
            }
        }
    }

    /**
     * Enables or disables the show delay. If disabled, the tooltip will appear
     * instantly on hover. Defaults to enabled.
     *
     * @param isDisabled
     *         true to disable the show delay
     */
    public void setShowDelayDisabled(boolean isDisabled) {
        this.isShowDelayDisabled = isDisabled;
    }

    /** Enable or disable this tooltip */
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    private void setPositionStyle() {
        PositionController.HorizontalAlign hAlign = positioner.getHorizontalAlignment();
        switch (positioner.getVerticalAlignment()) {
            case TOP:
                Elements.addClassName(css.tooltipAbove(), contentElement);
                break;
            case BOTTOM:
                if (hAlign == PositionController.HorizontalAlign.RIGHT) {
                    Elements.addClassName(css.tooltipBelowRightAligned(), contentElement);
                } else {
                    Elements.addClassName(css.tooltipBelow(), contentElement);
                }
                break;
            case MIDDLE:
                if (hAlign == PositionController.HorizontalAlign.LEFT) {
                    Elements.addClassName(css.tooltipLeft(), contentElement);
                } else if (hAlign == PositionController.HorizontalAlign.RIGHT) {
                    Elements.addClassName(css.tooltipRight(), contentElement);
                }
                break;
        }
    }

    /**
     * Adds event handlers to the target element for the tooltip to show it on
     * hover, and update position on mouse move.
     */
    private List<EventRemover> attachToTargetElement() {
        List<EventRemover> removers = new ArrayList<>();
        for (int i = 0; i < targetElements.size(); i++) {
            final Element targetElement = targetElements.get(i);
            addPartner(targetElement);

            removers.add(targetElement.addEventListener(Event.MOUSEOUT, new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    MouseEvent mouseEvt = (MouseEvent)evt;
                    EventTarget relatedTarget = mouseEvt.getRelatedTarget();
                    // Ignore the event unless we mouse completely out of the target element.
                    if (relatedTarget == null || !targetElement.contains((Node)relatedTarget)) {
                        cancelPendingShow();
                    }
                }
            }, false));

            removers.add(targetElement.addEventListener(Event.MOUSEDOWN, new EventListener() {
                @Override
                public void handleEvent(Event evt) {
                    cancelPendingShow();
                    hide();
                }
            }, false));
        }

        return removers;
    }

    /** Removes event handlers from the target element for the tooltip. */
    private void detachFromTargetElement() {
        for (int i = 0; i < targetElements.size(); i++) {
            removePartner(targetElements.get(i));
        }
        for (int i = 0, n = eventRemovers.size(); i < n; ++i) {
            eventRemovers.get(i).remove();
        }
        eventRemovers.clear();
    }

    /**
     * Creates the dom for this tooltip's content.
     * <p/>
     * <code>
     * <div class="tooltipPosition">
     * <div class="tooltip tooltipAbove/Below/Left/Right">
     * tooltipText
     * <div class="tooltipTriangle"></div>
     * </div>
     * </div>
     * </code>
     */
    private void ensureContent() {
        if (contentElement == null) {
            contentElement = renderer.renderDom();

            if (contentElement == null) {
                // Guard against malformed renderers.
                Log.warn(getClass(), "Renderer for tooltip returned a null content element");
                contentElement = Elements.createDivElement();
                contentElement.setTextContent("An empty Tooltip!");
            }

            if (title != null) {
                // Insert a title if one is set.
                Element titleElem = Elements.createElement("b");
                titleElem.setTextContent(title);
                Element breakElem = Elements.createBRElement();
                contentElement.insertBefore(breakElem, contentElement.getFirstChild());
                contentElement.insertBefore(titleElem, contentElement.getFirstChild());
            }

            // Set the maximum width.
            setMaxWidth(maxWidth);

            Elements.addClassName(css.tooltip(), contentElement);
            Element triangle = Elements.createDivElement(css.triangle());
            contentElement.appendChild(triangle);

            setPositionStyle();
        }
    }

    public void destroy() {
        showTimer.cancel();
        forceHide();
        detachFromTargetElement();
    }

    private void deferredShow() {
        if (isShowDelayDisabled || activeTooltip != null) {
      /*
       * If there is already a tooltip showing and the user mouses over an item
       * that has it's own tooltip, move the tooltip immediately. We don't want
       * to leave a lingering tooltip on the old item.
       */
            showTimer.cancel();
            showTimer.run();
        } else {
            showTimer.schedule(SHOW_DELAY);
        }
    }

    private void cancelPendingShow() {
        showTimer.cancel();
    }

    /** Interface for specifying an arbitrary renderer for tooltips. */
    public interface TooltipRenderer {
        Element renderDom();
    }

    public interface Css extends CssResource {
        String tooltipPosition();

        String tooltip();

        String triangle();

        String tooltipAbove();

        String tooltipRight();

        String tooltipBelow();

        String tooltipLeft();

        String tooltipBelowRightAligned();
    }

    public interface Resources extends ClientBundle {
        @Source({"org/eclipse/che/ide/ui/constants.css", "Tooltip.css", "org/eclipse/che/ide/api/ui/style.css"})
        Css tooltipCss();

    }

    /** A builder used to construct a new Tooltip. */
    public static class Builder {

        private final Resources                     res;
        private final List<Element> targetElements;
        private final PositionController.Positioner positioner;
        private boolean shouldShowOnHover = true;
        private TooltipRenderer renderer;

        /** @see TooltipPositionerBuilder */
        public Builder(Element targetElement, PositionController.Positioner positioner) {
            this.res = RESOURCES;
            this.positioner = positioner;
            this.targetElements = new ArrayList<>();
            this.targetElements.add(targetElement);
        }

        /**
         * Adds additional target elements. If the user hovers over any of the target elements, the
         * tooltip will appear.
         */
        public Builder addTargetElements(Element... additionalTargets) {
            for (int i = 0; i < additionalTargets.length; i++) {
                targetElements.add(additionalTargets[i]);
            }
            return this;
        }

        /**
         * Sets the tooltip text. Each item in the array appears on a new line. This
         * method overwrites the tooltip renderer.
         */
        public Builder setTooltipText(String... tooltipText) {
            return setTooltipRenderer(new SimpleStringRenderer(tooltipText));
        }

        public Builder setTooltipRenderer(TooltipRenderer renderer) {
            this.renderer = renderer;
            return this;
        }

        /** If false, will prevent the tooltip from automatically showing on hover. */
        public Builder setShouldListenToHover(boolean shouldShowOnHover) {
            this.shouldShowOnHover = shouldShowOnHover;
            return this;
        }

        public Tooltip build() {
            return new Tooltip(getViewInstance(res.tooltipCss()),
                               res,
                               targetElements,
                               positioner,
                               renderer,
                               shouldShowOnHover);
        }
    }

    /**
     * A {@link org.eclipse.che.ide.ui.menu.PositionController.PositionerBuilder} which uses some more convenient defaults for tooltips. This
     * builder
     * defaults to {@link org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign#BOTTOM} {@link
     * org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign#MIDDLE} and
     * {@link org.eclipse.che.ide.ui.menu.PositionController.Position#NO_OVERLAP}.
     */
    public static class TooltipPositionerBuilder extends PositionController.PositionerBuilder {
        public TooltipPositionerBuilder() {
            setVerticalAlign(PositionController.VerticalAlign.BOTTOM);
            setHorizontalAlign(PositionController.HorizontalAlign.MIDDLE);
            setPosition(PositionController.Position.NO_OVERLAP);
        }
    }

    /** Default renderer that simply renders the tooltip text with no other DOM. */
    private static class SimpleStringRenderer implements TooltipRenderer {
        private final String[] tooltipText;

        SimpleStringRenderer(String... tooltipText) {
            this.tooltipText = tooltipText;
        }

        @Override
        public Element renderDom() {
            Element content = Elements.createDivElement();
            int i = 0;
            for (String p : tooltipText) {
                content.appendChild(Elements.createTextNode(p));
                if (i < tooltipText.length - 1) {
                    content.appendChild(Elements.createBRElement());
                    content.appendChild(Elements.createBRElement());
                }
                i++;
            }
            return content;
        }
    }
}
