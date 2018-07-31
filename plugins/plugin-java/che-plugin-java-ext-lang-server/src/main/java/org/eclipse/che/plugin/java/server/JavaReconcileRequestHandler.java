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
package org.eclipse.che.plugin.java.server;

import static java.lang.String.format;

import com.google.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.ext.java.shared.dto.JavaClassInfo;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.jdt.javaeditor.JavaReconciler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Receives requests on reconcile operations from client side.
 *
 * @author Roman Nikitenko
 */
public class JavaReconcileRequestHandler {
  private static final String INCOMING_METHOD = "request:java-reconcile";
  private static final JavaModel JAVA_MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

  @Inject private JavaReconciler reconciler;

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(INCOMING_METHOD)
        .paramsAsDto(JavaClassInfo.class)
        .resultAsDto(ReconcileResult.class)
        .withFunction(this::getReconcileOperation);
  }

  private ReconcileResult getReconcileOperation(JavaClassInfo javaClassInfo) {
    IJavaProject javaProject = JAVA_MODEL.getJavaProject(javaClassInfo.getProjectPath());
    try {
      return reconciler.reconcile(javaProject, javaClassInfo.getFQN());
    } catch (JavaModelException e) {
      String error =
          format(
              "Can't reconcile class: %s in project: %s, the reason is %s",
              javaClassInfo.getFQN(), javaProject.getPath().toOSString(), e.getLocalizedMessage());
      throw new JsonRpcException(500, error);
    }
  }
}
