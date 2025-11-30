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

import com.google.auth.oauth2.GoogleCredentials
import org.gradle.api.provider.Property
import org.gradle.caching.BuildCacheException
import org.gradle.caching.configuration.AbstractBuildCache

/**
 * Configuration for the GCS-based remote build cache.
 *
 * Properties use the lazy [Property] API from Gradle
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 * @author Nico Thomas Beranek (nico@jube.at) (1.4.0 update)
 */
abstract class GCSBuildCache : AbstractBuildCache() {
    /**
     * Service account credentials: Path to JSON.
     * When empty uses “[GoogleCredentials.getApplicationDefault]”.
     */
    abstract val credentials: Property<String>

    /**
     * Target GCS bucket name (required for a remote cache).
     */
    abstract val bucket: Property<String>

    /**
     * Optional key prefix within the bucket (acts like a “folder”).
     */
    abstract val prefix: Property<String>

    /**
     * Metadata refresh interval in seconds; 0 disables refresh.
     */
    abstract val refreshAfterSeconds: Property<Int>

    /**
     * Threshold in bytes under which writes are buffered instead of streamed.
     *
     * @throws BuildCacheException when negative
     */
    abstract val writeThreshold: Property<Int>
}
