package com.tt.gitmusic.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tt.gitmusic.R
import com.tt.gitmusic.databinding.ActivityLoginBinding
import com.tt.gitmusic.model.AccessToken
import com.tt.gitmusic.di.getHttpLoggingInterceptor
import org.koin.android.ext.android.inject
import com.tt.gitmusic.di.provideOkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val sharePref: SharedPreferences by inject()

    // Create Github Oauth app in Github Developer Setting
    private val clientId = AppConfig.clientId
    private val clientSecret = AppConfig.clientSecret
    private val redirectUri = AppConfig.redirectUri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/login/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUri&scope=repo"))
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val uri: Uri? = intent.data
        if (uri != null && uri.toString().startsWith(redirectUri)) {
            // use the parameter your API exposes for the code (mostly it's "code")
            val code = uri.getQueryParameter("code")
            if (code != null) {
                val retrofit = Retrofit.Builder()
                        .baseUrl("https://github.com/")
                        .client(provideOkHttpClient(getHttpLoggingInterceptor()))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                val githubLoginService = retrofit.create(GithubLoginService::class.java)
                githubLoginService.login(clientId, clientSecret, code).enqueue(object : Callback<AccessToken> {
                    override fun onResponse(call: Call<AccessToken>,
                                            response: Response<AccessToken>) {
                        val accessToken = response.body()
                        if (accessToken != null) {
                            sharePref.edit().putString(getString(R.string.token),
                                    "${accessToken.tokenType} ${accessToken.accessToken}").apply()
                            startActivity(Intent(this@Login, MainActivity::class.java))
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<AccessToken>,
                                           t: Throwable) {
                        Toast.makeText(this@Login, "Network is down", Toast.LENGTH_SHORT).show()
                    }
                })
            } else if (uri.getQueryParameter("error") != null) {
                Toast.makeText(this, "Login has problem", Toast.LENGTH_SHORT).show()
            }
        }

        val token = sharePref.getString(getString(R.string.token), "")

        if (token != null) {
            if (!token.matches(Regex(""))) {
                startActivity(Intent(this@Login, MainActivity::class.java))
                finish()
            }
        }

    }
}

interface GithubLoginService {
    @Headers("Accept: application/json")
    @POST("login/oauth/access_token")
    fun login(@Query("client_id") clientId: String,
              @Query("client_secret") client_secret: String,
              @Query("code") code: String): Call<AccessToken>
}
