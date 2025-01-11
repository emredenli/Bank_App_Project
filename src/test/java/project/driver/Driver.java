package project.driver;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import project.model.ElementInfo;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.session.EdgeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.model.ElementInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;


public class Driver {
    public static WebDriver driver;
    protected static Actions actions;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    DesiredCapabilities capabilities;
    ChromeOptions chromeOptions;
    FirefoxOptions firefoxOptions;
    EdgeOptions edgeOptions;

    String browserName = "chrome";
    String selectPlatform = "win";
    String testURL = "https://catchylabs-webclient.testinium.com/";

    public static ConcurrentHashMap<String, Object> TestMap;

    public static String userDir = Paths.get("").toAbsolutePath().toString();
    public static String slash = System.getProperty("file.separator");

    public static final String DEFAULT_DIRECTORY_PATH = "elementValues";
    ConcurrentMap<String, Object> elementMapList = new ConcurrentHashMap<>();
    ConcurrentMap<String, List<ElementInfo>> elementInfoListMap;

    @BeforeScenario
    public void setUp() {
        logger.info("************************************  BeforeScenario  ************************************");
        try {
            if (StringUtils.isEmpty(System.getenv("key"))) {
                logger.info("Local cihazda " + selectPlatform + " ortamında " + browserName + " browserında test ayağa kalkacak");
                if ("win".equalsIgnoreCase(selectPlatform)) {
                    if ("chrome".equalsIgnoreCase(browserName)) {
                        driver = new ChromeDriver(chromeOptions());
                        //driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                        driver.get(testURL);
                    } else if ("firefox".equalsIgnoreCase(browserName)) {
                        driver = new FirefoxDriver(firefoxOptions());
                        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                        driver.get(testURL);
                    } else if ("edge".equalsIgnoreCase(browserName)){
                        driver = new EdgeDriver(edgeOptions());
                        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                        driver.get(testURL);
                    }
                } else if ("windows".equalsIgnoreCase(selectPlatform)) {
                    if ("chrome".equalsIgnoreCase(browserName)) {
                        driver = new ChromeDriver(chromeOptions());
                        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                    } else if ("firefox".equalsIgnoreCase(browserName)) {
                        driver = new FirefoxDriver(firefoxOptions());
                        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                    } else if ("edge".equalsIgnoreCase(selectPlatform)){
                        driver = new EdgeDriver(edgeOptions());
                        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                    }
                    actions = new Actions(driver);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        driver.manage().window().maximize();
    }

    @AfterScenario
    public void tearDown() {
        driver.quit();
    }

    public void initMap(File[] fileList) {
        elementMapList = new ConcurrentHashMap<String, Object>();
        elementInfoListMap = new ConcurrentHashMap<String, List<ElementInfo>>();
        Type elementType = new TypeToken<List<ElementInfo>>() {
        }.getType();
        Gson gson = new Gson();
        List<ElementInfo> elementInfoList = null;

        for (File file : fileList) {
            try {
                elementInfoList = gson
                        .fromJson(new FileReader(file), elementType);
                elementInfoListMap.put(file.getName(), elementInfoList);
                elementInfoList.parallelStream()
                        .forEach(elementInfo -> elementMapList.put(elementInfo.getKey(), elementInfo));
            } catch (FileNotFoundException e) {
                logger.warn("{} not found", e);
            }
        }
    }

    private void addFileList(Path path, List<File> list){

        File file = path.toFile();
        if (file.getName().endsWith(".json")){
            list.add(file);
        }
    }

    public File[] getFileList() {

        URI uri = null;
        String jsonPath = "";
        try {
            uri = new URI(this.getClass().getClassLoader().getResource(DEFAULT_DIRECTORY_PATH).getFile());
            File file = new File(uri.getPath());
            //System.out.println(file.getAbsolutePath());
            jsonPath = file.getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            // logger.error("File Directory Is Not Found! file name: {}", DEFAULT_DIRECTORY_PATH);
            throw new NullPointerException("File Directory Is Not Found! file name: " + DEFAULT_DIRECTORY_PATH);
        }
        List<File> list = new ArrayList<>();
        try {
            Files.walk(Paths.get(jsonPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> addFileList(path, list));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] fileList = list.toArray(new File[0]);
        logger.info("json uzantılı dosya sayısı: " + fileList.length);
        if (fileList.length == 0){
            throw new NullPointerException("Json uzantılı dosya bulunamadı."
                    + " Default Directory Path = " + uri.getPath());
        }
        return fileList;
    }

    public ElementInfo findElementInfoByKey(String key) {

        if(!elementMapList.containsKey(key)){
            fail(key + " adına sahip element bulunamadı. Lütfen kontrol ediniz.");
        }
        return (ElementInfo) elementMapList.get(key);
    }

    public void saveValue(String key, String value) {
        elementMapList.put(key, value);
    }

    public String getValue(String key) {
        return elementMapList.get(key).toString();
    }

    public ChromeOptions chromeOptions() {

        chromeOptions = new ChromeOptions();
        capabilities = DesiredCapabilities.chrome();
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        chromeOptions.setExperimentalOption("prefs", prefs);
        //chromeOptions.addArguments("--kiosk");
        chromeOptions.addArguments("--disable-notifications");
        //chromeOptions.addArguments("--start-fullscreen");
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        chromeOptions.merge(capabilities);


        return chromeOptions;
    }

    public EdgeOptions edgeOptions() {
        edgeOptions = new EdgeOptions();
        Map<String, Object> map = new HashMap<>();
        List<String> args = Arrays.asList("--start-fullscreen","--disable-blink-features=AutomationControlled"
                //,"-inprivate"
                ,"--ignore-certificate-errors");
        map.put("args", args);
        edgeOptions.setCapability("ms:edgeOptions", map);
        System.setProperty("webdriver.edge.driver","drivers/msedgedriver.exe");
        edgeOptions.merge(capabilities);
        edgeOptions.setCapability("--start-fullscreen",true);
        return edgeOptions;
    }

    public FirefoxOptions firefoxOptions() {
        firefoxOptions = new FirefoxOptions();
        capabilities = DesiredCapabilities.firefox();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        firefoxOptions.addArguments("--kiosk");
        firefoxOptions.addArguments("--disable-notifications");
        firefoxOptions.addArguments("--start-fullscreen");
        FirefoxProfile profile = new FirefoxProfile();
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        capabilities.setCapability("marionette", true);
        firefoxOptions.merge(capabilities);
        System.setProperty("webdriver.gecko.driver", "drivers/geckodriver");
        return firefoxOptions;
    }




}