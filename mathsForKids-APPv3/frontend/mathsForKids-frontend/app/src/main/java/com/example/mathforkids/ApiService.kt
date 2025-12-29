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

// [ĐÃ SỬA] Thêm thông tin GameType và LevelID để lưu tiến độ chính xác
data class GameResultRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("game_type") val gameType: String, // VD: "ADDITION", "COUNTING"
    @SerializedName("level_id") val levelId: Int,      // VD: 2001, 2002
    val score: Float,
    @SerializedName("correct_count") val correctCount: Int,
    @SerializedName("total_questions") val totalQuestions: Int
)

// Model gửi yêu cầu kết nối phụ huynh - con
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

// [ĐÃ SỬA] Thêm danh sách level đã hoàn thành để vẽ Map
data class DashboardData(
    @SerializedName("total") val totalQuestions: Int,
    @SerializedName("correct") val totalCorrect: Int,
    @SerializedName("completed_levels") val completedLevels: List<Int> = emptyList() // VD: [1001, 1002, 2001]
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

    // API Kết nối với con
    @POST("link_student")
    fun linkStudent(@Body request: LinkRequest): Call<BaseResponse<Any>>

    // API Lấy ID của con để xem thống kê
    @GET("get_my_child/{parentId}")
    fun getMyChild(@Path("parentId") parentId: Int): Call<BaseResponse<Int>>

    companion object {
        //private const val BASE_URL = "http://10.0.2.2:8000/"
    //}
        // Thay đổi IP này theo địa chỉ máy tính chạy Server của bạn
        private const val BASE_URL = " https://cigarettes-con-involving-giving.trycloudflare.com  "

       fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiService::class.java)
        }
    }
    // companion object {
         // BASE_URL mặc định (sẽ bị RemoteConfig cập nhật lúc app mở)
  //       @Volatile private var BASE_URL: String = "https://example.com/"

    //     fun updateBaseUrl(newUrl: String) {
     //        val u = newUrl.trim()
    //         if (u.isNotEmpty()) {
   //              BASE_URL = if (u.endsWith("/")) u else "$u/"
    //         }
    //     }

   //      fun create(): ApiService {
   //          return Retrofit.Builder()
     //            .baseUrl(BASE_URL)
     //            .addConverterFactory(GsonConverterFactory.create())
    //             .build()
    //             .create(ApiService::class.java)
   //      }
  //   }

}