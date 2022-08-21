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

import org.gradle.api.GradleException
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

/**
 * ServiceFactory that takes the given configuration to create a GCS based build-cache.
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 */
class GCSBuildCacheServiceFactory : BuildCacheServiceFactory<GCSBuildCache> {
    override fun createBuildCacheService(configuration: GCSBuildCache, describer: BuildCacheServiceFactory.Describer): BuildCacheService {
        val credentials = (if (configuration.credentials == null) "" else configuration.credentials) as String
        val bucket = configuration.bucket
        val refreshAfterSeconds = configuration.refreshAfterSeconds ?: 0
        val writeThreshold = configuration.writeThreshold ?: DEFAULT_WRITE_THRESHOLD

        if (bucket == null || bucket == "") {
            throw gradleException("The name of the bucket has to be defined.")
        }

        describer
            .type("Google Cloud Storage")
            .config("credentials", credentials)
            .config("bucket", bucket)
            .config("refreshAfterSeconds", refreshAfterSeconds.toString())
            .config("writeThreshold", writeThreshold.toString())

        return GCSBuildCacheService(credentials, bucket, refreshAfterSeconds.toLong(), writeThreshold)
    }

    fun gradleException(message: String): GradleException {
        return GradleException(
"""
                $message

                remote( GCSBuildCache.class ) {
                    credentials = 'my-key.json' // (optional)
                    bucket = 'my-bucket'
                    refreshAfterSeconds = 86400 // 24h (optional)
                    writeThreshold = 8 * 1024 * 1024 // 8 MiB
                    enabled = true
                    push = true
                }
            """.trimIndent()
        )
    }

    companion object {
        /**
         * The threshold to write data onto storage.
         */
        const val DEFAULT_WRITE_THRESHOLD = 8 * 1024 * 1024
    }
}
