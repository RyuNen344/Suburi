/*
 * Copyright (C) 2025 RyuNen344
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE.md
 */

package io.github.ryunen344.suburi.test.assertk.location

import android.location.Location
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThanOrEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.support.fail

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject]
 */
fun Assert<Location>.provider() = prop("provider", Location::getProvider)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.time]
 */
fun Assert<Location>.time() = prop("time", Location::getTime)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.elapsedRealtimeNanos]
 */
fun Assert<Location>.elapsedRealtimeNanos() = prop("elapsedRealtimeNanos", Location::getElapsedRealtimeNanos)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.elapsedRealtimeMillis]
 */
fun Assert<Location>.elapsedRealtimeMillis() = prop("elapsedRealtimeMillis", Location::getElapsedRealtimeMillis)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject]
 */
fun Assert<Location>.latitude() = prop("latitude", Location::getLatitude)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject]
 */
fun Assert<Location>.longitude() = prop("longitude", Location::getLongitude)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.accuracy]
 */
fun Assert<Location>.accuracy() = prop("accuracy", Location::getAccuracy)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.altitude]
 */
fun Assert<Location>.altitude() = prop("altitude", Location::getAltitude)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.verticalAccuracy]
 */
fun Assert<Location>.verticalAccuracyMeters() = prop("verticalAccuracyMeters", Location::getVerticalAccuracyMeters)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.speed]
 */
fun Assert<Location>.speed() = prop("speed", Location::getSpeed)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.speedAccuracy]
 */
fun Assert<Location>.speedAccuracyMetersPerSecond() = prop("speedAccuracyMetersPerSecond", Location::getSpeedAccuracyMetersPerSecond)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.bearing]
 */
fun Assert<Location>.bearing() = prop("bearing", Location::getBearing)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.bearingAccuracy]
 */
fun Assert<Location>.bearingAccuracyDegrees() = prop("bearingAccuracyDegrees", Location::getBearingAccuracyDegrees)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject]
 */
fun Assert<Location>.mslAltitudeMeters() = prop("mslAltitudeMeters", Location::getMslAltitudeMeters)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject]
 */
fun Assert<Location>.mslAltitudeAccuracyMeters() = prop("mslAltitudeAccuracyMeters", Location::getMslAltitudeAccuracyMeters)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.extras]
 */
fun Assert<Location>.extras() = prop("extras", Location::getExtras)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isEqualTo]
 */
fun Assert<Location>.isEqualTo(other: Location) = given { actual ->
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
        // from android S+, Location.equals() is well defined
        if (actual == other) return
        fail(other, actual)
    } else {
        all {
            provider().isEqualTo(other.provider)
            time().isEqualTo(other.time)
            elapsedRealtimeNanos().isEqualTo(other.elapsedRealtimeNanos)
            latitude().isEqualTo(other.latitude)
            longitude().isEqualTo(other.longitude)
            altitude().isEqualTo(other.altitude)
            speed().isEqualTo(other.speed)
            bearing().isEqualTo(other.bearing)
            accuracy().isEqualTo(other.accuracy)
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                verticalAccuracyMeters().isEqualTo(other.verticalAccuracyMeters)
                speedAccuracyMetersPerSecond().isEqualTo(other.speedAccuracyMetersPerSecond)
                bearingAccuracyDegrees().isEqualTo(other.bearingAccuracyDegrees)
            }
        }
    }
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isAt]
 */
fun Assert<Location>.isAt(other: Location) = isAt(other.latitude, other.longitude)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isAt]
 */
fun Assert<Location>.isAt(latitude: Double, longitude: Double) = all {
    latitude().isEqualTo(latitude)
    longitude().isEqualTo(longitude)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isNotAt]
 */
fun Assert<Location>.isNotAt(other: Location) = isNotAt(other.latitude, other.longitude)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isNotAt]
 */
fun Assert<Location>.isNotAt(latitude: Double, longitude: Double) = all {
    latitude().isNotEqualTo(latitude)
    longitude().isNotEqualTo(longitude)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.distanceTo]
 */
fun Assert<Location>.distanceTo(latitude: Double, longitude: Double) = distanceTo(
    Location("").apply {
        this.latitude = latitude
        this.longitude = longitude
    },
)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.distanceTo]
 */
fun Assert<Location>.distanceTo(other: Location) = transform { actual ->
    actual.distanceTo(other)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isNearby]
 */
fun Assert<Location>.isNearby(other: Location, distanceM: Float) {
    distanceTo(other).isLessThanOrEqualTo(distanceM)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isFaraway]
 */
fun Assert<Location>.isFaraway(other: Location, distanceM: Float) {
    distanceTo(other).isGreaterThanOrEqualTo(distanceM)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.bearingTo]
 */
fun Assert<Location>.bearingTo(latitude: Double, longitude: Double) = bearingTo(
    Location("").apply {
        this.latitude = latitude
        this.longitude = longitude
    },
)

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.bearingTo]
 */
fun Assert<Location>.bearingTo(other: Location) = transform { actual ->
    actual.bearingTo(other)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasProvider]
 */
fun Assert<Location>.hasProvider(provider: String) {
    provider().isEqualTo(provider)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.doesNotHaveProvider]
 */
fun Assert<Location>.doesNotHaveProvider(provider: String) {
    provider().isNotEqualTo(provider)
}

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasAltitude]
 */
fun Assert<Location>.hasAltitude() = prop("altitude") { actual -> actual.hasAltitude() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasSpeed]
 */
fun Assert<Location>.hasSpeed() = prop("speed") { actual -> actual.hasSpeed() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasSpeedAccuracy]
 */
fun Assert<Location>.hasSpeedAccuracy() = prop("speedAccuracy") { actual -> actual.hasSpeedAccuracy() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasBearing]
 */
fun Assert<Location>.hasBearing() = prop("bearing") { actual -> actual.hasBearing() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasBearingAccuracy]
 */
fun Assert<Location>.hasBearingAccuracy() = prop("bearingAccuracy") { actual -> actual.hasBearingAccuracy() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasAccuracy]
 */
fun Assert<Location>.hasAccuracy() = prop("accuracy") { actual -> actual.hasAccuracy() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.hasVerticalAccuracy]
 */
fun Assert<Location>.hasVerticalAccuracy() = prop("verticalAccuracy") { actual -> actual.hasVerticalAccuracy() }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isMock]
 */
fun Assert<Location>.isMock() = prop("isMock") { actual -> actual.isMock }.isTrue()

/**
 * assertk extension of [androidx.test.ext.truth.location.LocationSubject.isNotMock]
 */
fun Assert<Location>.isNotMock() = prop("isMock") { actual -> actual.isMock }.isFalse()
