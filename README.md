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
    <version>1.1</version>
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
        config.addSeedAsString("http://yourspecificwebsite.com");
        config.setFilterOffsiteRequests(true);
    }

    @Override
    protected void onResponseComplete(HtmlResponse response) {
        List<WebElement> links = response.getWebDriver().findElements(By.tagName("a"));
        links.stream().forEach((WebElement link) -> crawlUrlAsString(link.getAttribute("href")));
    }

    @Override
    protected void onNonHtmlResponse(NonHtmlResponse response) {
        System.out.println("Received a non-HTML response from: " + response.getCurrentUrl());
    }
    
    @Override
    protected void onUnsuccessfulRequest(UnsuccessfulRequest request) {
        System.out.println("Could not get response from: " + request.getCurrentUrl());
    }
}
```
That's it! In just a few lines you can make a crawler that extracts and crawls every URL it finds, while filtering duplicate and offsite requests. You also get access to the WebDriver, so you can use all the features that are provided by Selenium.

By default, the crawler uses [HtmlUnitDriver](https://github.com/SeleniumHQ/selenium/wiki/HtmlUnitDriver) but you can also set your preferred WebDriver:
```java
config.setWebDriver(new ChromeDriver());
```

## Support
The developers would like to thank [Precognox](http://precognox.com/) for the support.

## License
The source code of Serritor is made available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
