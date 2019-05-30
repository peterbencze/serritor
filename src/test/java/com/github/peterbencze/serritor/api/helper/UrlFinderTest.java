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

import com.github.peterbencze.serritor.api.CompleteCrawlResponse;
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
 * Test cases for {@link UrlFinder}.
 */
public final class UrlFinderTest {

    private static final Pattern URL_PATTERN = Pattern.compile(".+valid-url.+");
    private static final String ATTRIBUTE = "href";
    private static final String TAG_NAME = "a";
    private static final String VALID_URL = "http://valid-url.com";
    private static final String INVALID_URL = "invalid-url";
    private static final String URL_WITH_INVALID_DOMAIN = "http://invalid.domain";

    private CompleteCrawlResponse mockedCrawlResponse;
    private UrlFinder urlFinder;

    @Before
    public void initialize() {
        WebDriver mockedDriver = Mockito.mock(WebDriver.class);

        WebElement mockedElementWithValidUrl = Mockito.mock(WebElement.class);
        Mockito.when(mockedElementWithValidUrl.getAttribute(Mockito.eq(ATTRIBUTE)))
                .thenReturn(VALID_URL);

        WebElement mockedElementWithInvalidUrlFormat = Mockito.mock(WebElement.class);
        Mockito.when(mockedElementWithInvalidUrlFormat.getAttribute(Mockito.eq(ATTRIBUTE)))
                .thenReturn(INVALID_URL);

        WebElement mockedElementWithInvalidDomain = Mockito.mock(WebElement.class);
        Mockito.when(mockedElementWithInvalidDomain.getAttribute(Mockito.eq(ATTRIBUTE)))
                .thenReturn(URL_WITH_INVALID_DOMAIN);

        List<WebElement> elementList = Arrays.asList(mockedElementWithValidUrl,
                mockedElementWithInvalidUrlFormat, mockedElementWithInvalidDomain);
        Mockito.when(mockedDriver.findElements(By.tagName(TAG_NAME)))
                .thenReturn(elementList);

        mockedCrawlResponse = Mockito.mock(CompleteCrawlResponse.class);
        Mockito.when(mockedCrawlResponse.getWebDriver()).thenReturn(mockedDriver);

        urlFinder = new UrlFinderBuilder(URL_PATTERN).build();
    }

    @Test
    public void testFindUrlsInPage() {
        Assert.assertEquals(Arrays.asList(VALID_URL),
                urlFinder.findUrlsInPage(mockedCrawlResponse));
    }
}
