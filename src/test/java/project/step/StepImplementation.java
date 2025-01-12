package project.step;

import org.apache.xpath.operations.Bool;
import project.driver.Driver;
import project.methods.Methods;
import project.model.ElementInfo;
import com.thoughtworks.gauge.Step;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.Keys;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static project.model.ElementHelper.getElementInfoToBy;

public class StepImplementation extends Driver {

    public static int DEFAULT_MAX_ITERATION_COUNT = 150;
    public static int DEFAULT_MILLISECOND_WAIT_AMOUNT = 100;

    private static String SAVED_ATTRIBUTE;

    JavascriptExecutor jsDriver;
    long pollingEveryValue;

    HashMap<String, String> map = new HashMap<>();

    Methods methods;


    public StepImplementation() {
        initMap(getFileList());
    }


    @Step("<int> saniye bekle")
    public void waitBySeconds(int seconds) {
        try {
            logger.info(seconds + " saniye bekleniyor.");
            sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("<long> milisaniye bekle")
    public void waitByMilliSeconds(long milliseconds) {
        try {
            logger.info(milliseconds + " milisaniye bekleniyor.");
            sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("Elementi bekle ve sonra tıkla <key>")
    public void checkElementExistsThenClick(String key) {
        getElementWithKeyIfExists(key);
        clickElement(key);
        logger.info(key + " elementine tıklandı.");
    }

    @Step("Elementine tıkla <key>")
    public void clickElement(String key) {
        if (!key.isEmpty()) {
            //hoverElement(findElement(key));
            clickElement(findElement(key));
            logger.info(key + " elementine tıklandı.");
        }
    }


    @Step("Element var mı kontrol et <key>")
    public WebElement getElementWithKeyIfExists(String key) {
        WebElement webElement;
        int loopCount = 0;
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            try {
                webElement = findElementWithKey(key);
                logger.info(key + " elementi bulundu.");
                return webElement;
            } catch (WebDriverException e) {
            }
            loopCount++;
            waitByMilliSeconds(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        assertFalse(Boolean.parseBoolean("Element: '" + key + "' doesn't exist."));
        return null;
    }

    @Step("<key> elementini kontrol et")
    public void checkElement(String key) {
        assertTrue(findElement(key).isDisplayed(), "Aranan element bulunamadı");
    }


    @Step("<text> textini <key> elemente yaz")
    public void ssendKeys(String text, String key) {
        if (!key.equals("")) {
            findElement(key).sendKeys(text);
            logger.info(key + " elementine " + text + " texti yazıldı.");
        }
    }

    @Step("Şuanki URL <url> değerini içeriyor mu kontrol et")
    public void checkURLContainsRepeat(String expectedURL) {
        int loopCount = 0;
        String actualURL = "";
        while (loopCount < DEFAULT_MAX_ITERATION_COUNT) {
            actualURL = driver.getCurrentUrl();

            if (actualURL != null && actualURL.contains(expectedURL)) {
                logger.info("Şuanki URL" + expectedURL + " değerini içeriyor.");
                return;
            }
            loopCount++;
            waitByMilliSeconds(DEFAULT_MILLISECOND_WAIT_AMOUNT);
        }
        Assertions.fail(
                "Actual URL doesn't match the expected." + "Expected: " + expectedURL + ", Actual: "
                        + actualURL);
    }

    @Step("<value> değerini <attribute> niteliğine <key> elementi için yaz")
    public void setElementAttribute(String value, String attributeName, String key) {
        String attributeValue = findElement(key).getAttribute(attributeName);
        findElement(key).sendKeys(attributeValue, value);
    }

    @Step("<value> değerini <attribute> niteliğine <key> elementi için JS ile yaz")
    public void setElementAttributeWithJs(String value, String attributeName, String key) {
        WebElement webElement = findElement(key);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].setAttribute('" + attributeName + "', '" + value + "')",
                webElement);
    }

    @Step("<key> elementinin value degeri <expectedValue> degerine sahip mi")
    public void checkElementAttributeWithJs(String key, String expectedValue) {
        WebElement webElement = findElement(key);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object objectValue = js.executeScript("return arguments[0].value", webElement);
        assertNotNull(objectValue);
        logger.info(key + " elementinin value degeri " + expectedValue + " degerine sahip");
        String value = objectValue.toString();
    }

    @Step("<key> elementinin text alanını temizle")
    public void clearInputArea(String key) {
        findElement(key).clear();
        logger.info(key + " elementinin text alani temizlendi.");
    }

    @Step("<key> elementinin text alanını BACKSPACE ile temizle")
    public void clearInputAreaWithBackspace(String key) {
        WebElement element = findElement(key);
        element.clear();
        element.sendKeys("a");
        actions.sendKeys(Keys.BACK_SPACE).build().perform();
        logger.info(key + " elementinin text alani BACKSPACE ile temizlendi.");
    }

    @Step("<attribute> niteliğini sakla <key> elementi için")
    public void saveAttributeValueOfElement(String attribute, String key) {
        SAVED_ATTRIBUTE = findElement(key).getAttribute(attribute);
        logger.info(key + " elementi icin kaydedilen attribute degeri: " + SAVED_ATTRIBUTE);
        System.out.println("Saved attribute value is: " + SAVED_ATTRIBUTE);
    }

    @Step("Kaydedilmiş niteliği <key> elementine yaz")
    public void writeSavedAttributeToElement(String key) {
        findElement(key).sendKeys(SAVED_ATTRIBUTE);
        logger.info(key + "elementine kaydedilmis attribute degeri yazildi");
    }

    @Step("<key> elementi <text> değerini içeriyor mu kontrol et")
    public void checkElementContainsText(String key, String expectedText) {
        Boolean containsText = findElement(key).getText().contains(expectedText);
        assertTrue(containsText, "elementin degeri bulunamadi");
        logger.info(key + " elementi " + expectedText + " degerini iceriyor.");
    }

    @Step("<key> elementinin <attribute> attribute değerini <mapKey> keyinde tut")
    public void getElementAttributeAndSave(String key, String attribute, String mapKey) {

        String value = getAttribute(key, attribute);
        logger.info(value);
        map.put(mapKey, value);
    }

    @Step("<key> elementinin text değerini <mapKey> keyinde tut")
    public void getElementTextAndSave(String key, String mapKey) {

        String value = getText(key);
        logger.info(value);
        map.put(mapKey, value);
    }

    @Step("<key> elementinin <attribute> niteliği <expectedValue> değerine eşit mi")
    public void checkElementAttribute(String key, String attribute, String expectedValue) {

        expectedValue = expectedValue.endsWith("KeyValue") ? map.get(expectedValue).toString() : expectedValue;
        String attributeValue = getAttribute(key, attribute);
        assertNotNull(attributeValue, "Elementin degeri bulunamadi");
        System.out.println("expectedValue: " + expectedValue);
        System.out.println("actualValue: " + attributeValue);
        assertEquals(expectedValue, attributeValue, "Elementin degeri eslesmedi");
    }

    @Step("<key> elementinin <attribute> niteliği <expectedValue> değerini içeriyor mu")
    public void checkElementAttributeContains(String key, String attribute, String expectedValue) {

        expectedValue = expectedValue.endsWith("KeyValue") ? Driver.TestMap.get(expectedValue).toString() : expectedValue;
        String attributeValue = getAttribute(key, attribute);
        assertNotNull(attributeValue, "Elementin degeri bulunamadi");
        System.out.println("expectedValue: " + expectedValue);
        System.out.println("actualValue: " + attributeValue);
        assertTrue(attributeValue.contains(expectedValue)
                , expectedValue + " elementin degeriyle " + attributeValue + " eslesmedi");
    }

    @Step("<key> elementin text degeriyle, <mapKey> elementinin text degerini iceriyor mu")
    public void checkElementsText ( String key, String mapKey ) {

        String keyText = findElement(key).getText();
        String mapKeyText = map.get(mapKey);
        System.out.println("expectedValue : " + keyText);
        System.out.println("actualValue : " + mapKeyText);
        assertEquals(keyText,mapKeyText, "text degerleri birbirine esit degil!");
        System.out.println("Text degerleri esit");

    }

    @Step("Sayfayı yenile")
    public void refreshPage() {
        driver.navigate().refresh();
    }

    @Step("<key> elementinin text değeri <expectedText> değerini içeriyor mu")
    public void controlElementTextContain(String key, String expectedText) {

        expectedText = expectedText.endsWith("KeyValue") ? map.get(expectedText).toString() : expectedText;
        //methods.waitByMilliSeconds(250);
        String actualText = findElement(key).getText()
                .replace("\r", "").replace("\n", "").trim();
        System.out.println("Beklenen text: " + expectedText);
        System.out.println("Alınan text: " + actualText);
        assertTrue(actualText.contains(expectedText), "Text degerleri esit degil");
        System.out.println("Text degerleri esit");
    }

    @Step("<money> Hesaba gelen/gonderilen para miktari kontrol edilir")
    public void myAccountAmountCalculate(String money) {

        try{
            //First My Account Amount Text
            String firstMyAccountAmountStr = map.get("FirstMyAccountAmountText");
            firstMyAccountAmountStr = replaceFormat(firstMyAccountAmountStr, ",","");
            System.out.println(firstMyAccountAmountStr);
            Double firstMyAccountAmountDouble = Double.parseDouble(firstMyAccountAmountStr);

            //Amount Text
            String amountValueStr = map.get("AmountValueText");
            amountValueStr = replaceFormat(amountValueStr, ",","");
            System.out.println(amountValueStr);
            Double amountValueDouble = Double.parseDouble(amountValueStr);

            //First My Account Amount Text
            String secondMyAccountAmountStr = map.get("SecondMyAccountAmountText");
            secondMyAccountAmountStr = replaceFormat(secondMyAccountAmountStr, ",","");
            System.out.println(secondMyAccountAmountStr);
            Double secondMyAccountAmountDouble = Double.parseDouble(secondMyAccountAmountStr);

            Double totalAmount = null;
            if( money.equals("transferMoney") ) {
                totalAmount = ( firstMyAccountAmountDouble ) - ( amountValueDouble );
                System.out.println("firstMyAccountAmountDouble : " + firstMyAccountAmountDouble + "\n" + "Gelen/Gonderilen Amount : " + amountValueDouble);
                System.out.println("Total Amount :" + ( totalAmount ));
            } else if ( money.equals("addMoney") ) {
                totalAmount = ( firstMyAccountAmountDouble ) + ( amountValueDouble );
                System.out.println("firstMyAccountAmountDouble : " + firstMyAccountAmountDouble + "\n" + "Gelen/Gonderilen Amount : " + amountValueDouble);
                System.out.println("Total Amount :" + ( totalAmount ));
            } else {
                System.out.println("Hatali tip!");
            }

            assertEquals(totalAmount, secondMyAccountAmountDouble, "Hesaba gelen/gonderilen para miktari hatali!");
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz format");
        }

    }

    public void resetAccountName() {
        if(  (findElement("MyAccountAccountNameText").getText()).equals("Emre Denli")  ) {
            System.out.println("Account Name : " + findElement("MyAccountAccountNameText").getText());
        } else {
            //Boolean blnAccountName = false;
            for( int i = 0 ; i < 3 ; i++ ) {
                clickToElementWithJavaScript("EditAccountBtn");
                checkElementExist("EAEditAccountText");
                sendKeysByKey("EAAccountNameTextBox", "Emre Denli");
                waitByMilliSeconds(250);
                clickToElementWithJavaScript("EAUpdateBtn");
                waitByMilliSeconds(250);
                if ((findElement("MyAccountAccountNameText").getText()).equals("Emre Denli"))
                    break;
                //blnAccountName = (findElement("MyAccountAccountNameText").getText()).equals("Emre Denli");
            }
        }
    }

    public void resetMoney(){

        for(int x = 0 ; x < 5 ; x++) {
            String amountValueStr = replaceFormat((findElement("MyAccountAmountText").getText()), ",","");
            System.out.println(amountValueStr);
            Double amountValueDouble = Double.parseDouble(amountValueStr);
            String amnt = "100000.00";

            if( amountValueStr.equals(amnt)  ) {
                System.out.println("Amount : " + (findElement("MyAccountAmountText").getText()));
                break;
            } else {
                for( int y = 0 ; y < 3 ; y++ ){
                    if ( amountValueDouble > 100000.00 ) {
                        Double transferMoney = (amountValueDouble - 100000.00) - 100;
                        DecimalFormat df = new DecimalFormat("#.##"); // Up to 2 decimal places
                        String formattedTMoney = df.format(transferMoney);
                        System.out.println("formattedTMoney : " + formattedTMoney);
                        formattedTMoney = replaceFormat(formattedTMoney, ",",".");
                        clickToElementWithJavaScript("TransferMoneyBtn");
                        selectDropDown("TMSenderAccountDropdown","Emre Denli");
                        selectDropDown("TMSenderReceiverDropdown","Testinium-4");
                        chromeAlert();
                        sendKeysByKey("TMAmountTextBox",formattedTMoney);
                        clickToElementWithJavaScript("TMSendBtn");
                        waitByMilliSeconds(2000);
                        break;

                    } else if ( 0 < amountValueDouble & amountValueDouble < 100000.00 ) {
                        Double addMoney = (100000.00 - amountValueDouble)/3;
                        DecimalFormat df = new DecimalFormat("#.##"); // Up to 2 decimal places
                        String formattedAMoney = df.format(addMoney);
                        System.out.println("formattedTMoney : " + formattedAMoney);
                        formattedAMoney = replaceFormat(formattedAMoney, ",",".");
                        clickToElementWithJavaScript("AddMoneyBtn");
                        clickToElementWithJavaScript("AMAddBtn");
                        waitByMilliSeconds(250);
                        sendKeysByKey("AMCardNumberTextBox","1234123412341234");
                        sendKeysByKey("AMCardHolderTextBox","Deneme");
                        sendKeysByKey("AMExpiryDateTextBox","10/26");
                        sendKeysByKey("AMCVVTextBox","110");
                        sendKeysByKey("AMAmountTextBox",formattedAMoney);
                        clickElement("AMAddBtn");
                        waitByMilliSeconds(2000);
                        break;

                    }
                    else if ( amountValueDouble < 0 ) {
                        Double addMoney = (-(amountValueDouble))/3 + 100000.00;
                        DecimalFormat df = new DecimalFormat("#.##"); // Up to 2 decimal places
                        String formattedAMoney = df.format(addMoney);
                        System.out.println("formattedTMoney : " + formattedAMoney);
                        formattedAMoney = replaceFormat(formattedAMoney, ",",".");
                        clickToElementWithJavaScript("AddMoneyBtn");
                        clickToElementWithJavaScript("AMAddBtn");
                        waitByMilliSeconds(250);
                        sendKeysByKey("AMCardNumberTextBox","1234123412341234");
                        sendKeysByKey("AMCardHolderTextBox","Deneme");
                        sendKeysByKey("AMExpiryDateTextBox","10/26");
                        sendKeysByKey("AMCVVTextBox","110");
                        sendKeysByKey("AMAmountTextBox",formattedAMoney);
                        clickElement("AMAddBtn");
                        waitByMilliSeconds(2000);
                        break;

                    }else {
                        System.out.println("Amount : " + amountValueDouble);
                        break;
                    }
                }
            }
        }

    }

    @Step("Hesap ayarlarini sifirla")
    public void resetAccountSettings() {

        resetAccountName();
        waitByMilliSeconds(1000);
        checkElementExist("MyAccountText");
        refreshPage();
        waitByMilliSeconds(1000);
        checkElementExist("OpenMoneyTransfer");
        clickToElementWithJavaScript("OpenMoneyTransfer");
        resetMoney();
        waitByMilliSeconds(1000);
        checkElementExist("MyAccountText");
        refreshPage();
        waitByMilliSeconds(1000);
        checkElementExist("OpenMoneyTransfer");
        clickToElementWithJavaScript("OpenMoneyTransfer");
        waitByMilliSeconds(1000);

    }

    @Step("'Hesap bakiyesi sifirin altina dusuyor mu?' kontrol edilir")
    public void negativeAmountControl() {

        String amountValueStr = replaceFormat((findElement("MyAccountAmountText").getText()), ",","");
        System.out.println(amountValueStr);
        Double amountValueDouble = Double.parseDouble(amountValueStr);

        Double transferMoney = (amountValueDouble + 100);
        DecimalFormat df = new DecimalFormat("#.##"); // Up to 2 decimal places
        String formattedTMoney = df.format(transferMoney);
        System.out.println("formattedTMoney : " + formattedTMoney);
        formattedTMoney = replaceFormat(formattedTMoney, ",",".");
        //String tMoney = String.valueOf(transferMoney);
        clickToElementWithJavaScript("TransferMoneyBtn");
        selectDropDown("TMSenderAccountDropdown","Emre Denli");
        selectDropDown("TMSenderReceiverDropdown","Testinium-4");
        chromeAlert();
        sendKeysByKey("TMAmountTextBox",formattedTMoney);
        clickToElementWithJavaScript("TMSendBtn");
        waitByMilliSeconds(2000);
        Boolean bln = ((findElement("MyAccountAmountText").getText()).contains("-"));
        assertEquals(bln.equals(false),"Hesap bakiyesi sifirin altina dusemez!");
    }

    @Step("Chrome alerti Ok butonuna basilir")
    public void chromeAlert() {

        driver.switchTo().alert().accept();

    }

    public String replaceFormat(String str, String oldCh, String newCh) {
        String newStr = str.replace(oldCh, newCh);
        return newStr;
    }

    public String getText(String key){

        return findElement(key).getText();
    }

    public void enterAndSendElementIsVisible(String key, String text){
        if (isElementVisible(getBy(key),1)) {
            text = text.endsWith("KeyValue") ?
                    Driver.TestMap.get(text).toString() : text;
            sendKeys(key, text);
            waitByMilliSeconds(500);
            sendKeysWithKeys(key, "TAB");
        }
        else{
            System.out.println("...");
        }
    }

    public void sendKeysWithKeys(String key, String text){

        findElement(key).sendKeys(Keys.valueOf(text));
    }

    public void sendKeys(String key, String text){

        findElement(key).sendKeys(text);
        logger.info("Elemente " + text + " texti yazıldı.");
    }

    public boolean isElementVisible(By by, long timeout){

        try {
            setFluentWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(by));
            logger.info("true");
            return true;
        } catch (Exception e) {
            logger.info("false" + " " + e.getMessage());
            return false;
        }
    }
    public String getAttribute(String key, String attribute){

        return findElement(key).getAttribute(attribute);
    }

    private JavascriptExecutor getJSExecutor() {
        return (JavascriptExecutor) driver;
    }

    private Object executeJS(String script, boolean wait) {
        return wait ? getJSExecutor().executeScript(script, "") : getJSExecutor().executeAsyncScript(script, "");
    }

    private void scrollTo(int x, int y) {
        String script = String.format("window.scrollTo(%d, %d);", x, y);
        executeJS(script, true);
    }

    @Step({"<key> Elementine kadar kaydır",
            "Scroll to Element <key>"})
    public WebElement scrollToElementToBeVisible(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        WebElement webElement = driver.findElement(getElementInfoToBy(elementInfo));
        if (webElement != null) {
            scrollTo(webElement.getLocation().getX(), webElement.getLocation().getY() - 100);
        }
        return webElement;
    }

    @Step({"<key> alanına kaydır"})
    public void scrollToElement(String key) {
        scrollToElementToBeVisible(key);
        logger.info(key + " elementinin olduğu alana kaydırıldı");
    }

    @Step({"<key> alanına js ile kaydır"})
    public void scrollToElementWithJs(String key) {
        ElementInfo elementInfo = findElementInfoByKey(key);
        WebElement element = driver.findElement(getElementInfoToBy(elementInfo));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }



    //Zaman bilgisinin alınması
    private Long getTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return (timestamp.getTime());
    }


    @Step("<key> li elementi bul, temizle ve <text> değerini yaz")
    public void sendKeysByKey(String key, String text) {
        WebElement webElement = findElementWithKey(key);
        getElementWithKeyIfExists(key);
        webElement.clear();
        webElement.sendKeys(text);
        logger.info(key + " alanina " + text + " degeri yazildi ");
    }



    @Step("<key> elementine javascript ile tıkla")
    public void clickToElementWithJavaScript(String key) {
        WebElement element = findElement(key);
        javascriptclicker(element);
        logger.info(key + " elementine javascript ile tıklandı");
    }


    @Step("<key> elementli alana çift tıkla")
    public void doubleclick(WebElement elementLocator) {
        Actions actions = new Actions(driver);
        actions.doubleClick(elementLocator).perform();
    }

    @Step("<key> alanını javascript ile temizle")
    public void clearWithJS(String key) {
        WebElement element = findElement(key);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value ='';", element);
    }



    @Step("<key> elementine <text> değerini js ile yaz")
    public void writeToKeyWithJavaScript(String key, String text) {
        WebElement element = findElement(key);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value=arguments[1]", element, text);
        logger.info(key + " elementine " + text + " değeri js ile yazıldı.");
    }



    @Step("<variable> değişkenini <key> elementine yaz")
    public void sendKeysVariable(String variable, String key) {
        if (!key.equals("")) {
            clearInputArea(key);
            findElement(key).sendKeys(getValue(variable));
            logger.info(key + " elementine " + getValue(variable) + " texti yazıldı.");
        }
    }

    //Select elementli menuler için value ile seçim metodu
    @Step("<key> olarak comboboxtan <text> seçimini yap")
    public void selectDropDown(String key, String text) {
        Select drpCountry = new Select(findElement(key));
        drpCountry.selectByVisibleText(text);
    }


    public By getBy(String key){

        ElementInfo elementInfo = findElementInfoByKey(key);
        By by = getElementInfoToBy(elementInfo);
        logger.info(key + " elementi " + by.toString() + " by değerine sahip");
        return by;

    }
    public FluentWait<WebDriver> setFluentWait(long timeout){

        FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(driver);
        fluentWait.withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofMillis(pollingEveryValue))
                .ignoring(NoSuchElementException.class);
        return fluentWait;
    }

    public void waitPageLoadComplete(FluentWait<WebDriver> fluentWait) {

        ExpectedCondition<Boolean> expectation = driver -> jsDriver
                .executeScript("return document.readyState;").toString().equals("complete");
        try {
            fluentWait.until(expectation);
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }
    public void waitPageLoadCompleteJs() {

        waitPageLoadComplete(setFluentWait(30));
    }
    public boolean doesElementExist(By by, int loopCount){
        waitPageLoadCompleteJs();
        logger.info("Element " + by.toString() + " by değerine sahip");
        int countAgain = 0;
        int elementCount;
        while (true) {
            if (countAgain == loopCount) {
                return false;
            }
            elementCount = driver.findElements(by).size();
            if (elementCount != 0) {
                return true;
            }
            waitByMilliSeconds(250);
            countAgain++;
        }
    }
    public boolean doesElementNotExist(By by, int loopCount) {

        boolean isElementInvisible = false;
        int countAgain = 0;
        int elementCount;
        while (!isElementInvisible) {
            if (countAgain == loopCount) {
                return false;
            }
            elementCount = driver.findElements(by).size();
            if (elementCount == 0) {
                isElementInvisible = true;
            }
            waitByMilliSeconds(250);
            countAgain++;
        }
        return true;
    }
    @Step("<key> elementinin var olduğu kontrol edilir")
    public void checkElementExist(String key) {

        assertTrue(doesElementExist(getBy(key),80),"");
    }

    @Step("<key> elementinin var olmadığı kontrol edilir")
    public void checkElementNotExist(String key) {

        assertTrue(doesElementNotExist(getBy(key),80),"");
    }


    public boolean isElementInVisible(By by, long timeout){

        try {
            setFluentWait(timeout).until(ExpectedConditions.invisibilityOfElementLocated(by));
            logger.info("true");
            return true;
        } catch (Exception e) {
            logger.info("false" + " " + e.getMessage());
            return false;
        }
    }

    public void checkElementInVisibled(By by, long timeout) {

        assertTrue(isElementInVisible(by, timeout),by.toString() + " elementi gorunur");
    }

    @Step("<key> elementinin görünür olmadığı kontrol edilir <timeout>")
    public void checkElementInVisible(String key, long timeout) {

        checkElementInVisibled(getBy(key), timeout);
    }

    @Step("<key> elementinin text degerini, <mapKey> elementine yaz")
    public void hashMapPutElement ( String key, String mapKey ) {

        String keyText = getElementText(key);
        map.put(mapKey, keyText);

    }

    @Step("Ekrandaki <searchbox> inda <text> ini search et ve <key> elementinden <n> tane mi var kontrolunu yap")
    public void checkElementsCount( String searchbox, String text, String key, int n ) {

        sendKeysByKey(searchbox, text);
        waitByMilliSeconds(250);
        List<WebElement> items = driver.findElements(getBy(key));
        assertEquals(items.size(), n, "tablodaki eleman sayisi beklenen sayiya esit degil");

    }

    WebElement findElement(String key) {
        By infoParam = getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 30);
        WebElement webElement = webDriverWait
                .until(ExpectedConditions.presenceOfElementLocated(infoParam));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'})",
                webElement);
        return webElement;
    }

    List<WebElement> findElements(String key) {
        return driver.findElements(getElementInfoToBy(findElementInfoByKey(key)));
    }


    private void clickElement(WebElement element) {
        element.click();
    }

    private void clickElementBy(String key) {
        findElement(key).click();
    }

    private void hoverElement(WebElement element) {
        actions.moveToElement(element).build().perform();
    }

    private void hoverElementBy(String key) {
        WebElement webElement = findElement(key);
        actions.moveToElement(webElement).build().perform();
    }

    private void sendKeyESC(String key) {
        findElement(key).sendKeys(Keys.ESCAPE);
    }

    private boolean isDisplayed(WebElement element) {
        return element.isDisplayed();
    }

    private boolean isDisplayedBy(By by) {
        return driver.findElement(by).isDisplayed();
    }

    private String getPageSource() {
        return driver.switchTo().alert().getText();
    }

    public static String getSavedAttribute() {
        return SAVED_ATTRIBUTE;
    }


    public WebElement findElementWithKey(String key) {
        return findElement(key);
    }

    public String getElementText(String key) {
        return findElement(key).getText();
    }

    public String getElementAttributeValue(String key, String attribute) {
        return findElement(key).getAttribute(attribute);
    }

    public void javaScriptClicker(WebDriver driver, WebElement element) {
        JavascriptExecutor jse = ((JavascriptExecutor) driver);
        jse.executeScript("var evt = document.createEvent('MouseEvents');"
                + "evt.initMouseEvent('click',true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0,null);"
                + "arguments[0].dispatchEvent(evt);", element);
    }

    public void javascriptclicker(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }



}