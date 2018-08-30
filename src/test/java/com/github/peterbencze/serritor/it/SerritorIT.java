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

import com.github.peterbencze.serritor.api.BaseCrawler;
import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.api.CrawlerConfiguration;
import com.github.peterbencze.serritor.api.event.NonHtmlContentEvent;
import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Integration test cases for Serritor.
 *
 * @author Peter Bencze
 */
public class SerritorIT {

    private static WireMockServer mockServer;
    private static BrowserMobProxyServer proxyServer;

    private HtmlUnitDriver htmlUnitDriver;

    @BeforeClass
    public static void beforeClass() {
        mockServer = createMockWebServer();

        proxyServer = createProxyServer(mockServer.port());
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", String.valueOf(proxyServer.getPort()));
    }

    @Before
    public void before() {
        htmlUnitDriver = createHtmlUnitDriver(proxyServer);
    }

    @Test
    public void testFileDownload() throws IOException {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.APPLICATION_OCTET_STREAM.toString())
                        .withBodyFile("test-file")));

        File destinationFile = createTempFile();

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(new CrawlRequest.CrawlRequestBuilder("http://te.st/foo").build())
                .build();

        BaseCrawler crawler = new BaseCrawler(config) {
            @Override
            protected void onNonHtmlContent(final NonHtmlContentEvent event) {
                super.onNonHtmlContent(event);

                try {
                    downloadFile(event.getCrawlCandidate().getRequestUrl(), destinationFile);
                } catch (IOException ex) {
                    Assert.fail(ex.getMessage());
                }
            }
        };
        crawler.start(htmlUnitDriver);

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));

        Assert.assertEquals(0, WireMock.findUnmatchedRequests().size());

        InputStream input = this.getClass().getResourceAsStream("/__files/test-file");
        String expected = IOUtils.toString(input, Charset.defaultCharset());
        String actual = IOUtils.toString(destinationFile.toURI(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testResumeState() throws IOException {
        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/foo"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        WireMock.givenThat(WireMock.any(WireMock.urlEqualTo("/bar"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", ContentType.TEXT_HTML.toString())));

        File destinationFile = createTempFile();

        CrawlerConfiguration config = new CrawlerConfiguration.CrawlerConfigurationBuilder()
                .addCrawlSeed(new CrawlRequest.CrawlRequestBuilder("http://te.st/foo").build())
                .addCrawlSeed(new CrawlRequest.CrawlRequestBuilder("http://te.st/bar").build())
                .build();

        BaseCrawler crawler = new BaseCrawler(config) {
            @Override
            protected void onPageLoad(final PageLoadEvent event) {
                super.onPageLoad(event);

                try {
                    saveState(new FileOutputStream(destinationFile));
                } catch (FileNotFoundException ex) {
                    Assert.fail(ex.getMessage());
                }

                stop();
            }

        };
        crawler.start(htmlUnitDriver);
        crawler.resumeState(createHtmlUnitDriver(proxyServer),
                new FileInputStream(destinationFile));

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/foo")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/foo")));

        WireMock.verify(1, WireMock.headRequestedFor(WireMock.urlEqualTo("/bar")));
        WireMock.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/bar")));

        Assert.assertEquals(0, WireMock.findUnmatchedRequests().size());
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
                .addCrawlSeed(new CrawlRequest.CrawlRequestBuilder("http://te.st/foo").build())
                .addCrawlSeed(new CrawlRequest.CrawlRequestBuilder("http://te.st/bar").build())
                .build();

        BaseCrawler crawler = new BaseCrawler(config) {
        };
        crawler.start(htmlUnitDriver);

        WireMock.verify(WireMock.headRequestedFor(WireMock.urlEqualTo("/bar"))
                .withCookie("foo", WireMock.equalTo("bar")));
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

    private static HtmlUnitDriver createHtmlUnitDriver(final BrowserMobProxyServer server) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(BrowserType.HTMLUNIT);
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability(CapabilityType.PROXY, ClientUtil.createSeleniumProxy(server));

        return new HtmlUnitDriver(capabilities);
    }

    private static File createTempFile() throws IOException {
        File tempFile = File.createTempFile("tmp", null);
        tempFile.deleteOnExit();

        return tempFile;
    }
}
