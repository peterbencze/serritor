/* 
 * Copyright 2018 Peter Bencze.
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
package com.github.peterbencze.serritor.api.helper;

import com.github.peterbencze.serritor.api.HtmlResponse;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * A helper class which can be used to find URLs in HTML sources using regular
 * expressions.
 *
 * @author Peter Bencze
 */
public final class UrlFinder {

    private final Set<Pattern> urlPatterns;
    private final Set<By> locatingMechanisms;
    private final Set<String> attributes;

    private UrlFinder(final UrlFinderBuilder builder) {
        urlPatterns = builder.urlPatterns;
        locatingMechanisms = builder.locatingMechanisms;
        attributes = builder.attributes;
    }

    /**
     * Returns a list of (unvalidated) URLs found in the response's HTML source.
     *
     * @param response The <code>HtmlResponse</code> instance
     * @return The list of found URLs
     */
    public List<String> findUrlsInResponse(final HtmlResponse response) {
        Set<String> foundUrls = new HashSet<>();

        // Find elements using the specified locating mechanisms
        Set<WebElement> extractedElements = locatingMechanisms.stream()
                .map(response.getWebDriver()::findElements)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        // Find URLs in the attribute values of the found elements
        extractedElements.forEach((WebElement element) -> {
            attributes.stream()
                    .map(element::getAttribute)
                    .filter(StringUtils::isNotBlank)
                    .map(this::findUrlsInAttributeValue)
                    .flatMap(List::stream)
                    .forEach(foundUrls::add);
        });

        return foundUrls.stream()
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of (unvalidated) URLs found in the attribute's value.
     *
     * @param attributeValue The value of the attribute
     * @return The list of found URLs
     */
    private List<String> findUrlsInAttributeValue(final String attributeValue) {
        List<String> foundUrls = new ArrayList<>();

        urlPatterns.stream()
                .map((Pattern urlPattern) -> urlPattern.matcher(attributeValue))
                .forEach((Matcher urlPatternMatcher) -> {
                    while (urlPatternMatcher.find()) {
                        String foundUrl = urlPatternMatcher.group();

                        if (StringUtils.isNotBlank(foundUrl)) {
                            foundUrls.add(foundUrl);
                        }
                    }
                });

        return foundUrls;
    }

    public static final class UrlFinderBuilder {

        private final Set<Pattern> urlPatterns;

        private Set<By> locatingMechanisms;
        private Set<String> attributes;

        /**
         * Constructs a <code>UrlFinderBuilder</code> instance that can be used
         * to create <code>UrlFinder</code> instances.
         *
         * @param urlPattern The pattern which will be used to find URLs
         */
        public UrlFinderBuilder(final Pattern urlPattern) {
            this(Arrays.asList(urlPattern));
        }

        /**
         * Constructs a <code>UrlFinderBuilder</code> instance that can be used
         * to create <code>UrlFinder</code> instances. It
         *
         * @param urlPatterns The list of patterns which will be used to find
         * URLs
         */
        public UrlFinderBuilder(final List<Pattern> urlPatterns) {
            Validate.noNullElements(urlPatterns, "URL patterns cannot be null.");

            this.urlPatterns = Sets.newHashSet(urlPatterns);
            locatingMechanisms = Sets.newHashSet(By.tagName("a"));
            attributes = Sets.newHashSet("href");
        }

        /**
         * Sets the locating mechanism used by the finder. Only elements matched
         * by the locator will be considered when searching for URLs.
         *
         * @param locatingMechanism The <code>By</code> locating mechanism
         * instance
         */
        public void setLocatingMechanism(final By locatingMechanism) {
            setLocatingMechanisms(Arrays.asList(locatingMechanism));
        }

        /**
         * Sets the locating mechanisms used by the finder. Only elements
         * matched by the locators will be considered when searching for URLs.
         *
         * @param locatingMechanisms The list of <code>By</code> locating
         * mechanism instances
         */
        public void setLocatingMechanisms(final List<By> locatingMechanisms) {
            Validate.noNullElements(locatingMechanisms, "Locating mechanisms cannot be null.");

            this.locatingMechanisms = Sets.newHashSet(locatingMechanisms);
        }

        /**
         * Sets which attributes to search for URLs.
         *
         * @param attributes The list of attribute names
         */
        public void setAttributes(final List<String> attributes) {
            Validate.noNullElements(attributes, "Attributes cannot be null.");

            this.attributes = Sets.newHashSet(attributes);
        }

        /**
         * Sets which attribute to search for URLs.
         *
         * @param attribute The name of the attribute
         */
        public void setAttribute(final String attribute) {
            setAttributes(Arrays.asList(attribute));
        }

        /**
         * Builds the configured URL finder.
         *
         * @return The configured <code>UrlFinder</code> instance
         */
        public UrlFinder build() {
            return new UrlFinder(this);
        }
    }
}
