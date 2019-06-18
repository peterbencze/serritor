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

import com.github.peterbencze.serritor.api.CompleteCrawlResponse;
import com.google.common.net.InternetDomainName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
 * Finds URLs in web element attributes located by the locating mechanisms, that match the given
 * pattern. By default, the <code>By.tagName("a")</code> locating mechanism is used and the
 * element's <code>href</code> attribute is searched for URLs.
 */
public final class UrlFinder {

    private final Pattern urlPattern;
    private final Set<By> locatingMechanisms;
    private final String attributeName;
    private final Predicate<String> validator;

    private UrlFinder(final UrlFinderBuilder builder) {
        urlPattern = builder.urlPattern;
        locatingMechanisms = builder.locatingMechanisms;
        attributeName = builder.attributeName;
        validator = builder.validator;
    }

    /**
     * Creates a <code>UrlFinder</code> instance with the default configuration.
     *
     * @return a <code>UrlFinder</code> instance with the default configuration
     */
    public static UrlFinder createDefault() {
        return new UrlFinderBuilder().build();
    }

    /**
     * Returns the pattern used for matching.
     *
     * @return the pattern used for matching
     */
    public Pattern getPattern() {
        return urlPattern;
    }

    /**
     * Returns the locating mechanisms used for locating web elements.
     *
     * @return the locating mechanisms used for locating web elements
     */
    public Set<By> getLocatingMechanisms() {
        return locatingMechanisms;
    }

    /**
     * Returns the name of the web element attribute that is searched for a URL.
     *
     * @return the name of the web element attribute that is searched for a URL
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Returns the predicate used for validating URLs.
     *
     * @return the predicate used for validating URLs
     */
    public Predicate<String> getValidator() {
        return validator;
    }

    /**
     * Finds all the URLs that match the pattern in the response content.
     *
     * @param response the complete crawl response
     *
     * @return all the URLs that match the pattern in the response content
     */
    public List<String> findAllInResponse(final CompleteCrawlResponse response) {
        Validate.notNull(response, "The response parameter cannot be null");

        return locatingMechanisms.stream()
                .flatMap(locatingMechanism ->
                        response.getWebDriver().findElements(locatingMechanism).stream())
                .map(element -> element.getAttribute(attributeName))
                .map(this::findInAttributeValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Finds the URL that first matches the pattern in the response content.
     *
     * @param response the complete crawl response
     *
     * @return the URL that first matches the pattern in the response content
     */
    public Optional<String> findFirstInResponse(final CompleteCrawlResponse response) {
        Validate.notNull(response, "The response parameter cannot be null");

        List<WebElement> matchedElements = locatingMechanisms.stream()
                .flatMap(locatingMechanism ->
                        response.getWebDriver().findElements(locatingMechanism).stream())
                .collect(Collectors.toList());

        // Return on first match (not possible with Java 8 Stream API)
        for (WebElement element : matchedElements) {
            String attributeValue = element.getAttribute(attributeName);

            Optional<String> matchedUrlOpt = findInAttributeValue(attributeValue);
            if (matchedUrlOpt.isPresent()) {
                return matchedUrlOpt;
            }
        }

        return Optional.empty();
    }

    /**
     * Finds a valid URL in the given attribute value.
     *
     * @param attributeValue the value of the attribute
     *
     * @return a valid URL in the given attribute value
     */
    private Optional<String> findInAttributeValue(final String attributeValue) {
        if (StringUtils.isNotBlank(attributeValue)) {
            Matcher matcher = urlPattern.matcher(attributeValue);
            if (matcher.find()) {
                String foundUrl = matcher.group().trim();

                if (validator.test(foundUrl)) {
                    return Optional.of(foundUrl);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Builds {@link UrlFinder} instances.
     */
    public static final class UrlFinderBuilder {

        private static final Pattern DEFAULT_PATTERN = Pattern.compile("https?://\\S+");
        private static final By DEFAULT_LOCATING_MECHANISM = By.tagName("a");
        private static final String DEFAULT_ATTRIBUTE_NAME = "href";
        private static final Predicate<String> DEFAULT_VALIDATOR = UrlFinderBuilder::isValidUrl;

        private Pattern urlPattern;
        private Set<By> locatingMechanisms;
        private String attributeName;
        private Predicate<String> validator;

        /**
         * Creates a {@link UrlFinderBuilder} instance.
         */
        public UrlFinderBuilder() {
            urlPattern = DEFAULT_PATTERN;
            locatingMechanisms = Collections.singleton(DEFAULT_LOCATING_MECHANISM);
            attributeName = DEFAULT_ATTRIBUTE_NAME;
            validator = DEFAULT_VALIDATOR;
        }

        /**
         * Sets the pattern to use for matching.
         *
         * @param urlPattern the pattern to use for matching
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setPattern(final Pattern urlPattern) {
            Validate.notNull(urlPattern, "The urlPattern parameter cannot be null");

            this.urlPattern = urlPattern;
            return this;
        }

        /**
         * Sets the locating mechanism used by the finder. Only elements matched by the locator will
         * be considered when searching for URLs.
         *
         * @param locatingMechanism the mechanism to use for locating web elements
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setLocatingMechanism(final By locatingMechanism) {
            return setLocatingMechanisms(Collections.singleton(locatingMechanism));
        }

        /**
         * Sets the locating mechanisms used by the finder. Only elements matched by the locators
         * will be considered when searching for URLs.
         *
         * @param locatingMechanisms the mechanisms to use for locating web elements
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setLocatingMechanisms(final Set<By> locatingMechanisms) {
            Validate.notEmpty(locatingMechanisms,
                    "The locatingMechanisms parameter cannot be null or empty");
            Validate.noNullElements(locatingMechanisms,
                    "The locatingMechanisms parameter cannot contain null elements");

            this.locatingMechanisms = locatingMechanisms;
            return this;
        }

        /**
         * Sets the name of the web element attribute to search for a URL.
         *
         * @param attributeName the name of the web element attribute
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setAttributeName(final String attributeName) {
            Validate.notBlank(attributeName, "The attributeName cannot be null or blank");

            this.attributeName = attributeName;
            return this;
        }

        /**
         * Sets the predicate to use for validating URLs.
         *
         * @param validator the validator predicate
         *
         * @return the <code>UrlFinderBuilder</code> instance
         */
        public UrlFinderBuilder setValidator(final Predicate<String> validator) {
            Validate.notNull(validator, "The validator parameter cannot be null");

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
