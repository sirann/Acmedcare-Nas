package com.acmedcare.nas.ftp.server.impl;

import com.acmedcare.nas.ftp.server.ConnectionConfig;
import com.acmedcare.nas.ftp.server.ConnectionConfigFactory;
import com.acmedcare.nas.ftp.server.command.CommandFactory;
import com.acmedcare.nas.ftp.server.command.CommandFactoryFactory;
import com.acmedcare.nas.ftp.server.filesystem.nativefs.NativeFileSystemFactory;
import com.acmedcare.nas.ftp.server.ftplet.*;
import com.acmedcare.nas.ftp.server.ftpletcontainer.FtpletContainer;
import com.acmedcare.nas.ftp.server.ftpletcontainer.impl.DefaultFtpletContainer;
import com.acmedcare.nas.ftp.server.listener.Listener;
import com.acmedcare.nas.ftp.server.listener.ListenerFactory;
import com.acmedcare.nas.ftp.server.message.MessageResource;
import com.acmedcare.nas.ftp.server.message.MessageResourceFactory;
import com.acmedcare.nas.ftp.server.usermanager.PropertiesUserManagerFactory;
import com.acmedcare.nas.ftp.server.usermanager.impl.BaseUser;
import com.acmedcare.nas.ftp.server.usermanager.impl.ConcurrentLoginPermission;
import com.acmedcare.nas.ftp.server.usermanager.impl.TransferRatePermission;
import com.acmedcare.nas.ftp.server.usermanager.impl.WritePermission;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <p>FTP server configuration implementation. It holds all the components used.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class DefaultFtpServerContext implements FtpServerContext {

  private static final List<Authority> ADMIN_AUTHORITIES = new ArrayList<Authority>();
  private static final List<Authority> ANON_AUTHORITIES = new ArrayList<Authority>();

  static {
    ADMIN_AUTHORITIES.add(new WritePermission());

    ANON_AUTHORITIES.add(new ConcurrentLoginPermission(20, 2));
    ANON_AUTHORITIES.add(new TransferRatePermission(4800, 4800));
  }

  private final Logger LOG = LoggerFactory.getLogger(DefaultFtpServerContext.class);
  private MessageResource messageResource = new MessageResourceFactory().createMessageResource();
  private UserManager userManager = new PropertiesUserManagerFactory().createUserManager();
  private FileSystemFactory fileSystemManager = new NativeFileSystemFactory();
  private FtpletContainer ftpletContainer = new DefaultFtpletContainer();
  private FtpStatistics statistics = new DefaultFtpStatistics();
  private CommandFactory commandFactory = new CommandFactoryFactory().createCommandFactory();
  private ConnectionConfig connectionConfig =
      new ConnectionConfigFactory().createConnectionConfig();
  private Map<String, Listener> listeners = new HashMap<String, Listener>();
  /** The thread pool executor to be used by the server using this context */
  private ThreadPoolExecutor threadPoolExecutor = null;

  public DefaultFtpServerContext() {
    // create the default listener
    listeners.put("default", new ListenerFactory().createListener());
  }

  /** Create default users. */
  public void createDefaultUsers() throws Exception {
    UserManager userManager = getUserManager();

    // create admin user
    String adminName = userManager.getAdminName();
    if (!userManager.doesExist(adminName)) {
      LOG.info("Creating user : " + adminName);
      BaseUser adminUser = new BaseUser();
      adminUser.setName(adminName);
      adminUser.setPassword(adminName);
      adminUser.setEnabled(true);

      adminUser.setAuthorities(ADMIN_AUTHORITIES);

      adminUser.setHomeDirectory("./res/home");
      adminUser.setMaxIdleTime(0);
      userManager.save(adminUser);
    }

    // create anonymous user
    if (!userManager.doesExist("anonymous")) {
      LOG.info("Creating user : anonymous");
      BaseUser anonUser = new BaseUser();
      anonUser.setName("anonymous");
      anonUser.setPassword("");

      anonUser.setAuthorities(ANON_AUTHORITIES);

      anonUser.setEnabled(true);

      anonUser.setHomeDirectory("./res/home");
      anonUser.setMaxIdleTime(300);
      userManager.save(anonUser);
    }
  }

  /** Get user manager. */
  @Override
  public UserManager getUserManager() {
    return userManager;
  }

  public void setUserManager(UserManager userManager) {
    this.userManager = userManager;
  }

  /** Get file system manager. */
  @Override
  public FileSystemFactory getFileSystemManager() {
    return fileSystemManager;
  }

  public void setFileSystemManager(FileSystemFactory fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  /** Get message resource. */
  @Override
  public MessageResource getMessageResource() {
    return messageResource;
  }

  public void setMessageResource(MessageResource messageResource) {
    this.messageResource = messageResource;
  }

  /** Get ftp statistics. */
  @Override
  public FtpStatistics getFtpStatistics() {
    return statistics;
  }

  public void setFtpStatistics(FtpStatistics statistics) {
    this.statistics = statistics;
  }

  /** Get ftplet handler. */
  @Override
  public FtpletContainer getFtpletContainer() {
    return ftpletContainer;
  }

  public void setFtpletContainer(FtpletContainer ftpletContainer) {
    this.ftpletContainer = ftpletContainer;
  }

  /** Get the command factory. */
  @Override
  public CommandFactory getCommandFactory() {
    return commandFactory;
  }

  public void setCommandFactory(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
  }

  /** Get Ftplet. */
  @Override
  public Ftplet getFtplet(String name) {
    return ftpletContainer.getFtplet(name);
  }

  /** Close all the components. */
  @Override
  public void dispose() {
    listeners.clear();
    ftpletContainer.getFtplets().clear();
    if (threadPoolExecutor != null) {
      LOG.debug("Shutting down the thread pool executor");
      threadPoolExecutor.shutdown();
      try {
        threadPoolExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
      } finally {
        // TODO: how to handle?
      }
    }
  }

  @Override
  public Listener getListener(String name) {
    return listeners.get(name);
  }

  public void setListener(String name, Listener listener) {
    listeners.put(name, listener);
  }

  @Override
  public Map<String, Listener> getListeners() {
    return listeners;
  }

  public void setListeners(Map<String, Listener> listeners) {
    this.listeners = listeners;
  }

  public void addListener(String name, Listener listener) {
    listeners.put(name, listener);
  }

  public Listener removeListener(String name) {
    return listeners.remove(name);
  }

  @Override
  public ConnectionConfig getConnectionConfig() {
    return connectionConfig;
  }

  public void setConnectionConfig(ConnectionConfig connectionConfig) {
    this.connectionConfig = connectionConfig;
  }

  @Override
  public synchronized ThreadPoolExecutor getThreadPoolExecutor() {
    if (threadPoolExecutor == null) {
      int maxThreads = connectionConfig.getMaxThreads();
      if (maxThreads < 1) {
        int maxLogins = connectionConfig.getMaxLogins();
        if (maxLogins > 0) {
          maxThreads = maxLogins;
        } else {
          maxThreads = 16;
        }
      }
      LOG.debug("Intializing shared thread pool executor with max threads of {}", maxThreads);
      threadPoolExecutor = new OrderedThreadPoolExecutor(maxThreads);
    }
    return threadPoolExecutor;
  }
}
