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

import com.google.common.net.InternetDomainName;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link CrawlDomain}.
 *
 * @author Peter Bencze
 */
public final class CrawlDomainTest {

    private static final InternetDomainName DOMAIN = InternetDomainName.from("test.com");
    private static final InternetDomainName SUBDOMAIN = InternetDomainName.from("sub.test.com");

    private static final int DOMAIN_PARTS_HASHCODE = DOMAIN.parts().hashCode();

    private static final CrawlDomain CRAWL_DOMAIN_0 = new CrawlDomain(DOMAIN);
    private static final CrawlDomain CRAWL_DOMAIN_1 = new CrawlDomain(DOMAIN);
    private static final CrawlDomain CRAWL_DOMAIN_2 = new CrawlDomain(SUBDOMAIN);

    @Test
    public void testEquals() {
        // A crawl domain should be equal with itself
        Assert.assertEquals(CRAWL_DOMAIN_0, CRAWL_DOMAIN_0);

        // Crawl domains with the same domain should be equal
        Assert.assertEquals(CRAWL_DOMAIN_0, CRAWL_DOMAIN_1);

        // Crawl domains with different domains should not be equal
        Assert.assertNotEquals(CRAWL_DOMAIN_0, CRAWL_DOMAIN_2);
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(DOMAIN_PARTS_HASHCODE, CRAWL_DOMAIN_0.hashCode());
    }

    @Test
    public void testContains() {
        // A crawl domain should contain its own domain
        Assert.assertTrue(CRAWL_DOMAIN_0.contains(DOMAIN));

        // A crawl domain should contain its own domain's subdomain
        Assert.assertTrue(CRAWL_DOMAIN_0.contains(SUBDOMAIN));

        // A crawl domain should not contain a domain different from its own domain
        Assert.assertFalse(CRAWL_DOMAIN_2.contains(DOMAIN));
    }
}
