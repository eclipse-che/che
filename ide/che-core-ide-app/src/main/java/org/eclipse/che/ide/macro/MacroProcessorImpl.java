/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.macro;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.macro.MacroRegistry;

import java.util.Iterator;

/**
 * Implementation of {@link MacroProcessor}.
 *
 * @author Artem Zatsarynnyi
 * @see Macro
 * @see CommandManager#executeCommand(CommandImpl, Machine)
 */
public class MacroProcessorImpl implements MacroProcessor {

    private final MacroRegistry macroRegistry;

    @Inject
    public MacroProcessorImpl(MacroRegistry macroRegistry) {
        this.macroRegistry = macroRegistry;
    }

    @Override
    public Promise<String> expandMacros(String text) {
        Promise<String> promise = Promises.resolve(null);
        StringContainer stringContainer = new StringContainer(text);
        return expandMacros(promise, stringContainer, macroRegistry.getMacros().iterator());
    }

    private Promise<String> expandMacros(Promise<String> promise,
                                         StringContainer stringContainer,
                                         Iterator<Macro> iterator) {
        if (!iterator.hasNext()) {
            return promise;
        }

        Macro macro = iterator.next();
        Promise<String> derivedPromise = promise.thenPromise(expandMacro(stringContainer, macro));

        return expandMacros(derivedPromise, stringContainer, iterator);
    }

    private Function<String, Promise<String>> expandMacro(final StringContainer stringContainer, final Macro macro) {
        return new Function<String, Promise<String>>() {
            @Override
            public Promise<String> apply(String arg) throws FunctionException {
                return macro.expand().thenPromise(new Function<String, Promise<String>>() {
                    @Override
                    public Promise<String> apply(String arg) throws FunctionException {
                        stringContainer.setCommandLine(stringContainer.getCommandLine().replace(macro.getName(), arg));
                        return Promises.resolve(stringContainer.getCommandLine());
                    }
                });
            }
        };
    }

    private static class StringContainer {
        String commandLine;

        StringContainer(String commandLine) {
            this.commandLine = commandLine;
        }

        String getCommandLine() {
            return commandLine;
        }

        void setCommandLine(String commandLine) {
            this.commandLine = commandLine;
        }
    }
}
