/**
 * Copyright 2019 Thorsten Ehlers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.idlestate.gradle.caching

import net.idlestate.gradle.caching.GCSBuildCacheServiceFactory.Companion.DEFAULT_WRITE_THRESHOLD
import org.gradle.caching.configuration.AbstractBuildCache
import javax.inject.Inject

/**
 * Configuration of the GCS based build cache.
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 */
abstract class GCSBuildCache constructor(
    var credentials: String? = "",
    var bucket: String? = "",
    var prefix: String? = null,
    var refreshAfterSeconds: Int? = 0,
    var writeThreshold: Int? = DEFAULT_WRITE_THRESHOLD,
) : AbstractBuildCache() {
    @Inject constructor() : this("", "", null, 0, DEFAULT_WRITE_THRESHOLD) {
    }
}
