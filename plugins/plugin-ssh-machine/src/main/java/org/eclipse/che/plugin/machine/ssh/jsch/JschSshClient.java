/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.machine.ssh.jsch;

import com.google.inject.assistedinject.Assisted;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.machine.ssh.SshClient;
import org.eclipse.che.plugin.machine.ssh.SshMachineRecipe;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static java.lang.String.format;

/**
 * Client for communication with ssh machine using ssh protocol.
 *
 * @author Alexander Garagatyi
 */
// todo think about replacement JSch with Apace SSHD
// todo tests for ssh library that ensures that it works as expected
public class JschSshClient implements SshClient {
    private final JSch                jsch;
    private final JschUserInfoImpl    user;
    private final String              host;
    private final int                 port;
    private final String              username;
    private final Map<String, String> envVars;
    private final int                 connectionTimeout;

    private Session session;

    @Inject
    public JschSshClient(@Assisted SshMachineRecipe sshMachineRecipe,
                         @Assisted Map<String, String> envVars,
                         JSch jsch,
                         @Named("che.workspace.ssh_connection_timeout_ms") int connectionTimeoutMs) {
        this.envVars = envVars;
        this.connectionTimeout = connectionTimeoutMs;
        this.user = JschUserInfoImpl.builder()
                                    .password(sshMachineRecipe.getPassword())
                                    .promptPassword(true)
                                    .passphrase(null)
                                    .promptPassphrase(false)
                                    .promptYesNo(true)
                                    .build();
        this.jsch = jsch;
        this.host = sshMachineRecipe.getHost();
        this.port = sshMachineRecipe.getPort();
        this.username = sshMachineRecipe.getUsername();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void start() throws MachineException {
        try {
            session = jsch.getSession(username, host, port);
            session.setUserInfo(user);
            // todo remember parent pid of shell to be able to kill all processes on client stop
            if (!session.isConnected()) {
                session.connect(connectionTimeout);
            }
        } catch (JSchException e) {
            throw new MachineException("Ssh machine creation failed because ssh of machine is inaccessible. Error: " +
                                       e.getLocalizedMessage());
        }
    }

    //todo add method to read env vars by client
    // ChannelExec execAndGetCode = (ChannelExec)session.openChannel("execAndGetCode");
    // execAndGetCode.setCommand("env");
    //envVars.entrySet()
//    .stream()
//    .forEach(envVariableEntry -> execAndGetCode.setEnv(envVariableEntry.getKey(),
//            envVariableEntry.getValue()));
//     todo process output

    @Override
    public void stop() throws MachineException {
        session.disconnect();
    }

    @Override
    public JschSshProcess createProcess(String commandLine) throws MachineException {
        try {
            ChannelExec exec = (ChannelExec)session.openChannel("exec");
            exec.setCommand(commandLine);
            exec.setPty(true);
            envVars.entrySet()
                   .stream()
                   .forEach(envVariableEntry -> exec.setEnv(envVariableEntry.getKey(),
                                                            envVariableEntry.getValue()));
            return new JschSshProcess(exec);
        } catch (JSchException e) {
            throw new MachineException("Can't establish connection to perform command execution in ssh machine. Error: " +
                                       e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void copy(String sourcePath, String targetPath) throws MachineException {
        File source = new File(sourcePath);
        if (!source.exists()) {
            throw new MachineException("Source of copying '" + sourcePath + "' doesn't exist.");
        }
        if (source.isDirectory()) {
            copyRecursively(sourcePath, targetPath);
        } else {
            copyFile(sourcePath, targetPath);
        }
    }

    private void copyRecursively(String sourceFolder, String targetFolder) throws MachineException {
        // create target dir
        try {
            int execCode = execAndGetCode("mkdir -p " + targetFolder);

            if (execCode != 0) {
                throw new MachineException(format("Creation of folder %s failed. Exit code is %s", targetFolder, execCode));
            }
        } catch (JSchException | IOException e) {
            throw new MachineException(format("Creation of folder %s failed. Error: %s", targetFolder, e.getLocalizedMessage()));
        }

        // not normalized paths don't work
        final String targetAbsolutePath = getAbsolutePath(targetFolder);

        // copy files
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp)session.openChannel("sftp");
            sftp.connect(connectionTimeout);

            final ChannelSftp finalSftp = sftp;
            Files.walkFileTree(Paths.get(sourceFolder), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        if (!attrs.isDirectory()) {
                            copyFile(file.toString(),
                                     Paths.get(targetAbsolutePath, file.getFileName().toString()).toString(), finalSftp);
                        } else {
                            finalSftp.mkdir(file.normalize().toString());
                        }
                    } catch (MachineException | SftpException e) {
                        throw new IOException(format("Sftp copying of file %s failed. Error: %s", file, e.getLocalizedMessage()));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (JSchException | IOException e) {
            throw new MachineException("Copying failed. Error: " + e.getLocalizedMessage());
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    private void copyFile(String sourcePath, String targetPath) throws MachineException {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp)session.openChannel("sftp");
            sftp.connect(connectionTimeout);
            String absoluteTargetPath = getAbsolutePath(targetPath);
            copyFile(sourcePath, absoluteTargetPath, sftp);
        } catch (JSchException e) {
            throw new MachineException("Sftp copying failed. Error: " + e.getLocalizedMessage());
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    private void copyFile(String sourcePath, String absoluteTargetPath, ChannelSftp channelSftp) throws MachineException {
        try {
            channelSftp.put(sourcePath, absoluteTargetPath);

            // apply permissions
            File file = new File(sourcePath);
            // read
            int permissions = 256;
            // execute
            if (file.canExecute()) {
                permissions += 64;
            }
            // write
            if (file.canWrite()) {
                permissions += 128;
            }
            channelSftp.chmod(permissions, absoluteTargetPath);
        } catch (SftpException e) {
            throw new MachineException(format("Sftp copying of file %s failed. Error: %s",
                                              absoluteTargetPath,
                                              e.getLocalizedMessage()));
        }
    }

    private String getAbsolutePath(String path) throws MachineException {
        try {
            return execAndGetOutput("cd " + path + "; pwd");
        } catch (JSchException | IOException | MachineException e) {
            throw new MachineException("Target directory lookup failed. " + e.getLocalizedMessage());
        }
    }

    private int execAndGetCode(String command) throws JSchException, IOException {
        ChannelExec exec = (ChannelExec)session.openChannel("exec");
        exec.setCommand(command);

        try (InputStream inStream = exec.getInputStream();
             InputStream erStream = exec.getErrStream()) {

            exec.connect(connectionTimeout);

            // read streams to wait until command finishes its work
            IoUtil.readStream(inStream);
            IoUtil.readStream(erStream);
        } finally {
            exec.disconnect();
        }

        return exec.getExitStatus();
    }

    private String execAndGetOutput(String command) throws JSchException, MachineException, IOException {
        ChannelExec exec = (ChannelExec)session.openChannel("exec");
        exec.setCommand(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
             InputStream erStream = exec.getErrStream()) {

            exec.connect(connectionTimeout);

            ListLineConsumer listLineConsumer = new ListLineConsumer();
            String line;
            while ((line = reader.readLine()) != null) {
                listLineConsumer.writeLine(line);
            }
            // read stream to wait until command finishes its work
            IoUtil.readStream(erStream);
            if (exec.getExitStatus() != 0) {
                throw new MachineException(format("Error code: %s. Error: %s",
                                                  exec.getExitStatus(),
                                                  IoUtil.readAndCloseQuietly(exec.getErrStream())));
            }
            return listLineConsumer.getText();
        } finally {
            exec.disconnect();
        }
    }
}
