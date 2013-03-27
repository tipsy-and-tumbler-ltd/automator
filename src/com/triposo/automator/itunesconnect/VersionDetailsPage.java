package com.triposo.automator.itunesconnect;

import com.triposo.automator.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Predicates.not;

class VersionDetailsPage extends Page {
  @FindBy(css = ".wrapper-topright-button input") private WebElement readyToUploadBinary;
  @FindBy(linkText = "Binary Details") private WebElement binaryDetailsLink;

  // The save button is the same in all the "lightboxes" (see below).
  @FindBy(id = "lightboxSaveButtonEnabled") WebElement saveVersionDetails;
  @FindBy(id = "lightboxSaveButtonDisabled") WebElement saveVersionDetailsDisabled;
  @FindBy(css = "img.lightboxCancelButton") WebElement cancelButton;

  // The "Edit Version Information" lightbox.
  @FindBy(css = ".metadataFieldReadonly input") WebElement version;
  @FindBy(id = "fileInput_largeAppIcon") WebElement largeAppIconFile;
  @FindBy(css = "#versionInfoLightbox .lcUploadSpinner") WebElement versionInfoUploadSpinner;

  // The "Edit Metadata and Uploads" lightbox.
  @FindBy(id = "fileInput_35InchRetinaDisplayScreenshots") WebElement iphoneScreenshotUpload;
  @FindBy(css = "#35InchRetinaDisplayScreenshots .lcUploadSpinner") WebElement iphoneUploadSpinner;
  @FindBy(id = "fileInput_iPhone5") WebElement iphone4InchScreenshotUpload;
  @FindBy(css = "#iPhone5 .lcUploadSpinner") WebElement iphone4InchUploadSpinner;
  @FindBy(id = "fileInput_iPadScreenshots") WebElement ipadScreenshotUpload;
  @FindBy(css = "#iPadScreenshots .lcUploadSpinner") WebElement ipadUploadSpinner;

  public VersionDetailsPage(WebDriver driver) {
    super(driver);
  }

  public void clickEdit(int index) {
    // Make sure it's loaded.
    sleep(1000);
    final List<WebElement> elements = driver.findElements(By.xpath("//h2/a/span[text() = 'Edit']"));
    elements.get(index).click();
    // Make sure lightbox is loaded.
    sleep(1000);
  }

  public void clickEditVersionDetails() {
    clickEdit(0);
  }

  public void clickEditMetadataAndUploads() {
    clickEdit(1);
  }

  public void clickEditAppReviewInformation() {
    clickEdit(2);
    // The Save button is initially disabled.
    wait(saveVersionDetailsDisabled).until(isDisplayed());
    sleep(1000);
  }

  public void deleteAllIphoneScreenshots() {
    deleteScreenshots("35InchRetinaDisplayScreenshots");
  }

  public void deleteAllIphone4InchScreenshots() {
    deleteScreenshots("iPhone5");
  }

  public void deleteAllIpadScreenshots() {
    deleteScreenshots("iPadScreenshots");
  }

  private void deleteScreenshots(String containerId) {
    driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    deleteScreenshot(containerId, "906");
    deleteScreenshot(containerId, "905");
    deleteScreenshot(containerId, "904");
    deleteScreenshot(containerId, "903");
    deleteScreenshot(containerId, "902");
    deleteScreenshot(containerId, "901");
    deleteScreenshot(containerId, "900");
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  private void deleteScreenshot(String containerId, String imageId) {
    try {
      WebElement delete = driver.findElement(
          By.cssSelector("#lcUploaderImageContainer_" + containerId + "_" + imageId + " .lcUploaderImageDelete"));
      delete.click();
      sleep(1000);
    } catch (NoSuchElementException e) {
      // No such image I guess...
    }
  }

  public void uploadIphoneScreenshot(File file) {
    uploadImageAndWaitForSpinner(file, iphoneScreenshotUpload, iphoneUploadSpinner);
  }

  public void uploadIphone4InchScreenshot(File file) {
    uploadImageAndWaitForSpinner(file, iphone4InchScreenshotUpload, iphone4InchUploadSpinner);
  }

  public void uploadIpadScreenshot(File file) {
    uploadImageAndWaitForSpinner(file, ipadScreenshotUpload, ipadUploadSpinner);
  }

  public void uploadLargeIcon(File file) {
    uploadImageAndWaitForSpinner(file, largeAppIconFile, versionInfoUploadSpinner);
  }

  private void uploadImageAndWaitForSpinner(File file, WebElement uploadButton, WebElement spinner) {
    uploadButton.sendKeys(file.getAbsolutePath());
    try {
      wait(spinner).until(isDisplayed());
    } catch (Exception e) {
      // No biggie.
    }
    wait(spinner).until(not(isDisplayed()));
  }

  public void changeVersionNumber(String version) {
    clickEditVersionDetails();
    wait(this.version).until(isDisplayed());
    this.version.clear();
    this.version.sendKeys(version);
    clickSaveVersionDetails();
  }

  public void changeAppReviewInformation(
      String firstName, String lastName, String emailAddress, String phoneNumber) {
    clickEditAppReviewInformation();
    List<WebElement> elements = driver.findElements(By.cssSelector("#reviewInfoUpdateContainer > div > div > span.metadataFieldReadonly > input"));
    WebElement appReviewFirstName = elements.get(0);
    appReviewFirstName.clear();
    appReviewFirstName.sendKeys(firstName);
    WebElement appReviewLastName = elements.get(1);
    appReviewLastName.clear();
    appReviewLastName.sendKeys(lastName);
    WebElement appReviewEmailAddress = elements.get(2);
    appReviewEmailAddress.clear();
    appReviewEmailAddress.sendKeys(emailAddress);
    WebElement appReviewPhoneNumber = elements.get(3);
    appReviewPhoneNumber.clear();
    appReviewPhoneNumber.sendKeys(phoneNumber);
    sleep(500);
    try {
      clickSaveVersionDetails();
    } catch (ElementNotVisibleException e) {
      // Happens if the values did not change!
      cancelButton.click();
      wait(cancelButton).until(isHidden());
    }
  }

  public void changeMetadata(String whatsnew, String keywords) {
    clickEditMetadataAndUploads();
    List<WebElement> textareas = driver.findElements(By.cssSelector("#localizationLightbox textarea"));
//    WebElement descriptionElement = textareas.get(0);
    WebElement whatsnewElement = textareas.get(1);
    whatsnewElement.clear();
    whatsnewElement.sendKeys(whatsnew);

    List<WebElement> inputs = driver.findElements(By.cssSelector("#localizationLightbox input"));
    WebElement keywordsElement = inputs.get(1);
    keywordsElement.clear();
    keywordsElement.sendKeys(keywords);
    WebElement supportUrlElement = inputs.get(2);
    supportUrlElement.clear();
    supportUrlElement.sendKeys("http://www.triposo.com");
    sleep(500);
    try {
      clickSaveVersionDetails();
    } catch (ElementNotVisibleException e) {
      // Happens if the values did not change!
      cancelButton.click();
      wait(cancelButton).until(isHidden());
    }
  }

  public void clickSaveVersionDetails() {
    saveVersionDetails.click();
    wait(saveVersionDetails).until(isHidden());
    sleep(1000);
  }

  public LegalIssuesPage clickReadyToUploadBinary() {
    readyToUploadBinary.click();
    return new LegalIssuesPage(driver);
  }

  public BinaryDetailsPage clickBinaryDetails() {
    binaryDetailsLink.click();
    return new BinaryDetailsPage(driver);
  }
}
