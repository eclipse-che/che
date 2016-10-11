/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide.debug;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import zend.com.che.plugin.zdb.ide.configuration.ZendDebugConfigurationType;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolver;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolverFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Zend PHP debugger.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugger extends AbstractDebugger {

	public static final String ID = "zend-debugger";

	public final FqnResolverFactory fqnResolverFactory;
	public final FileTypeRegistry fileTypeRegistry;

	@Inject
	public ZendDebugger(DebuggerServiceClient service, DtoFactory dtoFactory, LocalStorageProvider localStorageProvider,
			MessageBusProvider messageBusProvider, EventBus eventBus, FqnResolverFactory fqnResolverFactory,
			ZendDebuggerFileHandler zendDebuggerFileHandler, DebuggerManager debuggerManager,
			FileTypeRegistry fileTypeRegistry, BreakpointManager breakpointManager) {
		super(service, dtoFactory, localStorageProvider, messageBusProvider, eventBus, zendDebuggerFileHandler,
				debuggerManager, breakpointManager, ID);
		this.fqnResolverFactory = fqnResolverFactory;
		this.fileTypeRegistry = fileTypeRegistry;
	}

	@Override
	protected String fqnToPath(@NotNull Location location) {
		String resourcePath = location.getResourcePath();
		return resourcePath != null ? resourcePath : location.getTarget();
	}

	@Override
	protected String pathToFqn(VirtualFile file) {
		String fileExtension = fileTypeRegistry.getFileTypeByFile(file).getExtension();
		FqnResolver resolver = fqnResolverFactory.getResolver(fileExtension);
		if (resolver != null) {
			return resolver.resolveFqn(file);
		}
		return null;
	}

	@Override
	protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
		return new DebuggerDescriptor("Zend Debugger", "Zend Debugger client, port: "
				+ connectionProperties.get(ZendDebugConfigurationType.ATTR_DEBUG_PORT));
	}

}
