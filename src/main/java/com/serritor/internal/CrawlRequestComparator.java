/* 
 * Copyright 2016 Peter Bencze.
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
package com.serritor.internal;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Defines a comparator for CrawlRequests to decide the next URL to crawl. Using
 * this implementation, the first element in the ordering is the request with
 * the least depth (equals to breadth-first search). Reversing the comparator
 * will result in depth-first search.
 *
 * @author Krisztian Mozsi
 */
public final class CrawlRequestComparator implements Comparator<CrawlRequest>, Serializable {

    @Override
    public int compare(CrawlRequest request1, CrawlRequest request2) {
        if (request1.getCrawlDepth() < request2.getCrawlDepth()) {
            return -1;
        }

        if (request1.getCrawlDepth() > request2.getCrawlDepth()) {
            return 1;
        }

        return 0;
    }

}
