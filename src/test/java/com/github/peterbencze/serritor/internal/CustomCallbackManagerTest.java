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
import com.github.peterbencze.serritor.api.event.ResponseSuccessEvent;
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
    private Consumer<ResponseSuccessEvent> defaultResponseSuccessCallbackMock;
    private ResponseSuccessEvent responseSuccessEventMock;

    @Before
    public void before() {
        callbackManager = new CustomCallbackManager();

        defaultResponseSuccessCallbackMock = Mockito.mock(Consumer.class);
        responseSuccessEventMock = Mockito.mock(ResponseSuccessEvent.class);
    }

    @Test
    public void testCallWithNoCustomCallback() {
        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(responseSuccessEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.callCustomOrDefault(ResponseSuccessEvent.class, responseSuccessEventMock,
                defaultResponseSuccessCallbackMock);

        Mockito.verify(defaultResponseSuccessCallbackMock).accept(responseSuccessEventMock);
    }

    @Test
    public void testCallWithNoApplicableCustomCallback() {
        PatternMatchingCallback<ResponseSuccessEvent> patternMatchingCallbackMock
                = Mockito.mock(PatternMatchingCallback.class);
        Pattern patternMock = createPatternMock(false);
        Consumer<ResponseSuccessEvent> customResponseSuccessCallbackMock
                = Mockito.mock(Consumer.class);
        Mockito.when(patternMatchingCallbackMock.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock.getCallback())
                .thenReturn(customResponseSuccessCallbackMock);

        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(responseSuccessEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.addCustomCallback(ResponseSuccessEvent.class, patternMatchingCallbackMock);
        callbackManager.callCustomOrDefault(ResponseSuccessEvent.class, responseSuccessEventMock,
                defaultResponseSuccessCallbackMock);

        Mockito.verify(defaultResponseSuccessCallbackMock).accept(responseSuccessEventMock);
        Mockito.verify(customResponseSuccessCallbackMock, Mockito.never())
                .accept(responseSuccessEventMock);
    }

    @Test
    public void testCallWithSingleApplicableCustomCallback() {
        Pattern patternMock = createPatternMock(true);
        Consumer<ResponseSuccessEvent> customResponseSuccessCallbackMock
                = Mockito.mock(Consumer.class);
        PatternMatchingCallback<ResponseSuccessEvent> patternMatchingCallbackMock
                = Mockito.mock(PatternMatchingCallback.class);
        Mockito.when(patternMatchingCallbackMock.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock.getCallback())
                .thenReturn(customResponseSuccessCallbackMock);

        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(responseSuccessEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.addCustomCallback(ResponseSuccessEvent.class, patternMatchingCallbackMock);
        callbackManager.callCustomOrDefault(ResponseSuccessEvent.class, responseSuccessEventMock,
                defaultResponseSuccessCallbackMock);

        Mockito.verify(defaultResponseSuccessCallbackMock, Mockito.never())
                .accept(responseSuccessEventMock);
        Mockito.verify(customResponseSuccessCallbackMock).accept(responseSuccessEventMock);
    }

    @Test
    public void testCallWithMultipleApplicableCustomCallback() {
        Pattern patternMock = createPatternMock(true);
        Consumer<ResponseSuccessEvent> customResponseSuccessCallbackMock
                = Mockito.mock(Consumer.class);

        PatternMatchingCallback<ResponseSuccessEvent> patternMatchingCallbackMock1
                = Mockito.mock(PatternMatchingCallback.class);
        Mockito.when(patternMatchingCallbackMock1.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock1.getCallback())
                .thenReturn(customResponseSuccessCallbackMock);

        PatternMatchingCallback<ResponseSuccessEvent> patternMatchingCallbackMock2
                = Mockito.mock(PatternMatchingCallback.class);
        Mockito.when(patternMatchingCallbackMock2.getUrlPattern()).thenReturn(patternMock);
        Mockito.when(patternMatchingCallbackMock2.getCallback())
                .thenReturn(customResponseSuccessCallbackMock);

        CrawlCandidate crawlCandidateMock = createCrawlCandidateMock();
        Mockito.when(responseSuccessEventMock.getCrawlCandidate()).thenReturn(crawlCandidateMock);

        callbackManager.addCustomCallback(ResponseSuccessEvent.class, patternMatchingCallbackMock1);
        callbackManager.addCustomCallback(ResponseSuccessEvent.class, patternMatchingCallbackMock2);
        callbackManager.callCustomOrDefault(ResponseSuccessEvent.class, responseSuccessEventMock,
                defaultResponseSuccessCallbackMock);

        Mockito.verify(defaultResponseSuccessCallbackMock, Mockito.never())
                .accept(responseSuccessEventMock);
        Mockito.verify(customResponseSuccessCallbackMock, Mockito.times(2))
                .accept(responseSuccessEventMock);
    }

    private static Pattern createPatternMock(final boolean shouldMatch) {
        Matcher matcherMock = Mockito.mock(Matcher.class);
        Mockito.when(matcherMock.find()).thenReturn(shouldMatch);

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
