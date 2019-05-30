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

package com.github.peterbencze.serritor.internal.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * An implementation of {@link ReentrantReadWriteLock} that adds the possibility of specifying
 * actions (in a functional manner) which are executed under the lock.
 */
public final class FunctionalReentrantReadWriteLock extends ReentrantReadWriteLock {

    /**
     * Executes the given action under the read lock of this lock.
     *
     * @param action the action to execute
     * @param <T>    the type of result supplied by the action
     *
     * @return the result of the action
     */
    public <T> T readWithLock(final Supplier<T> action) {
        readLock().lock();

        try {
            return action.get();
        } finally {
            readLock().unlock();
        }
    }

    /**
     * Executes the given action under the read lock of this lock.
     *
     * @param action the action to execute
     */
    public void readWithLock(final Runnable action) {
        readLock().lock();

        try {
            action.run();
        } finally {
            readLock().unlock();
        }
    }

    /**
     * Executes the given action under the write lock of this lock.
     *
     * @param action the action to execute
     * @param <T>    the type of result supplied by the action
     *
     * @return the result of the action
     */
    public <T> T writeWithLock(final Supplier<T> action) {
        writeLock().lock();

        try {
            return action.get();
        } finally {
            writeLock().unlock();
        }
    }

    /**
     * Executes the given action under the write lock of this lock.
     *
     * @param action the action to execute
     */
    public void writeWithLock(final Runnable action) {
        writeLock().lock();

        try {
            action.run();
        } finally {
            writeLock().unlock();
        }
    }
}
