/*
 * Copyright 2013 Daniel Kurka
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Uploader {

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Usage Uploader <filesDir> <credentialsFile>");
			throw new IllegalArgumentException(
					"Usage Uploader <filesDir> <credentialsFile>");
		}

		String filesDir = args[0];
		System.out.println("files directory: '" + filesDir + "'");

		String credentialsFile = args[1];
		System.out.println("credentials file: '" + credentialsFile + "'");

		FileTraverser traverser = new FileTraverser(filesDir);
		List<MarkdownFile> markdownFiles = traverser.traverse(args[0]);

		Credentials credentials = readCredentialsFromFile(credentialsFile);

		String username = credentials.getUsername();
		String password = credentials.getPassword();
		String host = credentials.getHost();
		int port = credentials.getPort();
		RemoteApiOptions options = new RemoteApiOptions().server(host, port)
				.credentials(username, password);
		RemoteApiInstaller installer = new RemoteApiInstaller();

		try {
			installer.install(options);
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

			for (MarkdownFile markdownFile : markdownFiles) {
				uploadResource(ds, markdownFile);
			}
		} catch (IOException e) {
			System.out.println("can not upload files");
			throw new RuntimeException("can not upload files", e);
		} finally {
			installer.uninstall();
		}
	}

	private static Credentials readCredentialsFromFile(String credentialsFile) {
		FileInputStream inputStream = null;

		try {
			inputStream = new FileInputStream(new File(credentialsFile));
			Properties properties = new Properties();
			properties.load(inputStream);
			String username = properties.getProperty("username");
			if (username == null) {
				System.out
						.println("No username found in credentials file, are you missing username=something");
				throw new RuntimeException(
						"No username found in credentials file, are you missing username=something");
			}

			String password = properties.getProperty("password");
			if (password == null) {
				System.out
						.println("No password found in credentials file, are you missing password=something");
				throw new RuntimeException(
						"No password found in credentials file, are you missing password=something");
			}

			String host = properties.getProperty("host");
			if (host == null) {
				System.out
						.println("No host found in credentials file, are you missing host=something");
				throw new RuntimeException(
						"No host found in credentials file, are you missing host=something");
			}

			String portString = properties.getProperty("port");
			if (portString == null) {
				System.out
						.println("No port found in credentials file, are you missing port=something");
				throw new RuntimeException(
						"No port found in credentials file, are you missing port=something");
			}
			try {
				int port = Integer.parseInt(portString);
				return new Credentials(host, port, username, password);
			} catch (NumberFormatException e) {
				System.out.println("error while parsing port");
				throw new RuntimeException("error while parsing port");
			}

		} catch (IOException e) {
			System.out.println("can not load credential files");
			throw new RuntimeException("can not load credential files", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}

	}

	private static void uploadResource(DatastoreService ds,
			MarkdownFile markdownFile) throws FileNotFoundException,
			IOException {

		System.out.println("uploading file: '" + markdownFile.getPath() + "'");

		Entity entity = new Entity("DocModel", markdownFile.getPath()
				.substring(1));

		String text = null;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(markdownFile.getFile());
			if (isBinaryFile(markdownFile.getPath())) {
				byte[] byteArray = IOUtils.toByteArray(fileInputStream);
				text = Base64.encodeBase64String(byteArray);
			} else {
				text = IOUtils.toString(fileInputStream, "UTF-8");
			}

			entity.setProperty("html", new Text(text));

			ds.put(entity);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}

	private static boolean isBinaryFile(String path) {
		return path.endsWith(".png") || path.endsWith(".jpg")
				|| path.endsWith(".jpeg") || path.endsWith(".gif");
	}

}
