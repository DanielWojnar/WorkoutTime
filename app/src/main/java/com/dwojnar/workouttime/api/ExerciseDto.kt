package com.dwojnar.workouttime.api

data class ExerciseDto(
    var uid: String,
    var name: String,
    var restBetweenSets: Float,
    var restAfter: Float,
    var sets: Int
)