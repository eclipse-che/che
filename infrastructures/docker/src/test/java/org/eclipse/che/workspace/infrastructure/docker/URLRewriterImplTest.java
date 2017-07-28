package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.docker.URLRewriterImpl.EXTERNAL_IP_PROPERTY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Alexander Garagatyi
 */
public class URLRewriterImplTest {
    @Test(dataProvider = "urlRewritingTestProvider")
    public void shouldRewriteURL(String externalIP, String incomeURL, String expectedURL) throws Exception {
        URLRewriterImpl rewriter = new URLRewriterImpl(externalIP);

        String rewrittenURL = rewriter.rewriteURL(null, null, incomeURL);

        assertEquals(rewrittenURL, expectedURL);
    }

    @DataProvider(name = "urlRewritingTestProvider")
    public static Object[][] urlRewritingTestProvider() {
        return new Object[][] {
                {"localhost", "http://127.0.0.1:8080/path", "http://localhost:8080/path"},
                {"127.0.0.1", "http://127.0.0.1:8080/path", "http://127.0.0.1:8080/path"},
                {"127.0.0.1", "wss://google.com:8080/some/path?param", "wss://127.0.0.1:8080/some/path?param"},
                {"www.some.host",
                 "tcp://google.com:8080/some/path?param=value",
                 "tcp://www.some.host:8080/some/path?param=value"},
                {"google.com", "http://127.0.0.1:8080/path", "http://google.com:8080/path"},
                {"178.19.20.12", "http://127.0.0.1:8080/path", "http://178.19.20.12:8080/path"},
                };
    }

    @Test
    public void shouldNotRewriteURLIfExternalIpIsNoConfigured() throws Exception {
        String toRewrite = "https://google.com:8080/some/path?param=value";
        URLRewriterImpl rewriter = new URLRewriterImpl(null);

        String rewrittenURL = rewriter.rewriteURL(null, null, toRewrite);

        assertEquals(rewrittenURL, toRewrite);
    }

    @Test(expectedExceptions = InternalInfrastructureException.class,
          expectedExceptionsMessageRegExp = "Rewriting of host 'localhost' in URL ':' failed. Error: .*")
    public void shouldThrowExceptionWhenRewritingFails() throws Exception {
        String toRewrite = ":";
        URLRewriterImpl rewriter = new URLRewriterImpl("localhost");

        rewriter.rewriteURL(null, null, toRewrite);
    }

    @Test(dataProvider = "badExternalIpProvider")
    public void shouldThrowExceptionOnRewriterCreationIfURLCheckFails(String badExternalIp) throws Exception {
        try {
            new URLRewriterImpl(badExternalIp);
            fail("URL rewriter creation had to throw an exception, but no exception was thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().startsWith(
                    format("Illegal value '%s' of property '%s'. Error: ", badExternalIp, EXTERNAL_IP_PROPERTY)));
        }
    }

    @DataProvider(name = "badExternalIpProvider")
    public static Object[][] badExternalIpProvider() {
        return new Object[][] {
                {""},
                {" "}
        };
    }
}
