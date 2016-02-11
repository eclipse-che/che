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
package org.eclipse.che.ide.api.project.tree.generic;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericTreeStructureTest {

    private static final String SPRING                  = "Spring";
    private static final String SRC                     = "src";
    private static final String MAIN                    = "main";
    private static final String JAVA                    = "java";
    private static final String COM_CODENVY_EXAMPLE     = "com/codenvy/example";
    private static final String CONTROLLER              = "controller";
    private static final String MESSAGE_CONTROLLER      = "MessageController.java";
    private static final String REGISTRATION_CONTROLLER = "RegistrationController.java";
    private static final String SERVICE                 = "service";
    private static final String README                  = "ReadMe";
    private static final String MESSAGE_SERVICE         = "MessageService";
    private static final String MESSAGE_SERVICE_IMPL    = "MessageServiceImpl";
    private static final String REGISTRATION_SERVICE    = "RegistrationServiceImpl.java";
    private static final String ORG_CODENVY_EXAMPLE     = "org/codenvy/example";
    private static final String CREATE_BD_SQL           = "createBd.sql";
    private static final String WEB_APP                 = "webapp";
    private static final String WEB_INF                 = "WEB-INF";
    private static final String JSP                     = "jsp";
    private static final String MESSAGE_JSP             = "message.jsp";
    private static final String REGISTRATION_JSP        = "registration.jsp";
    private static final String SOME_HTML               = "some.html";
    private static final String SPRING_SERVLET_XML      = "spring-servlet.xml";
    private static final String WEB_XML                 = "web.xml";
    private static final String POM_XML                 = "pom.xml";

    //mocks of the constructor
    @Mock
    private NodeFactory            nodeFactory;
    @Mock
    private EventBus               eventBus;
    @Mock
    private AppContext             appContext;
    @Mock
    private ProjectServiceClient   projectServiceClient;
    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Mock
    private CurrentProject                   currentProject;
    @Mock
    private ProjectConfigDto                 projectConfig;
    @Mock
    private AsyncCallback<List<TreeNode<?>>> callback;

    //mocks for tree
    @Mock
    private ProjectNode projectNode;
    @Mock
    private TreeNode<?> srcNode;
    @Mock
    private TreeNode<?> mainNode;
    @Mock
    private TreeNode<?> javaNode;
    @Mock
    private TreeNode<?> comCodenvyExampleNode;
    @Mock
    private TreeNode<?> controllerNode;
    @Mock
    private TreeNode<?> messageControllerNode;
    @Mock
    private TreeNode<?> registrationControllerNode;
    @Mock
    private TreeNode<?> serviceNode;
    @Mock
    private TreeNode<?> readmeNode;
    @Mock
    private TreeNode<?> messageServiceNode;
    @Mock
    private TreeNode<?> messageServiceImplNode;
    @Mock
    private TreeNode<?> registrationServiceNode;
    @Mock
    private TreeNode<?> orgCodenvyExampleNode;
    @Mock
    private TreeNode<?> createBdSqlNode;
    @Mock
    private TreeNode<?> webAppNode;
    @Mock
    private TreeNode<?> webInfNode;
    @Mock
    private TreeNode<?> jspNode;
    @Mock
    private TreeNode<?> messageJspNode;
    @Mock
    private TreeNode<?> registrationJspNode;
    @Mock
    private TreeNode<?> someHtmlNode;
    @Mock
    private TreeNode<?> springServletXmlNode;
    @Mock
    private TreeNode<?> webXmlNode;
    @Mock
    private TreeNode<?> pomXmlNode;

    @Captor
    private ArgumentCaptor<AsyncCallback<TreeNode<?>>> treeNodeAsyncCallback;

    @Mock
    private AsyncCallback<TreeNode<?>> asyncCallback;

    @InjectMocks
    private GenericTreeStructure treeStructure;

    @Before
    public void setUp() {
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getRootProject()).thenReturn(projectConfig);
        when(nodeFactory.newProjectNode(null, projectConfig, treeStructure)).thenReturn(projectNode);

        generateDifficultTree();
    }

    /*
    * This method generates difficult tree for testing
    *
    *  |- Spring
    *      |- src
    *          |- main
    *               |- java
    *                   |- com/codenvy/example          --> java package
    *                              |- controller
    *                                  |- MessageController.java
    *                                  |- RegistrationController.java
    *                              |- service
    *                                  |- ReadMe
    *                                  |- MessageService
    *                                  |- MessageServiceImpl
    *                                  |- RegistrationService.java
    *                   |- org/codenvy/example         --> java package
    *                                  |- createBd.sql
    *              |- webapp
    *                    |- WEB-INF
    *                          |- jsp
    *                              |- message.jsp
    *                              |- registration.jsp
    *                              |- some.html
    *                    |- spring-servlet.xml
    *                    |- web.xml
    *     |- pom.xml
    */
    private void generateDifficultTree() {
        when(projectNode.getId()).thenReturn(SPRING);
        when(srcNode.getId()).thenReturn(SRC);
        when(mainNode.getId()).thenReturn(MAIN);
        when(javaNode.getId()).thenReturn(JAVA);
        when(comCodenvyExampleNode.getId()).thenReturn(COM_CODENVY_EXAMPLE);
        when(controllerNode.getId()).thenReturn(CONTROLLER);
        when(messageControllerNode.getId()).thenReturn(MESSAGE_CONTROLLER);
        when(registrationControllerNode.getId()).thenReturn(REGISTRATION_CONTROLLER);
        when(serviceNode.getId()).thenReturn(SERVICE);
        when(readmeNode.getId()).thenReturn(README);
        when(messageServiceNode.getId()).thenReturn(MESSAGE_SERVICE);
        when(messageServiceImplNode.getId()).thenReturn(MESSAGE_SERVICE_IMPL);
        when(registrationServiceNode.getId()).thenReturn(REGISTRATION_SERVICE);
        when(orgCodenvyExampleNode.getId()).thenReturn(ORG_CODENVY_EXAMPLE);
        when(createBdSqlNode.getId()).thenReturn(CREATE_BD_SQL);
        when(webAppNode.getId()).thenReturn(WEB_APP);
        when(webInfNode.getId()).thenReturn(WEB_INF);
        when(jspNode.getId()).thenReturn(JSP);
        when(messageJspNode.getId()).thenReturn(MESSAGE_JSP);
        when(registrationJspNode.getId()).thenReturn(REGISTRATION_JSP);
        when(someHtmlNode.getId()).thenReturn(SOME_HTML);
        when(springServletXmlNode.getId()).thenReturn(SPRING_SERVLET_XML);
        when(webXmlNode.getId()).thenReturn(WEB_XML);
        when(pomXmlNode.getId()).thenReturn(POM_XML);

        when(projectNode.getChildren()).thenReturn(getListTreeNode(pomXmlNode, srcNode));
        when(srcNode.getChildren()).thenReturn(getListTreeNode(mainNode));
        when(mainNode.getChildren()).thenReturn(getListTreeNode(javaNode, webAppNode));
        when(javaNode.getChildren()).thenReturn(getListTreeNode(comCodenvyExampleNode, orgCodenvyExampleNode));
        when(comCodenvyExampleNode.getChildren()).thenReturn(getListTreeNode(controllerNode, serviceNode));
        when(controllerNode.getChildren()).thenReturn(getListTreeNode(messageControllerNode, registrationControllerNode));
        when(serviceNode.getChildren())
                .thenReturn(getListTreeNode(readmeNode, messageServiceNode, messageServiceImplNode, registrationServiceNode));
        when(orgCodenvyExampleNode.getChildren()).thenReturn(getListTreeNode(createBdSqlNode));
        when(webAppNode.getChildren()).thenReturn(getListTreeNode(webInfNode, webXmlNode, springServletXmlNode));
        when(webInfNode.getChildren()).thenReturn(getListTreeNode(jspNode));
        when(jspNode.getChildren()).thenReturn(getListTreeNode(messageJspNode, registrationJspNode, someHtmlNode));
    }

    private List<TreeNode<?>> getListTreeNode(TreeNode<?>... rootNodes) {
        List<TreeNode<?>> nodeList = new ArrayList<>();
        addAll(nodeList, rootNodes);
        return nodeList;
    }

    @Test
    public void testGetNodeFactory() throws Exception {
        assertEquals(nodeFactory, treeStructure.getNodeFactory());
    }

    @Test
    public void testNewFileNode() throws Exception {
        TreeNode parent = mock(TreeNode.class);
        ItemReference data = mock(ItemReference.class);
        when(data.getType()).thenReturn("file");

        treeStructure.newFileNode(parent, data);

        verify(nodeFactory).newFileNode(eq(parent), eq(data), eq(treeStructure));
    }

    @Test
    public void testNewFolderNode() throws Exception {
        TreeNode parent = mock(TreeNode.class);
        ItemReference data = mock(ItemReference.class);
        when(data.getType()).thenReturn("folder");

        treeStructure.newFolderNode(parent, data);

        verify(nodeFactory).newFolderNode(eq(parent), eq(data), eq(treeStructure));
    }

    @Test
    public void testNewProjectNode() throws Exception {
        ProjectConfigDto data = mock(ProjectConfigDto.class);

        treeStructure.newProjectNode(data);

        verify(nodeFactory).newProjectNode(isNull(TreeNode.class), eq(data), eq(treeStructure));
    }

    @Test
    public void newRootNodeShouldBePutIntoCallBack() {
        treeStructure.getRootNodes(callback);

        verify(appContext).getCurrentProject();
        verify(currentProject).getRootProject();
        verify(nodeFactory).newProjectNode(null, projectConfig, treeStructure);
        verify(callback).onSuccess(Matchers.<List<TreeNode<?>>>anyObject());
    }

    @Test
    public void rootNodeShouldBePutIntoCallBack() {
        treeStructure.getRootNodes(callback);
        treeStructure.getRootNodes(callback);

        verify(appContext).getCurrentProject();
        verify(currentProject).getRootProject();
        verify(nodeFactory).newProjectNode(null, projectConfig, treeStructure);

        verify(callback, times(2)).onSuccess(Matchers.<List<TreeNode<?>>>anyObject());
    }

    @Test
    public void rootNodeShouldBeNotAccessibleIfCurrentProjectIsNull() {
        when(appContext.getCurrentProject()).thenReturn(null);

        treeStructure.getRootNodes(callback);

        verify(appContext).getCurrentProject();
        verify(callback).onFailure(any(IllegalStateException.class));
    }

    @Test
    public void newProjectNodeShouldBeReturned() {
        assertThat(treeStructure.newProjectNode(projectConfig), is(projectNode));

        verify(nodeFactory).newProjectNode(null, projectConfig, treeStructure);
    }

    @Test
    public void nodeSrcShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(asyncCallback).onSuccess(srcNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodePomXmlShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + POM_XML;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(asyncCallback).onSuccess(pomXmlNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    private void rootNodeShouldBeReturned() {
        verify(appContext).getCurrentProject();
        verify(currentProject).getRootProject();
        verify(nodeFactory).newProjectNode(null, projectConfig, treeStructure);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeMainShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(mainNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeJavaShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(javaNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeComCodenvyExamplePackageShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(comCodenvyExampleNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeControllerShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + CONTROLLER;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(controllerNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeMessageControllerShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + CONTROLLER
                      + "/" + MESSAGE_CONTROLLER;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        verify(controllerNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(controllerNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(messageControllerNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeRegistrationControllerShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + CONTROLLER
                      + "/" + REGISTRATION_CONTROLLER;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        verify(controllerNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(controllerNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(registrationControllerNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeServiceShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + SERVICE;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(serviceNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeReadmeShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + SERVICE + "/" + README;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        verify(serviceNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(serviceNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(readmeNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeMessageServiceShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + SERVICE
                      + "/" + MESSAGE_SERVICE;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        verify(serviceNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(serviceNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(messageServiceNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    /* IDEX-2108 */
    @Test
    public void nodeMessageServiceImplShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + SERVICE
                      + "/" + MESSAGE_SERVICE_IMPL;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        verify(serviceNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(serviceNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(messageServiceImplNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeRegistrationServiceShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + COM_CODENVY_EXAMPLE + "/" + SERVICE
                      + "/" + REGISTRATION_SERVICE;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(comCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(comCodenvyExampleNode);

        verify(serviceNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(serviceNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(registrationServiceNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeOrgCodenvyExamplePackageShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + ORG_CODENVY_EXAMPLE;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(orgCodenvyExampleNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeCreateBdSqlShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + JAVA + "/" + ORG_CODENVY_EXAMPLE
                      + "/" + CREATE_BD_SQL;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(javaNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(javaNode);

        verify(orgCodenvyExampleNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(orgCodenvyExampleNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(createBdSqlNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeWebAppShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(webAppNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeWebInfShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + WEB_INF;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(webInfNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeJspShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + WEB_INF + "/" + JSP;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        verify(webInfNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webInfNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(jspNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeMessageShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + WEB_INF + "/" + JSP
                      + "/" + MESSAGE_JSP;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        verify(webInfNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webInfNode);

        verify(jspNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(jspNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(messageJspNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeRegistrationJspShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + WEB_INF + "/" + JSP + "/" + REGISTRATION_JSP;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        verify(webInfNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webInfNode);

        verify(jspNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(jspNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(registrationJspNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeSomeHtmlShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + WEB_INF + "/" + JSP + "/" + SOME_HTML;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        verify(webInfNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webInfNode);

        verify(jspNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(jspNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(someHtmlNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeWebSpringServletXmlShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + SPRING_SERVLET_XML;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(springServletXmlNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeWebXmlShouldBeReturnedByPath() {
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN + "/" + WEB_APP + "/" + WEB_XML;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(srcNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(srcNode);

        verify(mainNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(mainNode);

        verify(webAppNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(webAppNode);

        //main checking that we found and returned node
        verify(asyncCallback).onSuccess(webXmlNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeShouldNotBeReturnedEvenPathNotBeginningFromSlash() {
        String path = SPRING + "/" + SRC;

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(asyncCallback).onSuccess(srcNode);

        verify(asyncCallback, never()).onSuccess(null);
    }

    @Test
    public void nodeShouldNotBeReturnedBecauseThereIsNotExist() {
        String path = "/" + SPRING + "/" + SRC;
        when(projectNode.getChildren()).thenReturn(getListTreeNode());

        treeStructure.getNodeByPath(path, asyncCallback);

        rootNodeShouldBeReturned();

        verify(projectNode).refreshChildren(treeNodeAsyncCallback.capture());
        treeNodeAsyncCallback.getValue().onSuccess(projectNode);

        verify(asyncCallback).onFailure(any(IllegalStateException.class));
    }

    @Test
    public void nodeShouldNotBeReturnedBecauseThisNodeIsAbsent() {
        when(nodeFactory.newProjectNode(null, projectConfig, treeStructure)).thenReturn(null);
        String path = "/" + SPRING + "/" + SRC + "/" + MAIN;

        treeStructure.getNodeByPath(path, asyncCallback);

        verify(asyncCallback).onFailure(any(IllegalStateException.class));
    }
}
