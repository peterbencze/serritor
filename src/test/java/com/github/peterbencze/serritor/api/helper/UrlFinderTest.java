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
import java.util.Arrays;
import java.util.Collections;
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

    private WebDriver webDriverMock;
    private CompleteCrawlResponse crawlResponseMock;

    private UrlFinder urlFinder;

    @Before
    public void before() {
        webDriverMock = Mockito.mock(WebDriver.class);

        crawlResponseMock = Mockito.mock(CompleteCrawlResponse.class);
        Mockito.when(crawlResponseMock.getWebDriver()).thenReturn(webDriverMock);

        urlFinder = UrlFinder.createDefault();
    }

    @Test
    public void testFindAllInResponseWhenNoWebElementMatchesTheLocator() {
        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.emptyList());

        Assert.assertTrue(urlFinder.findAllInResponse(crawlResponseMock).isEmpty());
    }

    @Test
    public void testFindAllInResponseWhenNoUrlMatchesThePattern() {
        WebElement webElementMock = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("Should not match");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.singletonList(webElementMock));

        Assert.assertTrue(urlFinder.findAllInResponse(crawlResponseMock).isEmpty());
    }

    @Test
    public void testFindAllInResponseWhenUrlMatchesThePatternButIsInvalid() {
        WebElement webElementMock = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("http://invalid..url");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.singletonList(webElementMock));

        Assert.assertTrue(urlFinder.findAllInResponse(crawlResponseMock).isEmpty());
    }

    @Test
    public void testFindAllInResponseWhenUrlsMatchThePatternAndAreValid() {
        WebElement webElementMock1 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock1.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("http://example.com");
        WebElement webElementMock2 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock2.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("https://example.com");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Arrays.asList(webElementMock1, webElementMock2));

        Assert.assertEquals(2, urlFinder.findAllInResponse(crawlResponseMock).size());
    }

    @Test
    public void testFindFirstInResponseWhenNoWebElementMatchesTheLocator() {
        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.emptyList());

        Assert.assertFalse(urlFinder.findFirstInResponse(crawlResponseMock).isPresent());
    }

    @Test
    public void testFindFirstInResponseWhenNoUrlMatchesThePattern() {
        WebElement webElementMock = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("Should not match");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.singletonList(webElementMock));

        Assert.assertFalse(urlFinder.findFirstInResponse(crawlResponseMock).isPresent());
    }

    @Test
    public void testFindFirstInResponseWhenUrlMatchesThePatternButIsInvalid() {
        WebElement webElementMock = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("http://invalid..url");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.singletonList(webElementMock));

        Assert.assertFalse(urlFinder.findFirstInResponse(crawlResponseMock).isPresent());
    }

    @Test
    public void testFindFirstInResponseWhenUrlsMatchThePatternAndAreValid() {
        WebElement webElementMock1 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock1.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("http://example.com");
        WebElement webElementMock2 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock2.getAttribute(urlFinder.getAttributeName()))
                .thenReturn("https://example.com");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Arrays.asList(webElementMock1, webElementMock2));

        Assert.assertTrue(urlFinder.findFirstInResponse(crawlResponseMock).isPresent());

        // The function returns immediately on the first match, thus the second element should
        // never be accessed
        Mockito.verify(webElementMock2, Mockito.never()).getAttribute(urlFinder.getAttributeName());
    }
}
