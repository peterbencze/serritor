Serritor
========

Serritor is an open source web crawler framework built upon [Selenium](http://www.seleniumhq.org/) and written in Java. Crawling dynamic web pages is no longer a problem!

## Installation
### Using Maven

Add the following dependency to your pom.xml:
```xml
<dependency>
    <groupId>com.github.peterbencze</groupId>
    <artifactId>serritor</artifactId>
    <version>1.3.1</version>
</dependency>
```

### Without Maven

The standalone JAR files are available on the [releases](https://github.com/peterbencze/serritor/releases) page.

## Documentation
See the [Wiki](https://github.com/peterbencze/serritor/wiki) page.

## Quickstart
_BaseCrawler_ provides a skeletal implementation of a crawler to minimize the effort to create your own. First, create a class that extends _BaseCrawler_. In this class, you can implement the behavior of your crawler. There are callbacks available for every stage of crawling. Below you can find an example:
```java
public class MyCrawler extends BaseCrawler {

    private final UrlFinder urlFinder;

    public MyCrawler(final CrawlerConfiguration config) {
        super(config);
        
        // Extract URLs from links on the crawled page
        urlFinder = new UrlFinderBuilder(Pattern.compile(".+")).build();
    }

    @Override
    protected void onResponseComplete(final HtmlResponse response) {
        // Crawl every URL that match the given pattern
        urlFinder.findUrlsInResponse(response)
                .stream()
                .map(CrawlRequestBuilder::new)
                .map(CrawlRequestBuilder::build)
                .forEach(this::crawl);
    }

    @Override
    protected void onNonHtmlResponse(final NonHtmlResponse response) {
        System.out.println("Received a non-HTML response from: " + response.getCrawlRequest().getRequestUrl());
    }

    @Override
    protected void onUnsuccessfulRequest(final UnsuccessfulRequest request) {
        System.out.println("Could not get response from: " + request.getCrawlRequest().getRequestUrl());
    }
}
```
By default, the crawler uses [HtmlUnit headless browser](http://htmlunit.sourceforge.net/):
```java
public static void main(String[] args) {
    // Create the configuration
    CrawlerConfiguration config = new CrawlerConfigurationBuilder().setOffsiteRequestFiltering(true)
            .addAllowedCrawlDomain("example.com")
            .addCrawlSeed(new CrawlRequestBuilder("http://example.com").build())
            .build();

    // Create the crawler using the configuration above
    MyCrawler crawler = new MyCrawler(config);

    // Start it
    crawler.start();
}
```
Of course, you can also use any other browsers by specifying a corresponding _WebDriver_ instance:
```java
public static void main(String[] args) {
    // Create the configuration
    CrawlerConfiguration config = new CrawlerConfigurationBuilder().setOffsiteRequestFiltering(true)
            .addAllowedCrawlDomain("example.com")
            .addCrawlSeed(new CrawlRequestBuilder("http://example.com").build())
            .build();

    // Create the crawler using the configuration above
    MyCrawler crawler = new MyCrawler(config);

    // Start it
    crawler.start(new ChromeDriver());
}
```

That's it! In just a few lines you can make a crawler that crawls every link it finds, while filtering duplicate and offsite requests. You also get access to the _WebDriver_ instance, so you can use all the features that are provided by Selenium.

## License
The source code of Serritor is made available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
