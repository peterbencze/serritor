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

package com.github.peterbencze.serritor.internal;

import com.github.peterbencze.serritor.api.CrawlCandidate;
import com.github.peterbencze.serritor.api.PatternMatchingCallback;
import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import java.net.URI;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for {@link CustomCallbackManager}.
 */
public final class CustomCallbackManagerTest {

    private CustomCallbackManager callbackManager;
    private Consumer<PageLoadEvent> defaultPageLoadCallbackMock;
    private PageLoadEvent pageLoadEventMock;

    @Before
    public void before() {
        callbackManager = new CustomCallbackManager();

        defaultPageLoadCallbackMock = Mockito.mock(Consumer.class);
        pageLoadEventMock = Mockito.mock(PageLoadEvent.class);
    }

    @Test
    public void testCallWithNoCustomCallback() {
        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(pageLoadEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.callCustomOrDefault(PageLoadEvent.class, pageLoadEventMock,
                defaultPageLoadCallbackMock);

        Mockito.verify(defaultPageLoadCallbackMock, Mockito.times(1)).accept(pageLoadEventMock);
    }

    @Test
    public void testCallWithNoApplicableCustomCallback() {
        PatternMatchingCallback<PageLoadEvent> patternMatchingCallbackMock
                = Mockito.mock(PatternMatchingCallback.class);
        Pattern patternMock = createPatternMock(false);
        Consumer<PageLoadEvent> customPageLoadCallbackMock = Mockito.mock(Consumer.class);
        Mockito.when(patternMatchingCallbackMock.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock.getCallback())
                .thenReturn(customPageLoadCallbackMock);

        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(pageLoadEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.addCustomCallback(PageLoadEvent.class, patternMatchingCallbackMock);
        callbackManager.callCustomOrDefault(PageLoadEvent.class, pageLoadEventMock,
                defaultPageLoadCallbackMock);

        Mockito.verify(defaultPageLoadCallbackMock, Mockito.times(1)).accept(pageLoadEventMock);
        Mockito.verify(customPageLoadCallbackMock, Mockito.never()).accept(pageLoadEventMock);
    }

    @Test
    public void testCallWithSingleApplicableCustomCallback() {
        Pattern patternMock = createPatternMock(true);
        Consumer<PageLoadEvent> customPageLoadCallbackMock = Mockito.mock(Consumer.class);
        PatternMatchingCallback<PageLoadEvent> patternMatchingCallbackMock
                = Mockito.mock(PatternMatchingCallback.class);
        Mockito.when(patternMatchingCallbackMock.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock.getCallback())
                .thenReturn(customPageLoadCallbackMock);

        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(pageLoadEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.addCustomCallback(PageLoadEvent.class, patternMatchingCallbackMock);
        callbackManager.callCustomOrDefault(PageLoadEvent.class, pageLoadEventMock,
                defaultPageLoadCallbackMock);

        Mockito.verify(defaultPageLoadCallbackMock, Mockito.never()).accept(pageLoadEventMock);
        Mockito.verify(customPageLoadCallbackMock, Mockito.times(1)).accept(pageLoadEventMock);
    }

    @Test
    public void testCallWithMultipleApplicableCustomCallback() {
        Pattern patternMock = createPatternMock(true);
        Consumer<PageLoadEvent> customPageLoadCallbackMock = Mockito.mock(Consumer.class);

        PatternMatchingCallback<PageLoadEvent> patternMatchingCallbackMock1
                = Mockito.mock(PatternMatchingCallback.class);
        Mockito.when(patternMatchingCallbackMock1.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock1.getCallback())
                .thenReturn(customPageLoadCallbackMock);

        PatternMatchingCallback<PageLoadEvent> patternMatchingCallbackMock2
                = Mockito.mock(PatternMatchingCallback.class);
        Mockito.when(patternMatchingCallbackMock2.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock2.getCallback())
                .thenReturn(customPageLoadCallbackMock);

        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(pageLoadEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.addCustomCallback(PageLoadEvent.class, patternMatchingCallbackMock1);
        callbackManager.addCustomCallback(PageLoadEvent.class, patternMatchingCallbackMock2);
        callbackManager.callCustomOrDefault(PageLoadEvent.class, pageLoadEventMock,
                defaultPageLoadCallbackMock);

        Mockito.verify(defaultPageLoadCallbackMock, Mockito.never()).accept(pageLoadEventMock);
        Mockito.verify(customPageLoadCallbackMock, Mockito.times(2)).accept(pageLoadEventMock);
    }

    private static Pattern createPatternMock(final boolean shouldMatch) {
        Matcher matcherMock = Mockito.mock(Matcher.class);
        Mockito.when(matcherMock.matches()).thenReturn(shouldMatch);

        Pattern patternMock = Mockito.mock(Pattern.class);
        Mockito.when(patternMock.matcher(Mockito.anyString())).thenReturn(matcherMock);

        return patternMock;
    }

    private static CrawlCandidate createCrawlCandidateMock() {
        CrawlCandidate mockedCrawlCandidate = Mockito.mock(CrawlCandidate.class);
        Mockito.when(mockedCrawlCandidate.getRequestUrl()).thenReturn(Mockito.mock(URI.class));

        return mockedCrawlCandidate;
    }
}
