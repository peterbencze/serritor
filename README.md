

Serritor
========

[![GitHub release](https://img.shields.io/github/release/peterbencze/serritor.svg)](https://github.com/peterbencze/serritor/releases/latest)
[![GitHub Release Date](https://img.shields.io/github/release-date/peterbencze/serritor.svg)](https://github.com/peterbencze/serritor/releases/latest)
[![Gitter](https://badges.gitter.im/serritor/community.svg)](https://gitter.im/serritor/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

Serritor is an open source web crawler framework built upon [Selenium](http://www.seleniumhq.org/) 
and written in Java. It can be used to crawl dynamic web pages that require JavaScript to render 
data.

## Using Serritor in your build
### Maven

Add the following dependency to your pom.xml:
```xml
<dependency>
    <groupId>com.github.peterbencze</groupId>
    <artifactId>serritor</artifactId>
    <version>2.1.1</version>
</dependency>
```

### Gradle

Add the following dependency to your build.gradle:
```groovy
compile group: 'com.github.peterbencze', name: 'serritor', version: '2.1.1'
```

### Manual dependencies

The standalone JAR files are available on the 
[releases](https://github.com/peterbencze/serritor/releases) page.

## Documentation
* The [Wiki](https://github.com/peterbencze/serritor/wiki) contains usage information and examples
* The Javadoc is available [here](https://peterbencze.github.io/serritor/)

## Quickstart
The `Crawler` abstract class provides a skeletal implementation of a crawler to minimize the effort 
to create your own. The extending class should implement the logic of the crawler.

Below you can find a simple example that is enough to get you started:
```java
public class MyCrawler extends Crawler {

    private final UrlFinder urlFinder;

    public MyCrawler(final CrawlerConfiguration config) {
        super(config);

        // A helper class that is intended to make it easier to find URLs on web pages
        urlFinder = UrlFinder.createDefault();
    }

    @Override
    protected void onResponseSuccess(final ResponseSuccessEvent event) {
        // Crawl every URL found on the page
        urlFinder.findAllInResponse(event.getCompleteCrawlResponse())
                .stream()
                .map(CrawlRequest::createDefault)
                .forEach(this::crawl);

        // ...
    }
}
```
By default, the crawler uses the [HtmlUnit](http://htmlunit.sourceforge.net/) headless browser:
```java
// Create the configuration
CrawlerConfiguration config = new CrawlerConfigurationBuilder()
        .setOffsiteRequestFilterEnabled(true)
        .addAllowedCrawlDomain("example.com")
        .addCrawlSeed(CrawlRequest.createDefault("http://example.com"))
        .build();

// Create the crawler using the configuration above
MyCrawler crawler = new MyCrawler(config);

// Start crawling with HtmlUnit
crawler.start();
```
Of course, you can also use other browsers. Currently Chrome and Firefox are supported.
```java
// Create the configuration
CrawlerConfiguration config = new CrawlerConfigurationBuilder()
        .setOffsiteRequestFilterEnabled(true)
        .addAllowedCrawlDomain("example.com")
        .addCrawlSeed(CrawlRequest.createDefault("http://example.com"))
        .build();

// Create the crawler using the configuration above
MyCrawler crawler = new MyCrawler(config);

// Start crawling with Chrome
crawler.start(Browser.CHROME);
```

That's it! In just a few lines you can create a crawler that crawls every link it finds, while 
filtering duplicate and offsite requests. You also get access to the `WebDriver`, so you can use 
all the features that are provided by Selenium.

## Special thanks
[<img src="https://user-images.githubusercontent.com/1896287/62488023-8f2b5c00-b7c3-11e9-9108-82034819c462.png" width="100" height="100">](https://www.jetbrains.com/?from=Serritor)

For providing a free JetBrains Open Source license to support the development of this project.

## Support
If this framework helped you in any way, or you would like to support the development:

[![Support via PayPal](https://cdn.rawgit.com/twolfson/paypal-github-button/1.0.0/dist/button.svg)](https://paypal.me/peterbencze)

Any amount you choose to give will be greatly appreciated.

## License
The source code of Serritor is made available under the 
[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
