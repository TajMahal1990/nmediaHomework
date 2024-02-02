package ru.netology.nmedia.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import ru.netology.nmedia.dto.Push
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("MyLog", "1=${message.data.toString()}")
        val response = gson.fromJson(message.data[content], Push::class.java)
        Log.d("MyLog", "2=$response")

        val signInUserId = auth.authStateFlow.value.id

        when {
            response.recipientId==signInUserId -> {
                handleLike(response.content)
                Log.d("MyLog", "when1 recId=${response.recipientId} userId=$signInUserId")
            }
            (response.recipientId==0L && response.recipientId!=signInUserId) -> {
                auth.sendPushToken()
                Log.d("MyLog", "when2 recId=${response.recipientId} userId=$signInUserId")
            }
            (response.recipientId!=0L && response.recipientId!=signInUserId && response.recipientId!=null) -> {
                auth.sendPushToken()
                Log.d("MyLog", "when3 recId=${response.recipientId} userId=$signInUserId")
            }
            response.recipientId==null -> {
                handleLike(response.content)
                Log.d("MyLog", "when4 recId=${response.recipientId} userId=$signInUserId")
            }
        }

        message.data[action]?.let {
            when (it) {
                Action.LIKE.toString() -> {
                    val contentLike = gson.fromJson(message.data[content], Like::class.java)
                    val contentTitle = getString(
                        R.string.notification_user_liked,
                        contentLike.userName,
                        contentLike.postAuthor
                    )
                    Log.d("MyLog", "$contentTitle")
                    handleLike(contentTitle)
                }

                Action.POST.toString() -> {
                    val contentPost = gson.fromJson(message.data[content], NewPost::class.java)
                    val contentTitle = getString(
                        R.string.newpost,
                        contentPost.author
                    )
                    val contentText = contentPost.text
                    Log.d("MyLog", "Title: $contentTitle, text:$contentText")
                    handleLike(contentTitle,contentText)
                }

                else -> {
                    handleLike(getString(R.string.warning))
                }
//
            }
        }
    }

    override fun onNewToken(token: String) {
        auth.sendPushToken(token)
        println(token)
    }

    fun handleLike(contentTitle: String, contentText:String="") {

        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(contentText))
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }
}

enum class Action {
    LIKE,
    POST,
}

data class NewPost(
    val author:String,
    val text:String
)

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)