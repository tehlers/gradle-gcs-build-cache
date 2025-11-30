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
import java.time.OffsetDateTime

/**
 * BuildCacheService that stores the build artifacts in Google Cloud Storage.
 * The creation time will be reset in a configurable interval to make sure
 * that artifacts still in use are not deleted.
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 * @author Nico Thomas Beranek (nico@jube.at) (1.4.0 update)
 */
class GCSBuildCacheService(
    credentials: String,
    private val bucketName: String,
    private val prefix: String?,
    private val refreshAfterSeconds: Long,
    private val writeThreshold: Int,
) : BuildCacheService {
    private val bucket: Bucket

    init {
        val creds = resolveCredentials(credentials)
        try {
            val storage =
                StorageOptions
                    .newBuilder()
                    .setCredentials(creds)
                    .build()
                    .service

            bucket = storage[bucketName] ?: throw BuildCacheException("Bucket '$bucketName' is unavailable.")
        } catch (e: FileNotFoundException) {
            throw BuildCacheException("Unable to load credentials from $credentials.", e)
        } catch (e: IOException) {
            throw BuildCacheException("Unable to initialize access to bucket '$bucketName'.", e)
        } catch (e: StorageException) {
            throw BuildCacheException("Unable to access Google Cloud Storage bucket '$bucketName'.", e)
        }
    }

    override fun store(
        key: BuildCacheKey,
        writer: BuildCacheEntryWriter,
    ) {
        val objectName = objectNameFor(key)

        runCatching {
            FileBackedOutputStream(writeThreshold, true).use { sink ->
                writer.writeTo(sink)
                sink.asByteSource().openBufferedStream().use { inStream ->
                    bucket.create(objectName, inStream)
                }
            }
        }.onFailure {
            throw BuildCacheException("Unable to store '$objectName' in bucket '$bucketName'.", it)
        }
    }

    override fun load(
        key: BuildCacheKey,
        reader: BuildCacheEntryReader,
    ): Boolean {
        val objectName = objectNameFor(key)
        try {
            val blob = bucket.get(objectName) ?: return false

            blob.reader().use { rc ->
                Channels.newInputStream(rc).use { input ->
                    reader.readFrom(input)
                }
            }

            if (refreshAfterSeconds > 0) {
                val created = blob.createTimeOffsetDateTime
                if (created != null && created.plusSeconds(refreshAfterSeconds).isBefore(OffsetDateTime.now())) {
                    blob.copyTo(bucketName, objectName)
                }
            }

            return true
        } catch (e: StorageException) {
            // Only treat 404 as a cache miss; everything else is a hard failure.
            if (e.code == 404) return false
            throw BuildCacheException("Unable to load '$objectName' from Google Cloud Storage bucket '$bucketName'.", e)
        }
    }

    override fun close() {
        // Nothing to close; Storage is lightweight and per-request.
    }

    private fun objectNameFor(key: BuildCacheKey): String =
        listOfNotNull(prefix, key.hashCode).joinToString("/")

    private fun resolveCredentials(input: String): GoogleCredentials =
        if (input.isEmpty()) {
            GoogleCredentials.getApplicationDefault()
        } else {
            ServiceAccountCredentials.fromStream(FileInputStream(input))
        }
}
