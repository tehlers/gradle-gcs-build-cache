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

import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

/**
 * ServiceFactory that takes the given configuration to create a GCS based build-cache.
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 * @author Nico Thomas Beranek (nico@jube.at) (1.4.0 update)
 */
class GCSBuildCacheServiceFactory : BuildCacheServiceFactory<GCSBuildCache> {
    override fun createBuildCacheService(
        configuration: GCSBuildCache,
        describer: BuildCacheServiceFactory.Describer,
    ): BuildCacheService {
        val credentials = configuration.credentials.convention("")
        val bucket = configuration.bucket
        val prefix = configuration.prefix
        val refreshAfterSeconds = configuration.refreshAfterSeconds.convention(0)
        val writeThreshold = configuration.writeThreshold.convention(DEFAULT_WRITE_THRESHOLD)

        require(bucket.isPresent && bucket.get().isNotBlank()) {
            """
            |"The name of the bucket has to be defined."
            |remote( GCSBuildCache.class ) {
            |   credentials = 'my-key.json' // (optional)
            |   bucket = 'my-bucket'
            |   refreshAfterSeconds = 86400 // 24h (optional)
            |   writeThreshold = 8 * 1024 * 1024 // 8 MiB
            |   enabled = true
            |   push = true
            |}
            """.trimMargin()
        }

        describer
            .type("Google Cloud Storage")
            .config("credentials", credentials.get())
            .config("bucket", bucket.get())
            .config("prefix", prefix.getOrElse("<unset>"))
            .config("refreshAfterSeconds", refreshAfterSeconds.get().toString())
            .config("writeThreshold", writeThreshold.get().toString())

        return GCSBuildCacheService(
            credentials = credentials.get(),
            bucketName = bucket.get(),
            prefix = prefix.get(),
            refreshAfterSeconds = refreshAfterSeconds.map { it.toLong() }.get(),
            writeThreshold = writeThreshold.get(),
        )
    }

    companion object {
        /**
         * The threshold to write data onto storage.
         */
        const val DEFAULT_WRITE_THRESHOLD: Int = 8 * 1024 * 1024
    }
}
