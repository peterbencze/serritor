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

package com.github.peterbencze.serritor.internal.event;

import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.api.PatternMatchingCallback;
import com.github.peterbencze.serritor.api.event.CrawlEvent;
import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import java.net.URI;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link EventCallbackManager}.
 *
 * @author Peter Bencze
 */
public final class EventCallbackManagerTest {

    private EventCallbackManager callbackManager;
    private Consumer<PageLoadEvent> mockedDefaultPageLoadCallback;
    private PageLoadEvent mockedPageLoadEvent;

    @Before
    public void before() {
        callbackManager = new EventCallbackManager();

        mockedDefaultPageLoadCallback = Mockito.mock(Consumer.class);
        mockedPageLoadEvent = Mockito.mock(PageLoadEvent.class);
    }

    @Test
    public void testCallWithNoCustomEventCallback() {
        callbackManager.setDefaultEventCallback(CrawlEvent.PAGE_LOAD,
                mockedDefaultPageLoadCallback);
        callbackManager.call(CrawlEvent.PAGE_LOAD, mockedPageLoadEvent);

        Mockito.verify(mockedDefaultPageLoadCallback, Mockito.times(1)).accept(mockedPageLoadEvent);
    }

    @Test
    public void testCallWithNoApplicableCustomEventCallback() {
        Consumer<PageLoadEvent> mockedCustomPageLoadCallback = Mockito.mock(Consumer.class);

        PatternMatchingCallback mockedPatternMatchingCallback
                = Mockito.mock(PatternMatchingCallback.class);

        Pattern mockedPattern = createMockedPattern(false);

        Mockito.when(mockedPatternMatchingCallback.getUrlPattern()).thenReturn(mockedPattern);
        Mockito.doReturn(mockedCustomPageLoadCallback)
                .when(mockedPatternMatchingCallback)
                .getCallback();

        CrawlCandidate mockedCrawlCandidate = createMockedCrawlCandidate();

        Mockito.when(mockedPageLoadEvent.getCrawlCandidate()).thenReturn(mockedCrawlCandidate);

        callbackManager.setDefaultEventCallback(CrawlEvent.PAGE_LOAD,
                mockedDefaultPageLoadCallback);
        callbackManager.addCustomEventCallback(CrawlEvent.PAGE_LOAD, mockedPatternMatchingCallback);
        callbackManager.call(CrawlEvent.PAGE_LOAD, mockedPageLoadEvent);

        Mockito.verify(mockedDefaultPageLoadCallback, Mockito.times(1)).accept(mockedPageLoadEvent);
        Mockito.verify(mockedCustomPageLoadCallback, Mockito.never()).accept(mockedPageLoadEvent);
    }

    @Test
    public void testCallWithSingleApplicableCustomEventCallback() {
        Consumer<PageLoadEvent> mockedCustomPageLoadCallback = Mockito.mock(Consumer.class);

        PatternMatchingCallback mockedPatternMatchingCallback
                = Mockito.mock(PatternMatchingCallback.class);

        Pattern mockedPattern = createMockedPattern(true);

        Mockito.when(mockedPatternMatchingCallback.getUrlPattern()).thenReturn(mockedPattern);
        Mockito.doReturn(mockedCustomPageLoadCallback)
                .when(mockedPatternMatchingCallback)
                .getCallback();

        CrawlCandidate mockedCrawlCandidate = createMockedCrawlCandidate();

        Mockito.when(mockedPageLoadEvent.getCrawlCandidate()).thenReturn(mockedCrawlCandidate);

        callbackManager.setDefaultEventCallback(CrawlEvent.PAGE_LOAD,
                mockedDefaultPageLoadCallback);
        callbackManager.addCustomEventCallback(CrawlEvent.PAGE_LOAD, mockedPatternMatchingCallback);
        callbackManager.call(CrawlEvent.PAGE_LOAD, mockedPageLoadEvent);

        Mockito.verify(mockedDefaultPageLoadCallback, Mockito.never()).accept(mockedPageLoadEvent);
        Mockito.verify(mockedCustomPageLoadCallback, Mockito.times(1)).accept(mockedPageLoadEvent);
    }

    @Test
    public void testCallWithMultipleApplicableCustomEventCallback() {
        Consumer<PageLoadEvent> mockedCustomPageLoadCallback = Mockito.mock(Consumer.class);

        PatternMatchingCallback mockedPatternMatchingCallback1
                = Mockito.mock(PatternMatchingCallback.class);
        PatternMatchingCallback mockedPatternMatchingCallback2
                = Mockito.mock(PatternMatchingCallback.class);

        Pattern mockedPattern = createMockedPattern(true);

        Mockito.when(mockedPatternMatchingCallback1.getUrlPattern()).thenReturn(mockedPattern);
        Mockito.doReturn(mockedCustomPageLoadCallback)
                .when(mockedPatternMatchingCallback1)
                .getCallback();

        Mockito.when(mockedPatternMatchingCallback2.getUrlPattern()).thenReturn(mockedPattern);
        Mockito.doReturn(mockedCustomPageLoadCallback)
                .when(mockedPatternMatchingCallback2)
                .getCallback();

        CrawlCandidate mockedCrawlCandidate = createMockedCrawlCandidate();

        Mockito.when(mockedPageLoadEvent.getCrawlCandidate()).thenReturn(mockedCrawlCandidate);

        callbackManager.setDefaultEventCallback(CrawlEvent.PAGE_LOAD,
                mockedDefaultPageLoadCallback);
        callbackManager.addCustomEventCallback(CrawlEvent.PAGE_LOAD,
                mockedPatternMatchingCallback1);
        callbackManager.addCustomEventCallback(CrawlEvent.PAGE_LOAD,
                mockedPatternMatchingCallback2);
        callbackManager.call(CrawlEvent.PAGE_LOAD, mockedPageLoadEvent);

        Mockito.verify(mockedDefaultPageLoadCallback, Mockito.never()).accept(mockedPageLoadEvent);
        Mockito.verify(mockedCustomPageLoadCallback, Mockito.times(2)).accept(mockedPageLoadEvent);
    }

    private static Pattern createMockedPattern(boolean shouldMatch) {
        Matcher mockedMatcher = Mockito.mock(Matcher.class);
        Mockito.when(mockedMatcher.matches()).thenReturn(shouldMatch);

        Pattern mockedPattern = Mockito.mock(Pattern.class);
        Mockito.when(mockedPattern.matcher(Mockito.anyString()))
                .thenReturn(mockedMatcher);

        return mockedPattern;
    }

    private static CrawlCandidate createMockedCrawlCandidate() {
        CrawlCandidate mockedCrawlCandidate = Mockito.mock(CrawlCandidate.class);
        Mockito.when(mockedCrawlCandidate.getRequestUrl()).thenReturn(Mockito.mock(URI.class));

        return mockedCrawlCandidate;
    }
}
