

package utils

import Env
import isSnapshot

data class PublishConfigurableResult(
    val isSnapshotOnly: Boolean,
    val isReleaseOnly: Boolean,
    val isPublishConfigurable: Boolean = when {
        isSnapshotOnly -> isSnapshot()
        isReleaseOnly -> !isSnapshot()
        else -> true
    },
)


fun checkPublishConfigurable(): PublishConfigurableResult {
    val isSnapshotOnly =
        (System.getProperty("snapshotOnly") ?: System.getenv(Env.SNAPSHOT_ONLY))?.equals("true", true) == true
    val isReleaseOnly =
        (System.getProperty("releaseOnly") ?: System.getenv(Env.RELEASES_ONLY))?.equals("true", true) == true

    return PublishConfigurableResult(isSnapshotOnly, isReleaseOnly)
}

inline fun checkPublishConfigurable(block: PublishConfigurableResult.() -> Unit) {
    val v = checkPublishConfigurable()
    if (v.isPublishConfigurable) {
        v.block()
    }
}
