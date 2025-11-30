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

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.registerBuildCacheService
import org.gradle.kotlin.dsl.remote

/**
 * Settings-Plugin that configures the remote build cache to use the Google Cloud Storage based implementation.
 *
 * @author Thorsten Ehlers (thorsten.ehlers@googlemail.com) (initial creation)
 * @author Nico Thomas Beranek (nico@jube.at) (1.4.0 update)
 */
abstract class GCSBuildCachePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.run {
            buildCache {
                registerBuildCacheService<GCSBuildCache>(GCSBuildCacheServiceFactory::class)
                remote<GCSBuildCache> {
                    isPush = true
                }
            }
        }
    }
}
