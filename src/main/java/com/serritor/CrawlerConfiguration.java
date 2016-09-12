package com.serritor;

import com.google.common.net.InternetDomainName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an interface to configure the crawler.
  * 
 * @author Krisztian Mozsi
 * @author Peter Bencze
 */
public class CrawlerConfiguration implements Serializable {

    private static final Gson gson = new Gson();
    
    private final List<CrawlRequest> seeds;

    private CrawlerDriver crawlerDriver;
    private String driverPath;
    private transient Map<String, Object> desiredCapabilities;
    private CrawlingStrategy crawlingStrategy;
    private boolean filterDuplicateRequests;
    private boolean filterOffsiteRequests;
    private Duration delayBetweenRequests;
    

    public CrawlerConfiguration() {
        crawlerDriver = CrawlerDriver.HTML_UNIT_DRIVER;
        desiredCapabilities = new HashMap<>();    
        seeds = new ArrayList<>();
        crawlingStrategy = CrawlingStrategy.BREADTH_FIRST;
        filterDuplicateRequests = true;
        delayBetweenRequests = Duration.ZERO;
    }
    
    public CrawlerDriver getCrawlerDriver() {
        return crawlerDriver;
    }
    
    public void setCrawlerDriver(CrawlerDriver crawlerDriver) {
        this.crawlerDriver = crawlerDriver;
    }
    
    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }
    
    public Map<String, Object> getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void addDesiredCapability(String key, Object value) {
        desiredCapabilities.put(key, value);
    }
    
    public void addDesiredCapabilities(Map<String, Object> desiredCapabilities) {
        this.desiredCapabilities.putAll(desiredCapabilities);
    }
    
    public List<CrawlRequest> getSeeds() {
        return seeds;
    }
    
    public void addSeed(String seed) {
        try {
            URL requestUrl = new URL(seed);
            String topPrivateDomain = InternetDomainName.from(requestUrl.getHost())
                    .topPrivateDomain()
                    .toString();
            
            seeds.add(new CrawlRequest(requestUrl, topPrivateDomain));
        } catch (MalformedURLException | IllegalStateException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public void addSeeds(List<String> seeds) {
        seeds.stream().forEach(this::addSeed);
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
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        
        // desiredCapabilities is non-serializable, so convert it to JSON to serialize it
        out.writeUTF(gson.toJson(desiredCapabilities));
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Reconstruct desiredCapabilities from the JSON input
        Type desiredCapabilitiesType = new TypeToken<Map<String, Object>>() {}.getType();
        desiredCapabilities = gson.fromJson(in.readUTF(), desiredCapabilitiesType);
    }
}
