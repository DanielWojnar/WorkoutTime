package com.dwojnar.workouttime.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkoutApi {

    @GET("/Workout/GetWorkouts")
    suspend fun downloadWorkouts(): List<WorkoutDto>

    @GET("/Workout/GetWorkout/{id}")
    suspend fun downloadWorkout(@Path("id") id: String): WorkoutDto

    @POST("/Workout/AddWorkout")
    suspend fun uploadWorkout(@Body workoutDto: WorkoutDto)
}