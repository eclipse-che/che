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
package org.eclipse.che.plugin.testing.ide.model;

import static org.eclipse.che.plugin.testing.ide.model.Printer.OutputType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.testing.ide.model.event.TestFailedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestIgnoredEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestOutputEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestStartedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteStartedEvent;

/** Event processor converts events from runner to internal form. */
public class GeneralTestingEventsProcessor extends AbstractTestingEventsProcessor {
  private final TestRootState testRootState;
  private final TestSuiteStack testSuiteStack = new TestSuiteStack();
  private final Set<TestState> currentChildren = new LinkedHashSet<>();
  private final Map<String, TestState> testNameToTestState = new HashMap<>();
  private final List<Runnable> buildTreeEvents = new ArrayList<>();
  private boolean gettingChildren = true;

  private TestState lastTreeState;
  private boolean treeBuildBeforeStart = false;
  private TestLocator testLocator = null;

  private DebugConfiguration debugConfiguration;
  private DebugConfigurationsManager debugConfigurationsManager;

  public GeneralTestingEventsProcessor(String testFrameworkName, TestRootState testRootState) {
    super(testFrameworkName);
    this.testRootState = testRootState;
    this.lastTreeState = testRootState;
  }

  @Override
  public void onStartTesting() {
    testSuiteStack.push(testRootState);
    testRootState.setStarted();
    callTestingStarted(testRootState);
  }

  @Override
  public void onTestSuiteStarted(TestSuiteStartedEvent event) {
    String name = event.getName();
    String location = event.getLocation();
    TestState currentSuite;
    if (treeBuildBeforeStart) {
      findState(testRootState, name);
      currentSuite = lastTreeState;
    } else {
      currentSuite = getCurrentSuite();
    }
    TestState newState;
    if (location == null) {
      newState = findChildByName(currentSuite, name, true);
    } else {
      newState = findChildByLocation(currentSuite, location, true);
    }

    if (newState == null) {
      newState = new TestState(name, true, location);
      if (treeBuildBeforeStart) {
        newState.setTreeBuildBeforeStart();
      }

      if (testLocator != null) {
        newState.setTestLocator(testLocator);
      }

      currentSuite.addChild(newState);
    }

    gettingChildren = true;
    if (treeBuildBeforeStart) {
      testSuiteStack.add(newState);
    } else {
      testSuiteStack.push(newState);
    }
    callSuiteStarted(newState);
  }

  private void findState(TestState currentState, String name) {
    List<TestState> children = currentState.getChildren();
    for (TestState state : children) {
      if (state.getName().equals(name)) {
        lastTreeState = currentState;
        return;
      }
    }

    for (TestState state : children) {
      findState(state, name);
    }
  }

  @Override
  public void onTestSuiteFinished(TestSuiteFinishedEvent event) {
    TestState suite = testSuiteStack.pop(event.getName());
    if (suite != null) {
      suite.setFinished();

      currentChildren.clear();
      gettingChildren = true;

      callSuiteFinished(suite);
    }
  }

  @Override
  public void onTestOutput(TestOutputEvent event) {
    String testName = event.getName();
    String text = event.getText();
    boolean sdtout = event.isSdtout();
    TestState testState = getStateByStateName(testName);

    if (testState != null) {
      if (sdtout) {
        testState.addStdOut(text, OutputType.STDOUT);
      } else {
        testState.addStdErr(text);
      }
    } else {
      Log.error(getClass(), "Unexpected test output. Test not started: " + testName);
    }
  }

  @Override
  public void onTestStarted(TestStartedEvent event) {
    String name = event.getName();
    String location = event.getLocation();
    boolean config = event.isConfig();

    if (testNameToTestState.containsKey(name)) {
      Log.error(getClass(), "Test already running: " + name);
    }

    TestState currentSuite = getCurrentSuite();
    TestState testState;
    if (location != null) {
      testState = findChildByLocation(currentSuite, location, false);
    } else {
      testState = findChildByName(currentSuite, name, false);
    }

    if (testState == null) {
      testState = new TestState(name, false, location);
      testState.setConfig(config);
      if (treeBuildBeforeStart) {
        testState.setTreeBuildBeforeStart();
      }

      if (testLocator != null) {
        testState.setTestLocator(testLocator);
      }

      currentSuite.addChild(testState);
      if (treeBuildBeforeStart && gettingChildren) {
        for (TestState state : currentSuite.getChildren()) {
          if (!state.isFinal()) {
            currentChildren.add(state);
          }
        }
        gettingChildren = false;
      }
    }

    testNameToTestState.put(name, testState);

    testState.setStarted();

    callTestStarted(testState);
  }

  @Override
  public void onTestFailed(TestFailedEvent event) {
    String name = event.getName();

    TestState testState = getStateByStateName(name);
    if (testState == null) {
      onTestStarted(new TestStartedEvent(name, null));
      testState = getStateByStateName(name);
    }

    if (testState == null) {
      return;
    }

    testState.setTestFailed(event.getFailureMessage(), event.getStackTrace(), event.isError());

    callTestFailed(testState);
  }

  @Override
  public void onTestFinished(TestFinishedEvent event) {
    String name = event.getName();
    TestState testState = getStateByStateName(name);

    if (testState == null) {
      return;
    }

    testState.setDuration(event.getDuration());
    testState.setFinished();
    testNameToTestState.remove(name);
    currentChildren.remove(testState);

    callTestFinished(testState);
  }

  @Override
  public void onTestIgnored(TestIgnoredEvent event) {
    String name = event.getName();

    TestState testState = getStateByStateName(name);

    if (testState == null) {
      onTestStarted(new TestStartedEvent(name, null));
      testState = getStateByStateName(name);
    }

    if (testState == null) {
      return;
    }

    testState.setTestIgnored(event.getIgnoreComment(), event.getStackStrace());

    callTestIgnored(testState);
  }

  public void setDebuggerConfiguration(
      DebugConfiguration debugConfiguration,
      DebugConfigurationsManager debugConfigurationsManager) {
    this.debugConfiguration = debugConfiguration;
    this.debugConfigurationsManager = debugConfigurationsManager;
  }

  @Override
  public void onSuiteTreeStarted(String suiteName, String location) {
    treeBuildBeforeStart = true;
    TestState newSuite = new TestState(suiteName, true, location);
    if (testLocator != null) {
      newSuite.setTestLocator(testLocator);
    }

    if (TestRootState.ROOT.equals(lastTreeState.getName())) {
      testRootState.addChild(newSuite);
    } else {
      lastTreeState.addChild(newSuite);
    }

    lastTreeState = newSuite;
    testSuiteStack.add(newSuite);
    callSuiteTreeStarted(newSuite);
  }

  private void callSuiteTreeStarted(TestState newSuite) {
    listeners.forEach(listener -> listener.onSuiteTreeStarted(newSuite));
  }

  @Override
  public void onTestCountInSuite(int count) {
    callTestCountInSuite(count);
  }

  @Override
  public void onTestFrameworkAttached() {
    callTestFrameworkAttached(testRootState);
  }

  @Override
  public void onSuiteTreeEnded(String suiteName) {
    if (!(TestRootState.ROOT.equals(lastTreeState.getName()))) {
      lastTreeState = lastTreeState.getParent();
    }

    testSuiteStack.pop(suiteName);
  }

  @Override
  public void onSuiteTreeNodeAdded(String suiteName, String location) {
    treeBuildBeforeStart = true;
    TestState newState = new TestState(suiteName, false, location);
    newState.setTreeBuildBeforeStart();
    if (testLocator != null) {
      newState.setTestLocator(testLocator);
    }

    TestState currentSuite = testSuiteStack.peekLast();
    currentSuite.setTreeBuildBeforeStart();
    currentSuite.addChild(newState);

    callSuiteTreeNodeAdded(newState);
  }

  private void callSuiteTreeNodeAdded(TestState newState) {
    listeners.forEach(listener -> listener.onSuiteTreeNodeAdded(newState));
  }

  @Override
  public void onBuildTreeEnded() {
    List<Runnable> runnables = new ArrayList<>(buildTreeEvents);
    buildTreeEvents.clear();
    for (Runnable runnable : runnables) {
      runnable.run();
    }
    runnables.clear();
  }

  @Override
  public void onRootPresentationAdded(String rootName, String comment, String location) {
    testRootState.setPresentation(rootName);
    testRootState.setComment(comment);
    testRootState.setRootLocationUrl(location);
    if (testLocator != null) {
      testRootState.setTestLocator(testLocator);
    }
    callRootPresentationAdded(testRootState);
  }

  private void callRootPresentationAdded(TestRootState testRootState) {
    listeners.forEach(listener -> listener.onRootPresentationAdded(testRootState));
  }

  @Override
  public void onUncapturedOutput(String output, OutputType outputType) {
    TestState currentState = findCurrentSuiteOrTest();
    if (outputType == OutputType.STDERR) {
      currentState.addStdErr(output);
    } else {
      currentState.addStdOut(output, outputType);
    }
  }

  @Override
  public void onFinishTesting() {
    // TODO check test tree finish state

    testSuiteStack.clear();
    testRootState.setFinished();
    callTestingFinished(testRootState);
    removeDebugConfiguration();
  }

  private void removeDebugConfiguration() {
    if (debugConfiguration == null || debugConfigurationsManager == null) {
      return;
    }
    List<DebugConfiguration> configurations = debugConfigurationsManager.getConfigurations();
    for (DebugConfiguration configuration : configurations) {
      if (configuration.equals(debugConfiguration)) {
        debugConfigurationsManager.removeConfiguration(configuration);
      }
    }
  }

  private TestState findCurrentSuiteOrTest() {
    TestState result;
    if (testNameToTestState.size() == 1) {
      result = testNameToTestState.values().iterator().next();
    } else {
      if (testSuiteStack.isEmpty()) {
        result = testRootState;
      } else {
        result = getCurrentSuite();
      }
    }
    return result;
  }

  @Nullable
  private TestState findChildByName(TestState parentSuite, String name, boolean preferSuite) {
    return findChild(parentSuite, name, TestState::getName, preferSuite);
  }

  @Nullable
  private TestState findChildByLocation(
      TestState parentSuite, String location, boolean preferSuite) {
    return findChild(parentSuite, location, TestState::getLocationUrl, preferSuite);
  }

  @Nullable
  private TestState findChild(
      TestState parentSuite,
      String name,
      Function<TestState, String> nameFunction,
      boolean preferSuite) {
    if (treeBuildBeforeStart) {
      Set<TestState> result = new HashSet<>();
      Collection<TestState> children;
      if (gettingChildren) {
        children = parentSuite.getChildren();
      } else {
        children = currentChildren;
      }

      for (TestState child : children) {
        if (!child.isFinal() && name.equals(nameFunction.apply(child))) {
          result.add(child);
        }
      }

      if (!result.isEmpty()) {
        return result
            .stream()
            .filter(testState -> testState.isSuite() == preferSuite)
            .findFirst()
            .orElse(result.iterator().next());
      }
    }
    return null;
  }

  private TestState getCurrentSuite() {
    TestState current = testSuiteStack.getCurrent();
    if (current == null) {
      current = testRootState;
      gettingChildren = true;
    }
    return current;
  }

  private TestState getStateByStateName(String testName) {
    return testNameToTestState.get(testName);
  }
}
