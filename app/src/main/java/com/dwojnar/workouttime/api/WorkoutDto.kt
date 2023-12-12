package com.dwojnar.workouttime.api

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dwojnar.workouttime.models.Exercise

data class WorkoutDto (
    var uid: String,
    var name: String,
    var exercises: List<ExerciseDto>
)