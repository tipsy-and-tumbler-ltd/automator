package com.triposo.automator.itunesconnect;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UploadScreenshots extends ItunesConnectTask {

  private static final int SCREENSHOT_COUNT = 5;

  public static void main(String[] args) throws Exception {
    new UploadScreenshots().run();
  }

  @Override
  public void doRun() throws Exception {
    for (Object entry : getGuides().entrySet()) {
      Map.Entry guideEntry = (Map.Entry) entry;
      String location = (String) guideEntry.getKey();
      Map guide = (Map) guideEntry.getValue();
      Integer appleId = getAppleIdOfGuide(guide);
      if (appleId != null && appleId > 0) {
        System.out.println("Processing " + location);
        try {
          uploadScreenshots(location, appleId);
        } catch (Throwable e) {
          e.printStackTrace();
          System.out.println("Error processing, skipping: " + location);
        }

        System.out.println("Done " + location);
      }
    }

    System.out.println("All done.");
  }

  private void uploadScreenshots(String location, Integer appleId)
      throws VersionMissingException, MostRecentVersionRejectedException {
    if (appleId != null && appleId > 0) {
      File directoryIPhone = new File(getProperty("ios.screenshots.iphone.dir", "/nonexisting") + "/" + location);
      File directoryIPhone4Inch = new File(getProperty("ios.screenshots.iphone-4inch.dir", "/nonexisting") + "/" + location);
      File directoryIPad = new File(getProperty("ios.screenshots.ipad.dir", "/nonexisting") + "/" + location);
      List<File> screenshotsIPhone = getGuideScreenshots(directoryIPhone);
      List<File> screenshotsIPhone4Inch = getGuideScreenshots(directoryIPhone4Inch);
      List<File> screenshotsIPad = getGuideScreenshots(directoryIPad);
      if (screenshotsIPhone.isEmpty() && screenshotsIPad.isEmpty() && screenshotsIPhone4Inch.isEmpty()) {
        // Nothing to upload.
        return;
      }
      VersionDetailsPage versionDetailsPage = getVersionDetailsPage(appleId);
      versionDetailsPage.clickEditMetadataAndUploads();

      if (screenshotsIPhone.size() == SCREENSHOT_COUNT) {
        versionDetailsPage.deleteAllIphoneScreenshots();
        for (File screenshot : screenshotsIPhone) {
          versionDetailsPage.uploadIphoneScreenshot(screenshot);
        }
        markGuideScreenshotsUploaded(directoryIPhone);
      } else {
        System.out.println("Skipping iphone because incomplete: " + directoryIPhone);
      }
      if (screenshotsIPhone4Inch.size() == SCREENSHOT_COUNT) {
        versionDetailsPage.deleteAllIphone4InchScreenshots();
        for (File screenshot : screenshotsIPhone4Inch) {
          versionDetailsPage.uploadIphone4InchScreenshot(screenshot);
        }
        markGuideScreenshotsUploaded(directoryIPhone4Inch);
      } else {
        System.out.println("Skipping iphone-4inch because incomplete: " + directoryIPhone4Inch);
      }
      if (screenshotsIPad.size() == SCREENSHOT_COUNT) {
        versionDetailsPage.deleteAllIpadScreenshots();
        for (File screenshot : screenshotsIPad) {
          versionDetailsPage.uploadIpadScreenshot(screenshot);
        }
        markGuideScreenshotsUploaded(directoryIPad);
      } else {
        System.out.println("Skipping ipad because incomplete: " + directoryIPad);
      }

      versionDetailsPage.clickSaveVersionDetails();
    }
  }

  private VersionDetailsPage getVersionDetailsPage(Integer appleId)
      throws VersionMissingException, MostRecentVersionRejectedException {
    AppSummaryPage appSummaryPage = gotoAppSummary(appleId);
    if (appSummaryPage.containsText("The most recent version of your app has been rejected")) {
      throw new MostRecentVersionRejectedException();
    }
    // This is a bit awkward. In theory it's possible to upload the screenshots
    // to the Current Version or to the New Version, but:
    // - Not yet launched apps do not have a New Version.
    // - Already launched apps cannot have the screenshots of the Current Version updated.
    // That's why if there is a New Version, we always upload there.
    if (appSummaryPage.containsText("New Version")) {
      return appSummaryPage.clickNewVersionViewDetails();
    } else if (appSummaryPage.containsText("Current Version")) {
      return appSummaryPage.clickCurrentVersionViewDetails();
    } else {
      throw new VersionMissingException("Doesn't have a current or new version: " + appleId);
    }
  }
}
