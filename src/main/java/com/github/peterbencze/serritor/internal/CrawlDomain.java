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

package com.github.peterbencze.serritor.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InternetDomainName;
import java.io.Serializable;

/**
 * Represents an internet domain in which crawling is allowed.
 */
public final class CrawlDomain implements Serializable {

    private final String domain;
    private final ImmutableList<String> parts;

    /**
     * Creates a <code>CrawlDomain</code> instance.
     *
     * @param domain an immutable well-formed internet domain name
     */
    public CrawlDomain(final InternetDomainName domain) {
        this.domain = domain.toString();
        parts = domain.parts();
    }

    /**
     * Returns the domain name, normalized to all lower case.
     *
     * @return the domain name
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Indicates if two <code>CrawlDomain</code> instances are equal. Crawl domains with the same
     * domain name are considered equal.
     *
     * @param obj a <code>CrawlDomain</code> instance
     *
     * @return <code>true</code> if equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof CrawlDomain) {
            CrawlDomain other = (CrawlDomain) obj;
            return parts.equals(other.parts);
        }

        return false;
    }

    /**
     * Calculates the hash code from the individual components of the domain name.
     *
     * @return the hash code for the crawl domain
     */
    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    /**
     * Indicates if this crawl domain contains the specific internet domain.
     *
     * @param domain an immutable well-formed internet domain name
     *
     * @return <code>true</code> if belongs, <code>false</code> otherwise
     */
    public boolean contains(final InternetDomainName domain) {
        ImmutableList<String> otherDomainParts = domain.parts();

        if (parts.size() > otherDomainParts.size()) {
            return false;
        }

        otherDomainParts = otherDomainParts.reverse()
                .subList(0, parts.size());

        return parts.reverse()
                .equals(otherDomainParts);
    }

    /**
     * Returns the string representation of this crawl domain.
     *
     * @return the string representation of this crawl domain
     */
    @Override
    public String toString() {
        return domain;
    }
}
