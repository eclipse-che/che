package org.eclipse.che.ide.processes;

/**
 * An object that implements this interface provides registration for {@link StopProcessHandler} instances.
 *
 * @author Vlad Zhukovskyi
 * @see StopProcessHandler
 * @since 5.10.0
 */
public interface HasStopProcessHandler {

    /**
     * Adds a {@link StopProcessHandler} handler.
     *
     * @param handler
     *         the stop process handler
     */
    void addStopProcessHandler(StopProcessHandler handler);
}
