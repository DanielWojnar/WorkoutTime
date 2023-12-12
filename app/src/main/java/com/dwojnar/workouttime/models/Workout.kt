package com.dwojnar.workouttime.models

import androidx.compose.runtime.snapshots.SnapshotStateList

class Workout(
    var uid: String,
    var name: String,
    var exercises: SnapshotStateList<Exercise>
)

