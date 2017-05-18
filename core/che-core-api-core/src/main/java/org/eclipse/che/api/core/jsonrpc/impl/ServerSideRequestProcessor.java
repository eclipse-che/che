package org.eclipse.che.api.core.jsonrpc.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessor;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Singleton
public class ServerSideRequestProcessor implements RequestProcessor {
    private ExecutorService executorService;

    @Inject
    public ServerSideRequestProcessor() {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        ThreadFactory factory = builder.setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                                       .setNameFormat(ServerSideRequestProcessor.class.getSimpleName())
                                       .setDaemon(true)
                                       .build();

        executorService = Executors.newCachedThreadPool(factory);
    }

    @Override
    public void process(Runnable runnable) {
        executorService.execute(runnable);
    }
}
