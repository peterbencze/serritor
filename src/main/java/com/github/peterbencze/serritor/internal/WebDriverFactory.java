/*
 * Copyright 2019 Peter Bencze.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.peterbencze.serritor.internal;

import com.github.peterbencze.serritor.api.Browser;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Provides preconfigured {@link WebDriver} instances.
 */
public final class WebDriverFactory {

    /**
     * Private constructor to hide the implicit public one.
     */
    private WebDriverFactory() {
    }

    /**
     * Creates the specific <code>WebDriver</code> instance with the provided properties.
     *
     * @param browser      the type of the browser
     * @param capabilities the browser properties
     *
     * @return the preconfigured <code>WebDriver</code> instance
     */
    public static WebDriver createWebDriver(final Browser browser,
                                            final Capabilities capabilities) {
        switch (browser) {
            case HTML_UNIT:
                return createHtmlUnitDriver(capabilities);
            case CHROME:
                return createChromeDriver(capabilities);
            case FIREFOX:
                return createFirefoxDriver(capabilities);
            default:
                throw new IllegalArgumentException("Unsupported browser");
        }
    }

    /**
     * Creates a <code>HtmlUnitDriver</code> instance with the provided properties.
     *
     * @param extraCapabilities the browser properties
     *
     * @return the preconfigured <code>HtmlUnitDriver</code> instance
     */
    private static HtmlUnitDriver createHtmlUnitDriver(final Capabilities extraCapabilities) {
        DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
        capabilities.merge(extraCapabilities);
        capabilities.setJavascriptEnabled(true);

        return new HtmlUnitDriver(capabilities);
    }

    /**
     * Creates a <code>ChromeDriver</code> instance with the provided properties.
     *
     * @param capabilities the browser properties
     *
     * @return the preconfigured <code>ChromeDriver</code> instance
     */
    @SuppressWarnings("unchecked")
    private static ChromeDriver createChromeDriver(final Capabilities capabilities) {
        ChromeOptions options = new ChromeOptions();
        options.merge(capabilities);

        // This should have been implemented in ChromeOptions, just like in FirefoxOptions...
        Object capability = capabilities.getCapability(ChromeOptions.CAPABILITY);
        if (capability instanceof Map) {
            Map<?, ?> extraOptions = (Map<?, ?>) capability;
            extraOptions.forEach((key, value) -> {
                if ("binary".equals(key)) {
                    options.setBinary((String) extraOptions.get("binary"));
                } else if ("args".equals(key)) {
                    options.addArguments((List<String>) value);
                } else if ("extensions".equals(key)) {
                    options.addEncodedExtensions((List<String>) value);
                } else {
                    options.setExperimentalOption((String) key, value);
                }
            });
        }

        return new ChromeDriver(options);
    }

    /**
     * Creates a <code>FirefoxDriver</code> instance with the provided properties.
     *
     * @param capabilities the browser properties
     *
     * @return the preconfigured <code>FirefoxDriver</code> instance
     */
    private static FirefoxDriver createFirefoxDriver(final Capabilities capabilities) {
        return new FirefoxDriver(new FirefoxOptions(capabilities));
    }
}
