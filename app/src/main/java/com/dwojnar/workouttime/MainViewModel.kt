package com.dwojnar.workouttime

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwojnar.workouttime.R
import com.dwojnar.workouttime.api.ApiRepository
import com.dwojnar.workouttime.api.ExerciseDto
import com.dwojnar.workouttime.api.WorkoutApi
import com.dwojnar.workouttime.api.WorkoutDto
import com.dwojnar.workouttime.models.Database
import com.dwojnar.workouttime.models.Exercise
import com.dwojnar.workouttime.models.Workout
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseFile = "database.json"
    private val context = getApplication<Application>().applicationContext

    val repository = ApiRepository()

    val workouts = mutableStateListOf<Workout>()
    val downloadedWorkouts = mutableStateListOf<Workout>()
    val currentWorkout = mutableStateOf<Workout>(Workout("", "", mutableStateListOf()))

    fun downloadWorkout(uid: String){
        viewModelScope.launch {
            try {
                val workoutDto = repository.downloadWorkout(uid);
                val downExercises = mutableStateListOf<Exercise>()
                downExercises.addAll(workoutDto.exercises.map { x ->
                    Exercise(uid = x.uid, name = x.name, restBetweenSets = x.restBetweenSets, restAfter = x.restAfter, sets = x.sets)
                })
                val workout = Workout(uid = workoutDto.uid, name = workoutDto.name, exercises = downExercises)
                saveWorkout(workout)
            }
            catch(e : Exception){
                println(e.message)
            }
        }
    }

    fun uploadWorkout(workout: Workout){
        viewModelScope.launch {
            try {
                val workoutDto = WorkoutDto(uid = workout.uid, name = workout.name, exercises = workout.exercises.map { exercise ->
                    ExerciseDto(uid = exercise.uid, name = exercise.name, restBetweenSets = exercise.restBetweenSets, restAfter = exercise.restAfter, sets = exercise.sets)
                })
                repository.uploadWorkout(workoutDto);
            }
            catch(e : Exception){
                println(e.message)
            }
        }
    }

    fun saveWorkout(workout: Workout){
        if(workouts.filter{ it.uid == workout.uid }.isEmpty()){
            addWorkout(workout)
        } else {
            updateWorkout(workout)
        }
    }

    fun downloadWorkouts() {
        viewModelScope.launch {
            downloadedWorkouts.clear()
            try {
                val workoutDtos = repository.downloadWorkouts()
                val downWorkouts = mutableListOf<Workout>()
                workoutDtos.forEach() { x ->
                    val downExercises = mutableStateListOf<Exercise>()
                    downExercises.addAll(x.exercises.map { y ->
                        Exercise(uid = y.uid, name = y.name, restBetweenSets = y.restBetweenSets, restAfter = y.restAfter, sets = y.sets)
                    })
                    downWorkouts.add(Workout(uid = x.uid, name = x.name, exercises = downExercises))
                }
                downloadedWorkouts.addAll(downWorkouts)
            }
            catch(e : Exception){
                println(e.message)
            }
        }
    }

    fun removeExercise(workout: Workout, exerciseUid: String){
        workout.exercises.remove(workout.exercises.first{ it.uid == exerciseUid })
        updateWorkout(workout)
    }

    fun addExercise(workout: Workout, exercise: Exercise){
        workout.exercises.add(exercise)
        updateWorkout(workout)
    }

    fun removeWorkout(uid: String){
        var file = File(context.filesDir, databaseFile);
        val bufferedReader = file.bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        val database = Gson().fromJson(inputString, Database::class.java)
        database.workouts.remove(database.workouts.first { it.uid == uid})
        val fileContents = Gson().toJson(database)
        context.openFileOutput(databaseFile, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
        workouts.remove(workouts.first { it.uid == uid})
    }

    fun updateWorkout(newWorkout: Workout){
        var file = File(context.filesDir, databaseFile);
        val bufferedReader = file.bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        val database = Gson().fromJson(inputString, Database::class.java)
        database.workouts.remove(database.workouts.first { it.uid == newWorkout.uid})
        database.workouts.add(newWorkout)
        val fileContents = Gson().toJson(database)
        context.openFileOutput(databaseFile, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
        workouts.remove(workouts.first { it.uid == newWorkout.uid})
        workouts.add(newWorkout)
    }

    fun loadWorkouts() {
        try {
            var file = File(context.filesDir, databaseFile);
            if (!file.exists()) {
                val fileContents = Gson().toJson(Database(mutableListOf<Workout>()))
                context.openFileOutput(databaseFile, Context.MODE_PRIVATE).use {
                    it.write(fileContents.toByteArray())
                }
                file = File(context.filesDir, databaseFile);
            }
            val bufferedReader = file.bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            println(inputString)
            val database = Gson().fromJson(inputString, Database::class.java)
            workouts.addAll(database.workouts)
        }
        catch (e : Exception){
            val fileContents = Gson().toJson(Database(mutableListOf<Workout>()))
            context.openFileOutput(databaseFile, Context.MODE_PRIVATE).use {
                it.write(fileContents.toByteArray())
            }
            throw e
        }
    }

    fun addWorkout(workout: Workout){
        var file = File(context.filesDir, databaseFile);
        val bufferedReader = file.bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        val database = Gson().fromJson(inputString, Database::class.java)
        database.workouts.add(workout)
        val fileContents = Gson().toJson(database)
        context.openFileOutput(databaseFile, Context.MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }
        workouts.add(workout)
    }
}