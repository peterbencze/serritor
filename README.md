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
    <version>1.3.0</version>
</dependency>
```

### Without Maven

The standalone JAR files are available on the [releases](https://github.com/peterbencze/serritor/releases) page.

## Documentation
See the [Wiki](https://github.com/peterbencze/serritor/wiki) page.

## Quickstart
BaseCrawler provides a skeletal implementation of a crawler to minimize the effort to create your own. First, create a class that extends BaseCrawler. In this class, you can customize the behavior of your crawler. There are callbacks available for every stage of crawling. Below you can find a sample implementation:
```java
public class MyCrawler extends BaseCrawler {

    private final UrlFinder urlFinder;

    public MyCrawler() {
        // Enable offsite request filtering
        configurator.setOffsiteRequestFiltering(true);

        // Specify the allowed crawl domain
        configurator.addAllowedCrawlDomain("example.com");

        // Add a crawl seed, this is where the crawling starts
        CrawlRequest request = new CrawlRequestBuilder("http://example.com").build();
        configurator.addCrawlSeed(request);

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
That's it! In just a few lines you can make a crawler that crawls every link it finds, while filtering duplicate and offsite requests. You also get access to the WebDriver instance, so you can use all the features that are provided by Selenium.

By default, the crawler uses [HtmlUnit headless browser](http://htmlunit.sourceforge.net/):
```java
public static void main(String[] args) {
    MyCrawler myCrawler = new MyCrawler();

    // Use HtmlUnit headless browser
    myCrawler.start();
}
```
Of course, you can also use any other browsers by specifying a corresponding WebDriver instance:
```java
public static void main(String[] args) {
    MyCrawler myCrawler = new MyCrawler();

    // Use Google Chrome
    myCrawler.start(new ChromeDriver());
}
```

## License
The source code of Serritor is made available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
