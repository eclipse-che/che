package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.testng.annotations.Test;

/**
 * @author Alexander Garagatyi
 */
public class BrokersResultTest {
  private BrokersResult brokersResult;

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowExceptionOnCallingOneMoreResultAfterCallGet() throws Exception {
    brokersResult.get();
    brokersResult.oneMoreBroker();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowExceptionOnCallingErrorBeforeCallGet() throws Exception {
    brokersResult.error();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowExceptionOnCallingBrokerResultBeforeCallGet() throws Exception {
    brokersResult.brokerResult();
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionIfNumberOfBrokerResultCallsIsBiggerThanExpected() throws Exception {
    brokersResult.oneMoreBroker();
    brokersResult.oneMoreBroker();
    brokersResult.oneMoreBroker();

    brokersResult.brokerResult();
    brokersResult.brokerResult();
    brokersResult.brokerResult();
    brokersResult.brokerResult();
  }

  @Test
  public void shouldReturnResultOfOneBroker() throws Exception {
  }

  @Test
  public void shouldCombineResultsOfSeveralBrokers() throws Exception {
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowExceptionIfGetCalledTwice() throws Exception {
  }

  @Test
  public void shouldThrowTimeoutExceptionIfResultIsNotSubmitted() throws Exception {
  }

  @Test
  public void shouldThrowTimeoutExceptionIfNotAllResultsAreSubmitted() throws Exception {
  }

  @Test
  public void shouldThrowExceptionIfErrorIsSubmitted() throws Exception {
  }

  @Test
  public void shouldThrowExceptionIfErrorIsSubmittedAfterOneOfTheResults() throws Exception {
  }
}
