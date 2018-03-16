/* 
 * Copyright 2018 Peter Bencze.
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
package com.github.peterbencze.serritor.api.helper;

import com.github.peterbencze.serritor.api.HtmlResponse;
import com.github.peterbencze.serritor.api.HtmlResponse.HtmlResponseBuilder;
import com.github.peterbencze.serritor.api.helper.UrlFinder.UrlFinderBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Test cases for <code>UrlFinder</code>.
 *
 * @author Peter Bencze
 */
public class UrlFinderTest {
    
    private static final Pattern URL_PATTERN = Pattern.compile(".+valid-url.+");
    private static final String ATTRIBUTE = "href";
    private static final String TAG_NAME = "a";
    private static final String VALID_URL = "http://valid-url.com";
    private static final String INVALID_URL = "invalid-url";
    private static final String URL_WITH_INVALID_DOMAIN = "http://invalid.domain";

    private UrlFinder urlFinder;
    private HtmlResponse mockResponse;
    private WebDriver mockDriver;
    private WebElement mockElementWithValidUrl;
    private WebElement mockElementWithInvalidUrlFormat;
    private WebElement mockElementWithInvalidDomain;

    @Before
    public void initialize() {
        urlFinder = new UrlFinderBuilder(URL_PATTERN).build();
        
        // Create mocks
        mockDriver = Mockito.mock(WebDriver.class);
        
        // Cannot mock because of the final modifier
        mockResponse = new HtmlResponseBuilder(null, 0, null).setWebDriver(mockDriver).build();
        
        mockElementWithValidUrl = Mockito.mock(WebElement.class);
        Mockito.when(mockElementWithValidUrl.getAttribute(Mockito.eq(ATTRIBUTE)))
                .thenReturn(VALID_URL);
        
        mockElementWithInvalidUrlFormat = Mockito.mock(WebElement.class);
        Mockito.when(mockElementWithInvalidUrlFormat.getAttribute(Mockito.eq(ATTRIBUTE)))
                .thenReturn(INVALID_URL);
        
        mockElementWithInvalidDomain = Mockito.mock(WebElement.class);
        Mockito.when(mockElementWithInvalidDomain.getAttribute(Mockito.eq(ATTRIBUTE)))
                .thenReturn(URL_WITH_INVALID_DOMAIN);
        
        List<WebElement> elementList = Arrays.asList(mockElementWithValidUrl, mockElementWithInvalidUrlFormat, mockElementWithInvalidDomain);         
        Mockito.when(mockDriver.findElements(By.tagName(TAG_NAME)))
                .thenReturn(elementList);
    }

    @Test
    public void findUrlsInResponseTest() {
        Assert.assertEquals(Arrays.asList(VALID_URL), urlFinder.findUrlsInResponse(mockResponse));
    }
}
