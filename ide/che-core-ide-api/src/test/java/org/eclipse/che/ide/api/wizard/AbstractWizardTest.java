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
package org.eclipse.che.ide.api.wizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMapOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.validation.constraints.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Testing {@link AbstractWizard}.
 *
 * @author Andrey Plotnikov
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractWizardTest {
  private final String dataObject = "dataObject";
  @Mock private WizardPage<String> page1;
  @Mock private WizardPage<String> page2;
  @Mock private WizardPage<String> page3;
  @Mock private WizardPage<String> page4;
  @Mock private Wizard.UpdateDelegate updateDelegate;
  private AbstractWizard<String> wizard;

  @Before
  public void setUp() {
    wizard = new DummyWizard(dataObject);
    wizard.setUpdateDelegate(updateDelegate);
  }

  @Test
  public void testAddPage() throws Exception {
    wizard.addPage(page1);
    wizard.addPage(page2);
    wizard.addPage(page3);

    verify(page1).setUpdateDelegate(eq(updateDelegate));
    verify(page1).setContext(anyMapOf(String.class, String.class));
    verify(page1).init(eq(dataObject));

    verify(page2).setUpdateDelegate(eq(updateDelegate));
    verify(page2).setContext(anyMapOf(String.class, String.class));
    verify(page2).init(eq(dataObject));

    verify(page3).setUpdateDelegate(eq(updateDelegate));
    verify(page3).setContext(anyMapOf(String.class, String.class));
    verify(page3).init(eq(dataObject));

    assertEquals(page1, wizard.navigateToFirst());
    assertEquals(page2, wizard.navigateToNext());
    assertEquals(page3, wizard.navigateToNext());
    assertNull(wizard.navigateToNext());
  }

  @Test
  public void testAddPageByIndex() throws Exception {
    wizard.addPage(page1);
    wizard.addPage(page3);
    wizard.addPage(page2, 1, false);

    assertEquals(page1, wizard.navigateToFirst());
    assertEquals(page2, wizard.navigateToNext());
    assertEquals(page3, wizard.navigateToNext());
    assertNull(wizard.navigateToNext());
  }

  @Test
  public void testAddPageWithReplace() throws Exception {
    wizard.addPage(page1);
    wizard.addPage(page3);
    wizard.addPage(page2, 1, true);

    assertEquals(page1, wizard.navigateToFirst());
    assertEquals(page2, wizard.navigateToNext());
    assertNull(wizard.navigateToNext());
  }

  @Test
  public void testNavigateToFirstWhenNeedToSkipFirstPages() throws Exception {
    when(page1.canSkip()).thenReturn(true);

    wizard.addPage(page1);
    wizard.addPage(page2);
    wizard.addPage(page3);

    assertEquals(page2, wizard.navigateToFirst());
  }

  @Test
  public void testNavigateToFirst() throws Exception {
    wizard.addPage(page1);

    assertEquals(page1, wizard.navigateToFirst());
  }

  @Test
  public void testCanCompleteWhenAllPagesIsCompleted() throws Exception {
    when(page1.isCompleted()).thenReturn(true);
    when(page2.isCompleted()).thenReturn(true);
    when(page3.isCompleted()).thenReturn(true);

    wizard.addPage(page1);
    wizard.addPage(page2);
    wizard.addPage(page3);

    assertEquals(true, wizard.canComplete());
  }

  @Test
  public void testCanCompleteWhenSomePageIsNotCompleted() throws Exception {
    when(page1.isCompleted()).thenReturn(true);
    when(page2.isCompleted()).thenReturn(false);

    wizard.addPage(page1);
    wizard.addPage(page2);

    assertEquals(false, wizard.canComplete());
  }

  @Test
  public void testNavigateToNextUseCase1() throws Exception {
    prepareTestCase1();

    assertEquals(page1, wizard.navigateToFirst());
    assertEquals(page2, wizard.navigateToNext());
    assertEquals(page4, wizard.navigateToNext());
    assertNull(wizard.navigateToNext());
  }

  @Test
  public void testNavigateToPreviousUseCase1() throws Exception {
    prepareTestCase1();

    wizard.navigateToFirst();
    navigatePages(wizard, 2);

    assertEquals(page2, wizard.navigateToPrevious());
    assertEquals(page1, wizard.navigateToPrevious());
  }

  @Test
  public void testHasNextUseCase1() throws Exception {
    prepareTestCase1();

    wizard.navigateToFirst();
    assertEquals(true, wizard.hasNext());

    navigatePages(wizard, 1);
    assertEquals(true, wizard.hasNext());

    navigatePages(wizard, 1);
    assertEquals(false, wizard.hasNext());
  }

  @Test
  public void testHasPreviousUseCase1() throws Exception {
    prepareTestCase1();

    wizard.navigateToFirst();
    assertEquals(false, wizard.hasPrevious());

    navigatePages(wizard, 1);
    assertEquals(true, wizard.hasPrevious());

    navigatePages(wizard, 1);
    assertEquals(true, wizard.hasPrevious());
  }

  /** In case the wizard has got 3 skipped pages and 1 not skipped page. */
  private void prepareTestCase1() {
    when(page1.canSkip()).thenReturn(false);

    when(page2.canSkip()).thenReturn(false);

    when(page3.canSkip()).thenReturn(true);

    when(page4.canSkip()).thenReturn(false);

    wizard.addPage(page1);
    wizard.addPage(page2);
    wizard.addPage(page3);
    wizard.addPage(page4);
  }

  @Test
  public void testNavigateToNextUseCase2() throws Exception {
    prepareTestCase2();

    assertEquals(page1, wizard.navigateToFirst());
    assertEquals(page2, wizard.navigateToNext());
    assertNull(wizard.navigateToNext());
  }

  @Test
  public void testNavigateToPreviousUseCase2() throws Exception {
    prepareTestCase2();

    wizard.navigateToFirst();
    navigatePages(wizard, 1);

    assertEquals(page1, wizard.navigateToPrevious());
  }

  @Test
  public void testHasNextUseCase2() throws Exception {
    prepareTestCase2();

    wizard.navigateToFirst();
    assertEquals(true, wizard.hasNext());

    navigatePages(wizard, 1);
    assertEquals(false, wizard.hasNext());
  }

  @Test
  public void testHasPreviousUseCase2() throws Exception {
    prepareTestCase2();

    wizard.navigateToFirst();
    assertEquals(false, wizard.hasPrevious());

    navigatePages(wizard, 1);
    assertEquals(true, wizard.hasPrevious());
  }

  /** In case the wizard has got 2 not skipped pages and 2 skipped page. */
  private void prepareTestCase2() {
    when(page1.canSkip()).thenReturn(false);

    when(page2.canSkip()).thenReturn(false);

    when(page3.canSkip()).thenReturn(true);

    when(page4.canSkip()).thenReturn(true);

    wizard.addPage(page1);
    wizard.addPage(page2);
    wizard.addPage(page3);
    wizard.addPage(page4);
  }

  private void navigatePages(Wizard wizard, int count) {
    for (int i = 0; i < count; i++) {
      wizard.navigateToNext();
    }
  }

  private class DummyWizard extends AbstractWizard<String> {
    DummyWizard(String dataObject) {
      super(dataObject);
    }

    @Override
    public void complete(@NotNull CompleteCallback callback) {
      // do nothing
    }
  }
}
