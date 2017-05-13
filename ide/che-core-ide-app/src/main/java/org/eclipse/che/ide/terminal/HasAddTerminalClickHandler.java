package org.eclipse.che.ide.terminal;

/**
 * An object that implements this interface provides registration for {@link AddTerminalClickHandler} instances.
 *
 * @author Vlad Zhukovskyi
 * @see AddTerminalClickHandler
 * @since 5.10.0
 */
public interface HasAddTerminalClickHandler {

    /**
     * Adds a {@link AddTerminalClickHandler} handler.
     *
     * @param handler
     *         the add terminal click handler
     */
    void addAddTerminalClickHandler(AddTerminalClickHandler handler);
}
