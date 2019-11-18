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

package com.acmedcare.nas.ftp.server.usermanager;

import com.acmedcare.nas.ftp.server.util.EncryptUtils;
import com.acmedcare.nas.ftp.server.util.PasswordUtil;

import java.security.SecureRandom;

/**
 * Password encryptor that hashes a salt together with the password using MD5. Using a salt protects
 * against birthday attacks. The hashing is also made in iterations, making lookup attacks much
 * harder.
 *
 * <p>The algorithm is based on the principles described in
 * http://www.jasypt.org/howtoencryptuserpasswords.html
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class SaltedPasswordEncryptor implements PasswordEncryptor {

  private static final int MAX_SEED = 99999999;
  private static final int HASH_ITERATIONS = 1000;
  private SecureRandom rnd = new SecureRandom();

  private String encrypt(String password, String salt) {
    String hash = salt + password;
    for (int i = 0; i < HASH_ITERATIONS; i++) {
      hash = EncryptUtils.encryptMD5(hash);
    }
    return salt + ":" + hash;
  }

  /**
   * Encrypts the password using a salt concatenated with the password and a series of MD5 steps.
   */
  @Override
  public String encrypt(String password) {
    String seed = Integer.toString(rnd.nextInt(MAX_SEED));

    return encrypt(password, seed);
  }

  /** {@inheritDoc} */
  @Override
  public boolean matches(String passwordToCheck, String storedPassword) {
    if (storedPassword == null) {
      throw new NullPointerException("storedPassword can not be null");
    }
    if (passwordToCheck == null) {
      throw new NullPointerException("passwordToCheck can not be null");
    }

    // look for divider for hash
    int divider = storedPassword.indexOf(':');

    if (divider < 1) {
      throw new IllegalArgumentException("stored password does not contain salt");
    }

    String storedSalt = storedPassword.substring(0, divider);

    return PasswordUtil.secureCompareFast(
        encrypt(passwordToCheck, storedSalt).toLowerCase(), storedPassword.toLowerCase());
  }
}
