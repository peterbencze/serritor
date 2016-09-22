package com.serritor.internal;

import com.serritor.api.CrawlingStrategy;
import com.serritor.internal.CrawlRequest;
import com.google.common.net.InternetDomainName;
import com.serritor.internal.CrawlRequest.CrawlRequestBuilder;
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
