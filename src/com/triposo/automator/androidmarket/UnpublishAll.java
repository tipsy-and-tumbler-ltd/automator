package com.triposo.automator.androidmarket;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnpublishAll {

  private WebDriver driver;
  private final Properties properties = new Properties();

  public static void main(String[] args) throws Exception {
    try {
      new UnpublishAll().run();
    } finally {
      System.exit(0);
    }
  }

  public void run() throws Exception {
    properties.load(new FileInputStream("local.properties"));

    Logger logger = Logger.getLogger("");
    logger.setLevel(Level.OFF);
    driver = new FirefoxDriver();
    driver.manage().window().setSize(new Dimension(1200, 1000));
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    String whatsnew = "* bug fixes";

    String versionName = "1.6.7";
    String versionCode = "127";
    String sheetName = "19";
    String folderName = sheetName + "-" + versionName;
    File apksFolder = new File("../../Dropbox/apks/" + folderName);

    driver.get("https://market.android.com/publish");
    SigninPage signinPage = new SigninPage(driver);
    signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
    driver.get("https://market.android.com/publish");
    signinPage.waitForAppListLoaded();

    Yaml yaml = new Yaml();
    Map guides = (Map) yaml.load(new FileInputStream(new File("../pipeline/config/guides.yaml")));
    for (Iterator iterator = guides.entrySet().iterator(); iterator.hasNext(); ) {
      Map.Entry entry = (Map.Entry) iterator.next();
      String location = (String) entry.getKey();
      if (location.equals("world")) {
        continue;
      }
      Map guide = (Map) entry.getValue();
      if (Boolean.TRUE.equals(guide.get("apk"))) {
        System.out.println("Processing " + location);

        try {
          unpublishAll(location, guide, apksFolder, versionCode, whatsnew);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }

  private void unpublishAll(String location, Map guide, File versionRoot, String versionCode, String recentChanges) {
    String packageName = "com.triposo.droidguide." + location.toLowerCase();
    AppEditorPage appEditorPage = gotoAppEditor(packageName);
    appEditorPage.clickUnpublish();
  }

  private AppEditorPage gotoAppEditor(String packageName) {
    String devAccountId = "06870337150021354184";
    String url = "https://market.android.com/publish/Home?dev_acc=" + devAccountId + "#AppEditorPlace:p=" + packageName;
    driver.get(url);
    if (driver.findElement(By.cssSelector("body")).getText().contains("Password")) {
      SigninPage signinPage = new SigninPage(driver);
      signinPage.signin(properties.getProperty("android.username"), properties.getProperty("android.password"));
      driver.get(url);
    }
    return new AppEditorPage(driver);
  }
}
