package com.dwojnar.workouttime.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiRepository() {
    val api = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:7207/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WorkoutApi::class.java)

    suspend fun downloadWorkouts(): List<WorkoutDto> {
        return api.downloadWorkouts()
    }
    suspend fun downloadWorkout(id: String): WorkoutDto {
        return api.downloadWorkout(id)
    }
    suspend fun uploadWorkout(workoutDto: WorkoutDto) {
        return api.uploadWorkout(workoutDto)
    }
}