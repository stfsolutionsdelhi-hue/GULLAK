# Build Part 3B: Script Controller
import os

script_3b = r'''function getClientScriptPartB() {
  return `
  // TAB 4: ANNUAL BONUS REGISTER WITH DYNAMIC SUBTOTALS
  function renderBonusTab(){
    var st = document.getElementById("selFilterBonusStatus").value;
    var sort = document.getElementById("selSortBonus").value;
    var fromD = document.getElementById("inpBonusFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpBonusFilterTo").value || "2030-12-31";
    var q = (document.getElementById("searchBonusInput").value || "").toLowerCase().trim();

    var filtered = members.filter(function(m){
      var matchSearch = String(m.name||"").toLowerCase().indexOf(q) >= 0 || String(m.id||"").toLowerCase().indexOf(q) >= 0;
      var hasSettled = bonusSettlements.some(function(b){ return String(b.id) === String(m.id); });
      var matchStatus = (st === "ALL") || (st === "PAID" && hasSettled) || (st === "PENDING" && !hasSettled);
      return matchSearch && matchStatus;
    });

    if(sort==="bonus_high") filtered.sort(function(a,b){ return getMemberBonus(b)-getMemberBonus(a); });
    if(sort==="bonus_low") filtered.sort(function(a,b){ return getMemberBonus(a)-getMemberBonus(b); });
    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });

    var tbody = document.getElementById("tbodyBonusList"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='7' style='text-align:center;color:#94A3B8;'>No bonus records found</td></tr>"; 
      document.getElementById("tfootBonusTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumBonus = 0, sumLoan = 0, sumPen = 0;

    filtered.forEach(function(m){
      var mid = String(m.id);
      var totRd = getMemberTotalRd(m);
      var bVal = getMemberBonus(m);
      var lDue = getMemberActiveLoan(m);
      var pDue = calculateMemberLivePenaltyDue(m);
      var settled = bonusSettlements.find(function(b){ return String(b.id) === mid; });
      var stBadge = settled ? "<span class='badge-active'>PAID / SET-OFF</span>" : "<span class='badge-pending'>PENDING</span>";

      sumRd += totRd; sumBonus += bVal; sumLoan += lDue; sumPen += pDue;

      html += "<tr>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + "</strong><br><small style='color:#94A3B8;'>" + mid + "</small></td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + totRd.toLocaleString("en-IN") + "</td>" +
        "<td><span class='bonus-clickable action-view-bonus-stmt' data-id='" + mid + "' title='Click to view 12-month schedule'>₹" + bVal.toLocaleString("en-IN") + " 📋</span></td>" +
        "<td style='color:#EF4444;'>₹" + lDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + pDue.toLocaleString("en-IN") + "</td>" +
        "<td>" + stBadge + "</td>" +
        "<td><button class='btn-action-rcv action-setoff-bonus' data-id='" + mid + "' style='background:#7C3AED;'>Set-off / Pay</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootBonusTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#C084FC;font-size:1.05rem;'>₹" + sumBonus.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumLoan.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  // TAB 5: DEDICATED PENALTY REGISTER (₹10/DAY OVERDUE)
  function renderPenaltyTab(){
    var st = document.getElementById("selFilterPenStatus").value;
    var sort = document.getElementById("selSortPen").value;
    var q = (document.getElementById("searchPenInput").value || "").toLowerCase().trim();

    var filtered = members.filter(function(m){
      var matchSearch = String(m.name||"").toLowerCase().indexOf(q) >= 0 || String(m.id||"").toLowerCase().indexOf(q) >= 0;
      var penDue = calculateMemberLivePenaltyDue(m);
      var matchStatus = (st === "ALL") || (st === "OVERDUE" && penDue > 0) || (st === "CLEAR" && penDue <= 0);
      return matchSearch && matchStatus;
    });

    if(sort==="pen_high") filtered.sort(function(a,b){ return calculateMemberLivePenaltyDue(b)-calculateMemberLivePenaltyDue(a); });
    if(sort==="pen_low") filtered.sort(function(a,b){ return calculateMemberLivePenaltyDue(a)-calculateMemberLivePenaltyDue(b); });
    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });

    var tbody = document.getElementById("tbodyPenaltyList"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='9' style='text-align:center;color:#94A3B8;'>No penalty records found</td></tr>"; 
      document.getElementById("tfootPenaltyTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumAccrued = 0, sumPaid = 0, sumWaived = 0, sumNet = 0;

    filtered.forEach(function(m){
      var mid = String(m.id);
      var penDue = calculateMemberLivePenaltyDue(m);
      var penPaid = getMemberTotalPenalty(m);
      var waived = getMemberTotalWaiver(m);
      var accrued = penDue + penPaid + waived;

      sumAccrued += accrued; sumPaid += penPaid; sumWaived += waived; sumNet += penDue;

      var dueDay = m.dueDay || "15th of every month";
      var statusBadge = penDue > 0 ? "<span class='badge-inactive'>OVERDUE</span>" : "<span class='badge-active'>CLEAR</span>";
      var overdueInfo = penDue > 0 ? ("₹" + penDue + " (" + Math.ceil(penDue / 10) + " Days @ ₹10/D)") : "No Overdue";

      html += "<tr>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + "</strong><br><small style='color:#94A3B8;'>" + mid + "</small></td>" +
        "<td style='color:#38BDF8;font-weight:700;'>" + dueDay + "</td>" +
        "<td style='color:" + (penDue > 0 ? "#EF4444" : "#10B981") + ";font-weight:700;'>" + overdueInfo + "</td>" +
        "<td style='color:#FBBF24;font-weight:700;'>₹" + accrued.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;'>₹" + penPaid.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + waived.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;font-weight:800;font-size:0.95rem;'>₹" + penDue.toLocaleString("en-IN") + "</td>" +
        "<td>" + statusBadge + "</td>" +
        "<td><button class='btn-action-rcv action-receive-for' data-id='" + mid + "'>Collect / Waive</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootPenaltyTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='3' style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#FBBF24;'>₹" + sumAccrued.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;'>₹" + sumPaid.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + sumWaived.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumNet.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  // PASSBOOK LEDGER (FULL AUDIT TRAIL MATCHING BY ID & NAME)
  function openMemberLedger(mid){
    var m = members.find(function(x){ return String(x.id).trim().toUpperCase() === mid.trim().toUpperCase() || String(x.name).trim().toLowerCase() === mid.trim().toLowerCase(); });
    if(!m) return;
    currentActiveLedgerMember = m;

    var actualId = String(m.id).trim().toUpperCase();
    var actualName = String(m.name).trim().toLowerCase();

    document.getElementById("lblLedgerName").innerText = "📖 Passbook Ledger: " + m.name + " (" + m.id + ")";
    var fromD = document.getElementById("inpLedgerFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpLedgerFilterTo").value || "2030-12-31";

    var totRd = getMemberTotalRd(m);
    var loanDue = getMemberActiveLoan(m);
    var intDue = calculateMemberLiveInterestDue(m);
    var penDue = calculateMemberLivePenaltyDue(m);

    document.getElementById("ledgerHeaderStats").innerHTML = 
      "<div><div class='ledger-stat-lbl'>MEMBER ID / DUE</div><div class='ledger-stat-val' style='color:#38BDF8;font-size:0.95rem;'>" + m.id + "<br><small style='color:#FBBF24;font-size:0.75rem;'>" + (m.dueDay||"15th of month") + "</small></div></div>" +
      "<div><div class='ledger-stat-lbl'>TOTAL RD SAVED</div><div class='ledger-stat-val' style='color:#10B981;'>₹" + totRd.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>ACTIVE LOAN DUE</div><div class='ledger-stat-val' style='color:#EF4444;'>₹" + loanDue.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>INTEREST DUE</div><div class='ledger-stat-val' style='color:#FBBF24;'>₹" + intDue.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>PENALTY DUE (₹10/D)</div><div class='ledger-stat-val' style='color:#F59E0B;'>₹" + penDue.toLocaleString("en-IN") + "</div></div>";

    var txns = [];
    if(cleanNum(m.rdPaid, 0) > 0){
      txns.push({ date: m.dateJoined || "2026-01-01", ref: "OPENING", partic: "Opening RD Balance (As on 31 Dec 2025)", debit: 0, credit: cleanNum(m.rdPaid, 0), mode: "SYSTEM" });
    }
    if(cleanNum(m.opLoan, 0) > 0){
      txns.push({ date: m.dateJoined || "2026-01-01", ref: "OP-LOAN", partic: "Opening Loan Principal", debit: cleanNum(m.opLoan, 0), credit: 0, mode: "SYSTEM" });
    }

    // Match all loans for this member by ID or Name
    loans.forEach(function(l){
      if(String(l.id).trim().toUpperCase() === actualId || String(l.name).trim().toLowerCase() === actualName){
        var lNarr = l.narration ? ('<br><small style="color:#94A3B8;font-size:0.75rem;font-style:italic;">📝 ' + l.narration + '</small>') : '';
        txns.push({
          date: l.date,
          ref: l.loanId,
          partic: "Loan Disbursed (" + l.type + " @ " + l.rate + "% p.m.)" + lNarr,
          debit: cleanNum(l.principal, 0),
          credit: 0,
          mode: "ONLINE"
        });
      }
    });

    // Match all payments for this member by ID or Name
    payments.forEach(function(p){
      if(String(p.id).trim().toUpperCase() === actualId || String(p.name).trim().toLowerCase() === actualName){
        var pNarr = p.narration ? ('<br><small style="color:#94A3B8;font-size:0.75rem;font-style:italic;">📝 ' + p.narration + '</small>') : '';
        var parts = [];
        if(cleanNum(p.rd, 0) > 0) parts.push("RD ₹" + p.rd);
        if(cleanNum(p.interest, 0) > 0) parts.push("Int ₹" + p.interest);
        if(cleanNum(p.penalty, 0) > 0) parts.push("Pen ₹" + p.penalty);
        if(cleanNum(p.loanRepay, 0) > 0) parts.push("Repay ₹" + p.loanRepay);
        if(cleanNum(p.waiver, 0) > 0) parts.push("Waiver ₹" + p.waiver);

        var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
        txns.push({
          date: p.date,
          ref: p.receiptNo,
          partic: "Receipt: " + parts.join(", ") + pNarr,
          debit: 0,
          credit: cleanNum(p.total, 0),
          mode: safeMode
        });
      }
    });

    // Sort by date ascending
    txns.sort(function(a,b){ return new Date(a.date||"2026-01-01") - new Date(b.date||"2026-01-01"); });

    var runningBal = 0;
    var filteredTxns = txns.filter(function(t){ return (t.date >= fromD && t.date <= toD); });

    var tbody = document.getElementById("tbodyLedgerTxns");
    if(filteredTxns.length === 0){
      tbody.innerHTML = "<tr><td colspan='7' style='text-align:center;color:#94A3B8;'>No transactions in selected date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
    } else {
      var h = "";
      filteredTxns.forEach(function(t){
        runningBal += (t.credit - t.debit);
        var balCol = runningBal >= 0 ? "#10B981" : "#EF4444";
        h += "<tr>" +
          "<td>" + toDisplayDate(t.date) + "</td>" +
          "<td style='font-family:monospace;color:#FBBF24;'>" + t.ref + "</td>" +
          "<td>" + t.partic + "</td>" +
          "<td style='color:#EF4444;'>" + (t.debit > 0 ? "₹" + t.debit.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#10B981;font-weight:700;'>" + (t.credit > 0 ? "₹" + t.credit.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:" + balCol + ";font-weight:800;'>₹" + runningBal.toLocaleString("en-IN") + "</td>" +
          "<td><span style='font-size:0.75rem;color:" + (t.mode==="CASH"?"#F59E0B":"#38BDF8") + ";font-weight:700;'>" + t.mode + "</span></td>" +
        "</tr>";
      });
      tbody.innerHTML = h;
    }
    openModal("modalLedger");
  }

  // BULK ENTRY REGISTER RENDERING WITH SHORT NARRATION
  function renderBulkList(){
    var tbody = document.getElementById("tbodyBulkList"); if(!tbody) return;
    var h = "";
    var activeMems = members.filter(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
    if(activeMems.length === 0){
      tbody.innerHTML = "<tr><td colspan='10' style='text-align:center;color:#94A3B8;'>No active members available</td></tr>";
      return;
    }

    activeMems.forEach(function(m){
      var mid = String(m.id);
      var safeRd = cleanRd(m.rd);
      var intDue = calculateMemberLiveInterestDue(m);
      var penDue = calculateMemberLivePenaltyDue(m);
      var initialTotal = safeRd + intDue + penDue;

      h += "<tr data-id='" + mid + "'>" +
        "<td style='text-align:center;'><input type=\"checkbox\" class=\"b-chk\" checked onchange=\"calcBulkTotals()\"></td>" +
        "<td><strong>" + m.name + "</strong><br><small style='color:#38BDF8;'>RD ₹" + safeRd + " | Int ₹" + intDue + " | Pen ₹" + penDue + "</small></td>" +
        "<td><input type=\"number\" class=\"field-ctrl b-rd\" value=\"" + safeRd + "\" oninput=\"calcBulkRow(this)\" style=\"width:80px;text-align:right;\"></td>" +
        "<td><input type=\"number\" class=\"field-ctrl b-int\" value=\"" + intDue + "\" oninput=\"calcBulkRow(this)\" style=\"width:80px;text-align:right;\"></td>" +
        "<td><input type=\"number\" class=\"field-ctrl b-repay\" value=\"0\" oninput=\"calcBulkRow(this)\" style=\"width:80px;text-align:right;\"></td>" +
        "<td><input type=\"number\" class=\"field-ctrl b-pen\" value=\"" + penDue + "\" oninput=\"calcBulkRow(this)\" style=\"width:75px;text-align:right;\"></td>" +
        "<td><input type=\"number\" class=\"field-ctrl b-wvr\" value=\"0\" oninput=\"calcBulkRow(this)\" style=\"width:75px;text-align:right;\"></td>" +
        "<td style=\"text-align:right; font-weight:800; color:#10B981;\" class=\"b-tot-cell\">₹" + initialTotal.toLocaleString("en-IN") + "</td>" +
        "<td><select class=\"field-ctrl b-mode\" style=\"width:85px;padding:4px;\"><option value=\"CASH\">CASH</option><option value=\"ONLINE\">ONLINE</option></select></td>" +
        "<td><input type=\"text\" class=\"field-ctrl b-narr\" placeholder=\"Remarks\" style=\"width:130px;padding:4px;font-size:0.78rem;\"></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;
    calcBulkTotals();
  }

  window.calcBulkRow = function(inputEl){
    var tr = inputEl.closest("tr");
    if(!tr) return;
    var rd = cleanNum(tr.querySelector(".b-rd").value, 0);
    var intVal = cleanNum(tr.querySelector(".b-int").value, 0);
    var repay = cleanNum(tr.querySelector(".b-repay").value, 0);
    var pen = cleanNum(tr.querySelector(".b-pen").value, 0);
    var wvr = cleanNum(tr.querySelector(".b-wvr").value, 0);
    var tot = rd + intVal + repay + pen - wvr;
    if(tot < 0) tot = 0;
    tr.querySelector(".b-tot-cell").innerText = "₹" + tot.toLocaleString("en-IN");
    calcBulkTotals();
  };

  window.calcBulkTotals = function(){
    var rows = document.querySelectorAll("#tbodyBulkList tr");
    var selCount = 0;
    var grandTot = 0;
    rows.forEach(function(tr){
      var chk = tr.querySelector(".b-chk");
      if(chk && chk.checked){
        selCount++;
        var rd = cleanNum(tr.querySelector(".b-rd").value, 0);
        var intVal = cleanNum(tr.querySelector(".b-int").value, 0);
        var repay = cleanNum(tr.querySelector(".b-repay").value, 0);
        var pen = cleanNum(tr.querySelector(".b-pen").value, 0);
        var wvr = cleanNum(tr.querySelector(".b-wvr").value, 0);
        var tot = rd + intVal + repay + pen - wvr;
        if(tot < 0) tot = 0;
        grandTot += tot;
      }
    });
    var elCnt = document.getElementById("lblBulkSelectedCount");
    var elGrd = document.getElementById("lblBulkGrandTotal");
    if(elCnt) elCnt.innerText = selCount;
    if(elGrd) elGrd.innerText = "₹" + grandTot.toLocaleString("en-IN");
  };

  // NPA & BAD DEBTS LIST WITH DATE FILTER & DYNAMIC TOTAL
  function renderNpaList(){
    var fromD = document.getElementById("inpNpaFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpNpaFilterTo").value || "2026-12-31";
    var tbody = document.getElementById("tbodyNpaList"); if(!tbody) return;
    
    var filtered = exitSettlements.filter(function(e){
      var d = e.date || "2026-01-01";
      return cleanNum(e.npaLoss, 0) > 0 && (d >= fromD && d <= toD);
    });

    if(filtered.length === 0){
      tbody.innerHTML = "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No NPA write-offs recorded in date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
      document.getElementById("tfootNpaTotal").innerHTML = "";
      return;
    }

    var h = "";
    var totalNpaSum = 0;
    filtered.forEach(function(e){
      var nVal = cleanNum(e.npaLoss, 0);
      totalNpaSum += nVal;
      h += "<tr>" +
        "<td>" + toDisplayDate(e.date) + "</td>" +
        "<td style='font-family:monospace;color:#EF4444;'>" + e.exitId + "</td>" +
        "<td><strong>" + e.name + "</strong> (" + e.id + ")</td>" +
        "<td style='color:#EF4444;font-weight:800;'>₹" + nVal.toLocaleString("en-IN") + "</td>" +
        "<td><span class='badge-inactive'>WRITTEN OFF</span></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;

    document.getElementById("tfootNpaTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='3' style='color:#FBBF24;'>TOTAL WRITE-OFF IN DATE RANGE</td>" +
        "<td style='color:#EF4444;font-weight:800;'>₹" + totalNpaSum.toLocaleString("en-IN") + "</td>" +
        "<td></td>" +
      "</tr>";
  }

  // MODAL UTILITIES
  function openModal(id){
    var el = document.getElementById(id);
    if(el) el.style.display = "flex";
  }
  function closeModal(id){
    var el = document.getElementById(id);
    if(el) el.style.display = "none";
  }
  function closeAllModals(){
    document.querySelectorAll(".modal-backdrop").forEach(function(m){
      m.style.display = "none";
    });
  }

  // NOTICE POPUP THAT DOES NOT CLOSE FORM ON OK
  var activeKeepModal = null;
  function showNotice(title, message, keepModalId){
    document.getElementById("noticeHeader").innerText = title || "Notice";
    document.getElementById("noticeBody").innerText = message || "";
    activeKeepModal = keepModalId || null;
    openModal("modalNotice");
  }

  document.getElementById("btnNoticeOk").addEventListener("click", function(){
    closeModal("modalNotice");
    if(activeKeepModal){
      openModal(activeKeepModal);
      activeKeepModal = null;
    }
  });

  // OPEN RECEIVE MODAL FOR MEMBER (AUTO-FILL LIVE DUES)
  function openReceiveModalFor(mid, recNo){
    var m = members.find(function(x){ return String(x.id).trim().toUpperCase() === mid.trim().toUpperCase() || String(x.name).trim().toLowerCase() === mid.trim().toLowerCase(); });
    if(!m) return;

    var actualMid = String(m.id);
    document.getElementById("editReceiptNo").value = recNo || "";
    document.getElementById("lblReceiveModalHead").innerText = recNo ? ("✏️ Edit Receipt: " + recNo) : "📥 Receive Amount";
    
    var sel = document.getElementById("selPayMember");
    sel.innerHTML = "<option value='" + actualMid + "' selected>" + m.name + " (" + actualMid + ")</option>";
    members.forEach(function(other){
      if(String(other.id) !== actualMid && String(other.status).toUpperCase() === "ACTIVE"){
        sel.innerHTML += "<option value='" + other.id + "'>" + other.name + " (" + other.id + ")</option>";
      }
    });

    var todayYMD = getTodayYMD();
    document.getElementById("inpPayDate").value = todayYMD;
    document.getElementById("dispPayDateFormatted").innerText = "(" + toDisplayDate(todayYMD) + ")";
    document.getElementById("selPayMode").value = "CASH";

    if(recNo){
      var p = payments.find(function(x){ return String(x.receiptNo) === recNo; });
      if(p){
        document.getElementById("inpPayDate").value = p.date;
        document.getElementById("dispPayDateFormatted").innerText = "(" + toDisplayDate(p.date) + ")";
        document.getElementById("selPayMode").value = p.mode;
        document.getElementById("inpPayRd").value = cleanNum(p.rd, 0);
        document.getElementById("inpPayInterest").value = cleanNum(p.interest, 0);
        document.getElementById("inpPayPenalty").value = cleanNum(p.penalty, 0);
        document.getElementById("inpPayWaiver").value = cleanNum(p.waiver, 0);
        document.getElementById("inpPayPrincipal").value = cleanNum(p.loanRepay, 0);
        document.getElementById("inpPayNarration").value = p.narration || "";
      }
    } else {
      var liveInt = calculateMemberLiveInterestDue(m);
      var livePen = calculateMemberLivePenaltyDue(m);
      document.getElementById("inpPayRd").value = cleanRd(m.rd);
      document.getElementById("inpPayInterest").value = liveInt;
      document.getElementById("inpPayPenalty").value = livePen;
      document.getElementById("inpPayWaiver").value = 0;
      document.getElementById("inpPayPrincipal").value = 0;
      document.getElementById("inpPayNarration").value = "Monthly Collection";
    }

    openModal("modalReceive");
  }

  // OPEN LOAN MODAL FOR MEMBER
  function openLoanModalFor(mid, lId){
    var m = mid ? members.find(function(x){ return String(x.id).trim().toUpperCase() === mid.trim().toUpperCase() || String(x.name).trim().toLowerCase() === mid.trim().toLowerCase(); }) : null;
    document.getElementById("editLoanId").value = lId || "";
    document.getElementById("lblLoanModalHead").innerText = lId ? ("✏️ Edit Society Loan: " + lId) : "💸 Issue Society Loan";

    var sel = document.getElementById("selLoanMember");
    sel.innerHTML = "";
    members.forEach(function(mem){
      if(String(mem.status).toUpperCase() === "ACTIVE"){
        var isSel = (m && String(mem.id) === String(m.id)) ? "selected" : "";
        sel.innerHTML += "<option value='" + mem.id + "' " + isSel + ">" + mem.name + " (" + mem.id + ")</option>";
      }
    });

    var todayYMD = getTodayYMD();
    document.getElementById("inpLoanDate").value = todayYMD;
    document.getElementById("dispLoanDateFormatted").innerText = "(" + toDisplayDate(todayYMD) + ")";
    document.getElementById("selLoanType").value = "Gullak Loan";
    document.getElementById("inpLoanRate").value = globalDefaultRate;
    document.getElementById("inpLoanPrinc").value = "";
    document.getElementById("inpLoanNarration").value = "";

    if(m){
      var lmt = getMemberLoanLimit(m);
      document.getElementById("lblMemberLoanLimit").innerText = "Eligible Loan Limit: " + lmt.text;
    } else {
      document.getElementById("lblMemberLoanLimit").innerText = "";
    }

    if(lId){
      var l = loans.find(function(x){ return String(x.loanId) === lId; });
      if(l){
        document.getElementById("inpLoanDate").value = l.date;
        document.getElementById("dispLoanDateFormatted").innerText = "(" + toDisplayDate(l.date) + ")";
        document.getElementById("selLoanType").value = l.type;
        document.getElementById("inpLoanRate").value = l.rate;
        document.getElementById("inpLoanPrinc").value = cleanNum(l.principal, 0);
        document.getElementById("inpLoanNarration").value = l.narration || "";
      }
    }

    openModal("modalLoan");
  }

  // REFRESH ALL INTERFACES
  function refreshAll(){
    setupFinancialYearDropdown();
    updateKPIs();
    renderMembers();
    renderPayments();
    renderLoans();
    renderBonusTab();
    renderPenaltyTab();
    if(currentActiveLedgerMember && document.getElementById("modalLedger").style.display === "flex"){
      openMemberLedger(currentActiveLedgerMember.id);
    }
  }

  // EVENT LISTENERS & HOOKS
  document.addEventListener("DOMContentLoaded", function(){
    // Check saved session
    try {
      var saved = sessionStorage.getItem("gullak_v21_session");
      if(saved){
        var sess = JSON.parse(saved);
        if(sess && (sess.username === "SANISH" || sess.username === "ADMIN")){
          window.currentUserSession = sess;
          var overlay = document.getElementById("windowsLoginOverlay");
          if(overlay) overlay.style.display = "none";
        }
      }
    } catch(e) {}

    // F11 Listener for clean fullscreen toggle
    window.addEventListener("keydown", function(e){
      if(e.key === "F11" || e.keyCode === 122){
        e.preventDefault();
        window.safeToggleFullscreen();
      }
    });

    // Date formatting live indicators
    var pDateInp = document.getElementById("inpPayDate");
    if(pDateInp){
      pDateInp.addEventListener("change", function(){
        document.getElementById("dispPayDateFormatted").innerText = "(" + toDisplayDate(this.value) + ")";
      });
    }
    var lDateInp = document.getElementById("inpLoanDate");
    if(lDateInp){
      lDateInp.addEventListener("change", function(){
        document.getElementById("dispLoanDateFormatted").innerText = "(" + toDisplayDate(this.value) + ")";
      });
    }

    // Modal close buttons
    document.querySelectorAll(".action-close-modal").forEach(function(btn){
      btn.addEventListener("click", function(){
        var b = this.closest(".modal-backdrop");
        if(b) b.style.display = "none";
      });
    });

    // Tab switcher
    function switchTab(tIdx){
      for(var i=1; i<=5; i++){
        var head = document.getElementById("tabHead" + i);
        var panel = document.getElementById("tabPanel" + i);
        if(head) head.className = (i === tIdx) ? "tab-item active" : "tab-item";
        if(panel) panel.style.display = (i === tIdx) ? "block" : "none";
      }
    }

    document.getElementById("tabHead1").addEventListener("click", function(){ switchTab(1); renderMembers(); });
    document.getElementById("tabHead2").addEventListener("click", function(){ switchTab(2); renderPayments(); });
    document.getElementById("tabHead3").addEventListener("click", function(){ switchTab(3); renderLoans(); });
    document.getElementById("tabHead4").addEventListener("click", function(){ switchTab(4); renderBonusTab(); });
    document.getElementById("tabHead5").addEventListener("click", function(){ switchTab(5); renderPenaltyTab(); });

    // Top action buttons
    document.getElementById("btnTopReceive").addEventListener("click", function(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openReceiveModalFor(firstActive ? firstActive.id : "MEM010120261");
    });
    document.getElementById("btnPanelNewReceipt").addEventListener("click", function(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openReceiveModalFor(firstActive ? firstActive.id : "MEM010120261");
    });
    document.getElementById("btnTopLoan").addEventListener("click", function(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openLoanModalFor(firstActive ? firstActive.id : "MEM010120261");
    });
    document.getElementById("btnPanelNewLoan").addEventListener("click", function(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openLoanModalFor(firstActive ? firstActive.id : "MEM010120261");
    });

    document.getElementById("btnTopAddMember").addEventListener("click", function(){
      document.getElementById("editMemId").value = "";
      document.getElementById("lblMemberModalHead").innerText = "👤 Add New Member Profile";
      document.getElementById("inpNewMemName").value = "";
      document.getElementById("inpNewMemMobile").value = "";
      document.getElementById("inpNewMemStatus").value = "ACTIVE";
      document.getElementById("inpNewMemJoinDate").value = getTodayYMD();
      document.getElementById("inpNewMemRd").value = 400;
      document.getElementById("inpNewMemDueDay").value = "15th of every month";
      document.getElementById("inpNewMemAddress").value = "";
      document.getElementById("inpNewMemNominee").value = "";
      document.getElementById("inpNewMemBal").value = 0;
      document.getElementById("inpNewMemOpLoan").value = 0;
      document.getElementById("inpNewMemOpInt").value = 0;
      document.getElementById("inpNewMemOpPen").value = 0;
      document.getElementById("inpNewMemCustomLimit").value = 0;
      openModal("modalMember");
    });

    document.getElementById("btnTopBulk").addEventListener("click", function(){
      var today = getTodayYMD();
      document.getElementById("inpBulkDate").value = today;
      document.getElementById("dispBulkDateFormatted").innerText = "(" + toDisplayDate(today) + ")";
      renderBulkList();
      openModal("modalBulk");
    });

    document.getElementById("btnBulkSetAllCash").addEventListener("click", function(){
      document.querySelectorAll("#tbodyBulkList .b-mode").forEach(function(sel){ sel.value = "CASH"; });
    });
    document.getElementById("btnBulkSetAllOnline").addEventListener("click", function(){
      document.querySelectorAll("#tbodyBulkList .b-mode").forEach(function(sel){ sel.value = "ONLINE"; });
    });
    document.getElementById("chkSelectAllBulk").addEventListener("change", function(){
      var isChk = this.checked;
      document.querySelectorAll("#tbodyBulkList .b-chk").forEach(function(c){ c.checked = isChk; });
      calcBulkTotals();
    });

    document.getElementById("btnTopExit").addEventListener("click", function(){
      var sel = document.getElementById("selExitMember");
      sel.innerHTML = "<option value=''>-- Select Member --</option>";
      members.forEach(function(m){
        if(String(m.status).toUpperCase() === "ACTIVE"){
          sel.innerHTML += "<option value='" + m.id + "'>" + m.name + " (" + m.id + ")</option>";
        }
      });
      openModal("modalExit");
    });

    document.getElementById("btnTopSettings").addEventListener("click", function(){
      document.getElementById("inpGlobalDueDay").value = globalDefaultDue;
      document.getElementById("inpGlobalRate").value = globalDefaultRate;
      openModal("modalSettings");
    });

    document.getElementById("btnApplyGlobalSettings").addEventListener("click", function(){
      globalDefaultDue = document.getElementById("inpGlobalDueDay").value.trim() || "15th of every month";
      globalDefaultRate = Number(document.getElementById("inpGlobalRate").value) || 1.0;
      closeModal("modalSettings");
      showNotice("Settings Saved", "Global default due date set to " + globalDefaultDue + " and default loan rate set to " + globalDefaultRate + "% p.m.");
    });

    // SYNC FROM GOOGLE SHEET DATABASE
    document.getElementById("btnTopReload").addEventListener("click", function(){
      if(typeof google !== "undefined" && google.script && google.script.run){
        showNotice("Syncing...", "Fetching verified records from Google Spreadsheet...");
        google.script.run.withSuccessHandler(function(res){
          closeModal("modalNotice");
          if(res && res.members && res.members.length > 0){
            members = res.members;
            payments = res.payments || [];
            loans = res.loans || [];
            exitSettlements = res.exitSettlements || [];
            bonusSettlements = res.bonusSettlements || [];
            if(res.users && res.users.length > 0) window.authorizedUsers = res.users;
            saveStore();
            showNotice("Sync Complete", "Successfully synchronized " + members.length + " members, " + payments.length + " receipts, and " + loans.length + " loans from Google Sheet!");
          }
        }).getSocietyFullData();
      } else {
        refreshAll();
        showNotice("Local Reloaded", "Database re-indexed locally.");
      }
    });

    // KPI Card Click Events
    document.getElementById("kpiCardMembers").addEventListener("click", function(){ switchTab(1); });
    document.getElementById("kpiCardRd").addEventListener("click", function(){ switchTab(2); });
    document.getElementById("kpiCardLoans").addEventListener("click", function(){ switchTab(3); });
    document.getElementById("kpiCardBonus").addEventListener("click", function(){ switchTab(4); });
    document.getElementById("kpiCardNpa").addEventListener("click", function(){
      renderNpaList();
      openModal("modalNpa");
    });
    document.getElementById("btnApplyNpaFilter").addEventListener("click", function(){
      renderNpaList();
    });

    document.getElementById("kpiCardFund").addEventListener("click", function(){
      var cSum = 0, bSum = 0;
      payments.forEach(function(p){
        var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
        if(safeMode === "ONLINE") bSum += cleanNum(p.total, 0); else cSum += cleanNum(p.total, 0);
      });
      document.getElementById("lblRegCashBal").innerText = "₹" + cSum.toLocaleString("en-IN");
      document.getElementById("lblRegBankBal").innerText = "₹" + bSum.toLocaleString("en-IN");
      document.getElementById("lblRegTotalFund").innerText = "₹" + (cSum + bSum).toLocaleString("en-IN");
      openModal("modalFund");
    });

    // Filters on change
    document.getElementById("selFilterStatus").addEventListener("change", renderMembers);
    document.getElementById("selSortMembers").addEventListener("change", renderMembers);
    document.getElementById("memberFilterInput").addEventListener("input", renderMembers);

    document.getElementById("inpPayFilterFrom").addEventListener("change", renderPayments);
    document.getElementById("inpPayFilterTo").addEventListener("change", renderPayments);
    document.getElementById("selFilterPayMode").addEventListener("change", renderPayments);
    document.getElementById("selSortPayDate").addEventListener("change", renderPayments);
    document.getElementById("searchPayInput").addEventListener("input", renderPayments);

    document.getElementById("inpLoanFilterFrom").addEventListener("change", renderLoans);
    document.getElementById("inpLoanFilterTo").addEventListener("change", renderLoans);
    document.getElementById("selFilterLoanType").addEventListener("change", renderLoans);
    document.getElementById("selFilterLoanStatus").addEventListener("change", renderLoans);
    document.getElementById("searchLoanInput").addEventListener("input", renderLoans);

    document.getElementById("inpBonusFilterFrom").addEventListener("change", renderBonusTab);
    document.getElementById("inpBonusFilterTo").addEventListener("change", renderBonusTab);
    document.getElementById("selFilterBonusStatus").addEventListener("change", renderBonusTab);
    document.getElementById("selSortBonus").addEventListener("change", renderBonusTab);
    document.getElementById("searchBonusInput").addEventListener("input", renderBonusTab);

    document.getElementById("inpPenFilterFrom").addEventListener("change", renderPenaltyTab);
    document.getElementById("inpPenFilterTo").addEventListener("change", renderPenaltyTab);
    document.getElementById("selFilterPenStatus").addEventListener("change", renderPenaltyTab);
    document.getElementById("selSortPen").addEventListener("change", renderPenaltyTab);
    document.getElementById("searchPenInput").addEventListener("input", renderPenaltyTab);

    document.getElementById("selFinancialYear").addEventListener("change", function(){
      refreshAll();
    });

    // Passbook dynamic date filter
    document.getElementById("inpLedgerFilterFrom").addEventListener("change", function(){
      if(currentActiveLedgerMember) openMemberLedger(currentActiveLedgerMember.id);
    });
    document.getElementById("inpLedgerFilterTo").addEventListener("change", function(){
      if(currentActiveLedgerMember) openMemberLedger(currentActiveLedgerMember.id);
    });

    // Delegate Click Actions
    document.addEventListener("click", function(e){
      var t = e.target;
      if(t.classList.contains("action-view-ledger")){
        var mid = t.getAttribute("data-id");
        if(mid) openMemberLedger(mid);
      }
      if(t.classList.contains("action-receive-for")){
        var mid = t.getAttribute("data-id");
        if(mid) openReceiveModalFor(mid);
      }
      if(t.classList.contains("action-edit-receipt")){
        var recNo = t.getAttribute("data-rec");
        var p = payments.find(function(x){ return String(x.receiptNo) === recNo; });
        if(p) openReceiveModalFor(p.id, recNo);
      }
      if(t.classList.contains("action-edit-loan")){
        var lId = t.getAttribute("data-loanid");
        var l = loans.find(function(x){ return String(x.loanId) === lId; });
        if(l) openLoanModalFor(l.id, lId);
      }
      if(t.classList.contains("action-edit-member")){
        var mid = t.getAttribute("data-id");
        var m = members.find(function(x){ return String(x.id) === mid; });
        if(m){
          document.getElementById("editMemId").value = m.id;
          document.getElementById("lblMemberModalHead").innerText = "✏️ Edit Member: " + m.name + " (" + m.id + ")";
          document.getElementById("inpNewMemName").value = m.name;
          document.getElementById("inpNewMemMobile").value = m.mobile;
          document.getElementById("inpNewMemStatus").value = m.status;
          document.getElementById("inpNewMemJoinDate").value = m.dateJoined;
          document.getElementById("inpNewMemRd").value = m.rd;
          document.getElementById("inpNewMemDueDay").value = m.dueDay || "15th of every month";
          document.getElementById("inpNewMemAddress").value = m.address || "";
          document.getElementById("inpNewMemNominee").value = m.nominee || "";
          document.getElementById("inpNewMemBal").value = cleanNum(m.rdPaid, 0);
          document.getElementById("inpNewMemOpLoan").value = cleanNum(m.opLoan, 0);
          document.getElementById("inpNewMemOpInt").value = cleanNum(m.opInt, 0);
          document.getElementById("inpNewMemOpPen").value = cleanNum(m.opPen, 0);
          document.getElementById("inpNewMemCustomLimit").value = cleanNum(m.customLimit, 0);
          openModal("modalMember");
        }
      }
      if(t.classList.contains("action-view-bonus-stmt")){
        var mid = t.getAttribute("data-id");
        var m = members.find(function(x){ return String(x.id) === mid; });
        if(m){
          var res = calculate1PercentPmBonus(m);
          document.getElementById("lblBonusStmtTitle").innerText = "🎁 Bonus Calculation Statement: " + m.name + " (" + m.id + ")";
          document.getElementById("bonusStmtHeaderStats").innerHTML = 
            "<div><div class='ledger-stat-lbl'>MEMBER NAME</div><div class='ledger-stat-val' style='color:#FBBF24;'>" + m.name + "</div></div>" +
            "<div><div class='ledger-stat-lbl'>TOTAL RD SAVED</div><div class='ledger-stat-val' style='color:#10B981;'>₹" + getMemberTotalRd(m).toLocaleString("en-IN") + "</div></div>" +
            "<div><div class='ledger-stat-lbl'>ANNUAL BONUS (1% P.M.)</div><div class='ledger-stat-val' style='color:#C084FC;'>₹" + res.totalBonus.toLocaleString("en-IN") + "</div></div>";
          var h = "";
          res.schedule.forEach(function(row){
            h += "<tr>" +
              "<td style='font-weight:700;'>" + row.month + "</td>" +
              "<td>₹" + row.opening.toLocaleString("en-IN") + "</td>" +
              "<td style='color:#10B981;'>+₹" + row.deposit.toLocaleString("en-IN") + "</td>" +
              "<td style='font-weight:700;'>₹" + row.closing.toLocaleString("en-IN") + "</td>" +
              "<td style='color:#C084FC;font-weight:800;'>₹" + row.mBonus.toLocaleString("en-IN") + "</td>" +
              "<td style='color:#FBBF24;font-weight:800;'>₹" + row.accumBonus.toLocaleString("en-IN") + "</td>" +
            "</tr>";
          });
          document.getElementById("tbodyBonusSchedule").innerHTML = h;
          openModal("modalBonusStatement");
        }
      }
    });

    // SUBMIT RECEIVE AMOUNT / EDIT RECEIPT
    document.getElementById("btnSubmitReceive").addEventListener("click", function(){
      var recNo = document.getElementById("editReceiptNo").value.trim();
      var mid = document.getElementById("selPayMember").value;
      var m = members.find(function(x){ return String(x.id) === mid; });
      if(!m){ showNotice("Validation Error", "Please select a valid member."); return; }

      var pDate = document.getElementById("inpPayDate").value || getTodayYMD();
      var pMode = document.getElementById("selPayMode").value;
      var rdVal = cleanNum(document.getElementById("inpPayRd").value, 0);
      var intVal = cleanNum(document.getElementById("inpPayInterest").value, 0);
      var penVal = cleanNum(document.getElementById("inpPayPenalty").value, 0);
      var wvrVal = cleanNum(document.getElementById("inpPayWaiver").value, 0);
      var princVal = cleanNum(document.getElementById("inpPayPrincipal").value, 0);
      var narr = document.getElementById("inpPayNarration").value.trim();

      var tot = rdVal + intVal + penVal + princVal - wvrVal;
      if(tot < 0) tot = 0;

      if(!recNo){
        // STRICT ID FORMAT: CER + DD + MM + YY
        var dateSuffix = getDDMMYYFromYMD(pDate);
        var baseId = "CER" + dateSuffix;
        var finalId = baseId;
        var counter = 1;
        while(payments.some(function(p){ return String(p.receiptNo) === finalId; })){
          finalId = baseId + counter;
          counter++;
        }
        recNo = finalId;
      }

      var newP = {
        receiptNo: recNo,
        date: pDate,
        id: m.id,
        name: m.name,
        rd: rdVal,
        interest: intVal,
        penalty: penVal,
        loanRepay: princVal,
        waiver: wvrVal,
        total: tot,
        mode: pMode,
        by: "Admin",
        type: "REGULAR",
        narration: narr
      };

      var existingIdx = payments.findIndex(function(x){ return String(x.receiptNo) === recNo; });
      if(existingIdx >= 0){
        payments[existingIdx] = newP;
      } else {
        payments.unshift(newP);
      }

      // If principal repayment, reduce loan outstanding
      if(princVal > 0){
        var actLoan = loans.find(function(l){ return String(l.id) === m.id && String(l.status).toUpperCase() === "ACTIVE"; });
        if(actLoan){
          actLoan.repaid = cleanNum(actLoan.repaid, 0) + princVal;
          actLoan.outstanding = Math.max(0, cleanNum(actLoan.outstanding, 0) - princVal);
          if(actLoan.outstanding === 0) actLoan.status = "CLOSED";
          if(typeof google !== "undefined" && google.script && google.script.run){
            google.script.run.saveLoanBackend(actLoan);
          }
        }
      }

      if(typeof google !== "undefined" && google.script && google.script.run){
        google.script.run.savePaymentBackend(newP);
      }

      saveStore();
      closeModal("modalReceive");
      showNotice("Receipt Saved", "Receipt " + recNo + " of ₹" + tot.toLocaleString("en-IN") + " has been successfully posted and synced!");
    });

    // SUBMIT ISSUE LOAN / EDIT LOAN
    document.getElementById("btnSubmitLoan").addEventListener("click", function(){
      var lId = document.getElementById("editLoanId").value.trim();
      var mid = document.getElementById("selLoanMember").value;
      var m = members.find(function(x){ return String(x.id) === mid; });
      if(!m){ showNotice("Validation Error", "Please select a member."); return; }

      var lDate = document.getElementById("inpLoanDate").value || getTodayYMD();
      var lType = document.getElementById("selLoanType").value;
      var princ = cleanNum(document.getElementById("inpLoanPrinc").value, 0);
      var rate = Number(document.getElementById("inpLoanRate").value) || 1.0;
      var narr = document.getElementById("inpLoanNarration").value.trim();

      if(princ <= 0){
        showNotice("Validation Error", "Principal amount must be greater than 0.");
        return;
      }

      if(!lId){
        // Check active loan
        var activeDue = getMemberActiveLoan(m);
        if(activeDue > 0){
          showNotice("Loan Blocked", "Member already has an active loan outstanding of ₹" + activeDue.toLocaleString("en-IN") + ". Multiple active loans are not permitted.");
          return;
        }

        // STRICT ID FORMAT: LOAN + DD + MM + YY
        var dateSuffix = getDDMMYYFromYMD(lDate);
        var baseId = "LOAN" + dateSuffix;
        var finalId = baseId;
        var counter = 1;
        while(loans.some(function(l){ return String(l.loanId) === finalId; })){
          finalId = baseId + counter;
          counter++;
        }
        lId = finalId;
      }

      var newL = {
        loanId: lId,
        date: lDate,
        id: m.id,
        name: m.name,
        type: lType,
        principal: princ,
        rate: rate,
        repaid: 0,
        outstanding: princ,
        status: "ACTIVE",
        narration: narr
      };

      var exIdx = loans.findIndex(function(x){ return String(x.loanId) === lId; });
      if(exIdx >= 0){
        newL.repaid = loans[exIdx].repaid;
        newL.outstanding = Math.max(0, princ - newL.repaid);
        if(newL.outstanding === 0) newL.status = "CLOSED";
        loans[exIdx] = newL;
      } else {
        loans.unshift(newL);
      }

      if(typeof google !== "undefined" && google.script && google.script.run){
        google.script.run.saveLoanBackend(newL);
      }

      saveStore();
      closeModal("modalLoan");
      showNotice("Loan Disbursed", "Loan " + lId + " of ₹" + princ.toLocaleString("en-IN") + " has been approved & recorded.");
    });

    // SUBMIT ADD / EDIT MEMBER (WITH STRICT MEMDDMMYYYY AND PRESERVING FORM ON VALIDATION NOTICE)
    document.getElementById("btnSubmitMember").addEventListener("click", function(){
      var mid = document.getElementById("editMemId").value.trim();
      var name = document.getElementById("inpNewMemName").value.trim();
      var mob = document.getElementById("inpNewMemMobile").value.trim();
      var st = document.getElementById("inpNewMemStatus").value;
      var jDate = document.getElementById("inpNewMemJoinDate").value || getTodayYMD();
      var rdVal = cleanRd(document.getElementById("inpNewMemRd").value);
      var dueDayVal = document.getElementById("inpNewMemDueDay").value || "15th of every month";
      var addr = document.getElementById("inpNewMemAddress").value.trim();
      var nom = document.getElementById("inpNewMemNominee").value.trim();

      var opRd = cleanNum(document.getElementById("inpNewMemBal").value, 0);
      var opLoan = cleanNum(document.getElementById("inpNewMemOpLoan").value, 0);
      var opInt = cleanNum(document.getElementById("inpNewMemOpInt").value, 0);
      var opPen = cleanNum(document.getElementById("inpNewMemOpPen").value, 0);
      var custLim = cleanNum(document.getElementById("inpNewMemCustomLimit").value, 0);

      if(!name){
        showNotice("Required Field", "Please enter Member Full Name.", "modalMember");
        return;
      }
      if(!mob || mob.length !== 10 || isNaN(Number(mob))){
        showNotice("Required Field", "Mobile number must be exactly 10 digits.", "modalMember");
        return;
      }
      if(!addr){
        showNotice("Required Field", "Please enter Member Address.", "modalMember");
        return;
      }
      if(!nom){
        showNotice("Required Field", "Please enter Nominee / Reference details.", "modalMember");
        return;
      }

      if(!mid){
        // STRICT ID FORMAT: MEM + DD + MM + YYYY (4 digits, e.g. MEM04092026)
        var dateSuffix = getDDMMYYYYFromYMD(jDate);
        var baseId = "MEM" + dateSuffix;
        var finalId = baseId;
        var counter = 1;
        while(members.some(function(m){ return String(m.id) === finalId; })){
          finalId = baseId + counter;
          counter++;
        }
        mid = finalId;
      }

      var newM = {
        id: mid,
        name: name,
        mobile: mob,
        status: st,
        address: addr,
        nominee: nom,
        rd: rdVal,
        dateJoined: jDate,
        rdPaid: opRd,
        dueDay: dueDayVal,
        customLimit: custLim,
        opLoan: opLoan,
        opInt: opInt,
        opPen: opPen
      };

      var exIdx = members.findIndex(function(x){ return String(x.id) === mid; });
      if(exIdx >= 0){
        newM.rdPaid = members[exIdx].rdPaid; // retain opening
        members[exIdx] = newM;
      } else {
        members.push(newM);
      }

      if(typeof google !== "undefined" && google.script && google.script.run){
        google.script.run.saveMemberBackend(newM);
      }

      saveStore();
      closeModal("modalMember");
      showNotice("Member Saved", "Member profile for " + name + " (ID: " + mid + ") saved successfully!");
    });

    // SUBMIT BULK ENTRY (WITH SHORT NARRATION)
    document.getElementById("btnSubmitBulk").addEventListener("click", function(){
      var bDate = document.getElementById("inpBulkDate").value || getTodayYMD();
      var rows = document.querySelectorAll("#tbodyBulkList tr");
      var postedCount = 0;
      var grandTotalPosted = 0;

      rows.forEach(function(tr){
        var chk = tr.querySelector(".b-chk");
        if(chk && chk.checked){
          var mid = tr.getAttribute("data-id");
          var m = members.find(function(x){ return String(x.id) === mid; });
          if(m){
            var rdVal = cleanNum(tr.querySelector(".b-rd").value, 0);
            var intVal = cleanNum(tr.querySelector(".b-int").value, 0);
            var repayVal = cleanNum(tr.querySelector(".b-repay").value, 0);
            var penVal = cleanNum(tr.querySelector(".b-pen").value, 0);
            var wvrVal = cleanNum(tr.querySelector(".b-wvr").value, 0);
            var bMode = tr.querySelector(".b-mode").value;
            var bNarr = (tr.querySelector(".b-narr").value || "").trim() || "Bulk Collection";

            var tot = rdVal + intVal + repayVal + penVal - wvrVal;
            if(tot > 0){
              var dateSuffix = getDDMMYYFromYMD(bDate);
              var baseId = "CER" + dateSuffix;
              var finalId = baseId;
              var counter = 1;
              while(payments.some(function(p){ return String(p.receiptNo) === finalId; })){
                finalId = baseId + counter;
                counter++;
              }

              var newP = {
                receiptNo: finalId,
                date: bDate,
                id: m.id,
                name: m.name,
                rd: rdVal,
                interest: intVal,
                penalty: penVal,
                loanRepay: repayVal,
                waiver: wvrVal,
                total: tot,
                mode: bMode,
                by: "Admin",
                type: "REGULAR",
                narration: bNarr
              };

              payments.unshift(newP);

              if(repayVal > 0){
                var actLoan = loans.find(function(l){ return String(l.id) === m.id && String(l.status).toUpperCase() === "ACTIVE"; });
                if(actLoan){
                  actLoan.repaid = cleanNum(actLoan.repaid, 0) + repayVal;
                  actLoan.outstanding = Math.max(0, cleanNum(actLoan.outstanding, 0) - repayVal);
                  if(actLoan.outstanding === 0) actLoan.status = "CLOSED";
                  if(typeof google !== "undefined" && google.script && google.script.run){
                    google.script.run.saveLoanBackend(actLoan);
                  }
                }
              }

              if(typeof google !== "undefined" && google.script && google.script.run){
                google.script.run.savePaymentBackend(newP);
              }

              postedCount++;
              grandTotalPosted += tot;
            }
          }
        }
      });

      if(postedCount > 0){
        saveStore();
        closeModal("modalBulk");
        showNotice("Bulk Entry Posted", "Successfully posted " + postedCount + " receipts totaling ₹" + grandTotalPosted.toLocaleString("en-IN") + "!");
      } else {
        showNotice("Notice", "No collections selected or entered.");
      }
    });

    // PRINTING PASSBOOK
    document.getElementById("btnPrintLedgerPdf").addEventListener("click", function(){
      window.print();
    });
    document.getElementById("btnPrintBonusPdf").addEventListener("click", function(){
      window.print();
    });

    // Initial Render
    refreshAll();

    // Auto-fetch fresh sheet data on start
    if(typeof google !== "undefined" && google.script && google.script.run){
      google.script.run.withSuccessHandler(function(res){
        if(res && res.members && res.members.length > 0){
          members = res.members;
          payments = res.payments || [];
          loans = res.loans || [];
          exitSettlements = res.exitSettlements || [];
          bonusSettlements = res.bonusSettlements || [];
          if(res.users && res.users.length > 0) window.authorizedUsers = res.users;
          saveStore();
        }
      }).getSocietyFullData();
    }
  });
})();
</script>
`;
}

function getCompleteSoftwareClientScript() {
  return getClientScriptPartA() + getClientScriptPartB();
}
'''

with open("Part3B.gs", "w", encoding="utf-8") as f:
    f.write(script_3b.strip() + "\n")
print("Part3B.gs created successfully.")
