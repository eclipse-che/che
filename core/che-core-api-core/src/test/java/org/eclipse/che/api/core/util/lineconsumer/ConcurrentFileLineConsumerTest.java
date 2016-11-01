package org.eclipse.che.api.core.util.lineconsumer;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ConcurrentFileLineConsumerTest {
    private static final Logger LOG = getLogger(ConcurrentFileLineConsumer.class);

    @Mock
    private Writer writer;

    private ConcurrentFileLineConsumer concurrentFileLineConsumer;

    private File file;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        file = File.createTempFile("file", ".tmp");
        concurrentFileLineConsumer = new ConcurrentFileLineConsumer(file);
        injectWriterMock(concurrentFileLineConsumer, writer);
    }

    @AfterClass
    public void afterClass() {
        if (!file.delete()) {
            LOG.warn("Failed to remove temporary file: '{}'.", file);
        }
    }

    @Test
    public void shouldBeAbleToWriteIntoFile() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentFileLineConsumer.writeLine(message);

        // then
        verify(writer).write(eq(message));
    }

    @Test
    public void shouldNotWriteIntoFileAfterConsumerClosing() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentFileLineConsumer.close();
        concurrentFileLineConsumer.writeLine(message);

        // then
        verify(writer, never()).write(anyString());
    }

    /**
     * Inject Writer mock into FileLineConsumer class.
     * This allow to test the FileLineConsumer operations.
     *
     * @param concurrentFileLineConsumer
     *         instance in which mock will be injected
     * @param writerMock
     *         mock to inject
     * @throws Exception
     */
    private void injectWriterMock(ConcurrentFileLineConsumer concurrentFileLineConsumer, Writer writerMock) throws Exception {
        Field writerField = concurrentFileLineConsumer.getClass().getDeclaredField("writer");
        writerField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(writerField, writerField.getModifiers() & ~Modifier.FINAL);

        writerField.set(concurrentFileLineConsumer, writerMock);
    }

}
