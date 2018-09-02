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

import com.github.peterbencze.serritor.api.event.PageLoadEvent;
import com.google.common.collect.Sets;
import com.google.common.net.InternetDomainName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Finds URLs in HTML page sources using regular expressions.
 *
 * @author Peter Bencze
 */
public final class UrlFinder {

    private final Set<Pattern> urlPatterns;
    private final Set<By> locatingMechanisms;
    private final Set<String> attributes;
    private final Predicate<String> validator;

    private UrlFinder(final UrlFinderBuilder builder) {
        urlPatterns = builder.urlPatterns;
        locatingMechanisms = builder.locatingMechanisms;
        attributes = builder.attributes;
        validator = builder.validator;
    }

    /**
     * Creates a <code>UrlFinder</code> instance with default configuration which matches anything
     * in the <code>href</code> attribute of the HTML anchor tags.
     *
     * @return a <code>UrlFinder</code> instance with default configuration
     */
    public static UrlFinder createDefault() {
        return new UrlFinderBuilder(Pattern.compile(".+")).build();
    }

    /**
     * Returns a list of validated URLs found in the page's HTML source.
     *
     * @param event the <code>PageLoadEvent</code> instance
     *
     * @return the list of found URLs
     */
    public List<String> findUrlsInPage(final PageLoadEvent event) {
        Set<String> foundUrls = new HashSet<>();

        // Find elements using the specified locating mechanisms
        Set<WebElement> extractedElements = locatingMechanisms.stream()
                .map(event.getWebDriver()::findElements)
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
     * Returns a list of validated URLs found in the attribute's value.
     *
     * @param attributeValue the value of the attribute
     *
     * @return the list of found URLs
     */
    private List<String> findUrlsInAttributeValue(final String attributeValue) {
        List<String> foundUrls = new ArrayList<>();

        urlPatterns.stream()
                .map((Pattern urlPattern) -> urlPattern.matcher(attributeValue))
                .forEach((Matcher urlPatternMatcher) -> {
                    while (urlPatternMatcher.find()) {
                        String foundUrl = urlPatternMatcher.group().trim();

                        if (validator.test(foundUrl)) {
                            foundUrls.add(foundUrl);
                        }
                    }
                });

        return foundUrls;
    }

    /**
     * Builds {@link UrlFinder} instances.
     */
    public static final class UrlFinderBuilder {

        private static final Set<By> DEFAULT_LOCATING_MECHANISMS = Sets.newHashSet(By.tagName("a"));
        private static final Set<String> DEFAULT_ATTRIBUTES = Sets.newHashSet("href");
        private static final Predicate<String> DEFAULT_VALIDATOR = UrlFinderBuilder::isValidUrl;

        private final Set<Pattern> urlPatterns;

        private Set<By> locatingMechanisms;
        private Set<String> attributes;
        private Predicate<String> validator;

        /**
         * Creates a {@link UrlFinderBuilder} instance.
         *
         * @param urlPattern the pattern to use to find URLs
         */
        public UrlFinderBuilder(final Pattern urlPattern) {
            this(Arrays.asList(urlPattern));
        }

        /**
         * Creates a {@link UrlFinderBuilder} instance.
         *
         * @param urlPatterns the list of patterns to use to find URLs
         */
        public UrlFinderBuilder(final List<Pattern> urlPatterns) {
            Validate.noNullElements(urlPatterns, "URL patterns cannot be null.");

            this.urlPatterns = Sets.newHashSet(urlPatterns);
            locatingMechanisms = DEFAULT_LOCATING_MECHANISMS;
            attributes = DEFAULT_ATTRIBUTES;
            validator = DEFAULT_VALIDATOR;
        }

        /**
         * Sets the locating mechanism used by the finder. Only elements matched by the locator will
         * be considered when searching for URLs.
         *
         * @param locatingMechanism the <code>By</code> locating mechanism instance
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setLocatingMechanism(final By locatingMechanism) {
            return setLocatingMechanisms(Arrays.asList(locatingMechanism));
        }

        /**
         * Sets the locating mechanisms used by the finder. Only elements matched by the locators
         * will be considered when searching for URLs.
         *
         * @param locatingMechanisms the list of <code>By</code> locating mechanism instances
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setLocatingMechanisms(final List<By> locatingMechanisms) {
            Validate.noNullElements(locatingMechanisms, "Locating mechanisms cannot be null.");

            this.locatingMechanisms = Sets.newHashSet(locatingMechanisms);
            return this;
        }

        /**
         * Sets the list of attribute names to search for URLs.
         *
         * @param attributes the list of attribute names
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setAttributes(final List<String> attributes) {
            Validate.noNullElements(attributes, "Attributes cannot be null.");

            this.attributes = Sets.newHashSet(attributes);
            return this;
        }

        /**
         * Sets the attribute name to search for URLs.
         *
         * @param attribute the attribute name
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setAttribute(final String attribute) {
            return setAttributes(Arrays.asList(attribute));
        }

        /**
         * Sets a predicate to be used for validating found URLs.
         *
         * @param validator the validator predicate
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setValidator(final Predicate<String> validator) {
            Validate.notNull(validator, "The validator function cannot be null.");

            this.validator = validator;
            return this;
        }

        /**
         * Builds the configured <code>UrlFinder</code> instance.
         *
         * @return the configured <code>UrlFinder</code> instance
         */
        public UrlFinder build() {
            return new UrlFinder(this);
        }

        /**
         * The default URL validator function.
         *
         * @param url the URL to validate
         *
         * @return <code>true</code> if the URL is valid, <code>false</code> otherwise
         */
        private static boolean isValidUrl(final String url) {
            try {
                return InternetDomainName.isValid(URI.create(url).getHost());
            } catch (IllegalArgumentException | NullPointerException exc) {
                return false;
            }
        }
    }
}
