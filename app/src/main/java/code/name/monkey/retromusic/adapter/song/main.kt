package code.name.monkey.retromusic.adapter.song

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

//fun main() = runBlocking {
//    launch {
//        doWorld()
//    }
//    println("Done")
//}

//fun main() = runBlocking {
//    val job = launch {
//        delay(1000L)
//        println("World!")
//    }
//    println("Hello")
//    job.join() // chờ hoàn thành
//    println("Done")
//}

//fun main() = runBlocking {
//    supervisorScope { // giúp các coroutine con không bị chết do một coroutine con bị chết
//        launch {
//            throw Exception("Error in Task 1")
//        }
//        launch {
//            delay(1000)
//            println("Task 2 completed")
//        }
//    }
//    println("Finished")
//}

//fun main() = runBlocking {
//    val deferreds: List<Deferred<Int>> = (1..9).map {
//        async {
//            delay(1000L * it)
//            println("Loading $it")
//            it
//        }
//    }
//    println("start loading!")
//    val sum = deferreds.awaitAll().sum()
//    println("$sum")
//    println("Loaded!")
//}


fun getUsers(): Flow<String> = flow {
    println("▶️ Bắt đầu tải dữ liệu...")
    emit("Alice")
    delay(500)
    emit("Bob")
    delay(500)
    emit("Charlie")
    println("✅ Tải xong!")
}.flowOn(Dispatchers.IO)

fun main() = runBlocking {
    println("🟡 Khởi động collect")

    getUsers()
        .onStart { println("⏳ Đang chuẩn bị...") }
        .map { user ->
            println("🔧 Đang xử lý $user")
            user.uppercase()
        }
//        .onEach { println("📦 Đã sẵn sàng: $it") }
//        .catch { e -> println("❌ Lỗi: ${e.message}") }
        .collect { finalUser ->
            println("✅ Hiển thị: $finalUser")
        }


    println("🏁 Kết thúc")
}


//fun main() = runBlocking  {
//    val job = launch {
//        repeat(1000) { i ->
//            println("job: I'm sleeping $i ...")
//            delay(500L)
//        }
//    }
//    delay(1300L)
//    println("main: I'm tired of waiting!")
//    job.cancel() // cancels the job
//    job.join() // waits for job's completion
//    println("main: Now I can quit.")
//}


//fun main(): kotlin.Unit = runBlocking {
//    val startTime = System.currentTimeMillis()
//    val job = launch(Dispatchers.Default) {
//        var nextPrintTime = startTime
//        var i = 0
//        while (i < 5) { // computation loop, just wastes CPU
//            // print a message twice a second
//            if (System.currentTimeMillis() >= nextPrintTime) {
//                println("job: I'm sleeping ${i++} ...")
//                nextPrintTime += 500L
//            }
//        }
//    }
//    delay(1300L) // delay a bit
//    println("main: I'm tired of waiting!")
//    job.cancelAndJoin() // cancels the job and waits for its completion
//    println("main: Now I can quit.")
//}

suspend fun loadData(): Int {
    println("Loading...")
    delay(1000L)
    println("Loaded!")
    return 42
}


// Concurrently executes both sections
suspend fun doWorld() = coroutineScope { // this: CoroutineScope
    launch {
        delay(2000L)
        println("World 2")
    }
    launch {
        delay(1000L)
        println("World 1")
    }
    println("Hello")
}