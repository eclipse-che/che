/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.free;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.shared.dto.FreeResourcesLimitDto;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link org.eclipse.che.multiuser.resource.api.free.FreeResourcesLimitValidator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class FreeResourcesLimitValidatorTest {
  @Mock private ResourceValidator resourceValidator;

  @InjectMocks private FreeResourcesLimitValidator validator;

  @Test(
    expectedExceptions = BadRequestException.class,
    expectedExceptionsMessageRegExp = "Missed free resources limit description."
  )
  public void shouldThrowBadRequestExceptionWhenFreeResourcesIsNull() throws Exception {
    // when
    validator.check(null);
  }

  @Test(
    expectedExceptions = BadRequestException.class,
    expectedExceptionsMessageRegExp = "Missed account id."
  )
  public void shouldThrowBadRequestExceptionWhenAccountIdIsMissed() throws Exception {
    // when
    validator.check(
        DtoFactory.newDto(FreeResourcesLimitDto.class)
            .withResources(
                singletonList(
                    DtoFactory.newDto(ResourceDto.class)
                        .withType("test")
                        .withUnit("mb")
                        .withAmount(1230))));
  }

  @Test(
    expectedExceptions = BadRequestException.class,
    expectedExceptionsMessageRegExp = "invalid resource"
  )
  public void shouldRethrowBadRequestExceptionWhenThereIsAnyInvalidResource() throws Exception {
    // given
    Mockito.doNothing()
        .doThrow(new BadRequestException("invalid resource"))
        .when(resourceValidator)
        .validate(any());

    // when
    validator.check(
        DtoFactory.newDto(FreeResourcesLimitDto.class)
            .withAccountId("account123")
            .withResources(
                Arrays.asList(
                    DtoFactory.newDto(ResourceDto.class)
                        .withType("test")
                        .withUnit("mb")
                        .withAmount(1230),
                    DtoFactory.newDto(ResourceDto.class)
                        .withType("test2")
                        .withUnit("mb")
                        .withAmount(3214))));
  }

  @Test(
    expectedExceptions = BadRequestException.class,
    expectedExceptionsMessageRegExp =
        "Free resources limit should contain only one resources with type 'test'."
  )
  public void
      shouldThrowBadRequestExceptionWhenAccountResourcesLimitContainTwoResourcesWithTheSameType()
          throws Exception {
    // when
    validator.check(
        DtoFactory.newDto(FreeResourcesLimitDto.class)
            .withAccountId("account123")
            .withResources(
                Arrays.asList(
                    DtoFactory.newDto(ResourceDto.class)
                        .withType("test")
                        .withUnit("mb")
                        .withAmount(1230),
                    DtoFactory.newDto(ResourceDto.class)
                        .withType("test")
                        .withUnit("mb")
                        .withAmount(3))));
  }

  @Test
  public void shouldNotThrowAnyExceptionWhenAccountResourcesLimitIsValid() throws Exception {
    // when
    validator.check(
        DtoFactory.newDto(FreeResourcesLimitDto.class)
            .withAccountId("account123")
            .withResources(
                singletonList(
                    DtoFactory.newDto(ResourceDto.class)
                        .withType("test")
                        .withUnit("mb")
                        .withAmount(1230))));
  }
}
