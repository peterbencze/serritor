Serritor
========

Serritor is an open source web crawler framework built upon [Selenium](http://www.seleniumhq.org/) and written in Java. It can be used to crawl dynamic web pages that use JavaScript.

## Using Serritor in your build
### Maven

Add the following dependency to your pom.xml:
```xml
<dependency>
    <groupId>com.github.peterbencze</groupId>
    <artifactId>serritor</artifactId>
    <version>1.4.0</version>
</dependency>
```

### Gradle

Add the following dependency to your build.gradle:
```groovy
compile group: 'com.github.peterbencze', name: 'serritor', version: '1.4.0'
```

### Manual dependencies

The standalone JAR files are available on the [releases](https://github.com/peterbencze/serritor/releases) page.

## Documentation
* The [Wiki](https://github.com/peterbencze/serritor/wiki) contains usage information and examples
* The Javadoc is available [here](https://peterbencze.github.io/serritor/)

## Quickstart
The `BaseCrawler` abstract class provides a skeletal implementation of a crawler to minimize the effort to create your own. The extending class should define the logic of the crawler.

Below you can find a simple example that is enough to get you started:
```java
public class MyCrawler extends BaseCrawler {

    private final UrlFinder urlFinder;

    public MyCrawler(final CrawlerConfiguration config) {
        super(config);
        
        // Extract URLs from links on the crawled page
        urlFinder = new UrlFinderBuilder(Pattern.compile(".+")).build();
    }

    @Override
    protected void onPageLoad(final PageLoadEvent event) {
        // Crawl every URL that match the given pattern
        urlFinder.findUrlsInPage(event)
                .stream()
                .map(CrawlRequestBuilder::new)
                .map(CrawlRequestBuilder::build)
                .forEach(this::crawl);
        
        // ...
    }
}
```
By default, the crawler uses [HtmlUnit headless browser](http://htmlunit.sourceforge.net/):
```java
// Create the configuration
CrawlerConfiguration config = new CrawlerConfigurationBuilder()
        .setOffsiteRequestFiltering(true)
        .addAllowedCrawlDomain("example.com")
        .addCrawlSeed(new CrawlRequestBuilder("http://example.com").build())
        .build();

// Create the crawler using the configuration above
MyCrawler crawler = new MyCrawler(config);

// Start it
crawler.start();
```
Of course, you can also use any other browsers by specifying a corresponding `WebDriver` instance:
```java
// Create the configuration
CrawlerConfiguration config = new CrawlerConfigurationBuilder()
        .setOffsiteRequestFiltering(true)
        .addAllowedCrawlDomain("example.com")
        .addCrawlSeed(new CrawlRequestBuilder("http://example.com").build())
        .build();

// Create the crawler using the configuration above
MyCrawler crawler = new MyCrawler(config);

// Start it
crawler.start(new ChromeDriver());
```

That's it! In just a few lines you can create a crawler that crawls every link it finds, while filtering duplicate and offsite requests. You also get access to the `WebDriver` instance, so you can use all the features that are provided by Selenium.

## License
The source code of Serritor is made available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
