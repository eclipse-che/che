/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.Breakpoint;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.BreakpointStateEvent;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerState;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeExtension;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import org.eclipse.che.ide.ext.java.jdi.client.debug.expression.EvaluateExpressionPresenter;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolver;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolverFactory;
import org.eclipse.che.ide.ext.java.jdi.client.fqn.FqnResolverObserver;
import org.eclipse.che.ide.ext.java.jdi.client.marshaller.DebuggerEventListUnmarshallerWS;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPoint;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakPointEvent;
import org.eclipse.che.ide.ext.java.jdi.shared.BreakpointActivatedEvent;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEvent;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.java.jdi.shared.DebuggerInfo;
import org.eclipse.che.ide.ext.java.jdi.shared.Location;
import org.eclipse.che.ide.ext.java.jdi.shared.StackFrameDump;
import org.eclipse.che.ide.ext.java.jdi.shared.StepEvent;
import org.eclipse.che.ide.ext.java.jdi.shared.UpdateVariableRequest;
import org.eclipse.che.ide.ext.java.jdi.shared.Value;
import org.eclipse.che.ide.ext.java.jdi.shared.Variable;
import org.eclipse.che.ide.ext.java.jdi.shared.VariablePath;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPStatus;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;
import org.eclipse.che.ide.websocket.rest.exceptions.ServerException;
import org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.debug.DebuggerStateEvent.createConnectedStateEvent;
import static org.eclipse.che.ide.debug.DebuggerStateEvent.createDisconnectedStateEvent;
import static org.eclipse.che.ide.debug.DebuggerStateEvent.createInitializedStateEvent;
import static org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEvent.BREAKPOINT;
import static org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEvent.BREAKPOINT_ACTIVATED;
import static org.eclipse.che.ide.ext.java.jdi.shared.DebuggerEvent.STEP;
import static org.eclipse.che.ide.api.editor.EditorAgent.OpenEditorCallback;

/**
 * The presenter provides debug java application.
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 * @author Anatoliy Bazko
 * @author Mykola Morhun
 */
@Singleton
public class DebuggerPresenter extends BasePresenter implements DebuggerView.ActionDelegate, Debugger, FqnResolverObserver {

    protected static final String LOCAL_STORAGE_DEBUGGER_KEY = "che-debugger";
    private static final   String TITLE                      = "Debug";

    private final DtoFactory               dtoFactory;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final AppContext               appContext;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaNodeManager          javaNodeManager;
    private final JavaRuntimeResources     javaRuntimeResources;
    private final LocalStorageProvider     localStorageProvider;
    private final ToolbarPresenter         debuggerToolbar;
    private final FileTypeRegistry fileTypeRegistry;
    /** Channel identifier to receive events from debugger over WebSocket. */
    private String                                 debuggerEventsChannel;
    /** Channel identifier to receive event when debugger will be disconnected. */
    private String                                 debuggerDisconnectedChannel;
    private DebuggerView                           view;
    private EventBus                               eventBus;
    private DebuggerServiceClient                  service;
    private JavaRuntimeLocalizationConstant        constant;
    private DebuggerInfo                           debuggerInfo;
    private MessageBus                             messageBus;
    private BreakpointManager                      breakpointManager;
    private WorkspaceAgent                         workspaceAgent;
    private FqnResolverFactory                     resolverFactory;
    private EditorAgent                            editorAgent;
    private DebuggerVariable                       selectedVariable;
    private NotificationManager                    notificationManager;
    /** Handler for processing events which is received from debugger over WebSocket connection. */
    private SubscriptionHandler<DebuggerEventList> debuggerEventsHandler;
    private SubscriptionHandler<Void>              debuggerDisconnectedHandler;
    private List<DebuggerVariable>                 variables;
    private Location                               executionPoint;

    @Inject
    public DebuggerPresenter(final DebuggerView view,
                             final DebuggerServiceClient service,
                             final EventBus eventBus,
                             final JavaRuntimeLocalizationConstant constant,
                             final WorkspaceAgent workspaceAgent,
                             final BreakpointManager breakpointManager,
                             final FqnResolverFactory resolverFactory,
                             final EditorAgent editorAgent,
                             final EvaluateExpressionPresenter evaluateExpressionPresenter,
                             final NotificationManager notificationManager,
                             final DtoFactory dtoFactory,
                             final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                             final AppContext appContext,
                             final ProjectExplorerPresenter projectExplorer,
                             final MessageBusProvider messageBusProvider,
                             final JavaNodeManager javaNodeManager,
                             final JavaRuntimeResources javaRuntimeResources,
                             final LocalStorageProvider localStorageProvider,
                             final @DebuggerToolbar ToolbarPresenter debuggerToolbar,
                             final FileTypeRegistry fileTypeRegistry) {
        this.view = view;
        this.eventBus = eventBus;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
        this.projectExplorer = projectExplorer;
        this.javaRuntimeResources = javaRuntimeResources;
        this.debuggerToolbar = debuggerToolbar;
        this.fileTypeRegistry = fileTypeRegistry;
        this.view.setDelegate(this);
        this.view.setTitle(TITLE);
        this.service = service;
        this.constant = constant;
        this.workspaceAgent = workspaceAgent;
        this.breakpointManager = breakpointManager;
        this.resolverFactory = resolverFactory;
        this.resolverFactory.addFqnResolverObserver(this);
        this.variables = new ArrayList<>();
        this.editorAgent = editorAgent;
        this.notificationManager = notificationManager;
        this.javaNodeManager = javaNodeManager;
        this.addRule(ProjectPerspective.PROJECT_PERSPECTIVE_ID);
        this.localStorageProvider = localStorageProvider;
        this.debuggerInfo = EmptyDebuggerInfo.INSTANCE;


        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                messageBus = messageBusProvider.getMachineMessageBus();
                debuggerInfo = loadDebugInfo();

                if (isDebuggerConnected()) {
                    service.checkEvents(debuggerInfo.getId(), new AsyncRequestCallback<DebuggerEventList>() {
                        @Override
                        protected void onSuccess(DebuggerEventList result) {
                            onDebuggerConnected();
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            debuggerInfo = EmptyDebuggerInfo.INSTANCE;
                            preserveDebugInfo();
                        }
                    });
                }
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });

        this.debuggerEventsHandler = new SubscriptionHandler<DebuggerEventList>(new DebuggerEventListUnmarshallerWS(dtoFactory)) {
            @Override
            public void onMessageReceived(DebuggerEventList result) {
                onEventListReceived(result);
            }

            @Override
            public void onErrorReceived(Throwable exception) {
                try {
                    messageBus.unsubscribe(debuggerEventsChannel, this);
                } catch (WebSocketException e) {
                    Log.error(DebuggerPresenter.class, e);
                }

                if (exception instanceof ServerException) {
                    ServerException serverException = (ServerException)exception;
                    if (HTTPStatus.INTERNAL_ERROR == serverException.getHTTPStatus() && serverException.getMessage() != null
                        && serverException.getMessage().contains("not found")) {

                        onDebuggerDisconnected();
                        return;
                    }
                }
            }
        };

        this.debuggerDisconnectedHandler = new SubscriptionHandler<Void>() {
            @Override
            protected void onMessageReceived(Void result) {
                try {
                    messageBus.unsubscribe(debuggerDisconnectedChannel, this);
                } catch (WebSocketException e) {
                    Log.error(DebuggerPresenter.class, e);
                }

                evaluateExpressionPresenter.closeDialog();
                onDebuggerDisconnected();
            }

            @Override
            protected void onErrorReceived(Throwable exception) {
                try {
                    messageBus.unsubscribe(debuggerDisconnectedChannel, this);
                } catch (WebSocketException e) {
                    Log.error(DebuggerPresenter.class, e);
                }
            }
        };

        eventBus.fireEvent(createInitializedStateEvent(this));
    }

    @Override
    public void addBreakpoint(@NotNull final VirtualFile file, final int lineNumber, final AsyncCallback<Breakpoint> callback) {
        if (isDebuggerConnected()) {
            Location location = dtoFactory.createDto(Location.class);
            location.setLineNumber(lineNumber + 1);
            String mediaType = fileTypeRegistry.getFileTypeByFile(file).getMimeTypes().get(0);
            final FqnResolver resolver = resolverFactory.getResolver(mediaType);
            if (resolver != null) {
                location.setClassName(resolver.resolveFqn(file));
            } else {
                Log.warn(DebuggerPresenter.class, "FqnResolver is not found");
            }

            BreakPoint breakPoint = dtoFactory.createDto(BreakPoint.class);
            breakPoint.setLocation(location);
            breakPoint.setEnabled(true);
            service.addBreakpoint(debuggerInfo.getId(), breakPoint, new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    if (resolver != null) {
                        Breakpoint breakpoint = new Breakpoint(Breakpoint.Type.BREAKPOINT, lineNumber, file.getPath(), file, true);
                        callback.onSuccess(breakpoint);
                        updateBreakPoints();
                    }
                }

                @Override
                protected void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                    updateBreakPoints();

                }
            });
        } else {
            callback.onFailure(new IllegalStateException("Debugger not attached"));
            updateBreakPoints();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteBreakpoint(@NotNull VirtualFile file, int lineNumber, final AsyncCallback<Void> callback) {
        if (isDebuggerConnected()) {
            Location location = dtoFactory.createDto(Location.class);
            location.setLineNumber(lineNumber + 1);
            String mediaType = fileTypeRegistry.getFileTypeByFile(file).getMimeTypes().get(0);
            FqnResolver resolver = resolverFactory.getResolver(mediaType);
            if (resolver != null) {
                location.setClassName(resolver.resolveFqn(file));
            } else {
                Log.warn(DebuggerPresenter.class, "FqnResolver is not found");
            }

            BreakPoint point = dtoFactory.createDto(BreakPoint.class);
            point.setLocation(location);
            point.setEnabled(true);

            service.deleteBreakpoint(debuggerInfo.getId(), point, new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    callback.onSuccess(null);
                    updateBreakPoints();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    callback.onFailure(exception);
                    updateBreakPoints();
                }
            });
        } else {
            callback.onFailure(new IllegalStateException("Debugger not attached"));
            updateBreakPoints();
        }
    }

    @Override
    public void deleteAllBreakpoints() {
        if (isDebuggerConnected()) {
            service.deleteAllBreakpoints(debuggerInfo.getId(), new AsyncRequestCallback<String>() {
                @Override
                protected void onSuccess(String result) {
                    breakpointManager.removeAllBreakpoints();
                    updateBreakPoints();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    Log.error(DebuggerPresenter.class, exception);
                }
            });
        } else {
            breakpointManager.removeAllBreakpoints();
            updateBreakPoints();
        }
    }

    /**
     * Attached debugger via special host and port for current project.
     *
     * @param host host which need to connect to debugger
     * @param port port which need to connect to debugger
     */
    @Override
    public void attachDebugger(@NotNull final String host, @Min(1) final int port) {
        final String address = host + ':' + port;
        final StatusNotification notification = notificationManager.notify(constant.debuggerConnectingTitle(address), PROGRESS, true);
        service.connect(host, port, new AsyncRequestCallback<DebuggerInfo>(dtoUnmarshallerFactory.newUnmarshaller(DebuggerInfo.class)) {
            @Override
            public void onSuccess(DebuggerInfo result) {
                debuggerInfo = result;
                preserveDebugInfo();

                notification.setTitle(constant.debuggerConnectedTitle());
                notification.setContent(constant.debuggerConnectedDescription(address));
                notification.setStatus(SUCCESS);

                onDebuggerConnected();
            }

            @Override
            protected void onFailure(Throwable exception) {
                notification.setTitle(constant.failedToConnectToRemoteDebuggerDescription(address));
                notification.setStatus(FAIL);
                notification.setBalloon(true);
            }
        });
    }

    @Override
    public void disconnectDebugger() {
        if (isDebuggerConnected()) {
            stopCheckingDebugEvents();

            service.disconnect(debuggerInfo.getId(), new AsyncRequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    onDebuggerDisconnected();
                }

                @Override
                protected void onFailure(Throwable exception) {
                    onDebuggerDisconnected();
                    Log.error(DebuggerPresenter.class, exception);
                }
            });
        }
    }

    @Override
    public void stepInto() {
        breakpointManager.removeCurrentBreakpoint();

        service.stepInto(debuggerInfo.getId(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                resetStates();
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(DebuggerPresenter.class, exception);
            }
        });
    }

    @Override
    public void stepOver() {
        breakpointManager.removeCurrentBreakpoint();

        service.stepOver(debuggerInfo.getId(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                resetStates();
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(DebuggerPresenter.class, exception);
            }

        });
    }

    @Override
    public void stepOut() {
        breakpointManager.removeCurrentBreakpoint();

        service.stepOut(debuggerInfo.getId(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                resetStates();
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(DebuggerPresenter.class, exception);
            }

        });
    }

    @Override
    public void resume() {
        breakpointManager.removeCurrentBreakpoint();

        service.resume(debuggerInfo.getId(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                resetStates();
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(DebuggerPresenter.class, exception);
            }
        });
    }

    @Override
    public Promise<String> evaluateExpression(String expression) {
        return service.evaluateExpression(debuggerInfo.getId(), expression);
    }

    @Override
    public void changeVariableValue(List<String> path, String newValue) {
        UpdateVariableRequest updateVariableRequest = dtoFactory.createDto(UpdateVariableRequest.class);
        VariablePath variablePath = dtoFactory.createDto(VariablePath.class);
        variablePath.setPath(path);
        updateVariableRequest.setVariablePath(variablePath);
        updateVariableRequest.setExpression(newValue);

        service.setValue(debuggerInfo.getId(), updateVariableRequest, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                getStackFrameDump();
            }

            @Override
            protected void onFailure(Throwable throwable) {
                Log.error(DebuggerPresenter.class, throwable);
            }
        });
    }

    @Override
    public DebuggerState getDebuggerState() {
        return isDebuggerConnected() ? DebuggerState.CONNECTED : DebuggerState.DISCONNECTED;
    }


    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getTitle() {
        return TITLE;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleSVGImage() {
        return javaRuntimeResources.debug();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return "Debug";
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        view.setBreakpoints(breakpointManager.getBreakpointList());
        view.setVariables(variables);
        container.setWidget(view);
        debuggerToolbar.go(view.getDebuggerToolbarPanel());
    }

    private void onEventListReceived(@NotNull DebuggerEventList eventList) {
        if (eventList.getEvents().size() == 0) {
            return;
        }

        VirtualFile activeFile = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = activeEditor.getEditorInput().getFile();
        }
        Location location;
        List<DebuggerEvent> events = eventList.getEvents();
        for (DebuggerEvent event : events) {
            switch (event.getType()) {
                case STEP:
                    location = ((StepEvent)event).getLocation();
                    break;
                case BREAKPOINT_ACTIVATED:
                    BreakPoint breakPoint = ((BreakpointActivatedEvent)event).getBreakPoint();
                    activateBreakpoint(breakPoint);
                    return;
                case BREAKPOINT:
                    location = ((BreakPointEvent)event).getBreakPoint().getLocation();
                    showDebuggerPanel();
                    break;
                default:
                    Log.error(DebuggerPresenter.class, "Unknown type of debugger event: " + event.getType());
                    return;
            }
            this.executionPoint = location;

            List<String> filePaths = resolveFilePathByLocation(location);

            if (activeFile == null || !filePaths.contains(activeFile.getPath())) {
                final Location finalLocation = location;
                openFile(location, filePaths, 0, new AsyncCallback<VirtualFile>() {
                    @Override
                    public void onSuccess(VirtualFile result) {
                        breakpointManager.setCurrentBreakpoint(finalLocation.getLineNumber() - 1);
                        scrollEditorToExecutionPoint((EmbeddedTextEditorPresenter)editorAgent.getActiveEditor());
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        breakpointManager.setCurrentBreakpoint(finalLocation.getLineNumber() - 1);
                        notificationManager.notify(caught.getMessage(), StatusNotification.Status.FAIL, false);
                    }
                });
            } else {
                breakpointManager.setCurrentBreakpoint(location.getLineNumber() - 1);
                scrollEditorToExecutionPoint((EmbeddedTextEditorPresenter)activeEditor);
            }

            getStackFrameDump();
        }
    }

    /**
     * Breakpoint became active. It might happens because of different reasons:
     * <li>breakpoint was deferred and VM eventually loaded class and added it</li>
     * <li>condition triggered</li>
     * <li>etc</li>
     */
    private void activateBreakpoint(BreakPoint breakPoint) {
        Location location = breakPoint.getLocation();
        List<String> filePaths = resolveFilePathByLocation(location);
        for (String filePath : filePaths) {
            eventBus.fireEvent(
                    new BreakpointStateEvent(BreakpointStateEvent.BreakpointState.ACTIVE, filePath, location.getLineNumber() - 1));
        }
    }

    /**
     * Create file path from {@link Location}.
     *
     * @param location
     *         location of class
     * @return file path
     */
    @NotNull
    private List<String> resolveFilePathByLocation(@NotNull Location location) {
        CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null) {
            return Collections.emptyList();
        }

        String pathSuffix = location.getClassName().replace(".", "/") + ".java";

        List<String> sourceFolders = JavaSourceFolderUtil.getSourceFolders(currentProject);
        List<String> filePaths = new ArrayList<>(sourceFolders.size() + 1);

        for (String sourceFolder : sourceFolders) {
            filePaths.add(sourceFolder + pathSuffix);
        }
        filePaths.add(location.getClassName());

        return filePaths;
    }

    /**
     * Tries to open file from the project.
     * If fails then method will try to find resource from external dependencies.
     */
    private void openFile(@NotNull final Location location,
                          final List<String> filePaths,
                          final int pathNumber,
                          final AsyncCallback<VirtualFile> callback) {

        if (pathNumber == filePaths.size()) {
            Log.error(DebuggerPresenter.class, "Can't open resource " + location);
            return;
        }

        String filePath = filePaths.get(pathNumber);
        if (!filePath.startsWith("/")) {
            openExternalResource(location, callback);
            return;
        }

        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(filePath)).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }

                handleActivateFile((VirtualFile)node, callback);
                eventBus.fireEvent(new FileEvent((VirtualFile)node, OPEN));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                // try another path
                openFile(location, filePaths, pathNumber + 1, callback);
            }
        });
    }

    private void openExternalResource(Location location, final AsyncCallback<VirtualFile> callback) {
        String className = location.getClassName();

        JarEntry jarEntry = dtoFactory.createDto(JarEntry.class);
        jarEntry.setPath(className);
        jarEntry.setName(className.substring(className.lastIndexOf(".") + 1) + ".class");
        jarEntry.setType(JarEntry.JarEntryType.CLASS_FILE);

        final JarFileNode jarFileNode =
                javaNodeManager.getJavaNodeFactory().newJarFileNode(jarEntry,
                                                                    null,
                                                                    appContext.getCurrentProject().getProjectConfig(),
                                                                    javaNodeManager.getJavaSettingsProvider()
                                                                                   .getSettings());

        handleActivateFile(jarFileNode, callback);
        eventBus.fireEvent(new FileEvent(jarFileNode, OPEN));
    }

    public void handleActivateFile(final VirtualFile virtualFile, final AsyncCallback<VirtualFile> callback) {
        editorAgent.openEditor(virtualFile, new OpenEditorCallback() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onEditorActivated(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onInitializationFailed() {
                callback.onFailure(null);
            }
        });
    }

    private void getStackFrameDump() {
        service.getStackFrameDump(debuggerInfo.getId(),
                                  new AsyncRequestCallback<StackFrameDump>(dtoUnmarshallerFactory.newUnmarshaller(StackFrameDump.class)) {
                                      @Override
                                      protected void onSuccess(StackFrameDump result) {
                                          List<Variable> variables = new ArrayList<>();
                                          variables.addAll(result.getFields());
                                          variables.addAll(result.getLocalVariables());

                                          List<DebuggerVariable> debuggerVariables = getDebuggerVariables(variables);

                                          DebuggerPresenter.this.variables = debuggerVariables;
                                          view.setVariables(debuggerVariables);
                                          if (!variables.isEmpty()) {
                                              view.setExecutionPoint(variables.get(0).isExistInformation(), executionPoint);
                                          }
                                      }

                                      @Override
                                      protected void onFailure(Throwable exception) {
                                          Log.error(DebuggerPresenter.class, exception);
                                      }
                                  });
    }

    @NotNull
    private List<DebuggerVariable> getDebuggerVariables(@NotNull List<Variable> variables) {
        List<DebuggerVariable> debuggerVariables = new ArrayList<>();

        for (Variable variable : variables) {
            debuggerVariables.add(new DebuggerVariable(variable));
        }

        return debuggerVariables;
    }

    /** {@inheritDoc} */
    @Override
    public void onExpandVariablesTree() {
        List<DebuggerVariable> rootVariables = selectedVariable.getVariables();
        if (rootVariables.size() == 0) {
            service.getValue(debuggerInfo.getId(), selectedVariable.getVariable(),
                             new AsyncRequestCallback<Value>(dtoUnmarshallerFactory.newUnmarshaller(Value.class)) {
                                 @Override
                                 protected void onSuccess(Value result) {
                                     List<Variable> variables = result.getVariables();

                                     List<DebuggerVariable> debuggerVariables = getDebuggerVariables(variables);

                                     view.setVariablesIntoSelectedVariable(debuggerVariables);
                                     view.updateSelectedVariable();
                                 }

                                 @Override
                                 protected void onFailure(Throwable exception) {
                                     notificationManager
                                             .notify(constant.failedToGetVariableValueTitle(), exception.getMessage(), FAIL, true);
                                 }
                             });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSelectedVariableElement(@NotNull DebuggerVariable variable) {
        this.selectedVariable = variable;
    }

    public void showDebuggerPanel() {
        partStack.setActivePart(this);
    }

    public void hideDebuggerPanel() {
        partStack.hidePart(this);
    }

    public boolean isDebuggerPanelOpened() {
        return partStack.getActivePart() == this;
    }

    public boolean isDebuggerPanelPresent() {
        return partStack != null && partStack.containsPart(this);
    }

    private void resetStates() {
        variables.clear();
        view.setVariables(variables);
        selectedVariable = null;
    }

    public void showAndUpdateView() {
        view.setVMName(debuggerInfo.getVmName() + " " + debuggerInfo.getVmVersion());

        boolean isCurrentBreakpointExists = breakpointManager.getCurrentBreakpoint() != null;
        if (isCurrentBreakpointExists) {
            getStackFrameDump();
        }

        if (partStack == null || !partStack.containsPart(this)) {
            workspaceAgent.openPart(this, PartStackType.INFORMATION);
        }
    }

    private void onDebuggerConnected() {
        startCheckingEvents();
        eventBus.fireEvent(createConnectedStateEvent(DebuggerPresenter.this));

        showAndUpdateView();
        showDebuggerPanel();
    }

    private void startCheckingEvents() {
        debuggerEventsChannel = JavaRuntimeExtension.EVENTS_CHANNEL + debuggerInfo.getId();
        try {
            messageBus.subscribe(debuggerEventsChannel, debuggerEventsHandler);
        } catch (WebSocketException e) {
            Log.error(DebuggerPresenter.class, e);
        }

        try {
            debuggerDisconnectedChannel = JavaRuntimeExtension.DISCONNECT_CHANNEL + debuggerInfo.getId();
            messageBus.subscribe(debuggerDisconnectedChannel, debuggerDisconnectedHandler);
        } catch (WebSocketException e) {
            Log.error(DebuggerPresenter.class, e);
        }
    }

    private void stopCheckingDebugEvents() {
        try {
            if (messageBus.isHandlerSubscribed(debuggerEventsHandler, debuggerEventsChannel)) {
                messageBus.unsubscribe(debuggerEventsChannel, debuggerEventsHandler);
            }

            if (messageBus.isHandlerSubscribed(debuggerDisconnectedHandler, debuggerDisconnectedChannel)) {
                messageBus.unsubscribe(debuggerDisconnectedChannel, debuggerDisconnectedHandler);
            }
        } catch (WebSocketException e) {
            Log.error(DebuggerPresenter.class, e);
        }
    }

    /** Perform some action after disconnecting a debugger. */
    private void onDebuggerDisconnected() {
        notificationManager.notify(constant.debuggerDisconnectedTitle(),
                                   constant.debuggerDisconnectedDescription(debuggerInfo.getHost() + ':' + debuggerInfo.getPort()),
                                   SUCCESS,
                                   false);

        invalidateDebugInfo();
        preserveDebugInfo();

        resetStates();
        showAndUpdateView();

        eventBus.fireEvent(createDisconnectedStateEvent(this));
    }

    /**
     * Updates breakpoints list.
     * The main idea is to display FQN instead of file path.
     */
    private void updateBreakPoints() {
        List<Breakpoint> breakpoints = breakpointManager.getBreakpointList();
        List<Breakpoint> breakpoints2Display = new ArrayList<Breakpoint>(breakpoints.size());

        for (Breakpoint breakpoint : breakpoints) {
            String mediaType = fileTypeRegistry.getFileTypeByFile(breakpoint.getFile()).getMimeTypes().get(0);
            FqnResolver resolver = resolverFactory.getResolver(mediaType);

            breakpoints2Display.add(new Breakpoint(breakpoint.getType(), breakpoint.getLineNumber(), resolver == null
                                                                                                     ? breakpoint.getPath() : resolver
                                                                                                             .resolveFqn(
                                                                                                                     breakpoint.getFile()),
                                                   breakpoint.getFile(), breakpoint.getMessage(),
                                                   breakpoint.isActive()));
        }

        view.setBreakpoints(breakpoints2Display);
        showAndUpdateView();
    }

    /** {@inheritDoc} */
    @Override
    public void onFqnResolverAdded(FqnResolver fqnResolver) {
        if (!breakpointManager.getBreakpointList().isEmpty()) {
            updateBreakPoints();
        }
    }

    /**
     * @return selected variable in variables tree or null if no selected variables
     */
    public DebuggerVariable getSelectedVariable() {
        return view.getSelectedDebuggerVariable();
    }

    public ToolbarPresenter getDebuggerToolbar() {
        return debuggerToolbar;
    }

    /**
     * Preserves debug information into the local storage.
     */
    protected void preserveDebugInfo() {
        LocalStorage localStorage = localStorageProvider.get();

        if (localStorage == null) {
            return;
        }

        String data;
        if (!isDebuggerConnected()) {
            data = "";
        } else {
            data = dtoFactory.toJson(debuggerInfo);
        }

        localStorage.setItem(LOCAL_STORAGE_DEBUGGER_KEY, data);
    }

    /**
     * Loads debug information from the local storage.
     */
    protected DebuggerInfo loadDebugInfo() {
        LocalStorage localStorage = localStorageProvider.get();
        if (localStorage == null) {
            return EmptyDebuggerInfo.INSTANCE;
        }

        String data = localStorage.getItem(LOCAL_STORAGE_DEBUGGER_KEY);
        if (data == null || data.isEmpty()) {
            return EmptyDebuggerInfo.INSTANCE;
        }

        return dtoFactory.createDtoFromJson(data, DebuggerInfo.class);
    }

    private boolean isDebuggerConnected() {
        return debuggerInfo != null && debuggerInfo != EmptyDebuggerInfo.INSTANCE;
    }

    private void invalidateDebugInfo() {
        debuggerInfo = EmptyDebuggerInfo.INSTANCE;
    }

    private void scrollEditorToExecutionPoint(EmbeddedTextEditorPresenter editor) {
        Document document = editor.getDocument();

        if (document != null) {
            TextPosition newPosition = new TextPosition(executionPoint.getLineNumber(), 0);
            document.setCursorPosition(newPosition);
        }
    }
}
