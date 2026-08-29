package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast

object SmsHelper {

    fun sendSmsViaSim(
        context: Context,
        mobileNumber: String,
        message: String,
        simSlot: Int = 0 // 0 for SIM 1, 1 for SIM 2
    ): Boolean {
        return try {
            val cleanNumber = mobileNumber.trim().replace(" ", "").replace("-", "")
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val subList = try {
                    subManager?.activeSubscriptionInfoList
                } catch (e: SecurityException) {
                    null
                }
                if (!subList.isNullOrEmpty() && simSlot in subList.indices) {
                    val subId = subList[simSlot].subscriptionId
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(cleanNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(cleanNumber, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to sending intent if direct SMS fails / lacks runtime permission
            openSmsApp(context, mobileNumber, message)
            false
        }
    }

    fun openSmsApp(context: Context, mobileNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$mobileNumber")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open SMS app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, mobileNumber: String, message: String) {
        try {
            var formattedNumber = mobileNumber.trim().replace(" ", "").replace("-", "").replace("+", "")
            if (!formattedNumber.startsWith("91") && formattedNumber.length == 10) {
                formattedNumber = "91$formattedNumber"
            }
            val url = "https://api.whatsapp.com/send?phone=$formattedNumber&text=" + Uri.encode(message)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp nahi mila: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
