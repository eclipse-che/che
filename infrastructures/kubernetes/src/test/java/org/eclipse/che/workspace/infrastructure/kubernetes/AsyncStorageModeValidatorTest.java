/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static com.google.common.collect.ImmutableMap.of;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;

import org.eclipse.che.api.core.ValidationException;
import org.testng.annotations.Test;

public class AsyncStorageModeValidatorTest {

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for 'common' PVC strategy, but got not-common")
  public void shouldThrowExceptionIfNotCommonStrategy() throws ValidationException {
    AsyncStorageModeValidator validator = new AsyncStorageModeValidator("not-common", "", 1);

    validator.validate(of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for 'per-user' namespace strategy")
  public void shouldThrowExceptionIfNotPerUserNamespaceStrategy() throws ValidationException {
    AsyncStorageModeValidator validator = new AsyncStorageModeValidator("common", "my-name", 1);

    validator.validate(of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for 'per-user' namespace strategy")
  public void shouldThrowExceptionWithNullNamespaceStrategy() throws ValidationException {
    AsyncStorageModeValidator validator = new AsyncStorageModeValidator("common", null, 1);

    validator.validate(of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only if 'che.limits.user.workspaces.run.count' set to 1, but got 2")
  public void shouldThrowExceptionIfMoreThanOneRuntimeEnabled() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 2);

    validator.validate(of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test
  public void shouldBeFineForEphemeralMode() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validate(of(PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test
  public void shouldBeFineForPersistentMode() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validate(of(PERSIST_VOLUMES_ATTRIBUTE, "true"));
  }

  @Test
  public void shouldBeFineForEmptyAttribute() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validate(of());
  }

  @Test
  public void shouldBeFineForAsyncMode() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validate(of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for NOT persistent storage")
  public void shouldThrowExceptionIfAsyncAttributeForNotEphemeral() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validate(of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "true"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for 'common' PVC strategy, but got not-common")
  public void shouldThrowExceptionIfNotCommonStrategyUpdate() throws ValidationException {
    AsyncStorageModeValidator validator = new AsyncStorageModeValidator("not-common", "", 1);

    validator.validateUpdate(
        of(), of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for 'per-user' namespace strategy")
  public void shouldThrowExceptionIfNotPerUserNamespaceStrategyUpdate() throws ValidationException {
    AsyncStorageModeValidator validator = new AsyncStorageModeValidator("common", "my-name", 1);

    validator.validateUpdate(
        of(), of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only if 'che.limits.user.workspaces.run.count' set to 1, but got 2")
  public void shouldThrowExceptionIfMoreThanOneRuntimeEnabledUpdate() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 2);

    validator.validateUpdate(
        of(), of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test
  public void shouldBeFineForEphemeralModeUpdate() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validateUpdate(of(), of(PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test
  public void shouldBeFineForPersistentModeUpdate() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validateUpdate(of(), of(PERSIST_VOLUMES_ATTRIBUTE, "true"));
  }

  @Test
  public void shouldBeFineForEmptyAttributeUpdate() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validateUpdate(of(), of());
  }

  @Test
  public void shouldBeFineForAsyncModeUpdate() throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validateUpdate(
        of(), of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "false"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for NOT persistent storage")
  public void shouldThrowExceptionIfAsyncAttributeForNotEphemeralUpdate()
      throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validateUpdate(
        of(), of(ASYNC_PERSIST_ATTRIBUTE, "true", PERSIST_VOLUMES_ATTRIBUTE, "true"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace configuration not valid: Asynchronous storage available only for NOT persistent storage")
  public void shouldThrowExceptionIfAsyncAttributeForNotEphemeralUpdate2()
      throws ValidationException {
    AsyncStorageModeValidator validator =
        new AsyncStorageModeValidator("common", "<username>-che", 1);

    validator.validateUpdate(
        of(PERSIST_VOLUMES_ATTRIBUTE, "true"), of(ASYNC_PERSIST_ATTRIBUTE, "true"));
  }
}
