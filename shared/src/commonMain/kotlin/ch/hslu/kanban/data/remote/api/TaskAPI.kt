package ch.hslu.kanban.data.remote.api

import ch.hslu.kanban.SERVER_IP
import ch.hslu.kanban.domain.entity.Task
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class TaskApi() {

    suspend fun getTasks(): List<Task> {
        return try {
            ApiClient.client.get("${SERVER_IP}/tasks").body<List<Task>>()
        } catch (e: Throwable) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addTask(task: Task):Boolean {
        return try {
            val response = ApiClient.client.post("${SERVER_IP}/tasks") {
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            return response.status == HttpStatusCode.Created
                    || response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTask(task: Task) :Boolean {
        return try {
            val response = ApiClient.client.put("${SERVER_IP}/tasks/${task.id}") {
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(id: Long): Boolean {
        return try {
            val response = ApiClient.client.delete("${SERVER_IP}/tasks/$id")
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun replaceTasks(tasks: List<Task>): Boolean {
        return try {
            val response = ApiClient.client.post("${SERVER_IP}/tasks/replace") {
                contentType(ContentType.Application.Json)
                setBody(tasks)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

}