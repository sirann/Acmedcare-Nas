/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.acmedcare.nas.ftp.server.impl;

import com.acmedcare.nas.ftp.server.ftplet.FtpFile;
import com.acmedcare.nas.ftp.server.ftplet.User;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <strong>Internal class, do not use directly.</strong>
 *
 * <p>This is FTP statistics implementation.
 *
 * <p>TODO revisit concurrency, right now we're a bit over zealous with both Atomic* counters and
 * synchronization
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class DefaultFtpStatistics implements ServerFtpStatistics {

  public static final String LOGIN_NUMBER = "login_number";
  private StatisticsObserver observer = null;
  private FileObserver fileObserver = null;
  private Date startTime = new Date();
  private AtomicInteger uploadCount = new AtomicInteger(0);
  private AtomicInteger downloadCount = new AtomicInteger(0);
  private AtomicInteger deleteCount = new AtomicInteger(0);
  private AtomicInteger mkdirCount = new AtomicInteger(0);
  private AtomicInteger rmdirCount = new AtomicInteger(0);
  private AtomicInteger currLogins = new AtomicInteger(0);
  private AtomicInteger totalLogins = new AtomicInteger(0);
  private AtomicInteger totalFailedLogins = new AtomicInteger(0);
  private AtomicInteger currAnonLogins = new AtomicInteger(0);
  private AtomicInteger totalAnonLogins = new AtomicInteger(0);
  private AtomicInteger currConnections = new AtomicInteger(0);
  private AtomicInteger totalConnections = new AtomicInteger(0);
  private AtomicLong bytesUpload = new AtomicLong(0L);
  private AtomicLong bytesDownload = new AtomicLong(0L);
  /** The user login information. */
  private Map<String, UserLogins> userLoginTable = new ConcurrentHashMap<String, UserLogins>();

  /** Set the observer. */
  @Override
  public void setObserver(final StatisticsObserver observer) {
    this.observer = observer;
  }

  /** Set the file observer. */
  @Override
  public void setFileObserver(final FileObserver observer) {
    fileObserver = observer;
  }

  /** Get server start time. */
  @Override
  public Date getStartTime() {
    if (startTime != null) {
      return (Date) startTime.clone();
    } else {
      return null;
    }
  }

  // //////////////////////////////////////////////////////
  // /////////////// All getter methods /////////////////

  /** Get number of files uploaded. */
  @Override
  public int getTotalUploadNumber() {
    return uploadCount.get();
  }

  /** Get number of files downloaded. */
  @Override
  public int getTotalDownloadNumber() {
    return downloadCount.get();
  }

  /** Get number of files deleted. */
  @Override
  public int getTotalDeleteNumber() {
    return deleteCount.get();
  }

  /** Get total number of bytes uploaded. */
  @Override
  public long getTotalUploadSize() {
    return bytesUpload.get();
  }

  /** Get total number of bytes downloaded. */
  @Override
  public long getTotalDownloadSize() {
    return bytesDownload.get();
  }

  /** Get total directory created. */
  @Override
  public int getTotalDirectoryCreated() {
    return mkdirCount.get();
  }

  /** Get total directory removed. */
  @Override
  public int getTotalDirectoryRemoved() {
    return rmdirCount.get();
  }

  /** Get total number of connections. */
  @Override
  public int getTotalConnectionNumber() {
    return totalConnections.get();
  }

  /** Get current number of connections. */
  @Override
  public int getCurrentConnectionNumber() {
    return currConnections.get();
  }

  /** Get total number of logins. */
  @Override
  public int getTotalLoginNumber() {
    return totalLogins.get();
  }

  /** Get total failed login number. */
  @Override
  public int getTotalFailedLoginNumber() {
    return totalFailedLogins.get();
  }

  /** Get current number of logins. */
  @Override
  public int getCurrentLoginNumber() {
    return currLogins.get();
  }

  /** Get total number of anonymous logins. */
  @Override
  public int getTotalAnonymousLoginNumber() {
    return totalAnonLogins.get();
  }

  /** Get current number of anonymous logins. */
  @Override
  public int getCurrentAnonymousLoginNumber() {
    return currAnonLogins.get();
  }

  /** Get the login number for the specific user */
  @Override
  public synchronized int getCurrentUserLoginNumber(final User user) {
    UserLogins userLogins = userLoginTable.get(user.getName());
    if (userLogins == null) { // not found the login user's statistics info
      return 0;
    } else {
      return userLogins.totalLogins.get();
    }
  }

  /**
   * Get the login number for the specific user from the ipAddress
   *
   * @param user login user account
   * @param ipAddress the ip address of the remote user
   */
  @Override
  public synchronized int getCurrentUserLoginNumber(final User user, final InetAddress ipAddress) {
    UserLogins userLogins = userLoginTable.get(user.getName());
    if (userLogins == null) { // not found the login user's statistics info
      return 0;
    } else {
      return userLogins.loginsFromInetAddress(ipAddress).get();
    }
  }

  /** Increment upload count. */
  @Override
  public synchronized void setUpload(
      final FtpIoSession session, final FtpFile file, final long size) {
    uploadCount.incrementAndGet();
    bytesUpload.addAndGet(size);
    notifyUpload(session, file, size);
  }

  // //////////////////////////////////////////////////////
  // /////////////// All setter methods /////////////////

  /** Increment download count. */
  @Override
  public synchronized void setDownload(
      final FtpIoSession session, final FtpFile file, final long size) {
    downloadCount.incrementAndGet();
    bytesDownload.addAndGet(size);
    notifyDownload(session, file, size);
  }

  /** Increment delete count. */
  @Override
  public synchronized void setDelete(final FtpIoSession session, final FtpFile file) {
    deleteCount.incrementAndGet();
    notifyDelete(session, file);
  }

  /** Increment make directory count. */
  @Override
  public synchronized void setMkdir(final FtpIoSession session, final FtpFile file) {
    mkdirCount.incrementAndGet();
    notifyMkdir(session, file);
  }

  /** Increment remove directory count. */
  @Override
  public synchronized void setRmdir(final FtpIoSession session, final FtpFile file) {
    rmdirCount.incrementAndGet();
    notifyRmdir(session, file);
  }

  /** Increment open connection count. */
  @Override
  public synchronized void setOpenConnection(final FtpIoSession session) {
    currConnections.incrementAndGet();
    totalConnections.incrementAndGet();
    notifyOpenConnection(session);
  }

  /** Decrement open connection count. */
  @Override
  public synchronized void setCloseConnection(final FtpIoSession session) {
    if (currConnections.get() > 0) {
      currConnections.decrementAndGet();
    }
    notifyCloseConnection(session);
  }

  /** New login. */
  @Override
  public synchronized void setLogin(final FtpIoSession session) {
    currLogins.incrementAndGet();
    totalLogins.incrementAndGet();
    User user = session.getUser();
    if ("anonymous".equals(user.getName())) {
      currAnonLogins.incrementAndGet();
      totalAnonLogins.incrementAndGet();
    }

    synchronized (user) { // thread safety is needed. Since the login occurrs
      // at low frequency, this overhead is endurable
      UserLogins statisticsTable = userLoginTable.get(user.getName());
      if (statisticsTable == null) {
        // the hash table that records the login information of the user
        // and its ip address.

        InetAddress address = null;
        if (session.getRemoteAddress() instanceof InetSocketAddress) {
          address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
        }
        statisticsTable = new UserLogins(address);
        userLoginTable.put(user.getName(), statisticsTable);
      } else {
        statisticsTable.totalLogins.incrementAndGet();

        if (session.getRemoteAddress() instanceof InetSocketAddress) {
          InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
          statisticsTable.loginsFromInetAddress(address).incrementAndGet();
        }
      }
    }

    notifyLogin(session);
  }

  /** Increment failed login count. */
  @Override
  public synchronized void setLoginFail(final FtpIoSession session) {
    totalFailedLogins.incrementAndGet();
    notifyLoginFail(session);
  }

  /** User logout */
  @Override
  public synchronized void setLogout(final FtpIoSession session) {
    User user = session.getUser();
    if (user == null) {
      return;
    }

    currLogins.decrementAndGet();

    if ("anonymous".equals(user.getName())) {
      currAnonLogins.decrementAndGet();
    }

    synchronized (user) {
      UserLogins userLogins = userLoginTable.get(user.getName());

      if (userLogins != null) {
        userLogins.totalLogins.decrementAndGet();
        if (session.getRemoteAddress() instanceof InetSocketAddress) {
          InetAddress address = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
          userLogins.loginsFromInetAddress(address).decrementAndGet();
        }
      }
    }

    notifyLogout(session);
  }

  /** Observer upload notification. */
  private void notifyUpload(final FtpIoSession session, final FtpFile file, long size) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyUpload();
    }

    FileObserver fileObserver = this.fileObserver;
    if (fileObserver != null) {
      fileObserver.notifyUpload(session, file, size);
    }
  }

  // //////////////////////////////////////////////////////////
  // /////////////// all observer methods ////////////////////

  /** Observer download notification. */
  private void notifyDownload(final FtpIoSession session, final FtpFile file, final long size) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyDownload();
    }

    FileObserver fileObserver = this.fileObserver;
    if (fileObserver != null) {
      fileObserver.notifyDownload(session, file, size);
    }
  }

  /** Observer delete notification. */
  private void notifyDelete(final FtpIoSession session, final FtpFile file) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyDelete();
    }

    FileObserver fileObserver = this.fileObserver;
    if (fileObserver != null) {
      fileObserver.notifyDelete(session, file);
    }
  }

  /** Observer make directory notification. */
  private void notifyMkdir(final FtpIoSession session, final FtpFile file) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyMkdir();
    }

    FileObserver fileObserver = this.fileObserver;
    if (fileObserver != null) {
      fileObserver.notifyMkdir(session, file);
    }
  }

  /** Observer remove directory notification. */
  private void notifyRmdir(final FtpIoSession session, final FtpFile file) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyRmdir();
    }

    FileObserver fileObserver = this.fileObserver;
    if (fileObserver != null) {
      fileObserver.notifyRmdir(session, file);
    }
  }

  /** Observer open connection notification. */
  private void notifyOpenConnection(final FtpIoSession session) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyOpenConnection();
    }
  }

  /** Observer close connection notification. */
  private void notifyCloseConnection(final FtpIoSession session) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      observer.notifyCloseConnection();
    }
  }

  /** Observer login notification. */
  private void notifyLogin(final FtpIoSession session) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {

      // is anonymous login
      User user = session.getUser();
      boolean anonymous = false;
      if (user != null) {
        String login = user.getName();
        anonymous = (login != null) && login.equals("anonymous");
      }
      observer.notifyLogin(anonymous);
    }
  }

  /** Observer failed login notification. */
  private void notifyLoginFail(final FtpIoSession session) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      if (session.getRemoteAddress() instanceof InetSocketAddress) {
        observer.notifyLoginFail(((InetSocketAddress) session.getRemoteAddress()).getAddress());
      }
    }
  }

  /** Observer logout notification. */
  private void notifyLogout(final FtpIoSession session) {
    StatisticsObserver observer = this.observer;
    if (observer != null) {
      // is anonymous login
      User user = session.getUser();
      boolean anonymous = false;
      if (user != null) {
        String login = user.getName();
        anonymous = (login != null) && login.equals("anonymous");
      }
      observer.notifyLogout(anonymous);
    }
  }

  /** Reset the cumulative counters. */
  @Override
  public synchronized void resetStatisticsCounters() {
    startTime = new Date();

    uploadCount.set(0);
    downloadCount.set(0);
    deleteCount.set(0);

    mkdirCount.set(0);
    rmdirCount.set(0);

    totalLogins.set(0);
    totalFailedLogins.set(0);
    totalAnonLogins.set(0);
    totalConnections.set(0);

    bytesUpload.set(0);
    bytesDownload.set(0);
  }

  private static class UserLogins {
    public AtomicInteger totalLogins;
    private Map<InetAddress, AtomicInteger> perAddress =
        new ConcurrentHashMap<InetAddress, AtomicInteger>();

    public UserLogins(InetAddress address) {
      // init with the first connection
      totalLogins = new AtomicInteger(1);
      perAddress.put(address, new AtomicInteger(1));
    }

    public AtomicInteger loginsFromInetAddress(InetAddress address) {
      AtomicInteger logins = perAddress.get(address);
      if (logins == null) {
        logins = new AtomicInteger(0);
        perAddress.put(address, logins);
      }
      return logins;
    }
  }
}
