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
            val cleanNumber = mobileNumber.trim().replace(" ", "").replace("-", "").replace("+", "")
            val targetNumber = if (cleanNumber.length == 10) cleanNumber else cleanNumber.takeLast(10)

            val subManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            } else null

            val subList = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    subManager?.activeSubscriptionInfoList
                } else null
            } catch (e: SecurityException) {
                null
            }

            val subId = if (!subList.isNullOrEmpty() && simSlot in subList.indices) {
                subList[simSlot].subscriptionId
            } else if (!subList.isNullOrEmpty()) {
                subList[0].subscriptionId
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SubscriptionManager.getDefaultSubscriptionId()
                } else {
                    -1
                }
            }

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val base = context.getSystemService(SmsManager::class.java)
                if (subId != -1 && subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    base.createForSubscriptionId(subId)
                } else {
                    base
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && subId != -1 && subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(targetNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(targetNumber, null, message, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to sending intent if direct SMS fails / lacks runtime permission
            openSmsApp(context, mobileNumber, message, simSlot)
            false
        }
    }

    fun openSmsApp(
        context: Context,
        mobileNumber: String,
        message: String,
        simSlot: Int = 0
    ) {
        try {
            val subManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            } else null

            val subList = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    subManager?.activeSubscriptionInfoList
                } else null
            } catch (e: SecurityException) {
                null
            }

            val subId = if (!subList.isNullOrEmpty() && simSlot in subList.indices) {
                subList[simSlot].subscriptionId
            } else null

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:$mobileNumber")
                putExtra("sms_body", message)
                // Common extras for OEM dual-SIM SMS apps
                putExtra("simSlot", simSlot)
                putExtra("slot", simSlot)
                putExtra("phone_id", simSlot)
                putExtra("com.android.phone.extra.slot", simSlot)
                if (subId != null) {
                    putExtra("subscription", subId)
                    putExtra("sub_id", subId)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subId)
                    }
                }
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

