package com.serritor;

import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * Defines a factory API that enables applications to obtain specific WebDrivers.
 * 
 * @author Peter Bencze
 */
public class WebDriverFactory {
    
    private static final String chromeDriverSystemProperty = "webdriver.chrome.driver";
    private static final String firefoxDriverSystemProperty = "webdriver.gecko.driver";
    private static final String ieDriverSystemProperty = "webdriver.ie.driver";
    
    /**
     * Gets a new instance of the WebDriver specified in the configuration.
     * 
     * @param config The configuration of the crawler
     * @return An instance of the specified WebDriver
     */
    public static WebDriver getDriver(CrawlerConfiguration config) {
        Map<String, Object> desiredCapabilities = config.getDesiredCapabilities();
        Capabilities capabilities = new DesiredCapabilities(desiredCapabilities);
        
        switch (config.getCrawlerDriver()) {
            case CHROME_DRIVER:
                return getChromeDriver(config.getDriverPath(), capabilities);
            case FIREFOX_DRIVER:
                return getFirefoxDriver(config.getDriverPath(), capabilities);
            case HTML_UNIT_DRIVER:
                return new HtmlUnitDriver(capabilities);
            case INTERNET_EXPLORER_DRIVER:
                return getInternetExplorerDriver(config.getDriverPath(), capabilities);
            case PHANTOMJS_DRIVER:
                return new PhantomJSDriver(capabilities);
            case REMOTE_WEB_DRIVER:
                return new RemoteWebDriver(capabilities);
            case SAFARI_DRIVER:
                return new SafariDriver(capabilities);
        }
        
        throw new IllegalArgumentException("Not supported crawler driver.");
    }
    
    /**
     * Creates a new ChromeDriver.
     * 
     * @param driverPath The path to the driver
     * @param capabilities Describes a series of key/value pairs that encapsulate aspects of a browser
     * @return A new instance of ChromeDriver
     */
    private static WebDriver getChromeDriver(String driverPath, Capabilities capabilities) {
        if (driverPath != null)
            System.setProperty(chromeDriverSystemProperty, driverPath);
        
        return new ChromeDriver(capabilities);
    }
    
    /**
     * Creates a new FirefoxDriver.
     * 
     * @param driverPath The path to the driver
     * @param capabilities Describes a series of key/value pairs that encapsulate aspects of a browser
     * @return A new instance of FirefoxDriver
     */
    private static WebDriver getFirefoxDriver(String driverPath, Capabilities capabilities) {
        if (driverPath != null)
            System.setProperty(firefoxDriverSystemProperty, driverPath);
        
        return new FirefoxDriver(capabilities);
    }
    
    /**
     * Creates a new InternetExplorerDriver.
     * 
     * @param driverPath The path to the driver
     * @param capabilities Describes a series of key/value pairs that encapsulate aspects of a browser
     * @return A new instance of InternetExplorerDriver
     */
    private static WebDriver getInternetExplorerDriver(String driverPath, Capabilities capabilities) {
        if (driverPath != null)
            System.setProperty(ieDriverSystemProperty, driverPath);
        
        return new InternetExplorerDriver(capabilities);
    }
}
