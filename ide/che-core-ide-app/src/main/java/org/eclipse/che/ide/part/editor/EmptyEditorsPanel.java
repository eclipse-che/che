/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.editor;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.LIElement;
import elemental.html.SpanElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.actions.CreateProjectAction;
import org.eclipse.che.ide.actions.ImportProjectAction;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.newresource.NewFileAction;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.KeyMapUtil;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.api.resources.Resource.PROJECT;

/**
 * Represent empty state of editors panel
 */
public class EmptyEditorsPanel extends Composite implements ResourceChangedEvent.ResourceChangedHandler {

    private static EmptyEditorsPanelUiBinder ourUiBinder = GWT.create(EmptyEditorsPanelUiBinder.class);
    protected final AppContext                   appContext;
    private final   ActionManager                actionManager;
    private final   Provider<PerspectiveManager> perspectiveManagerProvider;
    private final   KeyBindingAgent              keyBindingAgent;
    private final   PresentationFactory          presentationFactory;
    private final   CoreLocalizationConstant     localizationConstant;

    private final Map<String, Action> noFiles = new HashMap<>();
    private final Map<String, Action> noProjects = new HashMap<>();
    @UiField
    protected       DivElement                   title;
    @UiField
    protected       DivElement                   root;
    @UiField
    protected       DivElement                   container;
    @UiField
    protected       DivElement                   logo;
    @UiField
    Css style;

    @Inject
    public EmptyEditorsPanel(ActionManager actionManager,
                             Provider<PerspectiveManager> perspectiveManagerProvider,
                             KeyBindingAgent keyBindingAgent,
                             ProductInfoDataProvider productInfoDataProvider,
                             AppContext appContext,
                             EventBus eventBus,
                             CoreLocalizationConstant localizationConstant,
                             NewFileAction newFileAction,
                             CreateProjectAction createProjectAction,
                             ImportProjectAction importProjectAction) {
        this(actionManager, perspectiveManagerProvider, keyBindingAgent, appContext, localizationConstant, newFileAction,
             createProjectAction, importProjectAction);


        eventBus.addHandler(ResourceChangedEvent.getType(), this);
        logo.appendChild(new SVGImage(productInfoDataProvider.getLogo()).getSvgElement().getElement());
        //Sometimes initialization of Create/Import Project actions are completed after the Empty editor page is rendered.
        //In this case we need to wait when actions will be initialized.
        Timer hoverToRenderTimer = new Timer() {
            @Override
            public void run() {
                renderNoProjects();
            }
        };
        hoverToRenderTimer.schedule(500);
    }

    public EmptyEditorsPanel(ActionManager actionManager,
                             Provider<PerspectiveManager> perspectiveManagerProvider,
                             KeyBindingAgent keyBindingAgent,
                             AppContext appContext,
                             CoreLocalizationConstant localizationConstant,
                             NewFileAction newFileAction,
                             CreateProjectAction createProjectAction,
                             ImportProjectAction importProjectAction) {
        this.actionManager = actionManager;
        this.perspectiveManagerProvider = perspectiveManagerProvider;
        this.keyBindingAgent = keyBindingAgent;
        this.appContext = appContext;
        this.localizationConstant = localizationConstant;

        noFiles.put("Create File...", newFileAction);
        noFiles.put("Create Project...", createProjectAction);

        noProjects.put("Import Project...", importProjectAction);
        noProjects.put("Create Project...", createProjectAction);

        presentationFactory = new PresentationFactory();

        Widget rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);
    }

    @Override
    public void onResourceChanged(ResourceChangedEvent event) {
        final ResourceDelta delta = event.getDelta();
        final Resource resource = delta.getResource();

        if (!(resource.getResourceType() == PROJECT && resource.getLocation().segmentCount() == 1)) {
            return;
        }

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                updateOnProjectsChange();
            }
        });

    }

    private void updateOnProjectsChange() {
        if (appContext.getProjects().length != 0) {
            renderNoFiles();
        } else {
            renderNoProjects();
        }
    }

    protected void renderNoProjects() {
        render(localizationConstant.emptyStateNoProjects(), noProjects);
    }

    protected void renderNoFiles() {
        render(localizationConstant.emptyStateNoFiles(), noFiles);
    }

    private void render(String title, Map<String, Action> actions) {
        this.title.setInnerText(title);
        container.removeAllChildren();
        Element listElement = Elements.createElement("ul", new String[] {style.list()});


        for (Map.Entry<String, Action> pair : actions.entrySet()) {
            LIElement liElement = Elements.createLiElement();
            liElement.appendChild(renderAction(pair.getKey(), pair.getValue()));
            listElement.appendChild(liElement);
        }

        container.appendChild((com.google.gwt.dom.client.Node)listElement);
    }

    private Node renderAction(String title, final Action action) {
        final Presentation presentation = presentationFactory.getPresentation(action);
        Element divElement = Elements.createDivElement(style.listElement());
        divElement.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                ActionEvent event = new ActionEvent(presentation, actionManager, perspectiveManagerProvider.get());
                action.actionPerformed(event);
            }
        }, true);
        divElement.getStyle().setCursor("pointer");
        divElement.getStyle().setColor(Style.getOutputLinkColor());
        Element label = Elements.createDivElement(style.actionLabel());
        label.setInnerText(title);
        divElement.appendChild(label);

        String hotKey = KeyMapUtil.getShortcutText(keyBindingAgent.getKeyBinding(actionManager.getId(action)));
        if (hotKey == null) {
            hotKey = "&nbsp;";
        } else {
            hotKey =
                    "<nobr>&nbsp;" + hotKey + "&nbsp;</nobr>";
        }
        SpanElement hotKeyElement = Elements.createSpanElement(style.hotKey());
        hotKeyElement.setInnerHTML(hotKey);
        divElement.appendChild(hotKeyElement);
        return divElement;
    }

    interface Css extends CssResource {
        String list();

        String parent();

        String center();

        String child();

        String listElement();

        String title();

        String hotKey();

        String actionLabel();
    }

    interface EmptyEditorsPanelUiBinder extends UiBinder<Widget, EmptyEditorsPanel> {}
}
