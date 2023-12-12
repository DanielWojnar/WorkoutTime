package com.dwojnar.workouttime

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dwojnar.workouttime.models.Exercise
import com.dwojnar.workouttime.models.Workout
import com.dwojnar.workouttime.ui.theme.WorkoutTimeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkoutTimeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),

                ) {
                    AndroidApp("Android")
                }
            }
        }
    }
}

@Composable
fun AndroidApp(greetingName: String) {
    val navController = rememberNavController()
    val mainViewModel = viewModel<MainViewModel>()
    mainViewModel.loadWorkouts()
    NavHost(
        navController = navController,
        startDestination = "mainMenu"
    ) {
        composable("mainMenu") {
            MainMenuView(
                clickStart = { navController.navigate("StartMenu") },
                clickAdd = { navController.navigate("AddMenu") },
                clickDownload = {
                    mainViewModel.downloadWorkouts()
                    navController.navigate("DownloadMenu")
                },
            )
        }
        composable("StartMenu") {
            StartMenuView(
                workouts = mainViewModel.workouts,
                goToWorkout = { workout ->
                    mainViewModel.currentWorkout.value = workout
                    navController.navigate("PlayWorkout")
                }
            )
        }
        composable("AddMenu") {
            AddMenuView(
                workouts = mainViewModel.workouts,
                createNew = { navController.navigate("AddWorkoutName") },
                removeWorkout = { uid -> mainViewModel.removeWorkout(uid) },
                goToWorkout = { workout ->
                    mainViewModel.currentWorkout.value = workout
                    navController.navigate("WorkoutEdit")
                }
            )
        }
        composable("DownloadMenu") {
            DownloadMenuView(
                downloadedWorkouts = mainViewModel.downloadedWorkouts,
                save = { workout -> mainViewModel.saveWorkout(workout) },
                openSearch = { navController.navigate("SearchWorkout") }
            )
        }
        composable("AddWorkoutName") {
            AddWorkoutNameView(
                addWorkout = { workout -> mainViewModel.addWorkout(workout) },
                goBack = { navController.popBackStack() }
            )
        }
        composable("WorkoutEdit") {
            WorkoutEditView(
                workout = mainViewModel.currentWorkout.value,
                createNew = { navController.navigate("AddExercise") },
                removeExercise = { workout, exerciseUid ->
                    mainViewModel.removeExercise(
                        workout,
                        exerciseUid
                    )
                },
                upload = { workout -> mainViewModel.uploadWorkout(workout) }
            )
        }
        composable("AddExercise") {
            AddExerciseView(
                workout = mainViewModel.currentWorkout.value,
                addExercise = { workout, exercise ->
                    mainViewModel.addExercise(
                        workout,
                        exercise
                    )
                },
                goBack = { navController.popBackStack() }
            )
        }
        composable("PlayWorkout") {
            PlayWorkoutView(
                workout = mainViewModel.currentWorkout.value
            )
        }
        composable("SearchWorkout") {
            SearchWorkoutView(
                downloadWorkout = { workoutUid -> mainViewModel.downloadWorkout(workoutUid) },
                goBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWorkoutView(downloadWorkout: (String) -> Unit, goBack: () -> Unit){
    val workoutUid = remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp, start = 20.dp, end = 20.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Enter share code", fontSize = 30.sp)
            TextField(
                value = workoutUid.value,
                onValueChange = { workoutUid.value = it },
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .border(3.dp, Color.White),
                colors = TextFieldDefaults.textFieldColors(Color.White),
                singleLine = true,
            )
            Button(onClick = {
                downloadWorkout(workoutUid.value)
                goBack()
            }, enabled = workoutUid.value.isNotEmpty(),modifier = Modifier
                .size(70.dp)
                .border(3.dp, Color.White, RoundedCornerShape(100)),
                colors = ButtonDefaults.buttonColors(Color.Black)) {
                Icon(painter = painterResource(id = R.drawable.check_solid), contentDescription = "add", tint = Color.White)
            }
        }
    }
}

@Composable
fun DownloadMenuView(downloadedWorkouts: List<Workout>, save: (Workout) -> Unit, openSearch: () -> Unit){
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            if(downloadedWorkouts.isNotEmpty()){
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    item{
                        Button(onClick = { openSearch() }, modifier = Modifier
                            .size(70.dp)
                            .border(3.dp, Color.White, RoundedCornerShape(100)),
                            colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                            Icon(painter = painterResource(id = R.drawable.magnifying_glass_solid), contentDescription = "search", tint = Color.White)
                        }
                    }
                    items(downloadedWorkouts.size){ index ->
                        Row(modifier = Modifier
                            .height(90.dp)
                            .padding(top = 10.dp, bottom = 10.dp)
                            .fillMaxWidth()){
                            Button(onClick = { save(downloadedWorkouts[index]) } , modifier = Modifier
                                .height(70.dp)
                                .weight(1f)
                                .border(3.dp, Color.White, RoundedCornerShape(50)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                                Text(text = " ${downloadedWorkouts[index].name}", fontSize = 20.sp, color = Color.White, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.padding(start = 2.dp, end = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun timerFlowUp(startTime: Float): Flow<Float> = flow {
    var time = startTime
    while (true) {
        delay(100.milliseconds)
        emit(time)
        time = time + 0.1f
    }
}

fun timerFlowDown(startTime: Float): Flow<Float> = flow {
    var time = startTime
    while (true) {
        delay(100.milliseconds)
        emit(time)
        time = time - 0.1f
    }
}

@Composable
fun PlayWorkoutView(workout: Workout){
    val index = remember { mutableStateOf(0 ) }
    val currentSet = remember { mutableStateOf(0 ) }
    val state = remember { mutableStateOf(0 ) }
    //state = 0 : resting between sets
    //state = 1 : working out
    //state = 2 : resting after exercise
    val time = remember { mutableStateOf(0f ) }
    val finished = remember { mutableStateOf( false ) }
    val job = remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            if(!finished.value){
                Text(text = workout.exercises[index.value].name, fontSize = 20.sp)
                Text(text = currentSet.value.toString() + "/" + workout.exercises[index.value].sets.toString(), fontSize = 20.sp)
                Text(text = String.format("%.1f" ,time.value), fontSize = 20.sp)
                Text(text = if(state.value != 1) "Resting" else "Working out", fontSize = 20.sp)
                Button(onClick = {
                    job.value?.cancel()
                    if(state.value == 0){
                        state.value = 1
                        currentSet.value = currentSet.value + 1
                        job.value = coroutineScope.launch {
                            timerFlowUp(0f).collect {
                                time.value = it
                            }
                        }
                    }
                    else if(state.value == 1){
                        if(currentSet.value == workout.exercises[index.value].sets){
                            state.value = 2
                            job.value = coroutineScope.launch {
                                timerFlowDown(workout.exercises[index.value].restAfter).collect {
                                    time.value = it
                                }
                            }
                        } else{
                            state.value = 0
                            job.value = coroutineScope.launch {
                                timerFlowDown(workout.exercises[index.value].restBetweenSets).collect {
                                    time.value = it
                                }
                            }
                        }
                    }
                    else {
                        state.value = 0
                        currentSet.value = 0
                        index.value = index.value + 1
                        time.value = 0f
                        if(index.value >= workout.exercises.size){
                            finished.value = true
                        }
                    }
                }, modifier = Modifier
                    .size(70.dp)
                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                    colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                    Icon(painter = painterResource(id = R.drawable.chevron_right_solid), contentDescription = "next", tint = Color.White)
                }
            } else {
                Text(text = "Workout finished", fontSize = 30.sp)
            }
        }
    }
}

@Composable
fun StartMenuView(workouts: List<Workout>, goToWorkout: (Workout) -> Unit){
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            if(workouts.isNotEmpty()){
                LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                    items(workouts.size){ index ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                                .height(90.dp)
                        ) {
                            if (workouts[workouts.size - 1 - index].exercises.size == 0) {
                                Button(onClick = {  } , modifier = Modifier
                                    .height(70.dp)
                                    .fillMaxWidth()
                                    .border(3.dp, Color.White, RoundedCornerShape(50)),
                                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                                    enabled = false) {
                                    Text(text = "empty - ${workouts[workouts.size - 1 - index].name}", fontSize = 30.sp, color = Color.White, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.padding(start = 2.dp, end = 2.dp))
                                }
                            } else {
                                Button(onClick = { goToWorkout(workouts[workouts.size - 1 - index]) } , modifier = Modifier
                                    .height(70.dp)
                                    .fillMaxWidth()
                                    .border(3.dp, Color.White, RoundedCornerShape(50)),
                                    colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                                    Text(text = " ${workouts[workouts.size - 1 - index].name}", fontSize = 30.sp, color = Color.White, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.padding(start = 2.dp, end = 2.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Text(text = "No workout found", fontSize = 30.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseView(workout: Workout, addExercise: (Workout, Exercise) -> Unit, goBack: () -> Unit){
    val restAfter = remember { mutableStateOf(180f ) }
    val restBetween = remember { mutableStateOf( 90f ) }
    val sets = remember { mutableStateOf( 5 ) }
    val exerciseName = remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Exercise name", fontSize = 30.sp)
                        TextField(
                            value = exerciseName.value,
                            onValueChange = { exerciseName.value = it },
                            modifier = Modifier
                                .height(60.dp)
                                .fillMaxWidth()
                                .border(3.dp, Color.White),
                            colors = TextFieldDefaults.textFieldColors(Color.White),
                            singleLine = true,
                        )
                    }
                }
                item {
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(text = "Sets", fontSize = 30.sp)
                        Row(
                            modifier = Modifier
                                .height(70.dp)
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = { sets.value = sets.value + 1 }, modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.plus_solid),
                                    contentDescription = "add",
                                    tint = Color.White
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                                    .height(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100))
                            ) {
                                Text(
                                    text = sets.value.toString() + " sets",
                                    color = Color.White,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp
                                )
                            }
                            Button(
                                onClick = {
                                    sets.value = sets.value - 1
                                    if (sets.value < 1) {
                                        sets.value = 1
                                    }
                                }, modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.minus_solid),
                                    contentDescription = "subtract",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(text = "Rest time between sets", fontSize = 30.sp)
                        Row(
                            modifier = Modifier
                                .height(70.dp)
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = { restBetween.value = restBetween.value + 15f },
                                modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.plus_solid),
                                    contentDescription = "add",
                                    tint = Color.White
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                                    .height(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100))
                            ) {
                                Text(
                                    text = String.format("%.0f",restBetween.value) + " seconds",
                                    color = Color.White,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp
                                )
                            }
                            Button(
                                onClick = {
                                    restBetween.value = restBetween.value - 15f
                                    if (restBetween.value < 0f) {
                                        restBetween.value = 0f
                                    }
                                }, modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.minus_solid),
                                    contentDescription = "subtract",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(top = 15.dp, bottom = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(text = "Rest time after", fontSize = 30.sp)
                        Row(
                            modifier = Modifier
                                .height(70.dp)
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = { restAfter.value = restAfter.value + 15f },
                                modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.plus_solid),
                                    contentDescription = "add",
                                    tint = Color.White
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                                    .height(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100))
                            ) {
                                Text(
                                    text = String.format("%.0f",restAfter.value) + " seconds",
                                    color = Color.White,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    fontSize = 20.sp
                                )
                            }
                            Button(
                                onClick = {
                                    restAfter.value = restAfter.value - 15f
                                    if (restAfter.value < 0f) {
                                        restAfter.value = 0f
                                    }
                                }, modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, Color.White, RoundedCornerShape(100)),
                                colors = ButtonDefaults.buttonColors(Color.Transparent)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.minus_solid),
                                    contentDescription = "subtract",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            val newExercise = Exercise(
                                uid = UUID.randomUUID().toString(),
                                name = exerciseName.value,
                                restAfter = restAfter.value,
                                restBetweenSets = restBetween.value,
                                sets = sets.value
                            )
                            addExercise(workout, newExercise)
                            goBack()
                        }, enabled = exerciseName.value.isNotEmpty(), modifier = Modifier
                            .size(70.dp)
                            .border(3.dp, Color.White, RoundedCornerShape(100)),
                        colors = ButtonDefaults.buttonColors(Color.Transparent)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.check_solid),
                            contentDescription = "add",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutEditView(workout: Workout, createNew: () -> Unit, removeExercise: (Workout, String) -> Unit, upload: (Workout) -> Unit){
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {

            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                item{
                    Button(onClick = { createNew() }, modifier = Modifier
                        .size(70.dp)
                        .border(3.dp, Color.White, RoundedCornerShape(100)),
                        colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                        Icon(painter = painterResource(id = R.drawable.plus_solid), contentDescription = "add", tint = Color.White)
                    }
                }
                items(workout.exercises.size){ index ->
                    Row(modifier = Modifier
                        .height(90.dp)
                        .padding(start = 15.dp, top = 10.dp, bottom = 10.dp)
                        .fillMaxWidth()){
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f)
                                .height(70.dp)
                                .border(3.dp, Color.White, RoundedCornerShape(100))
                        ) {
                            Text(
                                text = " ${workout.exercises[index].name}",
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                fontSize = 25.sp

                            )
                        }
                        Button(onClick = { removeExercise(workout, workout.exercises[index].uid) } , modifier = Modifier
                            .size(70.dp)
                            .border(3.dp, Color.White, RoundedCornerShape(100)),
                            colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                            Icon(painter = painterResource(id = R.drawable.xmark_solid), contentDescription = "remove", tint = Color.White)
                        }
                    }
                }
                item{
                    Row(modifier = Modifier
                        .height(75.dp)
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                        horizontalArrangement = Arrangement.Center) {
                        Button(
                            onClick = { upload(workout) }, modifier = Modifier
                                .size(70.dp)
                                .border(3.dp, Color.White, RoundedCornerShape(100)),
                            colors = ButtonDefaults.buttonColors(Color.Transparent)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.upload_solid),
                                contentDescription = "upload",
                                tint = Color.White
                            )
                        }
                    }
                }
                item{
                    Text(text = "Share code\n" + workout.uid, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutNameView(addWorkout: (Workout) -> Unit, goBack: () -> Unit){
    val workoutName = remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(vertical = 15.dp, horizontal = 30.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Enter workout name", color = Color.White, fontSize = 30.sp)
            TextField(
                value = workoutName.value,
                onValueChange = { workoutName.value = it },
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .border(3.dp, Color.White),
                colors = TextFieldDefaults.textFieldColors(Color.White),
                singleLine = true,
            )
            Button(onClick = {
                val newWorkout = Workout(uid = UUID.randomUUID().toString(),
                    name = workoutName.value,
                    exercises = mutableStateListOf<Exercise>()
                )
                addWorkout(newWorkout)
                goBack()
            }, enabled = workoutName.value.isNotEmpty(),modifier = Modifier
                .size(70.dp)
                .border(3.dp, Color.White, RoundedCornerShape(100)),
                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                Icon(painter = painterResource(id = R.drawable.check_solid), contentDescription = "add", tint = Color.White)
            }
        }
    }
}

@Composable
fun AddMenuView(workouts: List<Workout>, createNew: () -> Unit, removeWorkout: (String) -> Unit, goToWorkout: (Workout) -> Unit){
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {

            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                item{
                    Box( modifier = Modifier
                        .padding(bottom = 20.dp)
                    ) {
                        Button(
                            onClick = { createNew() }, modifier = Modifier
                                .size(70.dp)
                                .border(3.dp, Color.White, RoundedCornerShape(100)),
                            colors = ButtonDefaults.buttonColors(Color.Transparent)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.plus_solid),
                                contentDescription = "add",
                                tint = Color.White
                            )
                        }
                    }
                }
                items(workouts.size){ index ->
                    Row(modifier = Modifier
                        .height(80.dp)
                        .padding(bottom = 0.dp)
                        .fillMaxWidth()){
                        Button(onClick = { goToWorkout(workouts[workouts.size - 1 - index]) } , modifier = Modifier
                            .height(70.dp)
                            .weight(1f)
                            .border(3.dp, Color.White, RoundedCornerShape(50)),
                            colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                            Text(text = " ${workouts[workouts.size - 1 - index].name}", fontSize = 20.sp,color = Color.White, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.padding(start = 2.dp, end = 2.dp))
                        }
                        Button(onClick = { removeWorkout(workouts[workouts.size - 1 - index].uid) } , modifier = Modifier
                            .size(70.dp)
                            .border(3.dp, Color.White, RoundedCornerShape(100)),
                            colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                            Icon(painter = painterResource(id = R.drawable.xmark_solid), tint = Color.White, contentDescription = "remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenuView(clickStart: () -> Unit,
                 clickAdd: () -> Unit,
                 clickDownload: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "WorkoutTime", color = Color.White, fontSize = 50.sp, modifier = Modifier.padding(top = 15.dp))
            Button(onClick = { clickStart() } , modifier = Modifier
                .size(70.dp)
                .border(3.dp, Color.White, RoundedCornerShape(100)),
                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                Icon(painter = painterResource(id = R.drawable.play_solid), tint = Color.White, contentDescription = "play")
            }
            Button(onClick = { clickAdd() }, modifier = Modifier
                .size(70.dp)
                .border(3.dp, Color.White, RoundedCornerShape(100)),
                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                Icon(painter = painterResource(id = R.drawable.plus_solid), tint = Color.White, contentDescription = "add")
            }
            Button(onClick = { clickDownload() }, modifier = Modifier
                .size(70.dp)
                .border(3.dp, Color.White, RoundedCornerShape(100)),
                colors = ButtonDefaults.buttonColors(Color.Transparent)) {
                Icon(painter = painterResource(id = R.drawable.download_solid), tint = Color.White, contentDescription = "download")
            }
        }

    }
}

@Preview(showBackground = true, device = Devices.PIXEL_2)
@Composable
fun GreetingPreview() {
    WorkoutTimeTheme {
        AndroidApp("Android")
    }
}