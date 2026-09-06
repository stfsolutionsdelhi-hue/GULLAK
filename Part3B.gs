function getClientScriptPartB() {
  return `
  // TAB 4: ANNUAL BONUS REGISTER WITH DYNAMIC SUBTOTALS
  function renderBonusTab(){
    var st = document.getElementById("selFilterBonusStatus").value;
    var sort = document.getElementById("selSortBonus").value;
    var fromD = document.getElementById("inpBonusFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpBonusFilterTo").value || "2030-12-31";
    var q = (document.getElementById("searchBonusInput").value || "").toLowerCase().trim();

    // Compute live global bonus stats
    var totalIntReceived = 0;
    payments.forEach(function(p){ totalIntReceived += cleanNum(p.interest, 0); });
    var totalCumulativeRd = 0;
    members.forEach(function(m){ if(String(m.status).toUpperCase() === "ACTIVE") totalCumulativeRd += getMemberTotalRd(m); });
    var totalAccruedBonus = 0;
    members.forEach(function(m){ totalAccruedBonus += getMemberBonus(m); });

    var elInt = document.getElementById("dispBonusTotalInterestRecv");
    var elRd = document.getElementById("dispBonusTotalRdBase");
    var elAcc = document.getElementById("dispBonusTotalAccrued");
    if(elInt) elInt.innerText = "₹" + totalIntReceived.toLocaleString("en-IN");
    if(elRd) elRd.innerText = "₹" + totalCumulativeRd.toLocaleString("en-IN");
    if(elAcc) elAcc.innerText = "₹" + totalAccruedBonus.toLocaleString("en-IN");

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

  function renderBonusSetoffRegister(){
    var fromD = document.getElementById("inpBonusSetoffFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpBonusSetoffFilterTo").value || "2026-12-31";
    var tbody = document.getElementById("tbodyBonusSetoffRegister"); if(!tbody) return;
    
    var filtered = bonusSettlements.filter(function(b){
      var d = b.date || "2026-01-01";
      return (d >= fromD && d <= toD);
    });

    if(filtered.length === 0){
      tbody.innerHTML = "<tr><td colspan='10' style='text-align:center;color:#94A3B8;'>No bonus set-offs recorded in date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
      var tf = document.getElementById("tfootBonusSetoffRegister"); if(tf) tf.innerHTML = "";
      return;
    }

    var h = "";
    var totalBonusSum = 0, totalNetPaid = 0;
    filtered.forEach(function(b){
      var bTot = cleanNum(b.totalBonus, 0);
      var netP = cleanNum(b.netPaid, 0);
      totalBonusSum += bTot;
      totalNetPaid += netP;
      h += "<tr>" +
        "<td style='font-family:monospace;color:#FBBF24;'>" + (b.settlementId || "-") + "</td>" +
        "<td>" + toDisplayDate(b.date) + "</td>" +
        "<td><strong>" + b.name + "</strong> (" + b.id + ")</td>" +
        "<td style='color:#C084FC;font-weight:700;'>₹" + bTot.toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjLoan, 0).toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjInt, 0).toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjRd, 0).toLocaleString("en-IN") + "</td>" +
        "<td>₹" + cleanNum(b.adjPenalty, 0).toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;font-weight:800;'>₹" + netP.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-size:0.75rem;color:" + (b.mode==="CASH"?"#F59E0B":"#38BDF8") + ";font-weight:700;'>" + (b.mode||"ONLINE") + "</span></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;

    var tf = document.getElementById("tfootBonusSetoffRegister");
    if(tf) {
      tf.innerHTML = 
        "<tr class='tfoot-total-row'>" +
          "<td colspan='3' style='color:#FBBF24;'>TOTAL SET-OFFS IN DATE RANGE</td>" +
          "<td style='color:#C084FC;font-weight:800;'>₹" + totalBonusSum.toLocaleString("en-IN") + "</td>" +
          "<td colspan='4'></td>" +
          "<td style='color:#10B981;font-weight:800;'>₹" + totalNetPaid.toLocaleString("en-IN") + "</td>" +
          "<td></td>" +
        "</tr>";
    }
  }

  function renderFundRegister(){
    var fromD = document.getElementById("inpFundFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpFundFilterTo").value || "2026-12-31";
    var accFilter = document.getElementById("selFundFilterAccount").value;
    var typeFilter = document.getElementById("selFundFilterType").value;
    var tbody = document.getElementById("tbodyFundRegisterList"); if(!tbody) return;

    var filtered = fundTransactions.filter(function(f){
      var d = f.date || "2026-01-01";
      var matchDate = (d >= fromD && d <= toD);
      var matchAcc = (accFilter === "ALL" || String(f.account).toUpperCase() === accFilter);
      var matchType = (typeFilter === "ALL" || String(f.type).toUpperCase() === typeFilter);
      return matchDate && matchAcc && matchType;
    });

    if(filtered.length === 0){
      tbody.innerHTML = "<tr><td colspan='7' style='text-align:center;color:#94A3B8;'>No fund transactions recorded in selected filter range</td></tr>";
      var tf = document.getElementById("tfootFundRegisterList"); if(tf) tf.innerHTML = "";
      return;
    }

    var h = "";
    var totalInvest = 0, totalBorrow = 0;
    filtered.forEach(function(f){
      var amt = cleanNum(f.amount, 0);
      var isInvest = (String(f.type).toUpperCase() === "INVEST");
      if(isInvest) totalInvest += amt; else totalBorrow += amt;

      var typeBadge = isInvest 
        ? "<span class='badge-active'>INVESTMENT (+)</span>"
        : "<span class='badge-inactive'>BORROWING (-)</span>";
      var amtColor = isInvest ? "#10B981" : "#EF4444";

      h += "<tr>" +
        "<td>" + toDisplayDate(f.date) + "</td>" +
        "<td style='font-family:monospace;color:#38BDF8;'>" + (f.id || "-") + "</td>" +
        "<td>" + typeBadge + "</td>" +
        "<td><strong style='color:#FBBF24;'>" + (f.account === "BANK" ? "CASH AT BANK" : "CASH IN HAND") + "</strong></td>" +
        "<td>" + (f.entity || "-") + "</td>" +
        "<td style='font-weight:800;color:" + amtColor + ";'>" + (isInvest ? "+₹" : "-₹") + amt.toLocaleString("en-IN") + "</td>" +
        "<td><small style='color:#CBD5E1;'>" + (f.narration || "-") + "</small></td>" +
      "</tr>";
    });
    tbody.innerHTML = h;

    var tf = document.getElementById("tfootFundRegisterList");
    if(tf) {
      tf.innerHTML = 
        "<tr class='tfoot-total-row'>" +
          "<td colspan='5' style='color:#FBBF24;'>NET FUND IMPACT: INVEST (+₹" + totalInvest.toLocaleString("en-IN") + ") | BORROW (-₹" + totalBorrow.toLocaleString("en-IN") + ")</td>" +
          "<td style='font-weight:800;color:" + (totalInvest >= totalBorrow ? "#10B981" : "#EF4444") + ";'>" +
            (totalInvest >= totalBorrow ? "+₹" : "-₹") + Math.abs(totalInvest - totalBorrow).toLocaleString("en-IN") +
          "</td>" +
          "<td></td>" +
        "</tr>";
    }
  }

  // PROFIT & LOSS REGISTER CALCULATOR & RENDERER
  function renderProfitAndLossRegister(){
    var fromD = document.getElementById("inpPlFilterFrom") ? document.getElementById("inpPlFilterFrom").value : "2026-01-01";
    var toD = document.getElementById("inpPlFilterTo") ? document.getElementById("inpPlFilterTo").value : "2026-12-31";
    var selFy = document.getElementById("selPlFinancialYear") ? document.getElementById("selPlFinancialYear").value : "2026";

    var selPlFyEl = document.getElementById("selPlFinancialYear");
    if(selPlFyEl && selPlFyEl.options.length === 0 && document.getElementById("selFinancialYear")){
      selPlFyEl.innerHTML = document.getElementById("selFinancialYear").innerHTML;
      selPlFyEl.value = selFy;
    }

    var intEarned = 0;
    var penReceived = 0;
    var waiverGiven = 0;
    var bonusPaid = 0;
    var bonusPayable = 0;

    var breakupRows = [];

    // 1. Payments: Interest, Penalty, Waiver
    payments.forEach(function(p){
      var d = p.date || "2026-01-01";
      if(d >= fromD && d <= toD){
        var iAmt = cleanNum(p.interest, 0);
        var pAmt = cleanNum(p.penalty, 0);
        var wAmt = cleanNum(p.waiver, 0);

        if(iAmt > 0){
          intEarned += iAmt;
          breakupRows.push({ date: d, category: "Interest Earned (+)", source: p.name + " (" + p.id + ")", inflow: iAmt, outflow: 0, remarks: "Receipt " + p.receiptNo });
        }
        if(pAmt > 0){
          penReceived += pAmt;
          breakupRows.push({ date: d, category: "Penalty Received (+)", source: p.name + " (" + p.id + ")", inflow: pAmt, outflow: 0, remarks: "Receipt " + p.receiptNo });
        }
        if(wAmt > 0){
          waiverGiven += wAmt;
          breakupRows.push({ date: d, category: "Waiver Granted (-)", source: p.name + " (" + p.id + ")", inflow: 0, outflow: wAmt, remarks: "Receipt " + p.receiptNo });
        }
      }
    });

    // 2. Bonus Settlements
    bonusSettlements.forEach(function(b){
      var d = b.date || "2026-01-01";
      if(d >= fromD && d <= toD){
        var bAmt = cleanNum(b.totalBonus, 0);
        if(bAmt > 0){
          bonusPaid += bAmt;
          breakupRows.push({ date: d, category: "Bonus Paid (-)", source: b.name + " (" + b.id + ")", inflow: 0, outflow: bAmt, remarks: "Settlement " + b.settlementId });
        }
      }
    });

    // 3. Exit Settlements Bonus Adjustment
    exitSettlements.forEach(function(e){
      var d = e.date || "2026-01-01";
      if(d >= fromD && d <= toD){
        var bAdj = cleanNum(e.bonusAdj, 0);
        if(bAdj > 0){
          bonusPaid += bAdj;
          breakupRows.push({ date: d, category: "Bonus Settled on Exit (-)", source: e.name + " (" + e.id + ")", inflow: 0, outflow: bAdj, remarks: "Exit " + e.exitId });
        }
      }
    });

    // 4. Estimated Bonus Payable
    members.forEach(function(m){
      if(String(m.status).toUpperCase() === "ACTIVE"){
        var mBonus = getMemberBonus(m);
        if(mBonus > 0){
          bonusPayable += mBonus;
        }
      }
    });

    var netProfit = (intEarned + penReceived) - (waiverGiven + bonusPaid + bonusPayable);

    if(document.getElementById("lblPlIntEarned")) document.getElementById("lblPlIntEarned").innerText = "₹" + intEarned.toLocaleString("en-IN");
    if(document.getElementById("lblPlPenReceived")) document.getElementById("lblPlPenReceived").innerText = "₹" + penReceived.toLocaleString("en-IN");
    if(document.getElementById("lblPlWaiver")) document.getElementById("lblPlWaiver").innerText = "₹" + waiverGiven.toLocaleString("en-IN");
    if(document.getElementById("lblPlBonusPaid")) document.getElementById("lblPlBonusPaid").innerText = "₹" + bonusPaid.toLocaleString("en-IN");
    if(document.getElementById("lblPlBonusPayable")) document.getElementById("lblPlBonusPayable").innerText = "₹" + bonusPayable.toLocaleString("en-IN");
    
    var netEl = document.getElementById("lblPlNetProfit");
    if(netEl){
      netEl.innerText = (netProfit >= 0 ? "+₹" : "-₹") + Math.abs(netProfit).toLocaleString("en-IN");
      netEl.style.color = (netProfit >= 0 ? "#10B981" : "#EF4444");
    }

    var tbody = document.getElementById("tbodyPlBreakup");
    if(tbody){
      if(breakupRows.length === 0){
        tbody.innerHTML = "<tr><td colspan='6' style='text-align:center;color:#94A3B8;'>No profit & loss transactions in selected range</td></tr>";
        if(document.getElementById("tfootPlBreakup")) document.getElementById("tfootPlBreakup").innerHTML = "";
      } else {
        breakupRows.sort(function(a,b){ return (b.date || "").localeCompare(a.date || ""); });
        var h = "";
        breakupRows.forEach(function(r){
          h += "<tr>" +
            "<td>" + toDisplayDate(r.date) + "</td>" +
            "<td><strong style='color:#38BDF8;'>" + r.category + "</strong></td>" +
            "<td>" + r.source + "</td>" +
            "<td style='color:#10B981;font-weight:700;'>" + (r.inflow > 0 ? "+₹" + r.inflow.toLocaleString("en-IN") : "-") + "</td>" +
            "<td style='color:#EF4444;font-weight:700;'>" + (r.outflow > 0 ? "-₹" + r.outflow.toLocaleString("en-IN") : "-") + "</td>" +
            "<td><small style='color:#CBD5E1;'>" + r.remarks + "</small></td>" +
          "</tr>";
        });
        tbody.innerHTML = h;

        var tfoot = document.getElementById("tfootPlBreakup");
        if(tfoot){
          var totIn = intEarned + penReceived;
          var totOut = waiverGiven + bonusPaid;
          tfoot.innerHTML = "<tr class='tfoot-total-row'>" +
            "<td colspan='3' style='color:#FBBF24;'>REALIZED NET INFLOW SUMMARY</td>" +
            "<td style='color:#10B981;font-weight:800;'>+₹" + totIn.toLocaleString("en-IN") + "</td>" +
            "<td style='color:#EF4444;font-weight:800;'>-₹" + totOut.toLocaleString("en-IN") + "</td>" +
            "<td style='color:" + (totIn >= totOut ? "#10B981" : "#EF4444") + ";font-weight:800;'>" + (totIn >= totOut ? "+₹" : "-₹") + Math.abs(totIn - totOut).toLocaleString("en-IN") + "</td>" +
          "</tr>";
        }
      }
    }
  }
  window.renderProfitAndLossRegister = renderProfitAndLossRegister;

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
      txns.push({ date: m.dateJoined || "2026-01-01", ref: "OPENING", partic: "Opening RD Balance (As on 31 Dec 2025)", rdDeposit: cleanNum(m.rdPaid, 0), loanIssued: 0, loanRepaid: 0, mode: "SYSTEM" });
    }
    if(cleanNum(m.opLoan, 0) > 0){
      txns.push({ date: m.dateJoined || "2026-01-01", ref: "OP-LOAN", partic: "Opening Loan Principal", rdDeposit: 0, loanIssued: cleanNum(m.opLoan, 0), loanRepaid: 0, mode: "SYSTEM" });
    }

    // Match all loans for this member by ID or Name
    loans.forEach(function(l){
      if(String(l.id).trim().toUpperCase() === actualId || String(l.name).trim().toLowerCase() === actualName){
        var lNarr = l.narration ? ('<br><small style="color:#94A3B8;font-size:0.75rem;font-style:italic;">📝 ' + l.narration + '</small>') : '';
        txns.push({
          date: l.date,
          ref: l.loanId,
          partic: "Loan Disbursed (" + l.type + " @ " + l.rate + "% p.m.)" + lNarr,
          rdDeposit: 0,
          loanIssued: cleanNum(l.principal, 0),
          loanRepaid: 0,
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
        if(cleanNum(p.loanRepay, 0) > 0) parts.push("Loan Repay ₹" + p.loanRepay);
        if(cleanNum(p.waiver, 0) > 0) parts.push("Waiver ₹" + p.waiver);

        var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
        txns.push({
          date: p.date,
          ref: p.receiptNo,
          partic: "Receipt: " + parts.join(", ") + pNarr,
          rdDeposit: cleanNum(p.rd, 0),
          loanIssued: 0,
          loanRepaid: cleanNum(p.loanRepay, 0),
          mode: safeMode
        });
      }
    });

    // Sort by date ascending
    txns.sort(function(a,b){ return new Date(a.date||"2026-01-01") - new Date(b.date||"2026-01-01"); });

    var runningRdBal = 0;
    var runningLoanBal = 0;
    var filteredTxns = txns.filter(function(t){ return (t.date >= fromD && t.date <= toD); });

    var tbody = document.getElementById("tbodyLedgerTxns");
    if(filteredTxns.length === 0){
      tbody.innerHTML = "<tr><td colspan='9' style='text-align:center;color:#94A3B8;'>No transactions in selected date range (" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + ")</td></tr>";
    } else {
      var h = "";
      filteredTxns.forEach(function(t){
        runningRdBal += t.rdDeposit;
        runningLoanBal += t.loanIssued;
        runningLoanBal = Math.max(0, runningLoanBal - t.loanRepaid);

        h += "<tr>" +
          "<td>" + toDisplayDate(t.date) + "</td>" +
          "<td style='font-family:monospace;color:#FBBF24;'>" + t.ref + "</td>" +
          "<td>" + t.partic + "</td>" +
          "<td style='color:#10B981;font-weight:700;'>" + (t.rdDeposit > 0 ? "₹" + t.rdDeposit.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#10B981;font-weight:800;'>₹" + runningRdBal.toLocaleString("en-IN") + "</td>" +
          "<td style='color:#EF4444;'>" + (t.loanIssued > 0 ? "₹" + t.loanIssued.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#38BDF8;font-weight:700;'>" + (t.loanRepaid > 0 ? "₹" + t.loanRepaid.toLocaleString("en-IN") : "-") + "</td>" +
          "<td style='color:#EF4444;font-weight:800;'>₹" + runningLoanBal.toLocaleString("en-IN") + "</td>" +
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
        "<td style='text-align:center;'><input type='checkbox' class='b-chk' checked onchange='calcBulkTotals()'></td>" +
        "<td><strong>" + m.name + "</strong><br><small style='color:#38BDF8;'>RD ₹" + safeRd + " | Int ₹" + intDue + " | Pen ₹" + penDue + "</small></td>" +
        "<td><input type='number' class='field-ctrl b-rd' value='" + safeRd + "' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-int' value='" + intDue + "' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-repay' value='0' oninput='calcBulkRow(this)' style='width:80px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-pen' value='" + penDue + "' oninput='calcBulkRow(this)' style='width:75px;text-align:right;'></td>" +
        "<td><input type='number' class='field-ctrl b-wvr' value='0' oninput='calcBulkRow(this)' style='width:75px;text-align:right;'></td>" +
        "<td style='text-align:right; font-weight:800; color:#10B981;' class='b-tot-cell'>₹" + initialTotal.toLocaleString("en-IN") + "</td>" +
        "<td><select class='field-ctrl b-mode' style='width:85px;padding:4px;'><option value='CASH'>CASH</option><option value='ONLINE'>ONLINE</option></select></td>" +
        "<td><input type='text' class='field-ctrl b-narr' placeholder='Remarks' style='width:130px;padding:4px;font-size:0.78rem;'></td>" +
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
    var h = document.getElementById("noticeHeader");
    var b = document.getElementById("noticeBody");
    if(h) h.innerText = title || "Notice";
    if(b) b.innerText = message || "";
    activeKeepModal = keepModalId || null;
    openModal("modalNotice");
  }

  function handleNoticeOkClick(){
    closeModal("modalNotice");
    if(activeKeepModal){
      openModal(activeKeepModal);
      activeKeepModal = null;
    }
  }

  // OPEN RECEIVE MODAL FOR MEMBER (AUTO-FILL LIVE DUES)
  function openReceiveModalFor(mid, recNo){
    if(!mid){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      mid = firstActive ? firstActive.id : (members[0] ? members[0].id : "");
    }
    if(!mid) return;
    var m = members.find(function(x){ return String(x.id).trim().toUpperCase() === String(mid).trim().toUpperCase() || String(x.name).trim().toLowerCase() === String(mid).trim().toLowerCase(); });
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
    if(!mid){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      mid = firstActive ? firstActive.id : (members[0] ? members[0].id : "");
    }
    var m = mid ? members.find(function(x){ return String(x.id).trim().toUpperCase() === String(mid).trim().toUpperCase() || String(x.name).trim().toLowerCase() === String(mid).trim().toLowerCase(); }) : null;
    document.getElementById("editLoanId").value = lId || "";
    document.getElementById("lblLoanModalHead").innerText = lId ? ("✏️ Edit Society Loan: " + lId) : "💸 Issue Society Loan";

    var sel = document.getElementById("selLoanMember");
    sel.innerHTML = "";
    members.forEach(function(mem){
      if(String(mem.status).toUpperCase() === "ACTIVE"){
        var isSel = (m && String(mem.id) === String(mem.id)) ? "selected" : "";
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
    renderFundRegister();
    renderProfitAndLossRegister();
    if(currentActiveLedgerMember && document.getElementById("modalLedger").style.display === "flex"){
      openMemberLedger(currentActiveLedgerMember.id);
    }
  }

  // EVENT LISTENERS & HOOKS
  window.refreshAll = refreshAll;

  function bootApplication(){
    // Check saved session
    try {
      var saved = sessionStorage.getItem("gullak_v22_session") || sessionStorage.getItem("gullak_v21_session");
      if(saved){
        var sess = JSON.parse(saved);
        if(sess && (String(sess.username).toUpperCase() === "SANISH" || String(sess.username).toUpperCase() === "ADMIN")){
          window.currentUserSession = sess;
          var overlay = document.getElementById("windowsLoginOverlay");
          if(overlay) {
            overlay.style.display = "none";
            overlay.style.setProperty("display", "none", "important");
          }
        }
      }
    } catch(e) {}

    // Global ESC key and Arrow Up / Down listener
    window.addEventListener("keydown", function(e){
      if(e.key === "Escape" || e.keyCode === 27){
        var openModalEl = document.querySelector(".modal-backdrop[style*='display: flex'], .modal-backdrop[style*='display: block']");
        if(openModalEl){
          if(e.preventDefault) e.preventDefault();
          if(e.stopPropagation) e.stopPropagation();
          if(e.stopImmediatePropagation) e.stopImmediatePropagation();
          if(typeof closeAllModals === "function"){
            closeAllModals();
          }
          if(window.isAppInFullscreenMode || document.body.classList.contains("simulated-fullscreen")){
            document.body.classList.add("simulated-fullscreen");
          }
          return false;
        }
      }
      if(e.key === "F11" || e.keyCode === 122){
        e.preventDefault();
        window.safeToggleFullscreen();
      }
      // Arrow Up and Down scroll support in Fullscreen mode (both Dashboard and inside any open Form)
      var activeModalBox = document.querySelector(".modal-backdrop[style*='display: flex'] .modal-dialog-box") ||
                           document.querySelector(".modal-backdrop[style*='display: block'] .modal-dialog-box");
      if(e.key === "ArrowDown"){
        if(activeModalBox){
          activeModalBox.scrollTop += 60;
        } else {
          window.scrollBy(0, 60);
        }
      } else if(e.key === "ArrowUp"){
        if(activeModalBox){
          activeModalBox.scrollTop -= 60;
        } else {
          window.scrollBy(0, -60);
        }
      } else if(e.key === "PageDown"){
        if(activeModalBox){
          activeModalBox.scrollTop += 300;
        } else {
          window.scrollBy(0, 300);
        }
      } else if(e.key === "PageUp"){
        if(activeModalBox){
          activeModalBox.scrollTop -= 300;
        } else {
          window.scrollBy(0, -300);
        }
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

    function safeAddListener(id, evt, fn){
      var el = document.getElementById(id);
      if(el) {
        el.addEventListener(evt, fn);
        return true;
      }
      return false;
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
    window.switchTab = switchTab;

    safeAddListener("tabHead1", "click", function(){ switchTab(1); renderMembers(); });
    safeAddListener("tabHead2", "click", function(){ switchTab(2); renderPayments(); });
    safeAddListener("tabHead3", "click", function(){ switchTab(3); renderLoans(); });
    safeAddListener("tabHead4", "click", function(){ switchTab(4); renderBonusTab(); });
    safeAddListener("tabHead5", "click", function(){ switchTab(5); renderPenaltyTab(); });

    // Top action buttons
    function openDefaultReceiveModal(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openReceiveModalFor(firstActive ? firstActive.id : "MEM010120261");
    }
    function openDefaultLoanModal(){
      var firstActive = members.find(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
      openLoanModalFor(firstActive ? firstActive.id : "MEM010120261");
    }

    safeAddListener("btnTopReceive", "click", openDefaultReceiveModal);
    safeAddListener("btnPanelNewReceipt", "click", openDefaultReceiveModal);
    safeAddListener("btnTopLoan", "click", openDefaultLoanModal);
    safeAddListener("btnPanelNewLoan", "click", openDefaultLoanModal);

    safeAddListener("btnTopAddMember", "click", function(){
      document.getElementById("editMemId").value = "";
      document.getElementById("lblMemberModalHead").innerText = "👤 Add New Member Profile";
      document.getElementById("inpNewMemName").value = "";
      document.getElementById("inpNewMemMobile").value = "";
      document.getElementById("inpNewMemStatus").value = "ACTIVE";
      document.getElementById("inpNewMemJoinDate").value = getTodayYMD();
      document.getElementById("inpNewMemRd").value = 400;
      var defDue = getTodayYMD().substring(0,8) + "15";
      document.getElementById("inpNewMemDueDay").value = defDue;
      if(document.getElementById("dispDueDayFormatted")) document.getElementById("dispDueDayFormatted").innerText = "(" + toDisplayDate(defDue) + ")";
      document.getElementById("inpNewMemAddress").value = "";
      document.getElementById("inpNewMemNominee").value = "";
      document.getElementById("inpNewMemBal").value = 0;
      document.getElementById("inpNewMemOpLoan").value = 0;
      document.getElementById("inpNewMemOpInt").value = 0;
      document.getElementById("inpNewMemOpPen").value = 0;
      document.getElementById("inpNewMemCustomLimit").value = 0;
      openModal("modalMember");
    });

    safeAddListener("inpNewMemDueDay", "change", function(){
      var v = this.value;
      var el = document.getElementById("dispDueDayFormatted");
      if(el) el.innerText = v ? "(" + toDisplayDate(v) + ")" : "";
    });

    safeAddListener("btnTopBulk", "click", function(){
      var today = getTodayYMD();
      document.getElementById("inpBulkDate").value = today;
      document.getElementById("dispBulkDateFormatted").innerText = "(" + toDisplayDate(today) + ")";
      renderBulkList();
      openModal("modalBulk");
    });

    safeAddListener("btnBulkSetAllCash", "click", function(){
      document.querySelectorAll("#tbodyBulkList .b-mode").forEach(function(sel){ sel.value = "CASH"; });
    });
    safeAddListener("btnBulkSetAllOnline", "click", function(){
      document.querySelectorAll("#tbodyBulkList .b-mode").forEach(function(sel){ sel.value = "ONLINE"; });
    });
    safeAddListener("chkSelectAllBulk", "change", function(){
      var isChk = this.checked;
      document.querySelectorAll("#tbodyBulkList .b-chk").forEach(function(c){ c.checked = isChk; });
      calcBulkTotals();
    });

    safeAddListener("btnTopExit", "click", function(){
      var sel = document.getElementById("selExitMember");
      if(sel){
        sel.innerHTML = "<option value=''>-- Select Member --</option>";
        members.forEach(function(m){
          if(String(m.status).toUpperCase() === "ACTIVE"){
            sel.innerHTML += "<option value='" + m.id + "'>" + m.name + " (" + m.id + ")</option>";
          }
        });
      }
      openModal("modalExit");
    });

    safeAddListener("btnTopSettings", "click", function(){
      var d = document.getElementById("inpGlobalDueDay");
      var r = document.getElementById("inpGlobalRate");
      var ps = document.getElementById("inpPenaltyStartDate");
      var chk = document.getElementById("chkSkipPenalty");
      if(d) d.value = globalDefaultDue;
      if(r) r.value = globalDefaultRate;
      if(ps && window.globalSettings && window.globalSettings.penaltyStartDate) ps.value = window.globalSettings.penaltyStartDate;
      if(chk && window.globalSettings && typeof window.globalSettings.skipPenalty === "boolean") chk.checked = window.globalSettings.skipPenalty;
      openModal("modalSettings");
    });

    safeAddListener("btnOpenGoogleSheet", "click", window.handleOpenSpreadsheet);

    safeAddListener("btnApplyGlobalSettings", "click", function(){
      globalDefaultDue = (document.getElementById("inpGlobalDueDay").value || "").trim() || "15th of every month";
      globalDefaultRate = Number(document.getElementById("inpGlobalRate").value) || 1.0;
      var penStart = document.getElementById("inpPenaltyStartDate") ? document.getElementById("inpPenaltyStartDate").value : "2026-10-01";
      var skipPen = document.getElementById("chkSkipPenalty") ? document.getElementById("chkSkipPenalty").checked : true;

      if(!window.globalSettings) window.globalSettings = {};
      window.globalSettings.penaltyStartDate = penStart;
      window.globalSettings.skipPenalty = skipPen;
      saveStore();

      closeModal("modalSettings");
      refreshAll();
      showNotice("Settings Saved", "Global settings updated successfully! Overdue penalty status: " + (skipPen ? "PAUSED / SKIPPED" : "Active from " + toDisplayDate(penStart)));
    });

    safeAddListener("btnNoticeOk", "click", handleNoticeOkClick);

    // SYNC FROM GOOGLE SHEET DATABASE
    safeAddListener("btnTopReload", "click", function(){
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
            if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
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
    safeAddListener("kpiCardMembers", "click", function(){ switchTab(1); });
    safeAddListener("kpiCardRd", "click", function(){ switchTab(2); });
    safeAddListener("kpiCardLoans", "click", function(){ switchTab(3); });
    safeAddListener("kpiCardBonus", "click", function(){ switchTab(4); });
    safeAddListener("kpiCardNpa", "click", function(){
      renderNpaList();
      openModal("modalNpa");
    });
    safeAddListener("btnApplyNpaFilter", "click", function(){
      renderNpaList();
    });

    safeAddListener("kpiCardFund", "click", function(){
      var cSum = 0, bSum = 0;
      payments.forEach(function(p){
        var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
        if(safeMode === "ONLINE") bSum += cleanNum(p.total, 0); else cSum += cleanNum(p.total, 0);
      });
      var cashEl = document.getElementById("lblRegCashBal");
      var bankEl = document.getElementById("lblRegBankBal");
      var fundEl = document.getElementById("lblRegTotalFund");
      if(cashEl) cashEl.innerText = "₹" + cSum.toLocaleString("en-IN");
      if(bankEl) bankEl.innerText = "₹" + bSum.toLocaleString("en-IN");
      if(fundEl) fundEl.innerText = "₹" + (cSum + bSum).toLocaleString("en-IN");
      openModal("modalFund");
    });

    // Filters on change
    safeAddListener("selFilterStatus", "change", renderMembers);
    safeAddListener("selSortMembers", "change", renderMembers);
    safeAddListener("memberFilterInput", "input", renderMembers);

    safeAddListener("inpPayFilterFrom", "change", renderPayments);
    safeAddListener("inpPayFilterTo", "change", renderPayments);
    safeAddListener("selFilterPayMode", "change", renderPayments);
    safeAddListener("selSortPayDate", "change", renderPayments);
    safeAddListener("searchPayInput", "input", renderPayments);

    safeAddListener("inpLoanFilterFrom", "change", renderLoans);
    safeAddListener("inpLoanFilterTo", "change", renderLoans);
    safeAddListener("selFilterLoanType", "change", renderLoans);
    safeAddListener("selFilterLoanStatus", "change", renderLoans);
    safeAddListener("searchLoanInput", "input", renderLoans);

    safeAddListener("inpBonusFilterFrom", "change", renderBonusTab);
    safeAddListener("inpBonusFilterTo", "change", renderBonusTab);
    safeAddListener("selFilterBonusStatus", "change", renderBonusTab);
    safeAddListener("selSortBonus", "change", renderBonusTab);
    safeAddListener("searchBonusInput", "input", renderBonusTab);

    safeAddListener("inpPenFilterFrom", "change", renderPenaltyTab);
    safeAddListener("inpPenFilterTo", "change", renderPenaltyTab);
    safeAddListener("selFilterPenStatus", "change", renderPenaltyTab);
    safeAddListener("selSortPen", "change", renderPenaltyTab);
    safeAddListener("searchPenInput", "input", renderPenaltyTab);

    safeAddListener("selFinancialYear", "change", function(){
      refreshAll();
    });

    // Passbook dynamic date filter
    safeAddListener("inpLedgerFilterFrom", "change", function(){
      if(currentActiveLedgerMember) openMemberLedger(currentActiveLedgerMember.id);
    });
    safeAddListener("inpLedgerFilterTo", "change", function(){
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
          var dueVal = m.dueDay || "";
          if(!dueVal || dueVal.indexOf("month") >= 0 || dueVal.indexOf("th") >= 0 || dueVal.length < 8){
            dueVal = getTodayYMD().substring(0,8) + "15";
          }
          document.getElementById("inpNewMemDueDay").value = dueVal;
          if(document.getElementById("dispDueDayFormatted")) document.getElementById("dispDueDayFormatted").innerText = "(" + toDisplayDate(dueVal) + ")";
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
      if(t.classList.contains("action-setoff-bonus")){
        var mid = t.getAttribute("data-id");
        var m = members.find(function(x){ return String(x.id) === mid; });
        if(m){
          var bVal = getMemberBonus(m);
          document.getElementById("bonusMemId").value = m.id;
          document.getElementById("lblBonusTargetMember").innerText = m.name + " (" + m.id + ")";
          document.getElementById("lblBonusAmount").innerText = "₹" + bVal.toLocaleString("en-IN");
          document.getElementById("inpBonusDate").value = getTodayYMD();
          document.getElementById("inpBonusNetPaid").value = bVal;
          openModal("modalBonusSetoff");
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

    // SUBMIT BONUS SET-OFF
    safeAddListener("btnSubmitBonusSetoff", "click", function(){
      var mid = document.getElementById("bonusMemId").value;
      var m = members.find(function(x){ return String(x.id) === mid; });
      if(!m) return;
      var bVal = getMemberBonus(m);
      var sDate = document.getElementById("inpBonusDate").value || getTodayYMD();
      var mode = document.getElementById("selBonusMode").value || "ONLINE";
      var setoffId = generateAutoId("SET", sDate);

      var rec = {
        settlementId: setoffId,
        date: sDate,
        id: m.id,
        name: m.name,
        totalBonus: bVal,
        adjLoan: 0,
        adjInt: 0,
        adjRd: 0,
        adjPenalty: 0,
        netPaid: bVal,
        mode: mode
      };
      bonusSettlements.push(rec);
      saveStore();
      closeModal("modalBonusSetoff");
      refreshAll();
      showNotice("Bonus Settled", "Bonus set-off of ₹" + bVal.toLocaleString("en-IN") + " has been posted successfully for " + m.name + ".\\nRef ID: " + setoffId);
    });

    // BONUS SUB-TABS (INTEREST RECEIVED & SET-OFF REGISTER)
    safeAddListener("btnBonusSubTab1", "click", function(){
      document.getElementById("btnBonusSubTab1").classList.add("active");
      document.getElementById("btnBonusSubTab2").classList.remove("active");
      document.getElementById("bonusSubView1").style.display = "block";
      document.getElementById("bonusSubView2").style.display = "none";
    });
    safeAddListener("btnBonusSubTab2", "click", function(){
      document.getElementById("btnBonusSubTab2").classList.add("active");
      document.getElementById("btnBonusSubTab1").classList.remove("active");
      document.getElementById("bonusSubView2").style.display = "block";
      document.getElementById("bonusSubView1").style.display = "none";
      renderBonusSetoffRegister();
    });
    safeAddListener("inpBonusSetoffFilterFrom", "change", renderBonusSetoffRegister);
    safeAddListener("inpBonusSetoffFilterTo", "change", renderBonusSetoffRegister);

    // SETTINGS MODAL & FUND REGISTER SUB-TABS
    function switchSettingsSubTab(tabNum){
      [1, 2, 3, 4].forEach(function(n){
        var b = document.getElementById("btnSettingsSubTab" + n);
        var v = document.getElementById("settingsSubView" + n);
        if(b) {
          if(n === tabNum) b.classList.add("active"); else b.classList.remove("active");
        }
        if(v) {
          v.style.display = (n === tabNum) ? "block" : "none";
        }
      });
      if(tabNum === 2) renderFundRegister();
      if(tabNum === 4) renderProfitAndLossRegister();
    }
    safeAddListener("btnSettingsSubTab1", "click", function(){ switchSettingsSubTab(1); });
    safeAddListener("btnSettingsSubTab2", "click", function(){ switchSettingsSubTab(2); });
    safeAddListener("btnSettingsSubTab3", "click", function(){ switchSettingsSubTab(3); });
    safeAddListener("btnSettingsSubTab4", "click", function(){ switchSettingsSubTab(4); });
    safeAddListener("btnSwitchToFundForm", "click", function(){ switchSettingsSubTab(3); });

    safeAddListener("inpFundFilterFrom", "change", renderFundRegister);
    safeAddListener("inpFundFilterTo", "change", renderFundRegister);
    safeAddListener("selFundFilterAccount", "change", renderFundRegister);
    safeAddListener("selFundFilterType", "change", renderFundRegister);

    safeAddListener("inpPlFilterFrom", "change", renderProfitAndLossRegister);
    safeAddListener("inpPlFilterTo", "change", renderProfitAndLossRegister);
    safeAddListener("selPlFinancialYear", "change", renderProfitAndLossRegister);
    safeAddListener("btnApplyPlFilter", "click", renderProfitAndLossRegister);

    // SUBMIT FUND ENTRY (BORROW / INVEST)
    safeAddListener("btnSubmitFundEntry", "click", function(){
      var fType = document.getElementById("inpFundEntryType") ? document.getElementById("inpFundEntryType").value : "INVEST";
      var fAcc = document.getElementById("inpFundEntryAccount") ? document.getElementById("inpFundEntryAccount").value : "BANK";
      var fDate = (document.getElementById("inpFundEntryDate") && document.getElementById("inpFundEntryDate").value) ? document.getElementById("inpFundEntryDate").value : getTodayYMD();
      var rawAmt = document.getElementById("inpFundEntryAmount") ? document.getElementById("inpFundEntryAmount").value : "0";
      var fAmt = cleanNum(rawAmt, 0);
      var fEntity = document.getElementById("inpFundEntryEntity") ? (document.getElementById("inpFundEntryEntity").value || "").trim() : "";
      var fNarr = document.getElementById("inpFundEntryNarration") ? (document.getElementById("inpFundEntryNarration").value || "").trim() : "";

      if(fAmt <= 0){
        showNotice("Validation Error", "Kripya sahi transaction amount bharein (e.g. 50000). Amount 0 se bada hona chahiye.", "modalSettings");
        return;
      }
      var txnId = generateAutoId("FUND", fDate);
      var fObj = {
        id: txnId,
        date: fDate,
        type: fType,
        account: fAcc,
        entity: fEntity,
        amount: fAmt,
        narration: fNarr
      };
      fundTransactions.push(fObj);
      window.fundTransactions = fundTransactions;
      saveStore();

      if(typeof google !== "undefined" && google.script && google.script.run){
        google.script.run.saveFundTransactionBackend(fObj);
      }

      if(document.getElementById("inpFundEntryAmount")) document.getElementById("inpFundEntryAmount").value = "";
      if(document.getElementById("inpFundEntryEntity")) document.getElementById("inpFundEntryEntity").value = "";
      if(document.getElementById("inpFundEntryNarration")) document.getElementById("inpFundEntryNarration").value = "";
      switchSettingsSubTab(2);
      refreshAll();
      showNotice("Transaction Saved", "Fund transaction " + txnId + " (" + fType + ": ₹" + fAmt.toLocaleString("en-IN") + ") successfully recorded.", "modalSettings");
    });

    // SUBMIT RECEIVE AMOUNT / EDIT RECEIPT
    safeAddListener("btnSubmitReceive", "click", function(){
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
        recNo = generateAutoId("REC", pDate);
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
    safeAddListener("btnSubmitLoan", "click", function(){
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

        lId = generateAutoId("LN", lDate);
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
    safeAddListener("btnSubmitMember", "click", function(){
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
        mid = generateAutoId("MEM", jDate);
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
    safeAddListener("btnSubmitBulk", "click", function(){
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
              var finalId = generateAutoId("REC", bDate);

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
    safeAddListener("btnPrintLedgerPdf", "click", function(){
      window.print();
    });
    safeAddListener("btnPrintBonusPdf", "click", function(){
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
          if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
          saveStore();
          refreshAll();
        }
      }).getSocietyFullData();
    }
  }

  // EXPOSE ALL ESSENTIAL FUNCTIONS GLOBALLY
  window.bootApplication = bootApplication;
  window.refreshAll = refreshAll;
  window.openModal = openModal;
  window.closeModal = closeModal;
  window.closeAllModals = closeAllModals;
  window.showNotice = showNotice;
  window.openReceiveModalFor = openReceiveModalFor;
  window.openLoanModalFor = openLoanModalFor;
  window.renderMembers = renderMembers;
  window.renderPayments = renderPayments;
  window.renderLoans = renderLoans;
  window.renderBonusTab = renderBonusTab;
  window.renderPenaltyTab = renderPenaltyTab;

  window.openAddMemberModal = function(){
    var editId = document.getElementById("editMemId");
    if(editId) editId.value = "";
    var lbl = document.getElementById("lblMemberModalHead");
    if(lbl) lbl.innerText = "👤 Add New Member Profile";
    var nameEl = document.getElementById("inpNewMemName"); if(nameEl) nameEl.value = "";
    var mobEl = document.getElementById("inpNewMemMobile"); if(mobEl) mobEl.value = "";
    var stEl = document.getElementById("inpNewMemStatus"); if(stEl) stEl.value = "ACTIVE";
    var jDateEl = document.getElementById("inpNewMemJoinDate"); if(jDateEl) jDateEl.value = getTodayYMD();
    var rdEl = document.getElementById("inpNewMemRd"); if(rdEl) rdEl.value = 400;
    var defDue = getTodayYMD().substring(0,8) + "15";
    var dueEl = document.getElementById("inpNewMemDueDay"); if(dueEl) dueEl.value = defDue;
    var dueFmt = document.getElementById("dispDueDayFormatted"); if(dueFmt) dueFmt.innerText = "(" + toDisplayDate(defDue) + ")";
    var addrEl = document.getElementById("inpNewMemAddress"); if(addrEl) addrEl.value = "";
    var nomEl = document.getElementById("inpNewMemNominee"); if(nomEl) nomEl.value = "";
    var balEl = document.getElementById("inpNewMemBal"); if(balEl) balEl.value = 0;
    var opLoanEl = document.getElementById("inpNewMemOpLoan"); if(opLoanEl) opLoanEl.value = 0;
    var opIntEl = document.getElementById("inpNewMemOpInt"); if(opIntEl) opIntEl.value = 0;
    var opPenEl = document.getElementById("inpNewMemOpPen"); if(opPenEl) opPenEl.value = 0;
    var limEl = document.getElementById("inpNewMemCustomLimit"); if(limEl) limEl.value = 0;
    openModal("modalMember");
  };

  window.openBulkModal = function(){
    var today = getTodayYMD();
    var bDate = document.getElementById("inpBulkDate"); if(bDate) bDate.value = today;
    var bFmt = document.getElementById("dispBulkDateFormatted"); if(bFmt) bFmt.innerText = "(" + toDisplayDate(today) + ")";
    renderBulkList();
    openModal("modalBulk");
  };

  window.renderFundModal = function(){
    var cIn = 0, cOut = 0, bIn = 0, bOut = 0;

    payments.forEach(function(p){
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "BANK" : "CASH";
      var amt = cleanNum(p.total, 0);
      if(safeMode === "BANK") bIn += amt; else cIn += amt;
    });

    fundTransactions.forEach(function(f){
      var acc = String(f.account || "BANK").toUpperCase() === "CASH" ? "CASH" : "BANK";
      var type = String(f.type || "INVEST").toUpperCase();
      var amt = cleanNum(f.amount, 0);
      if(type === "INVEST"){
        if(acc === "CASH") cIn += amt; else bIn += amt;
      } else {
        if(acc === "CASH") cOut += amt; else bOut += amt;
      }
    });

    loans.forEach(function(l){
      var safeMode = String(l.mode || "CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "BANK" : "CASH";
      var amt = cleanNum(l.principal, 0);
      if(safeMode === "BANK") bOut += amt; else cOut += amt;
    });

    exitSettlements.forEach(function(x){
      var amt = cleanNum(x.payout, 0);
      cOut += amt;
    });

    var netCash = cIn - cOut;
    var netBank = bIn - bOut;
    var netTotal = netCash + netBank;

    var cashEl = document.getElementById("lblRegCashBal");
    var bankEl = document.getElementById("lblRegBankBal");
    var fundEl = document.getElementById("lblRegTotalFund");
    if(cashEl) cashEl.innerText = "₹" + netCash.toLocaleString("en-IN");
    if(bankEl) bankEl.innerText = "₹" + netBank.toLocaleString("en-IN");
    if(fundEl) fundEl.innerText = "₹" + netTotal.toLocaleString("en-IN");

    var monthMap = {};
    var monthList = ["2026-01","2026-02","2026-03","2026-04","2026-05","2026-06","2026-07","2026-08","2026-09","2026-10","2026-11","2026-12"];
    monthList.forEach(function(mKey){
      monthMap[mKey] = { inflow: 0, outflow: 0 };
    });

    payments.forEach(function(p){
      var mKey = String(p.date || "2026-01-01").substring(0, 7);
      if(!monthMap[mKey]) monthMap[mKey] = { inflow: 0, outflow: 0 };
      monthMap[mKey].inflow += cleanNum(p.total, 0);
    });

    fundTransactions.forEach(function(f){
      var mKey = String(f.date || "2026-01-01").substring(0, 7);
      if(!monthMap[mKey]) monthMap[mKey] = { inflow: 0, outflow: 0 };
      if(String(f.type || "INVEST").toUpperCase() === "INVEST"){
        monthMap[mKey].inflow += cleanNum(f.amount, 0);
      } else {
        monthMap[mKey].outflow += cleanNum(f.amount, 0);
      }
    });

    loans.forEach(function(l){
      var mKey = String(l.date || "2026-01-01").substring(0, 7);
      if(!monthMap[mKey]) monthMap[mKey] = { inflow: 0, outflow: 0 };
      monthMap[mKey].outflow += cleanNum(l.principal, 0);
    });

    exitSettlements.forEach(function(x){
      var mKey = String(x.date || "2026-01-01").substring(0, 7);
      if(!monthMap[mKey]) monthMap[mKey] = { inflow: 0, outflow: 0 };
      monthMap[mKey].outflow += cleanNum(x.payout, 0);
    });

    var tbodyMonths = document.getElementById("tbodyFundMonths");
    if(tbodyMonths){
      var monthNames = ["Jan 2026","Feb 2026","Mar 2026","Apr 2026","May 2026","Jun 2026","Jul 2026","Aug 2026","Sep 2026","Oct 2026","Nov 2026","Dec 2026"];
      var html = "";
      monthList.forEach(function(mKey, idx){
        var data = monthMap[mKey] || { inflow: 0, outflow: 0 };
        var net = data.inflow - data.outflow;
        var netBadge = net >= 0 ? "<span style='color:#10B981; font-weight:700;'>+₹" + net.toLocaleString("en-IN") + "</span>" : "<span style='color:#EF4444; font-weight:700;'>-₹" + Math.abs(net).toLocaleString("en-IN") + "</span>";
        
        html += "<tr>" +
          "<td style='font-weight:700; color:#FBBF24;'>" + (monthNames[idx] || mKey) + "</td>" +
          "<td style='color:#10B981; font-weight:700;'>+₹" + data.inflow.toLocaleString("en-IN") + "</td>" +
          "<td style='color:#EF4444; font-weight:700;'>-₹" + data.outflow.toLocaleString("en-IN") + "</td>" +
          "<td>" + netBadge + "</td>" +
          "<td><button class='btn btn-dark action-view-fund-drilldown' data-ym='" + mKey + "' style='padding:3px 8px; font-size:0.75rem;'>🔍 Details</button></td>" +
        "</tr>";
      });
      tbodyMonths.innerHTML = html;
    }
  };

  window.openFundModal = function(){
    renderFundModal();
    openModal("modalFund");
  };

  window.openNpaModal = function(){
    renderNpaList();
    openModal("modalNpa");
  };

  window.logoutSession = function(){
    try { sessionStorage.removeItem("gullak_v21_active_user"); } catch(e){}
    var overlay = document.getElementById("windowsLoginOverlay");
    if(overlay) overlay.style.display = "flex";
  };

  window.handleTopReload = function(){
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
          if(res.spreadsheetUrl) window.connectedSpreadsheetUrl = res.spreadsheetUrl;
          saveStore();
          showNotice("Sync Complete", "Successfully synchronized " + members.length + " members, " + payments.length + " receipts, and " + loans.length + " loans from Google Sheet!");
        }
      }).getSocietyFullData();
    } else {
      refreshAll();
      showNotice("Local Reloaded", "Database re-indexed locally.");
    }
  };

  // RUN IMMEDIATELY AND ON READY
  bootApplication();
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bootApplication);
  }
  window.addEventListener("load", bootApplication);
})();
</script>
`;
}

function getCompleteSoftwareClientScript() {
  return getClientScriptPartA() + getClientScriptPartB();
}
