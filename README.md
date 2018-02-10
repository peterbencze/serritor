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
    <version>1.2.1</version>
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

    public MyCrawler() {
        // Enable offsite request filtering
        config.setOffsiteRequestFiltering(true);

        // Add a crawl seed, this is where the crawling starts
        CrawlRequest request = new CrawlRequestBuilder("http://example.com").build();
        config.addCrawlSeed(request);
    }

    @Override
    protected void onResponseComplete(final HtmlResponse response) {
        // Crawl every link that can be found on the page
        response.getWebDriver().findElements(By.tagName("a"))
                .stream()
                .forEach((WebElement link) -> {
                    CrawlRequest request = new CrawlRequestBuilder(link.getAttribute("href")).build();
                    crawl(request);
                });
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
That's it! In just a few lines you can make a crawler that extracts and crawls every URL it finds, while filtering duplicate and offsite requests. You also get access to the WebDriver, so you can use all the features that are provided by Selenium.

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
