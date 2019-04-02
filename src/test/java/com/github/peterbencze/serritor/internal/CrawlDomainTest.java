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
        Assert.assertEquals(CRAWL_DOMAIN_0, CRAWL_DOMAIN_0);
        Assert.assertEquals(CRAWL_DOMAIN_0, CRAWL_DOMAIN_1);
        Assert.assertNotEquals(CRAWL_DOMAIN_0, CRAWL_DOMAIN_2);
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(DOMAIN_PARTS_HASHCODE, CRAWL_DOMAIN_0.hashCode());
    }

    @Test
    public void testContains() {
        Assert.assertTrue(CRAWL_DOMAIN_0.contains(DOMAIN));
        Assert.assertTrue(CRAWL_DOMAIN_0.contains(SUBDOMAIN));
        Assert.assertFalse(CRAWL_DOMAIN_2.contains(DOMAIN));
    }
}
