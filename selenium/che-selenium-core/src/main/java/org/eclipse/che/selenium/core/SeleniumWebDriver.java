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
package org.eclipse.che.selenium.core;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.APPLICATION_START_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.constant.TestBrowser;
import org.eclipse.che.selenium.core.utils.DockerUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link WebDriver} to have ability to use in Guice container.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class SeleniumWebDriver
    implements Closeable, WebDriver, JavascriptExecutor, TakesScreenshot, HasInputDevices {
  private static final Logger LOG = LoggerFactory.getLogger(SeleniumWebDriver.class);
  private static final int MAX_ATTEMPTS = 5;
  private static final int DELAY_IN_SECONDS = 5;

  private TestBrowser browser;
  private boolean gridMode;
  private String gridNodeContainerId;
  private String webDriverPort;
  private final RemoteWebDriver driver;
  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final DockerUtil dockerUtil;

  @Inject
  public SeleniumWebDriver(
      @Named("sys.browser") TestBrowser browser,
      @Named("sys.driver.port") String webDriverPort,
      @Named("sys.grid.mode") boolean gridMode,
      HttpJsonRequestFactory httpJsonRequestFactory,
      DockerUtil dockerUtil) {
    this.browser = browser;
    this.webDriverPort = webDriverPort;
    this.gridMode = gridMode;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
    this.dockerUtil = dockerUtil;

    try {
      URL webDriverUrl =
          new URL(format("http://localhost:%s%s", webDriverPort, gridMode ? "/wd/hub" : ""));
      this.driver = createDriver(webDriverUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error of construction URL to web driver.", e);
    }
  }

  @Override
  public void get(String url) {
    driver.get(url);
  }

  @Override
  public String getCurrentUrl() {
    return driver.getCurrentUrl();
  }

  @Override
  public String getTitle() {
    return driver.getTitle();
  }

  @Override
  public List<WebElement> findElements(By by) {
    return driver.findElements(by);
  }

  @Override
  public WebElement findElement(By by) {
    return driver.findElement(by);
  }

  @Override
  public String getPageSource() {
    return driver.getPageSource();
  }

  @Override
  public void close() {
    driver.close();
  }

  @Override
  public void quit() {
    driver.quit();
  }

  @Override
  public Set<String> getWindowHandles() {
    return driver.getWindowHandles();
  }

  @Override
  public String getWindowHandle() {
    return driver.getWindowHandle();
  }

  @Override
  public TargetLocator switchTo() {
    return driver.switchTo();
  }

  @Override
  public Navigation navigate() {
    return driver.navigate();
  }

  @Override
  public Options manage() {
    return driver.manage();
  }

  @Override
  public Object executeScript(String script, Object... args) {
    return driver.executeScript(script, args);
  }

  @Override
  public Object executeAsyncScript(String script, Object... args) {
    return driver.executeAsyncScript(script, args);
  }

  @Override
  public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
    return driver.getScreenshotAs(target);
  }

  @Override
  public Keyboard getKeyboard() {
    return driver.getKeyboard();
  }

  @Override
  public Mouse getMouse() {
    return driver.getMouse();
  }

  private RemoteWebDriver createDriver(URL webDriverUrl) {
    for (int i = 1; ; ) {
      try {
        return doCreateDriver(webDriverUrl);
      } catch (WebDriverException e) {
        if (i++ >= MAX_ATTEMPTS) {
          throw e;
        }
      }

      sleepQuietly(DELAY_IN_SECONDS);
    }
  }

  /**
   * Read supported version info from official site.
   *
   * @param webDriverOfficialNotes address of official page with Google driver info
   * @return string with supported version range (for example, "36-40"), or null if version info
   *     doesn't found inside the official notes.
   * @throws IOException
   */
  @Nullable
  private String readSupportedVersionInfoForGoogleDriver(URL webDriverOfficialNotes)
      throws IOException {
    try (Scanner scanner = new Scanner(webDriverOfficialNotes.openStream(), "UTF-8")) {
      while (scanner.hasNextLine()) {
        String versionLine = scanner.findInLine("Supports Chrome v([\\d-]+)");
        if (versionLine != null) {
          return scanner.match().group(1);
        }

        scanner.nextLine();
      }
    }

    return null;
  }

  private RemoteWebDriver doCreateDriver(URL webDriverUrl) {
    DesiredCapabilities capability;

    switch (browser) {
      case GOOGLE_CHROME:
        LoggingPreferences loggingPreferences = new LoggingPreferences();
        loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
        loggingPreferences.enable(LogType.BROWSER, Level.ALL);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--dns-prefetch-disable");

        // set parameters required for automatic download capability
        String downloadDirectory = "/tmp/";
        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", downloadDirectory);
        chromePrefs.put("download.prompt_for_download", false);
        chromePrefs.put("plugins.plugins_disabled", "['Chrome PDF Viewer']");
        options.setExperimentalOption("prefs", chromePrefs);

        capability = DesiredCapabilities.chrome();
        capability.setCapability(ChromeOptions.CAPABILITY, options);
        capability.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
        capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        break;

      default:
        capability = DesiredCapabilities.firefox();
        capability.setCapability("dom.max_script_run_time", 240);
        capability.setCapability("dom.max_chrome_script_run_time", 240);
    }

    RemoteWebDriver driver = new RemoteWebDriver(webDriverUrl, capability);
    if (driver.getErrorHandler().isIncludeServerErrors()
        && driver.getCapabilities().getCapability("message") != null) {
      String errorMessage =
          format(
              "Web driver creation error occurred: %s",
              driver.getCapabilities().getCapability("message"));
      LOG.error(errorMessage);
      throw new RuntimeException(errorMessage);
    }

    driver.manage().window().setSize(new Dimension(1920, 1080));

    return driver;
  }

  /** wait while in a browser appears more the 1 window */
  public void waitOpenedSomeWin() {
    new WebDriverWait(this, 30)
        .until(
            (ExpectedCondition<Boolean>)
                input -> {
                  Set<String> driverWindows = getWindowHandles();
                  return (driverWindows.size() > 1);
                });
  }

  /**
   * calculate name of workspace from browser url cut symbols from end of slash symbol ("/") to end
   *
   * @return
   */
  public String getWorkspaceNameFromBrowserUrl() {
    String currentUrl = getCurrentUrl();
    return currentUrl.substring(currentUrl.lastIndexOf("/") + 1, currentUrl.length());
  }

  /**
   * switch to the next browser window (this means that if opened 2 windows, and we are in the
   * window 1, we will be switched into the window 2 )
   *
   * @param currentWindowHandler
   */
  public void switchToNoneCurrentWindow(String currentWindowHandler) {
    waitOpenedSomeWin();
    for (String handle : getWindowHandles()) {
      if (!currentWindowHandler.equals(handle)) {
        switchTo().window(handle);
        break;
      }
    }
  }

  public void switchFromDashboardIframeToIde() {
    switchFromDashboardIframeToIde(APPLICATION_START_TIMEOUT_SEC);
  }

  public void switchFromDashboardIframeToIde(int timeout) {
    wait(timeout).until(visibilityOfElementLocated(By.id("ide-application-iframe")));

    wait(LOADER_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                driver ->
                    (((JavascriptExecutor) driver)
                            .executeScript("return angular.element('body').scope().showIDE"))
                        .toString()
                        .equals("true"));

    wait(timeout).until(frameToBeAvailableAndSwitchToIt(By.id("ide-application-iframe")));
  }

  private WebDriverWait wait(int timeOutInSeconds) {
    return new WebDriverWait(this, timeOutInSeconds);
  }

  public String getGridNodeContainerId() throws IOException {
    if (!gridMode) {
      throw new UnsupportedOperationException("We can't get grid node container id in local mode.");
    }

    if (gridNodeContainerId == null) {
      String getGridNodeInfoUrl =
          format(
              "http://localhost:%s/grid/api/testsession?session=%s",
              webDriverPort, driver.getSessionId());

      Map<String, String> gridNodeInfo;
      try {
        gridNodeInfo = httpJsonRequestFactory.fromUrl(getGridNodeInfoUrl).request().asProperties();
      } catch (ServerException
          | UnauthorizedException
          | ForbiddenException
          | NotFoundException
          | ConflictException
          | BadRequestException e) {
        throw new IOException(e);
      }

      if (!gridNodeInfo.containsKey("proxyId")) {
        throw new IOException("Proxy ID of grid node wasn't found.");
      }

      URL proxyId = new URL(gridNodeInfo.get("proxyId"));
      gridNodeContainerId = dockerUtil.findGridNodeContainerByIp(proxyId.getHost());
    }

    return gridNodeContainerId;
  }
}
