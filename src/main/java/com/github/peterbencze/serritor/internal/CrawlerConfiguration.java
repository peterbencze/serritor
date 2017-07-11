/* 
 * Copyright 2017 Peter Bencze.
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

import com.github.peterbencze.serritor.api.CrawlRequest;
import com.github.peterbencze.serritor.api.CrawlRequest.CrawlRequestBuilder;
import com.github.peterbencze.serritor.api.CrawlingStrategy;
import java.io.Serializable;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Provides an interface to configure the crawler.
 *
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public final class CrawlerConfiguration implements Serializable {

    private final List<CrawlRequest> crawlSeeds;

    private transient WebDriver webDriver;
    private CrawlingStrategy crawlingStrategy;
    private boolean filterDuplicateRequests;
    private boolean filterOffsiteRequests;
    private Duration delayBetweenRequests;
    private int maxCrawlDepth;

    public CrawlerConfiguration() {
        // Default configuration
        webDriver = new HtmlUnitDriver(true);
        crawlSeeds = new ArrayList<>();
        crawlingStrategy = CrawlingStrategy.BREADTH_FIRST;
        filterDuplicateRequests = true;
        delayBetweenRequests = Duration.ZERO;
        maxCrawlDepth = 0;
    }

    /**
     * Returns the WebDriver instance used by the crawler.
     *
     * @return The WebDriver instance
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }

    /**
     * Sets the WebDriver that will be used by the crawler.
     *
     * @param webDriver A WebDriver instance
     */
    public void setWebDriver(final WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * Returns the list of crawl seeds.
     *
     * @return The list of crawl seeds
     */
    public List<CrawlRequest> getCrawlSeeds() {
        return crawlSeeds;
    }

    /**
     * Returns the list of crawl seeds.
     *
     * @return The list of crawl seeds
     *
     * @deprecated As of release 1.2, replaced by {@link #getCrawlSeeds()}
     */
    @Deprecated
    public List<CrawlRequest> getSeeds() {
        return getCrawlSeeds();
    }

    /**
     * Appends a crawl request to the list of crawl seeds.
     *
     * @param request The crawl request
     */
    public void addCrawlSeed(final CrawlRequest request) {
        crawlSeeds.add(request);
    }

    /**
     * Appends an URL to the list of crawl seeds.
     *
     * @param seed The URL
     *
     * @deprecated As of release 1.2, replaced by
     * {@link #addCrawlSeed(com.github.peterbencze.serritor.api.CrawlRequest)}
     */
    @Deprecated
    public void addSeed(final URL seed) {
        CrawlRequest newRequest = new CrawlRequestBuilder(seed).build();
        addCrawlSeed(newRequest);
    }

    /**
     * Appends an URL (given as String) to the list of crawl seeds.
     *
     * @param seed The URL given as String
     *
     * @deprecated As of release 1.2, replaced by
     * {@link #addCrawlSeed(com.github.peterbencze.serritor.api.CrawlRequest)}
     */
    @Deprecated
    public void addSeedAsString(final String seed) {
        CrawlRequest newRequest = new CrawlRequestBuilder(seed).build();
        addCrawlSeed(newRequest);
    }

    /**
     * Appends a list of crawl requests to the list of crawl seeds.
     *
     * @param requests The list of crawl requests
     */
    public void addCrawlSeeds(final List<CrawlRequest> requests) {
        crawlSeeds.addAll(requests);
    }

    /**
     * Appends a list of URLs to the list of crawl seeds.
     *
     * @param seeds The list of URLs
     *
     * @deprecated As of version 1.2, replaced by
     * {@link #addCrawlSeeds(java.util.List)}
     */
    @Deprecated
    public void addSeeds(final List<URL> seeds) {
        seeds.stream().forEach(this::addSeed);
    }

    /**
     * Appends a list of URLs (given as Strings) to the list of crawl seeds.
     *
     * @param seeds The list of URLs as Strings
     *
     * @deprecated As of version 1.2, replaced by
     * {@link #addCrawlSeeds(java.util.List)}
     */
    @Deprecated
    public void addSeedsAsStrings(final List<String> seeds) {
        seeds.stream().forEach(this::addSeedAsString);
    }

    /**
     * Returns the crawling strategy of the crawler.
     *
     * @return The crawling strategy
     */
    public CrawlingStrategy getCrawlingStrategy() {
        return crawlingStrategy;
    }

    /**
     * Sets the crawling strategy of the crawler.
     *
     * @param crawlingStrategy The crawling strategy
     */
    public void setCrawlingStrategy(final CrawlingStrategy crawlingStrategy) {
        this.crawlingStrategy = crawlingStrategy;
    }

    /**
     * Indicates if duplicate request filtering is enabled or not.
     *
     * @return True if it is enabled, false otherwise
     */
    public boolean isDuplicateRequestFilteringEnabled() {
        return filterDuplicateRequests;
    }

    /**
     * Indicates if duplicate request filtering is enabled or not.
     *
     * @return True if it is enabled, false otherwise
     *
     * @deprecated As of version 1.2, replaced by
     * {@link #isDuplicateRequestFilteringEnabled()}
     */
    @Deprecated
    public boolean getFilterDuplicateRequests() {
        return filterDuplicateRequests;
    }

    /**
     * Sets duplicate request filtering.
     *
     * @param filterDuplicateRequests True means enabled, false means disabled
     */
    public void setDuplicateRequestFiltering(final boolean filterDuplicateRequests) {
        this.filterDuplicateRequests = filterDuplicateRequests;
    }

    /**
     * Sets duplicate request filtering.
     *
     * @param filterDuplicateRequests True means enabled, false means disabled
     *
     * @deprecated As of release 1.2, replaced by
     * {@link #setDuplicateRequestFiltering(boolean)}
     */
    @Deprecated
    public void setFilterDuplicateRequests(final boolean filterDuplicateRequests) {
        setDuplicateRequestFiltering(filterDuplicateRequests);
    }

    /**
     * Indicates if offsite request filtering is enabled or not.
     *
     * @return True if it is enabled, false otherwise
     */
    public boolean isOffsiteRequestFilteringEnabled() {
        return filterOffsiteRequests;
    }

    /**
     * Indicates if offsite request filtering is enabled or not.
     *
     * @return True if it is enabled, false otherwise
     *
     * @deprecated As of release 1.2, replaced by
     * {@link #isOffsiteRequestFilteringEnabled()}
     */
    @Deprecated
    public boolean getFilterOffsiteRequests() {
        return filterOffsiteRequests;
    }

    /**
     * Sets offsite request filtering.
     *
     * @param filterOffsiteRequests True means enabled, false means disabled
     */
    public void setOffsiteRequestFiltering(final boolean filterOffsiteRequests) {
        this.filterOffsiteRequests = filterOffsiteRequests;
    }

    /**
     * Sets offsite request filtering.
     *
     * @param filterOffsiteRequests True means enabled, false means disabled
     *
     * @deprecated As of release 1.2, replaced by
     * {@link #setOffsiteRequestFiltering(boolean)}
     */
    @Deprecated
    public void setFilterOffsiteRequests(final boolean filterOffsiteRequests) {
        setOffsiteRequestFiltering(filterOffsiteRequests);
    }

    /**
     * Returns the delay between each request.
     *
     * @return The delay between each request
     */
    public Duration getDelayBetweenRequests() {
        return delayBetweenRequests;
    }

    /**
     * Sets the delay between each request.
     *
     * @param delayBetweenRequests The delay between each request
     */
    public void setDelayBetweenRequests(final Duration delayBetweenRequests) {
        this.delayBetweenRequests = delayBetweenRequests;
    }

    /**
     * Returns the maximum possible crawl depth.
     *
     * @return The maximum crawl depth
     */
    public int getMaxCrawlDepth() {
        return maxCrawlDepth;
    }

    /**
     * Sets the maximum possible crawl depth.
     *
     * @param maxCrawlDepth The maximum crawl depth, zero means no limit
     */
    public void setMaxCrawlDepth(int maxCrawlDepth) {
        this.maxCrawlDepth = maxCrawlDepth;
    }
}
