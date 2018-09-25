package org.eclipse.che.selenium.pageobject.theia;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;

@Singleton
public class TheiaTerminal {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private TheiaTerminal(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String TERMINAL_BODY_ID_TEMPLATE = "terminal-%s";
  }
}
