/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.site.uploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.google.gwt.site.uploader.model.Credentials;

public class CredentialsProvider {

  private static final Logger logger = Logger.getLogger(CredentialsProvider.class.getName());

  public Credentials readCredentialsFromFile(String credentialsFile) {
    FileInputStream inputStream = null;

    try {
      inputStream = new FileInputStream(new File(credentialsFile));
      Properties properties = new Properties();
      properties.load(inputStream);
      String username = properties.getProperty("username");
      if (username == null) {

        logger.log(Level.SEVERE, "No username found in credentials file, are you missing username=something");

        throw new RuntimeException(
            "No username found in credentials file, are you missing username=something");
      }

      String password = properties.getProperty("password");
      if (password == null) {

        logger.log(Level.SEVERE, "No password found in credentials file, are you missing password=something");

        throw new RuntimeException(
            "No password found in credentials file, are you missing password=something");
      }

      String host = properties.getProperty("host");
      if (host == null) {
        logger.log(Level.SEVERE, "No host found in credentials file, are you missing host=something");

        throw new RuntimeException(
            "No host found in credentials file, are you missing host=something");
      }

      String portString = properties.getProperty("port");
      if (portString == null) {
        logger.log(Level.SEVERE, "No port found in credentials file, are you missing port=something");

        throw new RuntimeException(
            "No port found in credentials file, are you missing port=something");
      }
      try {
        int port = Integer.parseInt(portString);
        return new Credentials(host, port, username, password);
      } catch (NumberFormatException e) {
        logger.log(Level.SEVERE, "error while parsing port", e);
        throw new RuntimeException("error while parsing port");
      }

    } catch (IOException e) {
      logger.log(Level.SEVERE, "can not load credential files", e);

      throw new RuntimeException("can not load credential files", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
  }
}
