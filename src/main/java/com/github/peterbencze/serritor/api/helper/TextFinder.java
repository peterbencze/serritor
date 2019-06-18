/*
 * Copyright 2019 Peter Bencze.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Finds text in web elements located by the locating mechanisms that match the given pattern. If no
 * locating mechanism is specified, the default one will be used (<code>By.tagName("body")</code>).
 */
public final class TextFinder {

    private static final By DEFAULT_LOCATING_MECHANISM = By.tagName("body");

    private final Pattern textPattern;
    private final Set<By> locatingMechanisms;

    /**
     * Creates a {@link TextFinder} instance.
     *
     * @param textPattern        the pattern to use for matching
     * @param locatingMechanisms the mechanisms to use for locating web elements
     */
    public TextFinder(final Pattern textPattern, final Set<By> locatingMechanisms) {
        Validate.notNull(textPattern, "The textPattern parameter cannot be null");
        Validate.notEmpty(locatingMechanisms,
                "The locatingMechanisms parameter cannot be null or empty");
        Validate.noNullElements(locatingMechanisms,
                "The locatingMechanisms parameter cannot contain null elements");

        this.textPattern = textPattern;
        this.locatingMechanisms = locatingMechanisms;
    }

    /**
     * Creates a {@link TextFinder} instance.
     *
     * @param textPattern       the pattern to use for matching
     * @param locatingMechanism the mechanism to use for locating web elements
     */
    public TextFinder(final Pattern textPattern, final By locatingMechanism) {
        this(textPattern, Collections.singleton(locatingMechanism));
    }

    /**
     * Creates a {@link TextFinder} instance.
     *
     * @param textPattern the pattern to use for matching
     */
    public TextFinder(final Pattern textPattern) {
        this(textPattern, DEFAULT_LOCATING_MECHANISM);
    }

    /**
     * Returns the pattern used for matching.
     *
     * @return the pattern used for matching
     */
    public Pattern getPattern() {
        return textPattern;
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
     * Finds all the text that match the pattern in the text content of the response.
     *
     * @param response the complete crawl response
     *
     * @return all the text that match the pattern in the text content of the response
     */
    public List<MatchResult> findAllInResponse(final CompleteCrawlResponse response) {
        Validate.notNull(response, "The response parameter cannot be null");

        return locatingMechanisms.stream()
                .flatMap(locatingMechanism ->
                        response.getWebDriver().findElements(locatingMechanism).stream())
                .flatMap(element -> findAllInElement(element).stream())
                .collect(Collectors.toList());
    }

    /**
     * Finds the text that first matches the pattern in the text content of the response.
     *
     * @param response the complete crawl response
     *
     * @return the text that first matches the pattern in the text content of the response
     */
    public Optional<MatchResult> findFirstInResponse(final CompleteCrawlResponse response) {
        Validate.notNull(response, "The response parameter cannot be null");

        List<WebElement> matchedElements = locatingMechanisms.stream()
                .flatMap(locatingMechanism ->
                        response.getWebDriver().findElements(locatingMechanism).stream())
                .collect(Collectors.toList());

        // Return on first match (not possible with Java 8 Stream API)
        for (WebElement element : matchedElements) {
            Optional<MatchResult> matchResultOpt = findFirstInElement(element);
            if (matchResultOpt.isPresent()) {
                return matchResultOpt;
            }
        }

        return Optional.empty();
    }

    /**
     * Finds all the text that match the pattern in the text content of the given web element.
     *
     * @param element the web element to check for text
     *
     * @return all the text that match the pattern in the text content of the given web element
     */
    private List<MatchResult> findAllInElement(final WebElement element) {
        List<MatchResult> matchResults = new ArrayList<>();

        Matcher matcher = textPattern.matcher(element.getText());
        while (matcher.find()) {
            matchResults.add(matcher.toMatchResult());
        }

        return matchResults;
    }

    /**
     * Finds the text that first matches the pattern in the text content of the given web element.
     *
     * @param element the web element to check for text
     *
     * @return the text that first matches the pattern in the text content of the given web element
     */
    private Optional<MatchResult> findFirstInElement(final WebElement element) {
        Matcher matcher = textPattern.matcher(element.getText());
        return matcher.find() ? Optional.of(matcher.toMatchResult()) : Optional.empty();
    }
}
