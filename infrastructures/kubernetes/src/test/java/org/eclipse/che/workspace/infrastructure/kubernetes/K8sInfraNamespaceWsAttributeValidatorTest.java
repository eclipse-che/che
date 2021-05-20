/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.google.common.collect.ImmutableMap;
import javax.inject.Provider;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link K8sInfraNamespaceWsAttributeValidator}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class K8sInfraNamespaceWsAttributeValidatorTest {

  @Mock private KubernetesNamespaceFactory namespaceFactory;
  @Mock private Provider<KubernetesNamespaceFactory> namespaceFactoryProvider;

  @InjectMocks private K8sInfraNamespaceWsAttributeValidator validator;

  @BeforeMethod
  public void setUp() {
    lenient().when(namespaceFactoryProvider.get()).thenReturn(namespaceFactory);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "The specified namespace [a]{64} is invalid: must be no more than 63 characters")
  public void testWhenNamespaceNameContainsMoreThan63Characters() throws ValidationException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 64; i++) {
      sb.append('a');
    }

    validator.validate(
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, sb.toString()));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "The specified namespace .* is invalid: a DNS\\-1123 label must consist "
              + "of lower case alphanumeric characters or '\\-', and must start and end with an "
              + "alphanumeric character \\(e\\.g\\. .*",
      dataProvider = "invalidNamespaces")
  public void testWhenNamespaceNameHasNoValidFormat(String name) throws ValidationException {
    validator.validate(ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, name));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "The specified namespace name is not valid")
  public void testWhenNamespaceFactoryThrowsErrorOnCheckingIfNamespaceIsAllowed()
      throws ValidationException {
    doThrow(new ValidationException("The specified namespace name is not valid"))
        .when(namespaceFactory)
        .checkIfNamespaceIsAllowed(anyString());

    validator.validate(ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "any"));
  }

  @Test
  public void shouldDoNothingWhenNamespaceAttributeIsMissing() throws ValidationException {
    validator.validate(emptyMap());

    verifyZeroInteractions(namespaceFactory);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "The namespace from the provided object \"new\" does not match the actual namespace \"actual\"")
  public void shouldDoNotAllowToChangeNamespaceAttribute() throws ValidationException {
    validator.validateUpdate(
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "actual"),
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "new"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "The namespace information must not be updated or deleted. You must provide \"infrastructureNamespace\" attribute with \"che-workspaces\" as a value")
  public void shouldDoNotAllowToRemoveNamespaceAttribute() throws ValidationException {
    validator.validateUpdate(
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-workspaces"),
        emptyMap());
  }

  @Test
  public void shouldValidateFullyIfExistingIsEmpty() throws ValidationException {
    validator.validateUpdate(
        emptyMap(),
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-workspaces"));

    verify(namespaceFactory).checkIfNamespaceIsAllowed(eq("che-workspaces"));
  }

  @Test
  public void shouldNotValidateFullyIfExistingIsNotEmpty() throws ValidationException {
    validator.validateUpdate(
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-workspaces"),
        ImmutableMap.of(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-workspaces"));

    verify(namespaceFactory, never()).checkIfNamespaceIsAllowed(any());
  }

  @DataProvider
  public Object[][] invalidNamespaces() {
    return new String[][] {{"name!space"}, {"name@space"}, {"-namespace"}, {"namespace-"}};
  }
}
