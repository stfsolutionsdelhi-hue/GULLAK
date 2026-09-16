package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

enum class NotificationTarget {
    ALL,
    ADMIN_ONLY,
    MEMBER_ONLY
}

object NotificationHelper {

    const val CHANNEL_ID = "gullak_society_channel_v6_modern"
    private const val CHANNEL_NAME = "Gullak Society Official Alerts"
    private const val CHANNEL_DESC = "Official notices for RD collection, loan dues, bonus and passbook updates."

    // Provider to check whether Admin is currently active and which Member is logged in
    // Returns Pair(isAdminLoggedIn, loggedInMemberId)
    var currentRoleProvider: (() -> Pair<Boolean, String?>)? = null

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                enableLights(true)
                lightColor = Color.rgb(16, 185, 129) // Theme Primary Green
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationLargeIcon(): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark navy circular background matching app theme
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // BgDark / CardDark
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

        // Gold border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11) // AccentGold
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 3f, borderPaint)

        // Bold letter 'G' in gold
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 158, 11)
            textSize = 68f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val yOffset = ((canvas.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2))
        canvas.drawText("₹", size / 2f, yOffset, textPaint)

        return bitmap
    }

    fun sendPushNotification(
        context: Context,
        title: String,
        message: String,
        target: NotificationTarget = NotificationTarget.ALL,
        targetMemberId: String? = null,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt(),
        forceShow: Boolean = false
    ) {
        // Smart Role Routing:
        // Admin device receives Admin alerts (approvals, sync status, dispatch confirmation)
        // Member device receives Member alerts (receipts, PIN changes, due reminders)
        if (!forceShow) {
            val roleInfo = currentRoleProvider?.invoke()
            if (roleInfo != null) {
                val (isAdminLoggedIn, currentMemberId) = roleInfo
                when (target) {
                    NotificationTarget.ADMIN_ONLY -> {
                        // Only show if Admin is logged in or app is in Admin mode
                        if (!isAdminLoggedIn) return
                    }
                    NotificationTarget.MEMBER_ONLY -> {
                        // If Admin is actively logged in, avoid member-specific background noise unless targeted
                        if (isAdminLoggedIn && targetMemberId != null) {
                            // If admin is in admin panel, don't play sound for a single member's reminder
                            return
                        }
                        // If targeted to a specific member ID, check if matching or broadcast
                        if (targetMemberId != null && currentMemberId != null &&
                            !currentMemberId.equals(targetMemberId, ignoreCase = true)
                        ) {
                            return
                        }
                    }
                    NotificationTarget.ALL -> {
                        // System-wide broadcast alerts shown on all devices
                    }
                }
            }
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Format Title into modern clear CAPS with distinct branding
        val cleanTitle = title.trim().ifEmpty { "GULLAK SOCIETY ALERT" }
        val formattedTitle = if (cleanTitle.startsWith("📢") || cleanTitle.startsWith("🔔") || cleanTitle.startsWith("💳") || cleanTitle.startsWith("✅")) {
            val emoji = cleanTitle.take(2)
            val rest = cleanTitle.drop(2).trim().uppercase()
            "$emoji $rest"
        } else {
            "📢 ${cleanTitle.uppercase()}"
        }

        val largeIconBitmap = try {
            createNotificationLargeIcon()
        } catch (_: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_gullak)
            .setContentTitle(formattedTitle)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(formattedTitle)
                    .bigText(message)
                    .setSummaryText("GULLAK SOCIETY")
            )
            .setColor(Color.rgb(16, 185, 129)) // App Theme Primary Green
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)

        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Android 13+ permission not granted yet
        } catch (_: Exception) {
            // Fallback
        }
    }
}
