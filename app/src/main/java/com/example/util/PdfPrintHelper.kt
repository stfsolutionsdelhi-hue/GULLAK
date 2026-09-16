package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.Member
import com.example.data.Payment
import com.example.data.getEffectiveLoanLimit
import com.example.data.getLoanLimitDisplay
import com.example.data.getTotalRdDeposited
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfPrintHelper {

    fun printMemberPassbookHtml(
        context: Context,
        member: Member,
        payments: List<Payment>,
        societyName: String = "GULLAK CO-OPERATIVE SOCIETY"
    ) {
        try {
            val html = generatePassbookHtml(member, payments, societyName)
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service not available on this device", Toast.LENGTH_SHORT).show()
                return
            }

            Handler(Looper.getMainLooper()).post {
                val webView = WebView(context)
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        val cleanName = member.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
                        val jobName = "Passbook_${cleanName}_${member.id}"
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        printManager.print(
                            jobName,
                            printAdapter,
                            PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .build()
                        )
                    }
                }
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to initialize print: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendPassbookToWhatsApp(
        context: Context,
        member: Member,
        payments: List<Payment>,
        societyName: String = "Gullak Co-operative Society"
    ) {
        try {
            val totalRd = member.getTotalRdDeposited(payments)
            val activeLoan = member.gullakLoan + member.emergencyLoan
            val limitDisplay = member.getLoanLimitDisplay(payments)
            val todayStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

            val memberTxns = payments.filter { it.memberId == member.id }

            val sb = StringBuilder()
            sb.append("🏦 *${societyName.uppercase()}*\n")
            sb.append("📜 *MEMBER ACCOUNT PASSBOOK & STATEMENT*\n")
            sb.append("📅 Date: $todayStr\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("👤 *Member:* ${member.name}\n")
            sb.append("🆔 *ID:* ${member.id}\n")
            sb.append("📱 *Mobile:* ${member.mobile}\n")
            sb.append("📍 *Address:* ${member.address.ifEmpty { "Kakrola, Delhi" }}\n")
            sb.append("📅 *Due Date:* ${member.dueDay}\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("💰 *Monthly RD:* ₹${member.monthlyRd}/month\n")
            sb.append("💵 *Accumulated RD Balance:* ₹%,d\n".format(Locale.ENGLISH, totalRd))
            sb.append("📉 *Outstanding Loan:* ₹%,d\n".format(Locale.ENGLISH, activeLoan))
            sb.append("💳 *Eligible Loan Limit:* $limitDisplay\n")
            if (member.penaltyApplicable > 0) {
                sb.append("⚠️ *Penalty Applicable:* ₹${member.penaltyApplicable}\n")
            }
            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📊 *RECENT TRANSACTIONS:*\n")

            if (memberTxns.isEmpty()) {
                sb.append("No transactions recorded in the current active cycle.\n")
            } else {
                memberTxns.take(10).forEachIndexed { idx, p ->
                    sb.append("${idx + 1}. *${p.date}* | ${p.txnId}\n")
                    sb.append("   • RD: ₹${p.rdAmount} | Int: ₹${p.interestAmount} | Loan Repay: ₹${p.loanRepayAmount}\n")
                    sb.append("   • Total Paid: *₹${p.totalAmount}* (${p.mode})\n")
                    if (p.remarks.isNotBlank()) {
                        sb.append("   • Remarks: _${p.remarks}_\n")
                    }
                }
            }

            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🔒 _Official statement generated directly from Gullak Society System._\n")
            sb.append("🙏 _Thank you!_")

            val cleanPhone = member.mobile.filter { it.isDigit() }
            val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
            val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${URLEncoder.encode(sb.toString(), "UTF-8")}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generatePassbookHtml(
        member: Member,
        payments: List<Payment>,
        societyName: String
    ): String {
        val totalRd = member.getTotalRdDeposited(payments)
        val activeLoan = member.gullakLoan + member.emergencyLoan
        val limitDisplay = member.getLoanLimitDisplay(payments)
        val memberTxns = payments.filter { it.memberId == member.id }.sortedBy { it.date }
        val generatedDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

        var runningRd = member.openingRd
        var runningLoan = member.gullakLoan + member.emergencyLoan

        val rowsHtml = StringBuilder()

        // Opening balance row
        rowsHtml.append("""
            <tr style="background:#f8fafc; font-weight:600;">
                <td style="text-align:center;">${member.joinDate}</td>
                <td>OPENING</td>
                <td>Opening Society Balance / Joined</td>
                <td style="text-align:right; color:#059669;">₹${member.openingRd}</td>
                <td style="text-align:right; font-weight:bold; color:#0f172a;">₹$runningRd</td>
                <td style="text-align:right; color:#dc2626;">₹0</td>
                <td style="text-align:right; color:#2563eb;">₹0</td>
                <td style="text-align:right; font-weight:bold;">₹$runningLoan</td>
                <td style="text-align:center;">OPENING</td>
            </tr>
        """.trimIndent())

        if (memberTxns.isEmpty()) {
            rowsHtml.append("""
                <tr>
                    <td colspan="9" style="text-align:center; padding:20px; color:#64748b;">
                        No further payment receipts recorded in this period.
                    </td>
                </tr>
            """.trimIndent())
        } else {
            memberTxns.forEach { p ->
                runningRd += p.rdAmount
                runningLoan = (runningLoan - p.loanRepayAmount).coerceAtLeast(0)

                val markupOrigin = when {
                    p.remarks.contains("Web App", ignoreCase = true) -> "Web App"
                    p.remarks.contains("Admin", ignoreCase = true) -> "Admin Counter"
                    p.remarks.contains("Self", ignoreCase = true) || p.mode.contains("UPI", ignoreCase = true) -> "Member App / UPI"
                    else -> "Society Desk"
                }

                val narration = if (p.remarks.isNotBlank()) p.remarks else "Monthly RD Deposit"

                rowsHtml.append("""
                    <tr>
                        <td style="text-align:center;">${p.date}</td>
                        <td style="font-family:monospace; font-size:11px; font-weight:bold; color:#2563eb;">${p.txnId}</td>
                        <td>
                            <div>${narration}</div>
                            <small style="color:#64748b; font-size:10px;">[Source: $markupOrigin | Mode: ${p.mode}]</small>
                        </td>
                        <td style="text-align:right; color:#059669; font-weight:600;">₹${p.rdAmount}</td>
                        <td style="text-align:right; font-weight:bold; color:#0f172a;">₹$runningRd</td>
                        <td style="text-align:right; color:#dc2626;">₹0</td>
                        <td style="text-align:right; color:#2563eb;">₹${p.loanRepayAmount}</td>
                        <td style="text-align:right; font-weight:bold;">₹$runningLoan</td>
                        <td style="text-align:center;"><span style="background:#e0f2fe; color:#0369a1; padding:2px 6px; border-radius:4px; font-size:10px; font-weight:bold;">${p.mode}</span></td>
                    </tr>
                """.trimIndent())
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Passbook - ${member.name}</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        margin: 0;
                        padding: 24px;
                        color: #0f172a;
                        background: #ffffff;
                    }
                    .header-box {
                        border-bottom: 2px solid #0f172a;
                        padding-bottom: 12px;
                        margin-bottom: 16px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    .society-title {
                        font-size: 20px;
                        font-weight: 800;
                        color: #0f172a;
                        letter-spacing: 0.5px;
                    }
                    .society-sub {
                        font-size: 12px;
                        color: #475569;
                        margin-top: 2px;
                    }
                    .doc-badge {
                        background: #0f172a;
                        color: #f8fafc;
                        padding: 6px 12px;
                        border-radius: 6px;
                        font-size: 12px;
                        font-weight: 700;
                        text-align: right;
                    }
                    .member-banner {
                        background: #f1f5f9;
                        border: 1px solid #cbd5e1;
                        border-radius: 8px;
                        padding: 14px 16px;
                        margin-bottom: 16px;
                    }
                    .grid-2 {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 8px;
                        font-size: 12px;
                    }
                    .stat-cards {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 10px;
                        margin-bottom: 16px;
                    }
                    .stat-card {
                        background: #f8fafc;
                        border: 1px solid #e2e8f0;
                        border-radius: 6px;
                        padding: 10px;
                        text-align: center;
                    }
                    .stat-label {
                        font-size: 10px;
                        color: #64748b;
                        text-transform: uppercase;
                        font-weight: 700;
                    }
                    .stat-value {
                        font-size: 16px;
                        font-weight: 800;
                        margin-top: 4px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 11px;
                        margin-top: 8px;
                    }
                    th {
                        background: #0f172a;
                        color: #ffffff;
                        padding: 8px 6px;
                        text-align: left;
                        font-size: 10px;
                        letter-spacing: 0.5px;
                    }
                    td {
                        padding: 8px 6px;
                        border-bottom: 1px solid #e2e8f0;
                    }
                    .footer {
                        margin-top: 24px;
                        padding-top: 12px;
                        border-top: 1px dashed #cbd5e1;
                        display: flex;
                        justify-content: space-between;
                        font-size: 10px;
                        color: #64748b;
                    }
                    @media print {
                        body { padding: 10px; }
                        .stat-card { border: 1px solid #ccc; }
                    }
                </style>
            </head>
            <body>
                <div class="header-box">
                    <div>
                        <div class="society-title">🏦 ${societyName.uppercase()}</div>
                        <div class="society-sub">Official Member Account Ledger & Savings Passbook • Kakrola, New Delhi</div>
                    </div>
                    <div class="doc-badge">
                        MEMBER PASSBOOK<br>
                        <span style="font-size:10px; font-weight:normal;">Generated: ${generatedDate}</span>
                    </div>
                </div>

                <div class="member-banner">
                    <div class="grid-2">
                        <div><strong>Member Name:</strong> ${member.name}</div>
                        <div><strong>Member ID:</strong> <span style="font-family:monospace; font-weight:bold; color:#2563eb;">${member.id}</span></div>
                        <div><strong>Mobile Number:</strong> ${member.mobile}</div>
                        <div><strong>Address:</strong> ${member.address.ifEmpty { "Kakrola, Delhi" }}</div>
                        <div><strong>Nominee:</strong> ${member.nominee.ifEmpty { "Self / Nominee" }}</div>
                        <div><strong>Payment Due Date:</strong> ${member.dueDay}</div>
                    </div>
                </div>

                <div class="stat-cards">
                    <div class="stat-card">
                        <div class="stat-label">Monthly RD</div>
                        <div class="stat-value" style="color:#059669;">₹${member.monthlyRd}</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Total RD Balance</div>
                        <div class="stat-value" style="color:#0284c7;">₹%,d</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Outstanding Loan</div>
                        <div class="stat-value" style="color:#dc2626;">₹%,d</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Eligible Loan Limit</div>
                        <div class="stat-value" style="color:#d97706;">${limitDisplay}</div>
                    </div>
                </div>

                <div style="font-weight:bold; font-size:12px; margin-bottom:4px; color:#0f172a;">
                    📜 Transaction History (RD Savings & Loan Accounts Segregated)
                </div>

                <table>
                    <thead>
                        <tr>
                            <th style="text-align:center;">DATE</th>
                            <th>TXN REF</th>
                            <th>PARTICULARS / NARRATION</th>
                            <th style="text-align:right;">RD CR (₹)</th>
                            <th style="text-align:right;">RD BAL (₹)</th>
                            <th style="text-align:right;">LOAN ISS (₹)</th>
                            <th style="text-align:right;">LOAN REP (₹)</th>
                            <th style="text-align:right;">LOAN BAL (₹)</th>
                            <th style="text-align:center;">MODE</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rowsHtml}
                    </tbody>
                </table>

                <div class="footer">
                    <div>* Computer generated official electronic passbook record. Verified and secured.</div>
                    <div>Page 1 of 1 • Gullak Society V64 Master</div>
                </div>
            </body>
            </html>
        """.trimIndent().format(Locale.ENGLISH, totalRd, activeLoan)
    }
}
