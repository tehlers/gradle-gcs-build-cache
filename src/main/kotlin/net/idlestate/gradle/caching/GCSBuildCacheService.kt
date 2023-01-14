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
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import com.google.common.io.FileBackedOutputStream
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheException
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.channels.Channels
import java.time.Instant

/**
 * BuildCacheService that stores the build artifacts in Google Cloud Storage.
 * The creation time will be reset in a configurable interval to make sure
 * that artifacts still in use are not deleted.
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 */
class GCSBuildCacheService(credentials: String, val bucketName: String, val refreshAfterSeconds: Long, val writeThreshold: Int) : BuildCacheService {
    private val bucket: Bucket
    init {
        try {
            val storage = StorageOptions.newBuilder()
                .setCredentials(
                    if (credentials.isEmpty()) GoogleCredentials.getApplicationDefault() else ServiceAccountCredentials.fromStream(FileInputStream(credentials))
                )
                .build()
                .service

            bucket = storage.get(bucketName) ?: throw BuildCacheException("$bucketName is unavailable")
        } catch (e: FileNotFoundException) {
            throw BuildCacheException("Unable to load credentials from $credentials.", e)
        } catch (e: IOException) {
            throw BuildCacheException("Unable to access Google Cloud Storage bucket '$bucketName'.", e)
        } catch (e: StorageException) {
            throw BuildCacheException("Unable to access Google Cloud Storage bucket '$bucketName'.", e)
        }
    }

    override fun store(key: BuildCacheKey, writer: BuildCacheEntryWriter) {
        val value = FileBackedOutputStream(writeThreshold, true)
        writer.writeTo(value)

        try {
            value.asByteSource().openBufferedStream().use {
                bucket.create(key.hashCode, it)
            }
        } catch (e: StorageException) {
            throw BuildCacheException("Unable to store '${key.hashCode}' in Google Cloud Storage bucket '$bucketName'.", e)
        }
    }

    override fun load(key: BuildCacheKey, reader: BuildCacheEntryReader): Boolean {
        try {
            val blob = bucket.get(key.hashCode)

            if (blob != null) {
                reader.readFrom(Channels.newInputStream(blob.reader()))

                if (refreshAfterSeconds > 0) {
                    // Update creation time so that artifacts that are still used won't be deleted.
                    val createTime = blob.createTimeOffsetDateTime.toInstant()
                    if (createTime.plusSeconds(refreshAfterSeconds).isBefore(Instant.now())) {
                        bucket.create(key.hashCode, blob.getContent())
                    }
                }

                return true
            }
        } catch (e: StorageException) {
            // https://github.com/googleapis/google-cloud-java/issues/3402
            if (e.message?.contains("404") == true) {
                return false
            }

            throw BuildCacheException("Unable to load '${key.hashCode}' from Google Cloud Storage bucket '$bucketName'.", e)
        }

        return false
    }

    override fun close() {
        // nothing to do
    }
}
