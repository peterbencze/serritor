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

package com.github.peterbencze.serritor.api.helper;

import com.github.peterbencze.serritor.api.CompleteCrawlResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Test cases for {@link TextFinder}.
 */
public final class TextFinderTest {

    private static final Pattern textPattern = Pattern.compile("^Should match$");

    private WebDriver webDriverMock;
    private CompleteCrawlResponse crawlResponseMock;

    private TextFinder textFinder;

    @Before
    public void before() {
        webDriverMock = Mockito.mock(WebDriver.class);

        crawlResponseMock = Mockito.mock(CompleteCrawlResponse.class);
        Mockito.when(crawlResponseMock.getWebDriver()).thenReturn(webDriverMock);

        textFinder = new TextFinder(textPattern);
    }

    @Test
    public void testFindAllInResponseWhenNoWebElementMatchesTheLocator() {
        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.emptyList());

        Assert.assertTrue(textFinder.findAllInResponse(crawlResponseMock).isEmpty());
    }

    @Test
    public void testFindAllInResponseWhenNoTextMatchesThePattern() {
        WebElement webElementMock = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock.getText()).thenReturn("Should not match");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.singletonList(webElementMock));

        Assert.assertTrue(textFinder.findAllInResponse(crawlResponseMock).isEmpty());
    }

    @Test
    public void testFindAllInResponseWhenTextMatchesThePattern() {
        WebElement webElementMock1 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock1.getText()).thenReturn("Should match");
        WebElement webElementMock2 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock2.getText()).thenReturn("Should match");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Arrays.asList(webElementMock1, webElementMock2));

        Assert.assertEquals(2, textFinder.findAllInResponse(crawlResponseMock).size());
    }

    @Test
    public void testFindFirstInResponseWhenNoWebElementMatchesTheLocator() {
        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.emptyList());

        Assert.assertFalse(textFinder.findFirstInResponse(crawlResponseMock).isPresent());
    }

    @Test
    public void testFindFirstInResponseWhenNoTextMatchesThePattern() {
        WebElement webElementMock = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock.getText()).thenReturn("Should not match");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Collections.singletonList(webElementMock));

        Assert.assertFalse(textFinder.findFirstInResponse(crawlResponseMock).isPresent());
    }

    @Test
    public void testFindFirstInResponseWhenTextMatchesThePattern() {
        WebElement webElementMock1 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock1.getText()).thenReturn("Should match");
        WebElement webElementMock2 = Mockito.mock(WebElement.class);
        Mockito.when(webElementMock2.getText()).thenReturn("Should match");

        Mockito.when(webDriverMock.findElements(Mockito.any(By.class)))
                .thenReturn(Arrays.asList(webElementMock1, webElementMock2));

        Assert.assertTrue(textFinder.findFirstInResponse(crawlResponseMock).isPresent());

        // The function returns immediately on the first match, thus the second element should
        // never be accessed
        Mockito.verify(webElementMock2, Mockito.never()).getText();
    }
}
