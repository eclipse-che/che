/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.inject;

import static com.google.inject.Guice.createInjector;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.pageobject.PageObjectsInjector;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverFactory;
import org.eclipse.che.selenium.core.webdriver.log.WebDriverLogsReaderFactory;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceLogsReader;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestException;

/**
 * Tests lifecycle handler.
 *
 * <p>Adding {@link IConfigurationListener} brings bug when all methods of the listener will be
 * invoked twice.
 *
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 */
public abstract class SeleniumTestHandler
    implements ITestListener, ISuiteListener, IInvokedMethodListener, IExecutionListener {

  private static final Logger LOG = LoggerFactory.getLogger(SeleniumTestHandler.class);
  private static final AtomicBoolean isCleanUpCompleted = new AtomicBoolean();

  @Inject private SeleniumWebDriverFactory seleniumWebDriverFactory;

  @Inject
  @Named("tests.screenshots_dir")
  private String screenshotsDir;

  @Inject
  @Named("tests.htmldumps_dir")
  private String htmldumpsDir;

  @Inject
  @Named("tests.webdriverlogs_dir")
  private String webDriverLogsDir;

  @Inject
  @Named("tests.workspacelogs_dir")
  private String workspaceLogsDir;

  @Inject private PageObjectsInjector pageObjectsInjector;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject
  @Named("sys.excludedGroups")
  private String excludedGroups;

  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private TestWorkspaceLogsReader testWorkspaceLogsReader;
  @Inject private SeleniumTestStatistics seleniumTestStatistics;
  @Inject private WebDriverLogsReaderFactory webDriverLogsReaderFactory;

  private final Injector injector;

  // this is the map {thread ID} -> {test instance}
  private final Map<Long, Object> runningTests = new ConcurrentHashMap<>();

  // this is the map {test class FQN} -> {failed test method}
  private final Map<String, ITestNGMethod> testsWithFailure = new ConcurrentHashMap<>();

  public SeleniumTestHandler() {
    injector = createInjector(getParentModules());
    injector.injectMembers(this);

    getRuntime().addShutdownHook(new Thread(this::shutdown));

    revokeGithubOauthToken();
    checkWebDriverSessionCreation();
  }

  private void revokeGithubOauthToken() {
    // do not revoke if github tests are not being executed
    if (excludedGroups != null && excludedGroups.contains(TestGroup.GITHUB)) {
      return;
    }

    try {
      gitHubClientService.deleteAllGrants(gitHubUsername, gitHubPassword);
    } catch (Exception e) {
      LOG.warn("There was an error of revoking the github oauth token.", e);
    }
  }

  @Override
  public void onTestStart(ITestResult result) {
    LOG.info(
        "Starting test #{} {}. {}",
        seleniumTestStatistics.hitStart(),
        getStartingTestLabel(result.getMethod()),
        seleniumTestStatistics.toString());

    skipTestIfNeeded(result);
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    seleniumTestStatistics.hitPass();
    onTestFinish(result);
  }

  @Override
  public void onTestFailure(ITestResult result) {
    seleniumTestStatistics.hitFail();
    onTestFinish(result);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    seleniumTestStatistics.hitSkip();
    onTestFinish(result);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    seleniumTestStatistics.hitFail();
    onTestFinish(result);
  }

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onFinish(ITestContext context) {}

  @Override
  public void onStart(ISuite suite) {
    suite.setParentInjector(injector);
    LOG.info(
        "Starting suite '{}' with {} test methods.", suite.getName(), suite.getAllMethods().size());
  }

  /** Check if webdriver session can be created without errors. */
  private void checkWebDriverSessionCreation() {
    SeleniumWebDriver seleniumWebDriver = null;
    try {
      seleniumWebDriver = seleniumWebDriverFactory.create();
    } finally {
      Optional.ofNullable(seleniumWebDriver)
          .ifPresent(SeleniumWebDriver::quit); // finish webdriver session
    }
  }

  @Override
  public void onFinish(ISuite suite) {}

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    Object invokingTestInstance = method.getTestMethod().getInstance();
    if (runningTests.containsValue(invokingTestInstance)) {
      return;
    }

    long currentThreadId = Thread.currentThread().getId();
    if (isNewTestInProgress(invokingTestInstance)) {
      Object previousTestInstance = runningTests.remove(currentThreadId);
      preDestroy(previousTestInstance);
      testsWithFailure.remove(previousTestInstance.getClass().getName());
    }

    String testName = invokingTestInstance.getClass().getName();

    try {
      LOG.info("Dependencies injection in {}", testName);
      injectDependencies(testResult.getTestContext(), invokingTestInstance);
    } catch (Exception e) {
      String errorMessage = "Failed to inject fields in " + testName;
      LOG.error(errorMessage, e);
      throw new TestException(errorMessage, e);
    } finally {
      runningTests.put(currentThreadId, invokingTestInstance);
    }
  }

  private boolean isNewTestInProgress(Object testInstance) {
    Thread currentThread = Thread.currentThread();

    return runningTests.containsKey(currentThread.getId())
        && runningTests.get(currentThread.getId()) != testInstance;
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult result) {}

  @Override
  public void onExecutionStart() {}

  @Override
  public void onExecutionFinish() {
    shutdown();
  }

  /** Injects dependencies into the given test class using {@link Guice} and custom injectors. */
  private void injectDependencies(ITestContext testContext, Object testInstance) throws Exception {
    Injector injector = testContext.getSuite().getParentInjector();

    List<Module> childModules = new ArrayList<>(getChildModules());
    childModules.add(new SeleniumClassModule());

    Injector classInjector = injector.createChildInjector(childModules);
    classInjector.injectMembers(testInstance);

    pageObjectsInjector.injectMembers(testInstance, classInjector);
  }

  /** Is invoked when test or configuration is finished. */
  private void onTestFinish(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE || result.getStatus() == ITestResult.SKIP) {
      switch (result.getStatus()) {
        case ITestResult.FAILURE:
          String errorDetails =
              result.getThrowable() != null
                  ? " Error: " + result.getThrowable().getLocalizedMessage()
                  : "";

          LOG.error("Test {} failed.{}", getCompletedTestLabel(result.getMethod()), errorDetails);
          LOG.debug(result.getThrowable().getLocalizedMessage(), result.getThrowable());

          testsWithFailure.put(result.getTestClass().getRealClass().getName(), result.getMethod());

          break;

        case ITestResult.SKIP:
          LOG.warn("Test {} skipped.", getCompletedTestLabel(result.getMethod()));

          // don't capture test data if test is skipped because of previous test with higher
          // priority failed
          if (testsWithFailure.containsKey(result.getMethod().getInstance().getClass().getName())) {
            return;
          }

          break;

        default:
      }

      captureScreenshot(result);
      captureHtmlSource(result);
      captureTestWorkspaceLogs(result);
      storeWebDriverLogs(result);
    }
  }

  private void captureTestWorkspaceLogs(ITestResult result) {
    Object testInstance = result.getInstance();
    for (Field field : testInstance.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      Object obj;
      try {
        obj = field.get(testInstance);
      } catch (IllegalAccessException e) {
        LOG.error(
            "Field {} is inaccessible in {}.", field.getName(), testInstance.getClass().getName());
        continue;
      }

      if (obj == null || !(obj instanceof TestWorkspace) || !isInjectedWorkspace(field)) {
        continue;
      }

      Path pathToStoreWorkspaceLogs = Paths.get(workspaceLogsDir, getTestReference(result));
      testWorkspaceLogsReader.read((TestWorkspace) obj, pathToStoreWorkspaceLogs);
    }
  }

  private boolean isInjectedWorkspace(Field field) {
    return field.isAnnotationPresent(com.google.inject.Inject.class)
        || field.isAnnotationPresent(javax.inject.Inject.class)
        || field.isAnnotationPresent(InjectTestWorkspace.class);
  }

  /** Releases resources by invoking methods annotated with {@link PreDestroy} */
  private void preDestroy(Object testInstance) {
    LOG.info("Processing @PreDestroy annotation in {}", testInstance.getClass().getName());

    for (Field field : testInstance.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      Object obj;
      try {
        obj = field.get(testInstance);
      } catch (IllegalAccessException e) {
        LOG.error(
            "Field {} is inaccessible in {}.", field.getName(), testInstance.getClass().getName());
        continue;
      }

      if (obj == null || !hasInjectAnnotation(field)) {
        continue;
      }

      for (Method m : obj.getClass().getMethods()) {
        if (m.isAnnotationPresent(PreDestroy.class)) {
          try {
            m.invoke(obj);
          } catch (Exception e) {
            LOG.error(
                format(
                    "Failed to invoke method %s annotated with @PreDestroy in %s. Test instance: %s",
                    m.getName(), obj.getClass().getName(), testInstance.getClass().getName()),
                e);
          }
        }
      }
    }
  }

  private boolean hasInjectAnnotation(AccessibleObject f) {
    return f.isAnnotationPresent(com.google.inject.Inject.class)
        || f.isAnnotationPresent(javax.inject.Inject.class)
        || f.isAnnotationPresent(InjectTestWorkspace.class)
        || f.isAnnotationPresent(InjectTestOrganization.class)
        || f.isAnnotationPresent(InjectPageObject.class);
  }

  private void captureScreenshot(ITestResult result) {
    Set<SeleniumWebDriver> webDrivers = new HashSet<>();
    Object testInstance = result.getInstance();

    collectInjectedWebDrivers(testInstance, webDrivers);
    webDrivers.forEach(webDriver -> captureScreenshotsFromOpenedWindows(result, webDriver));
  }

  private void captureHtmlSource(ITestResult result) {
    Set<SeleniumWebDriver> webDrivers = new HashSet<>();
    Object testInstance = result.getInstance();
    collectInjectedWebDrivers(testInstance, webDrivers);
    webDrivers.forEach(webDriver -> dumpHtmlCodeFromTheCurrentPage(result, webDriver));
  }

  /**
   * Iterates recursively throw all fields and collects instances of {@link SeleniumWebDriver}.
   *
   * @param testInstance the based object to examine
   * @param webDrivers as the result of the method will contain all {@link WebDriver}
   */
  private void collectInjectedWebDrivers(Object testInstance, Set<SeleniumWebDriver> webDrivers) {
    for (Field field : testInstance.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      Object obj;
      try {
        obj = field.get(testInstance);
      } catch (IllegalAccessException e) {
        LOG.error(
            "Field {} is inaccessible in {}.", field.getName(), testInstance.getClass().getName());
        continue;
      }

      if (obj == null) {
        continue;
      }

      Optional<Constructor<?>> injectedConstructor =
          Stream.of(obj.getClass().getConstructors()).filter(this::hasInjectAnnotation).findAny();

      if (!hasInjectAnnotation(field) && !injectedConstructor.isPresent()) {
        continue;
      }

      if (obj instanceof com.google.inject.Provider || obj instanceof javax.inject.Provider) {
        continue;
      }

      if (obj instanceof SeleniumWebDriver) {
        webDrivers.add((SeleniumWebDriver) obj);
      } else {
        collectInjectedWebDrivers(obj, webDrivers);
      }
    }
  }

  private void captureScreenshotFromWindow(ITestResult result, SeleniumWebDriver webDriver) {
    String testReference = getTestReference(result);
    String filename = NameGenerator.generate(testReference + "_", 8) + ".png";
    try {
      byte[] data = webDriver.getScreenshotAs(OutputType.BYTES);
      Path screenshot = Paths.get(screenshotsDir, filename);
      Files.createDirectories(screenshot.getParent());
      Files.copy(new ByteArrayInputStream(data), screenshot);
    } catch (WebDriverException | IOException e) {
      LOG.error(format("Can't capture screenshot for test %s", testReference), e);
    }
  }

  private String getTestReference(ITestResult result) {
    return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
  }

  private void captureScreenshotsFromOpenedWindows(
      ITestResult result, SeleniumWebDriver webDriver) {
    webDriver
        .getWindowHandles()
        .forEach(
            currentWin -> {
              webDriver.switchTo().window(currentWin);
              captureScreenshotFromWindow(result, webDriver);
            });
  }

  private void storeWebDriverLogs(ITestResult result) {
    Set<SeleniumWebDriver> webDrivers = new HashSet<>();
    Object testInstance = result.getInstance();
    collectInjectedWebDrivers(testInstance, webDrivers);
    webDrivers.forEach(webDriver -> storeWebDriverLogs(result, webDriver));
  }

  private void storeWebDriverLogs(ITestResult result, SeleniumWebDriver webDriver) {
    String testReference = getTestReference(result);

    try {
      String filename = NameGenerator.generate(testReference + "_", 4) + ".log";
      Path webDriverLogsDirectory = Paths.get(webDriverLogsDir, filename);
      Files.createDirectories(webDriverLogsDirectory.getParent());
      Files.write(
          webDriverLogsDirectory,
          webDriverLogsReaderFactory
              .create(webDriver)
              .getAllLogs()
              .getBytes(Charset.forName("UTF-8")),
          StandardOpenOption.CREATE);
    } catch (WebDriverException | IOException | JsonParseException e) {
      LOG.error(format("Can't store web driver logs related to test %s.", testReference), e);
    }
  }

  private void dumpHtmlCodeFromTheCurrentPage(ITestResult result, SeleniumWebDriver webDriver) {
    String testReference = getTestReference(result);
    String filename = NameGenerator.generate(testReference + "_", 8) + ".html";
    try {
      String pageSource = webDriver.getPageSource();
      Path dumpDirectory = Paths.get(htmldumpsDir, filename);
      Files.createDirectories(dumpDirectory.getParent());
      Files.write(
          dumpDirectory, pageSource.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
    } catch (WebDriverException | IOException e) {
      LOG.error(format("Can't dump of html source for test %s", testReference), e);
    }
  }

  /** Cleans up test environment. */
  private void shutdown() {
    if (isCleanUpCompleted.get()) {
      return;
    }

    LOG.info("Cleaning up test environment...");

    for (Object testInstance : runningTests.values()) {
      preDestroy(testInstance);
      testsWithFailure.remove(testInstance.getClass().getName());
    }

    if (testWorkspaceProvider != null) {
      testWorkspaceProvider.shutdown();
    }

    if (defaultTestUser != null) {
      try {
        defaultTestUser.delete();
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    isCleanUpCompleted.set(true);
  }

  /**
   * Skip test if preceding test with higher priority from the same test class has failed.
   *
   * @param result holds result of test execution
   * @throws SkipException if test should be skipped
   */
  private void skipTestIfNeeded(ITestResult result) {
    ITestNGMethod testMethodToSkip = result.getMethod();
    ITestNGMethod failedTestMethod =
        testsWithFailure.get(testMethodToSkip.getInstance().getClass().getName());

    // Test with lower priority value is started firstly by TestNG.
    if (failedTestMethod != null
        && testMethodToSkip.getPriority() > failedTestMethod.getPriority()) {
      throw new SkipException(
          format(
              "Skipping test %s because it depends on test %s which has failed earlier.",
              getStartingTestLabel(testMethodToSkip), getCompletedTestLabel(failedTestMethod)));
    }
  }

  private String getStartingTestLabel(ITestNGMethod test) {
    String invocationLabel = "";
    if (test.getCurrentInvocationCount() > 0) {
      invocationLabel = format(" (run %d)", test.getCurrentInvocationCount() + 1);
    }

    return getTestLabel(test, invocationLabel);
  }

  private String getCompletedTestLabel(ITestNGMethod test) {
    String invocationLabel = "";
    if (test.getCurrentInvocationCount() > 1) {
      invocationLabel = format(" (run %d)", test.getCurrentInvocationCount());
    }

    return getTestLabel(test, invocationLabel);
  }

  private String getTestLabel(ITestNGMethod test, String invocationLabel) {
    return format(
        "%s.%s%s",
        test.getInstance().getClass().getSimpleName(), test.getMethodName(), invocationLabel);
  }

  /** Returns list of parent modules */
  @NotNull
  public abstract List<Module> getParentModules();

  /** Returns list of child modules */
  @NotNull
  public abstract List<Module> getChildModules();
}
