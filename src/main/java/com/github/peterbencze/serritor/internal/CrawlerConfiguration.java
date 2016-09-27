/* 
 * Copyright 2016 Peter Bencze.
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

import com.github.peterbencze.serritor.api.CrawlingStrategy;
import com.google.common.net.InternetDomainName;
import com.github.peterbencze.serritor.internal.CrawlRequest.CrawlRequestBuilder;
import java.io.Serializable;
import java.net.MalformedURLException;
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

    private final List<CrawlRequest> seeds;

    private transient WebDriver webDriver;
    private CrawlingStrategy crawlingStrategy;
    private boolean filterDuplicateRequests;
    private boolean filterOffsiteRequests;
    private Duration delayBetweenRequests;

    public CrawlerConfiguration() {
        webDriver = new HtmlUnitDriver(true);
        seeds = new ArrayList<>();
        crawlingStrategy = CrawlingStrategy.BREADTH_FIRST;
        filterDuplicateRequests = true;
        delayBetweenRequests = Duration.ZERO;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public List<CrawlRequest> getSeeds() {
        return seeds;
    }

    public void addSeed(URL seed) {
        try {
            String topPrivateDomain = InternetDomainName.from(seed.getHost())
                    .topPrivateDomain()
                    .toString();

            CrawlRequest newCrawlRequest = new CrawlRequestBuilder()
                    .setRequestUrl(seed)
                    .setTopPrivateDomain(topPrivateDomain)
                    .build();

            seeds.add(newCrawlRequest);
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public void addSeedAsString(String seed) {
        try {
            addSeed(new URL(seed));
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public void addSeeds(List<URL> seeds) {
        seeds.stream().forEach(this::addSeed);
    }

    public void addSeedsAsStrings(List<String> seeds) {
        seeds.stream().forEach(this::addSeedAsString);
    }

    public CrawlingStrategy getCrawlingStrategy() {
        return crawlingStrategy;
    }

    public void setCrawlingStrategy(CrawlingStrategy crawlingStrategy) {
        this.crawlingStrategy = crawlingStrategy;
    }

    public boolean getFilterDuplicateRequests() {
        return filterDuplicateRequests;
    }

    public void setFilterDuplicateRequests(boolean filterDuplicateRequests) {
        this.filterDuplicateRequests = filterDuplicateRequests;
    }

    public boolean getFilterOffsiteRequests() {
        return filterOffsiteRequests;
    }

    public void setFilterOffsiteRequests(boolean filterOffsiteRequests) {
        this.filterOffsiteRequests = filterOffsiteRequests;
    }

    public Duration getDelayBetweenRequests() {
        return delayBetweenRequests;
    }

    public void setDelayBetweenRequests(Duration delayBetweenRequests) {
        this.delayBetweenRequests = delayBetweenRequests;
    }
}
