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
package org.eclipse.che.ide.ext.git.client.checkout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Testing {@link CheckoutReferencePresenter} functionality.
 *
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
public class CheckoutReferenceTest extends BaseTest {
  private static final String CORRECT_REFERENCE = "someTag";
  private static final String INCORRECT_REFERENCE = "";

  @Mock private CheckoutReferenceView view;
  @Mock private CheckoutRequest checkoutRequest;

  private CheckoutReferencePresenter presenter;

  @Override
  public void disarm() {
    super.disarm();

    presenter =
        new CheckoutReferencePresenter(
            view,
            service,
            constant,
            notificationManager,
            gitOutputConsoleFactory,
            processesPanelPresenter,
            dtoFactory);
  }

  @Test
  public void testOnReferenceValueChangedWhenValueIsIncorrect() throws Exception {
    presenter.referenceValueChanged(INCORRECT_REFERENCE);

    view.setCheckoutButEnableState(eq(false));
  }

  @Test
  public void testOnReferenceValueChangedWhenValueIsCorrect() throws Exception {
    presenter.referenceValueChanged(CORRECT_REFERENCE);

    view.setCheckoutButEnableState(eq(true));
  }

  @Test
  public void testShowDialog() throws Exception {
    presenter.showDialog(project);

    verify(view).setCheckoutButEnableState(eq(false));
    verify(view).showDialog();
  }

  @Test
  public void testOnCancelClicked() throws Exception {
    presenter.onCancelClicked();

    verify(view).close();
  }

  @Test
  public void onEnterClickedWhenValueIsIncorrect() throws Exception {
    reset(service);
    when(view.getReference()).thenReturn(INCORRECT_REFERENCE);

    presenter.onEnterClicked();

    verify(view, never()).close();
    verify(service, never()).checkout(any(Path.class), any(CheckoutRequest.class));
  }

  @Test
  public void onEnterClickedWhenValueIsCorrect() throws Exception {
    when(appContext.getRootProject()).thenReturn(project);
    when(dtoFactory.createDto(CheckoutRequest.class)).thenReturn(checkoutRequest);
    when(checkoutRequest.withName(anyString())).thenReturn(checkoutRequest);
    when(checkoutRequest.withCreateNew(anyBoolean())).thenReturn(checkoutRequest);
    reset(service);
    when(service.checkout(any(Path.class), any(CheckoutRequest.class))).thenReturn(stringPromise);
    when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
    when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);
    when(view.getReference()).thenReturn(CORRECT_REFERENCE);

    presenter.showDialog(project);
    presenter.onEnterClicked();

    verify(stringPromise).then(stringCaptor.capture());
    stringCaptor.getValue().apply(null);

    verify(synchronizePromise).then(synchronizeCaptor.capture());
    synchronizeCaptor.getValue().apply(new Resource[0]);

    verify(view).close();
    verify(service).checkout(any(Path.class), any(CheckoutRequest.class));
    verify(checkoutRequest).withName(CORRECT_REFERENCE);
    verifyNoMoreInteractions(checkoutRequest);
  }
}
