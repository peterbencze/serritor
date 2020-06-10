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

package com.github.peterbencze.serritor.it;

import com.github.peterbencze.serritor.api.Browser;
import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.api.CrawlRequest.CrawlRequestBuilder;
import com.github.peterbencze.serritor.api.CrawlStats;
import com.github.peterbencze.serritor.api.Crawler;
import com.github.peterbencze.serritor.api.CrawlerConfiguration;
import com.github.peterbencze.serritor.api.event.NonHtmlResponseEvent;
import com.github.peterbencze.serritor.api.event.ResponseSuccessEvent;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.net.HttpHeaders;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Integration test cases for Serritor.
 */
public final class CrawlingIT {

    private static WireMockServer mockServer;
    private static BrowserMobProxyServer proxyServer;

    private static DesiredCapabilities capabilities;

    @BeforeClass
    public static void beforeClass() {
        mockServer = createMockWebServer();

        proxyServer = createProxyServer(mockServer.port());

        capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY,
                ClientUtil.createSeleniumProxy(proxyServer));
    }

    @Test
    public void testFileDownload() throws IOException {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM.toString())
                        .withBodyFile("test-file")));

        File destinationFile = createTempFile();

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(CrawlRequest.createDefault("http://te.st/foo"))
                .build();

        Crawler crawler = new Crawler(config) {
            @Override
            protected void onNonHtmlResponse(final NonHtmlResponseEvent event) {
                super.onNonHtmlResponse(event);

                try {
                    downloadFile(event.getCrawlCandidate().getRequestUrl(), destinationFile);
                } catch (IOException ex) {
                    Assert.fail(ex.getMessage());
                }
            }
        };
        crawler.start(Browser.HTML_UNIT, capabilities);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));

        Assert.assertEquals(0, WireMock.findUnmatchedRequests().size());

        InputStream input = this.getClass().getResourceAsStream("/__files/test-file");
        String expected = IOUtils.toString(input, Charset.defaultCharset());
        String actual = IOUtils.toString(destinationFile.toURI(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testResumeState() {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/bar"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(CrawlRequest.createDefault("http://te.st/foo"))
                .addCrawlSeed(CrawlRequest.createDefault("http://te.st/bar"))
                .build();

        Crawler crawler = new Crawler(config) {
            @Override
            protected void onResponseSuccess(final ResponseSuccessEvent event) {
                super.onResponseSuccess(event);

                stop();
            }

        };
        crawler.start(Browser.HTML_UNIT, capabilities);

        crawler = new Crawler(crawler.getState()) {
        };
        crawler.resume(Browser.HTML_UNIT, capabilities);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/bar")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/bar")));

        Assert.assertEquals(0, WireMock.findUnmatchedRequests().size());
    }

    @Test
    public void testCrawlerRestartWhenStateWasRestored() {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/bar"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(new CrawlRequestBuilder("http://te.st/foo").setPriority(1).build())
                .addCrawlSeed(CrawlRequest.createDefault("http://te.st/bar"))
                .build();
        Crawler crawler = new Crawler(config) {
            @Override
            protected void onResponseSuccess(final ResponseSuccessEvent event) {
                super.onResponseSuccess(event);

                stop();
            }

        };
        crawler.start(Browser.HTML_UNIT, capabilities);

        CrawlStats stats = crawler.getCrawlStats();
        Assert.assertThat(stats.getRemainingCrawlCandidateCount(), Matchers.is(1));
        Assert.assertThat(stats.getProcessedCrawlCandidateCount(), Matchers.is(1));
        Assert.assertThat(stats.getResponseSuccessCount(), Matchers.is(1));
        Assert.assertThat(stats.getPageLoadTimeoutCount(), Matchers.is(0));
        Assert.assertThat(stats.getRequestRedirectCount(), Matchers.is(0));
        Assert.assertThat(stats.getNonHtmlResponseCount(), Matchers.is(0));
        Assert.assertThat(stats.getResponseErrorCount(), Matchers.is(0));
        Assert.assertThat(stats.getNetworkErrorCount(), Matchers.is(0));
        Assert.assertThat(stats.getFilteredDuplicateRequestCount(), Matchers.is(0));
        Assert.assertThat(stats.getFilteredOffsiteRequestCount(), Matchers.is(0));
        Assert.assertThat(stats.getFilteredCrawlDepthLimitExceedingRequestCount(), Matchers.is(0));

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(0, WireMock.headRequestedFor(WireMock.urlEqualTo("/bar")));
        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/bar")));

        crawler = new Crawler(crawler.getState()) {
        };
        crawler.start(Browser.HTML_UNIT, capabilities);

        stats = crawler.getCrawlStats();
        Assert.assertThat(stats.getRemainingCrawlCandidateCount(), Matchers.is(0));
        Assert.assertThat(stats.getProcessedCrawlCandidateCount(), Matchers.is(2));
        Assert.assertThat(stats.getResponseSuccessCount(), Matchers.is(2));
        Assert.assertThat(stats.getPageLoadTimeoutCount(), Matchers.is(0));
        Assert.assertThat(stats.getRequestRedirectCount(), Matchers.is(0));
        Assert.assertThat(stats.getNonHtmlResponseCount(), Matchers.is(0));
        Assert.assertThat(stats.getResponseErrorCount(), Matchers.is(0));
        Assert.assertThat(stats.getNetworkErrorCount(), Matchers.is(0));
        Assert.assertThat(stats.getFilteredDuplicateRequestCount(), Matchers.is(0));
        Assert.assertThat(stats.getFilteredOffsiteRequestCount(), Matchers.is(0));
        Assert.assertThat(stats.getFilteredCrawlDepthLimitExceedingRequestCount(), Matchers.is(0));

        WireMock.verify(2, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(2, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/bar")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/bar")));

        Assert.assertThat(WireMock.findUnmatchedRequests().size(), Matchers.is(0));
    }

    @Test
    public void testHttpClientCookieSynchronization() {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.ok()
                        .withHeader("Set-Cookie", "foo=bar")
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/bar"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(new CrawlRequest.CrawlRequestBuilder("http://te.st/foo")
                        .setPriority(1)
                        .build())
                .addCrawlSeed(CrawlRequest.createDefault("http://te.st/bar"))
                .build();

        Crawler crawler = new Crawler(config) {
        };
        crawler.start(Browser.HTML_UNIT, capabilities);

        WireMock.verify(WireMock.headRequestedFor(WireMock.urlEqualTo("/bar"))
                .withCookie("foo", WireMock.equalTo("bar")));
    }

    @Test
    public void testRedirectHandling() {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.permanentRedirect("http://te.st/bar")));

        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/bar"))
                .willReturn(WireMock.ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML.toString())
                        .withBody("<script>window.location.replace('http://te.st/baz')</script>")));

        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/baz"))
                .willReturn(WireMock.ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML.toString())));

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(CrawlRequest.createDefault("http://te.st/foo"))
                .build();

        Crawler crawler = new Crawler(config) {
        };
        crawler.start(Browser.HTML_UNIT, capabilities);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/bar")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/bar")));

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/baz")));

        // Visited 2 times because of JS redirect
        WireMock.verify(2, WireMock.getRequestedFor(WireMock.urlEqualTo("/baz")));

        Assert.assertEquals(0, WireMock.findUnmatchedRequests().size());
    }

    @After
    public void after() {
        WireMock.reset();
    }

    @AfterClass
    public static void afterClass() {
        proxyServer.stop();
        mockServer.stop();
    }

    private static WireMockServer createMockWebServer() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        WireMock.configureFor(server.port());

        return server;
    }

    private static BrowserMobProxyServer createProxyServer(final int mockWebServerPort) {
        BrowserMobProxyServer server = new BrowserMobProxyServer();
        server.getHostNameResolver().remapHost("te.st", "localhost");
        server.rewriteUrl("http://te.st(.*)",
                String.format("http://te.st:%d$1", mockWebServerPort));
        server.start();

        return server;
    }

    private static File createTempFile() throws IOException {
        File tempFile = File.createTempFile("tmp", null);
        tempFile.deleteOnExit();

        return tempFile;
    }
}
