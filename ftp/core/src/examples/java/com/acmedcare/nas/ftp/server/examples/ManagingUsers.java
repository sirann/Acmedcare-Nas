package com.acmedcare.nas.ftp.server.examples;

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

import com.acmedcare.nas.ftp.server.ftplet.Authority;
import com.acmedcare.nas.ftp.server.ftplet.User;
import com.acmedcare.nas.ftp.server.ftplet.UserManager;
import com.acmedcare.nas.ftp.server.usermanager.PropertiesUserManagerFactory;
import com.acmedcare.nas.ftp.server.usermanager.SaltedPasswordEncryptor;
import com.acmedcare.nas.ftp.server.usermanager.UserFactory;
import com.acmedcare.nas.ftp.server.usermanager.impl.WritePermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>* */
public class ManagingUsers {

  public static void main(String[] args) throws Exception {
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    userManagerFactory.setFile(new File(System.getProperty("user.dir"),"ftp/nas-users.properties"));
    userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
    UserManager um = userManagerFactory.createUserManager();

    String dir = System.getProperty("user.dir") + "/ftp/local.dir";

    UserFactory userFact = new UserFactory();
    userFact.setName("admin");
    userFact.setPassword("123456");
    userFact.setHomeDirectory(dir);

    List<Authority> authorities = new ArrayList<>();
    authorities.add(new WritePermission());
    userFact.setAuthorities(authorities);

    User user = userFact.createUser();

    um.save(user);
  }
}
