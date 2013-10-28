package com.google.gwt.site.uploader;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.repackaged.com.google.common.io.Files;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.google.common.base.Charsets;

import java.io.File;
import java.io.IOException;

public class SaveCredentials {

  public static void main(String[] args) {
    try {
      run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void run() throws IOException {
    if (System.console() == null) {
      System.out.println("System.console not available. Please re-run from a shell.");
      System.exit(1);
    }

    File f = new File("credentials");
    if (!f.exists()) {
      p("Expected a file named 'credentials' to exist.");
      p("It should be in gwt-site-uploader.");
      p("Please make sure you're in the right directory and touch the file if it's not there.");
      System.exit(1);
    }
    if (!f.canWrite()) {
      p("Can't write to credentials.");
      System.exit(1);
    }

    p("Please enter your gwtproject.org credentials.");

    String email = System.console().readLine("Email: ");
    char[] password = System.console().readPassword("Password: ");

    RemoteApiOptions options =
        new RemoteApiOptions().server("gwtproject-demo.appspot.com", 443).credentials(email, new String(password));
    RemoteApiInstaller installer = new RemoteApiInstaller();
    installer.install(options);
    // test the connection
    DatastoreServiceFactory.getDatastoreService().allocateIds("foo", 1);

    String creds = installer.serializeCredentials();
    Files.write(creds, f, Charsets.UTF_8);
    System.out.println("Credentials saved.");

    installer.uninstall();
  }


  private static void p(String s) {
    System.out.println(s);
  }
}
