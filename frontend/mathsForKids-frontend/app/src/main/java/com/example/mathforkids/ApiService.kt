package com.example.mathforkids

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- MODELS ---
data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(
    val username: String, val password: String,
    @SerializedName("full_name") val fullName: String, val role: String
)

data class GameResultRequest(
    @SerializedName("user_id") val userId: Int,
    val score: Float,
    @SerializedName("correct_count") val correctCount: Int,
    @SerializedName("total_questions") val totalQuestions: Int
)

// [MỚI] Model gửi yêu cầu kết nối phụ huynh - con
data class LinkRequest(
    @SerializedName("parent_id") val parentId: Int,
    @SerializedName("student_username") val studentUsername: String
)

data class BaseResponse<T>(val status: String, val message: String?, val data: T?)

data class UserData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    val avatar: String
)

data class DashboardData(
    @SerializedName("total") val totalQuestions: Int,
    @SerializedName("correct") val totalCorrect: Int
)

// --- INTERFACE ---
interface ApiService {
    @POST("login")
    fun login(@Body request: LoginRequest): Call<BaseResponse<UserData>>

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<BaseResponse<Any>>

    @POST("save_result")
    fun saveResult(@Body request: GameResultRequest): Call<BaseResponse<Any>>

    @GET("dashboard/{userId}")
    fun getDashboard(@Path("userId") userId: Int): Call<BaseResponse<DashboardData>>

    // [MỚI] API Kết nối với con
    @POST("link_student")
    fun linkStudent(@Body request: LinkRequest): Call<BaseResponse<Any>>

    // [MỚI] API Lấy ID của con để xem thống kê
    @GET("get_my_child/{parentId}")
    fun getMyChild(@Path("parentId") parentId: Int): Call<BaseResponse<Int>>

    companion object {
        //private const val BASE_URL = "http://10.0.2.2:8000/"
        private const val BASE_URL = "http://192.168.1.9:8000/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService::class.java)
        }
    }
}