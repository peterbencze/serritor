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

package com.github.peterbencze.serritor.internal.stats;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Duration;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * A custom serializer mechanism that is used to serialize durations in crawl statistics. With this
 * the given duration can be represented both in words and in numerical format.
 */
public final class DurationSerializer extends StdSerializer<Duration> {

    /**
     * Creates a {@link DurationSerializer} instance.
     */
    public DurationSerializer() {
        super(Duration.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(
            final Duration value,
            final JsonGenerator gen,
            final SerializerProvider provider) throws IOException {
        long durationInMillis = value.toMillis();
        String durationInWords = DurationFormatUtils.formatDurationWords(durationInMillis, true,
                true);

        gen.writeStartObject();
        gen.writeStringField("inWords", durationInWords);
        gen.writeNumberField("inMillis", durationInMillis);
        gen.writeEndObject();
    }
}
