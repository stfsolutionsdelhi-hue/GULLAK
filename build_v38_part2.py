# Write Part3B.gs and Code.gs
with open("Part3B.gs", "w", encoding="utf-8") as f:
    f.write('''function getPart3BClientScript() {
  return `<script>
// CALCULATION ENGINE & RENDERING FUNCTIONS (V38 PRO MASTER)

function formatCurrency(amt) {
  var val = Math.round(Number(amt)) || 0;
  return "₹" + val.toLocaleString("en-IN");
}

function formatDateDisplay(dStr) {
  if (!dStr) return "01-01-2026";
  var s = String(dStr).split("T")[0].split(" ")[0];
  var parts = s.split("-");
  if (parts.length === 3 && parts[0].length === 4) {
    return parts[2] + "-" + parts[1] + "-" + parts[0];
  }
  return s;
}

function populateMemberDropdown(selectElemId, filterText) {
  var sel = document.getElementById(selectElemId);
  if (!sel) return;
  sel.innerHTML = "";
  var term = String(filterText || "").trim().toLowerCase();
  
  var activeMems = memberList.filter(function(m){
    if (m.status !== "ACTIVE") return false;
    if (!term) return true;
    return (m.name || "").toLowerCase().indexOf(term) >= 0 || (m.mobile || "").indexOf(term) >= 0 || (m.id || "").toLowerCase().indexOf(term) >= 0;
  });

  if (activeMems.length === 0) {
    sel.innerHTML = '<option value="">No Active Member Found</option>';
    return;
  }

  activeMems.forEach(function(m) {
    var opt = document.createElement("option");
    opt.value = m.id;
    opt.text = m.name + " (" + m.mobile + ") - RD: ₹" + m.rd;
    sel.appendChild(opt);
  });
}

function updateMemberLoanLimitDisplay() {
  var sel = document.getElementById("selLoanMember");
  var lbl = document.getElementById("lblMemberLoanLimit");
  if (!sel || !lbl) return;
  var memId = sel.value;
  var m = memberList.find(function(x){ return x.id === memId; });
  if (m) {
    var stats = getMemberStats(m.id);
    var maxLimit = (m.customLimit && m.customLimit > 0) ? m.customLimit : (stats.totalRd * 3);
    lbl.innerText = "Max Loan Limit: " + formatCurrency(maxLimit) + " | Current Outstanding: " + formatCurrency(stats.loanBalance);
  } else {
    lbl.innerText = "";
  }
}

function getMemberStats(memId) {
  var m = memberList.find(function(x){ return x.id === memId; });
  if (!m) return { totalRd: 0, totalLoanIssued: 0, totalLoanRepaid: 0, loanBalance: 0, totalInterest: 0, totalPenalty: 0, totalWaiver: 0, netPenaltyOutstanding: 0 };

  var rdBase = m.rdPaid || 0;
  var loanBase = m.opLoan || 0;
  var intBase = m.opInt || 0;
  var penBase = m.opPen || 0;

  var pList = paymentList.filter(function(p){ return p.id === memId; });
  var lList = loanList.filter(function(l){ return l.id === memId; });

  var payRd = 0, payInt = 0, payPen = 0, payLoanRepay = 0, payWaiver = 0;
  pList.forEach(function(p){
    payRd += (p.rd || 0);
    payInt += (p.interest || 0);
    payPen += (p.penalty || 0);
    payLoanRepay += (p.loanRepay || 0);
    payWaiver += (p.waiver || 0);
  });

  var issuedLoan = 0;
  lList.forEach(function(l){
    issuedLoan += (l.principal || 0);
  });

  var totalRd = rdBase + payRd;
  var totalLoanIssued = loanBase + issuedLoan;
  var totalLoanRepaid = payLoanRepay;
  var loanBalance = Math.max(0, totalLoanIssued - totalLoanRepaid);

  // Accrued Penalty Calculation at ₹10/day
  var accruedPen = penBase;
  if (!skipPenalty && m.dueDay) {
    var dueDayNum = parseInt(m.dueDay) || 15;
    var today = new Date();
    var pStart = new Date(penaltyStartDateStr);
    if (today > pStart) {
      var curDay = today.getDate();
      if (curDay > dueDayNum) {
        var diffDays = curDay - dueDayNum;
        accruedPen += (diffDays * 10);
      }
    }
  }

  var netPenOutstanding = Math.max(0, accruedPen - payPen - payWaiver);

  return {
    totalRd: totalRd,
    totalLoanIssued: totalLoanIssued,
    totalLoanRepaid: totalLoanRepaid,
    loanBalance: loanBalance,
    totalInterest: intBase + payInt,
    totalPenalty: accruedPen,
    totalWaiver: payWaiver,
    netPenaltyOutstanding: netPenOutstanding
  };
}

function calculateAnnualBonusForMember(memId, fromDateStr, toDateStr) {
  var m = memberList.find(function(x){ return x.id === memId; });
  if (!m) return 0;
  var stats = getMemberStats(memId);
  var cumRd = stats.totalRd;
  // Standard 1% per month on cumulative RD saved = ~12% per annum
  var bonus = Math.round(cumRd * 0.12);
  return Math.max(0, bonus);
}

function recalcAndRenderAll() {
  renderKpiCards();
  renderMembersTable();
  renderPaymentsTable();
  renderLoansTable();
  renderBonusTable();
  renderPenaltyTable();
}

function renderKpiCards() {
  var totalMems = memberList.filter(function(m){ return m.status === "ACTIVE"; }).length;
  var grandTotalRd = 0;
  var grandTotalLoanDues = 0;
  var grandTotalBonus = 0;

  memberList.forEach(function(m){
    var st = getMemberStats(m.id);
    if (m.status === "ACTIVE") {
      grandTotalRd += st.totalRd;
      grandTotalLoanDues += st.loanBalance;
      grandTotalBonus += calculateAnnualBonusForMember(m.id);
    }
  });

  // Calculate Net Cash / Bank Fund
  var fundCash = 0, fundBank = 0;
  fundList.forEach(function(f){
    var amt = f.amount || 0;
    if (f.type === "INVEST") {
      if (f.account === "CASH") fundCash += amt; else fundBank += amt;
    } else if (f.type === "BORROW") {
      if (f.account === "CASH") fundCash -= amt; else fundBank -= amt;
    }
  });

  paymentList.forEach(function(p){
    var tot = p.total || 0;
    if (p.mode === "CASH") fundCash += tot; else fundBank += tot;
  });

  loanList.forEach(function(l){
    var pr = l.principal || 0;
    fundCash -= pr; // Default loan disburse from cash
  });

  var totalFund = fundCash + fundBank;

  var dispMem = document.getElementById("dispTotalMem");
  if (dispMem) dispMem.innerText = totalMems + " / " + memberList.length;

  var dispRd = document.getElementById("dispTotalRd");
  if (dispRd) dispRd.innerText = formatCurrency(grandTotalRd);

  var dispLoan = document.getElementById("dispTotalLoan");
  if (dispLoan) dispLoan.innerText = formatCurrency(grandTotalLoanDues);

  var dispBonus = document.getElementById("dispTotalBonus");
  if (dispBonus) dispBonus.innerText = formatCurrency(grandTotalBonus);

  var dispFund = document.getElementById("dispTotalFund");
  if (dispFund) dispFund.innerText = (totalFund >= 0 ? "+" : "") + formatCurrency(totalFund);

  var dispNpa = document.getElementById("dispTotalNpa");
  var totNpa = 0;
  exitList.forEach(function(e){ totNpa += (e.npaLoss || 0); });
  if (dispNpa) dispNpa.innerText = formatCurrency(totNpa);
}

function renderMembersTable() {
  var tbody = document.getElementById("tbodyMembers");
  var tfoot = document.getElementById("tfootMembersTotal");
  if (!tbody) return;
  tbody.innerHTML = "";

  var term = (document.getElementById("memberFilterInput") ? document.getElementById("memberFilterInput").value : "").trim().toLowerCase();
  var statFilter = document.getElementById("selFilterStatus") ? document.getElementById("selFilterStatus").value : "ACTIVE";
  var sortVal = document.getElementById("selSortMembers") ? document.getElementById("selSortMembers").value : "name_az";

  var filtered = memberList.filter(function(m){
    if (statFilter !== "ALL" && m.status !== statFilter) return false;
    if (!term) return true;
    return (m.name||"").toLowerCase().indexOf(term) >= 0 || (m.mobile||"").indexOf(term) >= 0 || (m.id||"").toLowerCase().indexOf(term) >= 0;
  });

  filtered.sort(function(a, b){
    var stA = getMemberStats(a.id);
    var stB = getMemberStats(b.id);
    if (sortVal === "rd_high") return stB.totalRd - stA.totalRd;
    if (sortVal === "rd_low") return stA.totalRd - stB.totalRd;
    if (sortVal === "loan_high") return stB.loanBalance - stA.loanBalance;
    if (sortVal === "pen_high") return stB.netPenaltyOutstanding - stA.netPenaltyOutstanding;
    return (a.name||"").localeCompare(b.name||"");
  });

  var sumRd = 0, sumLoan = 0, sumInt = 0, sumPen = 0;

  filtered.forEach(function(m){
    var st = getMemberStats(m.id);
    sumRd += st.totalRd;
    sumLoan += st.loanBalance;
    sumInt += st.totalInterest;
    sumPen += st.netPenaltyOutstanding;

    var maxLimit = (m.customLimit && m.customLimit > 0) ? m.customLimit : (st.totalRd * 3);

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>
        <span class="member-link" onclick="openMemberLedgerModal('${m.id}')">${m.name}</span>
        <div style="font-size:0.72rem; color:#94A3B8;">📱 ${m.mobile} | ID: ${m.id}</div>
      </td>
      <td style="font-weight:800; color:#10B981;">${formatCurrency(st.totalRd)}</td>
      <td style="font-weight:800; color:#EF4444;">${formatCurrency(st.loanBalance)}</td>
      <td>${formatCurrency(st.totalInterest)}</td>
      <td style="color:#FBBF24;">${formatCurrency(st.netPenaltyOutstanding)}</td>
      <td style="font-size:0.78rem; color:#94A3B8;">${formatCurrency(maxLimit)}</td>
      <td><span class="${m.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">${m.status}</span></td>
      <td>
        <button class="btn-action-rcv" onclick="openReceiveModalFor('${m.id}')">📥 Receive</button>
        <button class="btn-action-edit" onclick="openEditMemberModal('${m.id}')">✏️ Edit</button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td>TOTAL (${filtered.length} Members)</td>
        <td style="color:#10B981;">${formatCurrency(sumRd)}</td>
        <td style="color:#EF4444;">${formatCurrency(sumLoan)}</td>
        <td>${formatCurrency(sumInt)}</td>
        <td style="color:#FBBF24;">${formatCurrency(sumPen)}</td>
        <td colspan="3"></td>
      </tr>
    `;
  }
}

function renderPaymentsTable() {
  var tbody = document.getElementById("tbodyPayments");
  var tfoot = document.getElementById("tfootPaymentsTotal");
  if (!tbody) return;
  tbody.innerHTML = "";

  var term = (document.getElementById("searchPayInput") ? document.getElementById("searchPayInput").value : "").trim().toLowerCase();
  var modeF = document.getElementById("selFilterPayMode") ? document.getElementById("selFilterPayMode").value : "ALL";
  var sortF = document.getElementById("selSortPayDate") ? document.getElementById("selSortPayDate").value : "new";
  var dFrom = document.getElementById("inpPayFilterFrom") ? document.getElementById("inpPayFilterFrom").value : "2026-01-01";
  var dTo = document.getElementById("inpPayFilterTo") ? document.getElementById("inpPayFilterTo").value : "2026-12-31";

  var filtered = paymentList.filter(function(p){
    if (modeF !== "ALL" && p.mode !== modeF) return false;
    if (p.date < dFrom || p.date > dTo) return false;
    if (!term) return true;
    return (p.name||"").toLowerCase().indexOf(term) >= 0 || (p.receiptNo||"").toLowerCase().indexOf(term) >= 0 || (p.id||"").toLowerCase().indexOf(term) >= 0;
  });

  filtered.sort(function(a, b){
    if (sortF === "old") return (a.date||"").localeCompare(b.date||"");
    if (sortF === "amt_high") return (b.total||0) - (a.total||0);
    return (b.date||"").localeCompare(a.date||"");
  });

  var sumRd = 0, sumInt = 0, sumPen = 0, sumLoan = 0, sumWaiver = 0, sumTot = 0;

  filtered.forEach(function(p){
    sumRd += (p.rd||0);
    sumInt += (p.interest||0);
    sumPen += (p.penalty||0);
    sumLoan += (p.loanRepay||0);
    sumWaiver += (p.waiver||0);
    sumTot += (p.total||0);

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td style="font-weight:700; color:#FBBF24;">${p.receiptNo}</td>
      <td>${formatDateDisplay(p.date)}</td>
      <td>
        <span class="member-link" onclick="openMemberLedgerModal('${p.id}')">${p.name}</span>
        ${p.narration ? '<span class="narration-badge">📝 ' + p.narration + '</span>' : ''}
      </td>
      <td>${formatCurrency(p.rd)}</td>
      <td>${formatCurrency(p.interest)}</td>
      <td>${formatCurrency(p.penalty)}</td>
      <td>${formatCurrency(p.loanRepay)}</td>
      <td style="color:#F87171;">${formatCurrency(p.waiver)}</td>
      <td style="font-weight:800; color:#10B981;">${formatCurrency(p.total)}</td>
      <td><span style="font-size:0.75rem; font-weight:700; color:${p.mode === 'ONLINE' ? '#38BDF8' : '#FBBF24'};">${p.mode}</span></td>
      <td><button class="btn-action-edit" onclick="openEditReceiptModal('${p.receiptNo}')">✏️ Edit</button></td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="3">TOTAL (${filtered.length} Receipts)</td>
        <td>${formatCurrency(sumRd)}</td>
        <td>${formatCurrency(sumInt)}</td>
        <td>${formatCurrency(sumPen)}</td>
        <td>${formatCurrency(sumLoan)}</td>
        <td style="color:#F87171;">${formatCurrency(sumWaiver)}</td>
        <td style="color:#10B981;">${formatCurrency(sumTot)}</td>
        <td colspan="2"></td>
      </tr>
    `;
  }
}

function renderLoansTable() {
  var tbody = document.getElementById("tbodyLoans");
  var tfoot = document.getElementById("tfootLoansTotal");
  if (!tbody) return;
  tbody.innerHTML = "";

  var term = (document.getElementById("searchLoanInput") ? document.getElementById("searchLoanInput").value : "").trim().toLowerCase();
  var typeF = document.getElementById("selFilterLoanType") ? document.getElementById("selFilterLoanType").value : "ALL";
  var statF = document.getElementById("selFilterLoanStatus") ? document.getElementById("selFilterLoanStatus").value : "ACTIVE";
  var dFrom = document.getElementById("inpLoanFilterFrom") ? document.getElementById("inpLoanFilterFrom").value : "2026-01-01";
  var dTo = document.getElementById("inpLoanFilterTo") ? document.getElementById("inpLoanFilterTo").value : "2026-12-31";

  var filtered = loanList.filter(function(l){
    if (typeF !== "ALL" && l.type !== typeF) return false;
    if (statF !== "ALL" && l.status !== statF) return false;
    if (l.date < dFrom || l.date > dTo) return false;
    if (!term) return true;
    return (l.name||"").toLowerCase().indexOf(term) >= 0 || (l.loanId||"").toLowerCase().indexOf(term) >= 0 || (l.id||"").toLowerCase().indexOf(term) >= 0;
  });

  var sumPrinc = 0, sumOut = 0;

  filtered.forEach(function(l){
    var st = getMemberStats(l.id);
    sumPrinc += (l.principal||0);
    sumOut += st.loanBalance;

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td style="font-weight:700; color:#EF4444;">${l.loanId}</td>
      <td>${formatDateDisplay(l.date)}</td>
      <td>
        <span class="member-link" onclick="openMemberLedgerModal('${l.id}')">${l.name}</span>
        ${l.narration ? '<span class="narration-badge">📝 ' + l.narration + '</span>' : ''}
      </td>
      <td><span style="font-size:0.75rem; font-weight:700; color:#38BDF8;">${l.type}</span></td>
      <td style="font-weight:700;">${formatCurrency(l.principal)}</td>
      <td>${l.rate}%</td>
      <td style="font-weight:800; color:#EF4444;">${formatCurrency(st.loanBalance)}</td>
      <td><span class="${l.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}">${l.status}</span></td>
      <td><button class="btn-action-edit" onclick="openEditLoanModal('${l.loanId}')">✏️ Edit</button></td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="4">TOTAL (${filtered.length} Loans)</td>
        <td>${formatCurrency(sumPrinc)}</td>
        <td></td>
        <td style="color:#EF4444;">${formatCurrency(sumOut)}</td>
        <td colspan="2"></td>
      </tr>
    `;
  }
}

function renderBonusTable() {
  var tbody = document.getElementById("tbodyBonusList");
  var tfoot = document.getElementById("tfootBonusTotal");
  if (!tbody) return;
  tbody.innerHTML = "";

  var term = (document.getElementById("searchBonusInput") ? document.getElementById("searchBonusInput").value : "").trim().toLowerCase();
  var statF = document.getElementById("selFilterBonusStatus") ? document.getElementById("selFilterBonusStatus").value : "ALL";
  var sortF = document.getElementById("selSortBonus") ? document.getElementById("selSortBonus").value : "bonus_high";

  var filtered = memberList.filter(function(m){
    if (m.status !== "ACTIVE") return false;
    if (!term) return true;
    return (m.name||"").toLowerCase().indexOf(term) >= 0 || (m.mobile||"").indexOf(term) >= 0 || (m.id||"").toLowerCase().indexOf(term) >= 0;
  });

  filtered.sort(function(a, b){
    var bA = calculateAnnualBonusForMember(a.id);
    var bB = calculateAnnualBonusForMember(b.id);
    if (sortF === "bonus_low") return bA - bB;
    if (sortF === "name_az") return (a.name||"").localeCompare(b.name||"");
    return bB - bA;
  });

  var sumRd = 0, sumBonus = 0, sumLoan = 0, sumPen = 0;

  filtered.forEach(function(m){
    var st = getMemberStats(m.id);
    var bonus = calculateAnnualBonusForMember(m.id);

    sumRd += st.totalRd;
    sumBonus += bonus;
    sumLoan += st.loanBalance;
    sumPen += st.netPenaltyOutstanding;

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>
        <span class="member-link" onclick="openMemberLedgerModal('${m.id}')">${m.name}</span>
        <div style="font-size:0.72rem; color:#94A3B8;">📱 ${m.mobile}</div>
      </td>
      <td style="font-weight:700;">${formatCurrency(st.totalRd)}</td>
      <td class="bonus-clickable" onclick="openBonusStatementModal('${m.id}')">${formatCurrency(bonus)}</td>
      <td style="color:#EF4444;">${formatCurrency(st.loanBalance)}</td>
      <td style="color:#FBBF24;">${formatCurrency(st.netPenaltyOutstanding)}</td>
      <td><span class="badge-pending">ACCRUED</span></td>
      <td><button class="btn btn-purple" style="font-size:0.75rem; padding:4px 8px;" onclick="openBonusSetoffModal('${m.id}')">🎁 Set-off</button></td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td>TOTAL (${filtered.length} Members)</td>
        <td>${formatCurrency(sumRd)}</td>
        <td style="color:#C084FC;">${formatCurrency(sumBonus)}</td>
        <td style="color:#EF4444;">${formatCurrency(sumLoan)}</td>
        <td style="color:#FBBF24;">${formatCurrency(sumPen)}</td>
        <td colspan="2"></td>
      </tr>
    `;
  }
}

function renderBonusSetoffRegisterTable() {
  var tbody = document.getElementById("tbodyBonusSetoffRegister");
  var tfoot = document.getElementById("tfootBonusSetoffRegister");
  if (!tbody) return;
  tbody.innerHTML = "";

  var sumBonus = 0, sumLoan = 0, sumInt = 0, sumRd = 0, sumPen = 0, sumNet = 0;

  bonusList.forEach(function(b){
    sumBonus += (b.totalBonus||0);
    sumLoan += (b.adjLoan||0);
    sumInt += (b.adjInterest||0);
    sumRd += (b.adjRd||0);
    sumPen += (b.adjPenalty||0);
    sumNet += (b.netPaid||0);

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td style="font-weight:700; color:#C084FC;">${b.settlementId}</td>
      <td>${formatDateDisplay(b.date)}</td>
      <td>${b.name} (${b.id})</td>
      <td style="font-weight:800; color:#C084FC;">${formatCurrency(b.totalBonus)}</td>
      <td>${formatCurrency(b.adjLoan)}</td>
      <td>${formatCurrency(b.adjInterest)}</td>
      <td>${formatCurrency(b.adjRd)}</td>
      <td>${formatCurrency(b.adjPenalty)}</td>
      <td style="font-weight:800; color:#10B981;">${formatCurrency(b.netPaid)}</td>
      <td><span style="font-size:0.75rem; font-weight:700; color:#38BDF8;">${b.mode}</span></td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="3">TOTAL (${bonusList.length} Set-offs)</td>
        <td style="color:#C084FC;">${formatCurrency(sumBonus)}</td>
        <td>${formatCurrency(sumLoan)}</td>
        <td>${formatCurrency(sumInt)}</td>
        <td>${formatCurrency(sumRd)}</td>
        <td>${formatCurrency(sumPen)}</td>
        <td style="color:#10B981;">${formatCurrency(sumNet)}</td>
        <td></td>
      </tr>
    `;
  }
}

function renderPenaltyTable() {
  var tbody = document.getElementById("tbodyPenaltyList");
  var tfoot = document.getElementById("tfootPenaltyTotal");
  if (!tbody) return;
  tbody.innerHTML = "";

  var term = (document.getElementById("searchPenInput") ? document.getElementById("searchPenInput").value : "").trim().toLowerCase();
  var statF = document.getElementById("selFilterPenStatus") ? document.getElementById("selFilterPenStatus").value : "ALL";

  var filtered = memberList.filter(function(m){
    if (m.status !== "ACTIVE") return false;
    if (!term) return true;
    return (m.name||"").toLowerCase().indexOf(term) >= 0 || (m.mobile||"").indexOf(term) >= 0 || (m.id||"").toLowerCase().indexOf(term) >= 0;
  });

  var sumAccrued = 0, sumPaid = 0, sumWaiver = 0, sumNet = 0;

  filtered.forEach(function(m){
    var st = getMemberStats(m.id);
    sumAccrued += st.totalPenalty;
    sumWaiver += st.totalWaiver;
    sumNet += st.netPenaltyOutstanding;

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>
        <span class="member-link" onclick="openMemberLedgerModal('${m.id}')">${m.name}</span>
        <div style="font-size:0.72rem; color:#94A3B8;">📱 ${m.mobile}</div>
      </td>
      <td>${m.dueDay || '15th of every month'}</td>
      <td><span class="${st.netPenaltyOutstanding > 0 ? 'badge-inactive' : 'badge-active'}">${st.netPenaltyOutstanding > 0 ? 'OVERDUE FINE' : 'CLEAR'}</span></td>
      <td>${formatCurrency(st.totalPenalty)}</td>
      <td>${formatCurrency(0)}</td>
      <td style="color:#F87171;">${formatCurrency(st.totalWaiver)}</td>
      <td style="font-weight:800; color:#FBBF24;">${formatCurrency(st.netPenaltyOutstanding)}</td>
      <td><span class="${st.netPenaltyOutstanding > 0 ? 'badge-pending' : 'badge-active'}">${st.netPenaltyOutstanding > 0 ? 'PENDING' : 'PAID'}</span></td>
      <td><button class="btn-action-rcv" onclick="openReceiveModalFor('${m.id}')">📥 Pay Fine</button></td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="3">TOTAL (${filtered.length} Members)</td>
        <td>${formatCurrency(sumAccrued)}</td>
        <td>${formatCurrency(0)}</td>
        <td style="color:#F87171;">${formatCurrency(sumWaiver)}</td>
        <td style="color:#FBBF24;">${formatCurrency(sumNet)}</td>
        <td colspan="2"></td>
      </tr>
    `;
  }
}

// MODALS OPEN / EDIT / SUBMIT IMPLEMENTATIONS
function openReceiveModalFor(memId) {
  var sel = document.getElementById("selPayMember");
  populateMemberDropdown("selPayMember");
  if (memId && sel) sel.value = memId;
  document.getElementById("editReceiptNo").value = "";
  document.getElementById("lblReceiveModalHead").innerText = "📥 Receive Amount";
  document.getElementById("inpPayDate").value = formatPureDateClient(new Date());
  document.getElementById("dispPayDateFormatted").innerText = formatDateDisplay(new Date());
  document.getElementById("inpPayRd").value = 400;
  document.getElementById("inpPayInterest").value = 0;
  document.getElementById("inpPayPenalty").value = 0;
  document.getElementById("inpPayWaiver").value = 0;
  document.getElementById("inpPayPrincipal").value = 0;
  document.getElementById("inpPayNarration").value = "";
  openModal("modalReceive");
}

function openEditReceiptModal(recNo) {
  var p = paymentList.find(function(x){ return x.receiptNo === recNo; });
  if (!p) return;
  populateMemberDropdown("selPayMember");
  document.getElementById("selPayMember").value = p.id;
  document.getElementById("editReceiptNo").value = p.receiptNo;
  document.getElementById("lblReceiveModalHead").innerText = "✏️ Edit Receipt #" + p.receiptNo;
  document.getElementById("inpPayDate").value = p.date;
  document.getElementById("dispPayDateFormatted").innerText = formatDateDisplay(p.date);
  document.getElementById("selPayMode").value = p.mode;
  document.getElementById("inpPayRd").value = p.rd;
  document.getElementById("inpPayInterest").value = p.interest;
  document.getElementById("inpPayPenalty").value = p.penalty;
  document.getElementById("inpPayWaiver").value = p.waiver;
  document.getElementById("inpPayPrincipal").value = p.loanRepay;
  document.getElementById("inpPayNarration").value = p.narration || "";
  openModal("modalReceive");
}

function submitReceiveForm() {
  var memId = document.getElementById("selPayMember").value;
  if (!memId) { showNoticeModal("Please select a valid member!"); return; }
  var m = memberList.find(function(x){ return x.id === memId; });
  var recNo = document.getElementById("editReceiptNo").value;
  if (!recNo) {
    recNo = "RCP-" + formatPureDateClient(new Date()).replace(/-/g, "").substring(2) + "-" + Math.floor(100 + Math.random() * 900);
  }

  var payObj = {
    receiptNo: recNo,
    date: document.getElementById("inpPayDate").value || formatPureDateClient(new Date()),
    id: memId,
    name: m ? m.name : "Member",
    rd: Math.round(Number(document.getElementById("inpPayRd").value)) || 0,
    interest: Math.round(Number(document.getElementById("inpPayInterest").value)) || 0,
    penalty: Math.round(Number(document.getElementById("inpPayPenalty").value)) || 0,
    loanRepay: Math.round(Number(document.getElementById("inpPayPrincipal").value)) || 0,
    waiver: Math.round(Number(document.getElementById("inpPayWaiver").value)) || 0,
    mode: document.getElementById("selPayMode").value,
    narration: document.getElementById("inpPayNarration").value,
    type: "REGULAR"
  };

  payObj.total = payObj.rd + payObj.interest + payObj.penalty + payObj.loanRepay - payObj.waiver;

  var existIdx = paymentList.findIndex(function(x){ return x.receiptNo === payObj.receiptNo; });
  if (existIdx >= 0) paymentList[existIdx] = payObj;
  else paymentList.push(payObj);

  document.getElementById("modalReceive").style.display = "none";
  recalcAndRenderAll();

  if (typeof google !== "undefined" && google.script && google.script.run) {
    google.script.run.savePaymentBackend(payObj);
  }
  showNoticeModal("Receipt #" + payObj.receiptNo + " saved successfully!");
}

function openLoanModalFor(memId) {
  var sel = document.getElementById("selLoanMember");
  populateMemberDropdown("selLoanMember");
  if (memId && sel) sel.value = memId;
  document.getElementById("editLoanId").value = "";
  document.getElementById("lblLoanModalHead").innerText = "💸 Issue Society Loan";
  document.getElementById("inpLoanDate").value = formatPureDateClient(new Date());
  document.getElementById("dispLoanDateFormatted").innerText = formatDateDisplay(new Date());
  document.getElementById("inpLoanPrinc").value = "";
  document.getElementById("inpLoanRate").value = globalInterestRate;
  document.getElementById("inpLoanNarration").value = "";
  updateMemberLoanLimitDisplay();
  openModal("modalLoan");
}

function openEditLoanModal(loanId) {
  var l = loanList.find(function(x){ return x.loanId === loanId; });
  if (!l) return;
  populateMemberDropdown("selLoanMember");
  document.getElementById("selLoanMember").value = l.id;
  document.getElementById("editLoanId").value = l.loanId;
  document.getElementById("lblLoanModalHead").innerText = "✏️ Edit Loan #" + l.loanId;
  document.getElementById("inpLoanDate").value = l.date;
  document.getElementById("dispLoanDateFormatted").innerText = formatDateDisplay(l.date);
  document.getElementById("selLoanType").value = l.type;
  document.getElementById("inpLoanPrinc").value = l.principal;
  document.getElementById("inpLoanRate").value = l.rate;
  document.getElementById("inpLoanNarration").value = l.narration || "";
  updateMemberLoanLimitDisplay();
  openModal("modalLoan");
}

function submitLoanForm() {
  var memId = document.getElementById("selLoanMember").value;
  if (!memId) { showNoticeModal("Please select a borrower!"); return; }
  var m = memberList.find(function(x){ return x.id === memId; });
  var princ = Math.round(Number(document.getElementById("inpLoanPrinc").value)) || 0;
  if (princ <= 0) { showNoticeModal("Please enter valid principal amount!"); return; }

  var lId = document.getElementById("editLoanId").value;
  if (!lId) {
    lId = "LON-" + formatPureDateClient(new Date()).replace(/-/g, "").substring(2) + "-" + Math.floor(100 + Math.random() * 900);
  }

  var loanObj = {
    loanId: lId,
    date: document.getElementById("inpLoanDate").value || formatPureDateClient(new Date()),
    id: memId,
    name: m ? m.name : "Borrower",
    type: document.getElementById("selLoanType").value,
    principal: princ,
    rate: Number(document.getElementById("inpLoanRate").value) || 1.0,
    repaid: 0,
    outstanding: princ,
    status: "ACTIVE",
    narration: document.getElementById("inpLoanNarration").value
  };

  var existIdx = loanList.findIndex(function(x){ return x.loanId === loanObj.loanId; });
  if (existIdx >= 0) loanList[existIdx] = loanObj;
  else loanList.push(loanObj);

  document.getElementById("modalLoan").style.display = "none";
  recalcAndRenderAll();

  if (typeof google !== "undefined" && google.script && google.script.run) {
    google.script.run.saveLoanBackend(loanObj);
  }
  showNoticeModal("Loan #" + loanObj.loanId + " issued successfully!");
}

function openAddMemberModal() {
  document.getElementById("editMemId").value = "";
  document.getElementById("lblMemberModalHead").innerText = "👤 Add New Member Profile";
  document.getElementById("inpNewMemName").value = "";
  document.getElementById("inpNewMemMobile").value = "";
  document.getElementById("inpNewMemStatus").value = "ACTIVE";
  document.getElementById("inpNewMemJoinDate").value = "2026-01-01";
  document.getElementById("inpNewMemRd").value = 400;
  document.getElementById("inpNewMemDueDay").value = "2026-01-15";
  document.getElementById("dispDueDayFormatted").innerText = "Due Day: 15th of every month";
  document.getElementById("inpNewMemAddress").value = "";
  document.getElementById("inpNewMemNominee").value = "";
  document.getElementById("inpNewMemBal").value = 0;
  document.getElementById("inpNewMemOpLoan").value = 0;
  document.getElementById("inpNewMemOpInt").value = 0;
  document.getElementById("inpNewMemOpPen").value = 0;
  document.getElementById("inpNewMemCustomLimit").value = 0;
  openModal("modalMember");
}

function openEditMemberModal(memId) {
  var m = memberList.find(function(x){ return x.id === memId; });
  if (!m) return;
  document.getElementById("editMemId").value = m.id;
  document.getElementById("lblMemberModalHead").innerText = "✏️ Edit Member Profile (" + m.id + ")";
  document.getElementById("inpNewMemName").value = m.name;
  document.getElementById("inpNewMemMobile").value = m.mobile;
  document.getElementById("inpNewMemStatus").value = m.status;
  document.getElementById("inpNewMemJoinDate").value = m.dateJoined || "2026-01-01";
  document.getElementById("inpNewMemRd").value = m.rd;
  document.getElementById("inpNewMemAddress").value = m.address || "";
  document.getElementById("inpNewMemNominee").value = m.nominee || "";
  document.getElementById("inpNewMemBal").value = m.rdPaid || 0;
  document.getElementById("inpNewMemOpLoan").value = m.opLoan || 0;
  document.getElementById("inpNewMemOpInt").value = m.opInt || 0;
  document.getElementById("inpNewMemOpPen").value = m.opPen || 0;
  document.getElementById("inpNewMemCustomLimit").value = m.customLimit || 0;
  openModal("modalMember");
}

function submitMemberForm() {
  var name = document.getElementById("inpNewMemName").value.trim();
  var mobile = document.getElementById("inpNewMemMobile").value.trim();
  if (!name || mobile.length !== 10) {
    showNoticeModal("Please enter valid Full Name and 10-digit Mobile Number!");
    return;
  }

  var mId = document.getElementById("editMemId").value;
  if (!mId) {
    mId = "MEM01012026" + (memberList.length + 1);
  }

  var dueVal = document.getElementById("inpNewMemDueDay").value;
  var dueDayStr = "15th of every month";
  if (dueVal) {
    var dt = new Date(dueVal);
    if (!isNaN(dt.getTime())) {
      var dNum = dt.getDate();
      dueDayStr = dNum + getOrdinalSuffix(dNum) + " of every month";
    }
  }

  var memObj = {
    id: mId,
    name: name,
    mobile: mobile,
    status: document.getElementById("inpNewMemStatus").value,
    dateJoined: document.getElementById("inpNewMemJoinDate").value || "2026-01-01",
    rd: Math.round(Number(document.getElementById("inpNewMemRd").value)) || 400,
    dueDay: dueDayStr,
    address: document.getElementById("inpNewMemAddress").value,
    nominee: document.getElementById("inpNewMemNominee").value,
    rdPaid: Math.round(Number(document.getElementById("inpNewMemBal").value)) || 0,
    opLoan: Math.round(Number(document.getElementById("inpNewMemOpLoan").value)) || 0,
    opInt: Math.round(Number(document.getElementById("inpNewMemOpInt").value)) || 0,
    opPen: Math.round(Number(document.getElementById("inpNewMemOpPen").value)) || 0,
    customLimit: Math.round(Number(document.getElementById("inpNewMemCustomLimit").value)) || 0
  };

  var existIdx = memberList.findIndex(function(x){ return x.id === memObj.id; });
  if (existIdx >= 0) memberList[existIdx] = memObj;
  else memberList.push(memObj);

  document.getElementById("modalMember").style.display = "none";
  recalcAndRenderAll();

  if (typeof google !== "undefined" && google.script && google.script.run) {
    google.script.run.saveMemberBackend(memObj);
  }
  showNoticeModal("Member profile saved successfully!");
}

function openMemberLedgerModal(memId) {
  var m = memberList.find(function(x){ return x.id === memId; });
  if (!m) return;
  activeMemberId = memId;
  document.getElementById("lblLedgerName").innerText = "👥 Passbook Ledger: " + m.name + " (" + m.id + ")";

  var st = getMemberStats(memId);
  var head = document.getElementById("ledgerHeaderStats");
  if (head) {
    head.innerHTML = `
      <div><div class="ledger-stat-lbl">TOTAL RD DEPOSITED</div><div class="ledger-stat-val" style="color:#10B981;">${formatCurrency(st.totalRd)}</div></div>
      <div><div class="ledger-stat-lbl">TOTAL LOAN ISSUED</div><div class="ledger-stat-val" style="color:#EF4444;">${formatCurrency(st.totalLoanIssued)}</div></div>
      <div><div class="ledger-stat-lbl">LOAN REPAID</div><div class="ledger-stat-val" style="color:#34D399;">${formatCurrency(st.totalLoanRepaid)}</div></div>
      <div><div class="ledger-stat-lbl">OUTSTANDING LOAN</div><div class="ledger-stat-val" style="color:#F87171;">${formatCurrency(st.loanBalance)}</div></div>
    `;
  }

  renderMemberLedgerTxns(memId);
  openModal("modalLedger");
}

function renderMemberLedgerTxns(memId) {
  var tbody = document.getElementById("tbodyLedgerTxns");
  if (!tbody) return;
  tbody.innerHTML = "";

  var m = memberList.find(function(x){ return x.id === memId; });
  var pList = paymentList.filter(function(p){ return p.id === memId; });
  var lList = loanList.filter(function(l){ return l.id === memId; });

  var txns = [];
  if (m && m.rdPaid > 0) {
    txns.push({ date: m.dateJoined || "2026-01-01", ref: "INIT", desc: "Opening RD Balance", rdIn: m.rdPaid, rdBal: m.rdPaid, loanIn: 0, loanOut: 0, loanBal: m.opLoan||0, mode: "SYSTEM" });
  }

  pList.forEach(function(p){
    txns.push({ date: p.date, ref: p.receiptNo, desc: "Payment Receipt " + (p.narration || ""), rdIn: p.rd, rdBal: 0, loanIn: 0, loanOut: p.loanRepay, loanBal: 0, mode: p.mode });
  });

  lList.forEach(function(l){
    txns.push({ date: l.date, ref: l.loanId, desc: "Loan Disbursal (" + l.type + ")", rdIn: 0, rdBal: 0, loanIn: l.principal, loanOut: 0, loanBal: 0, mode: "CASH" });
  });

  txns.sort(function(a,b){ return (a.date||"").localeCompare(b.date||""); });

  var curRd = 0, curLoan = (m ? m.opLoan||0 : 0);

  txns.forEach(function(t){
    curRd += t.rdIn;
    curLoan += (t.loanIn - t.loanOut);

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${formatDateDisplay(t.date)}</td>
      <td style="font-weight:700; color:#FBBF24;">${t.ref}</td>
      <td>${t.desc}</td>
      <td style="color:#10B981;">${t.rdIn > 0 ? formatCurrency(t.rdIn) : '-'}</td>
      <td style="font-weight:700; color:#10B981;">${formatCurrency(curRd)}</td>
      <td style="color:#EF4444;">${t.loanIn > 0 ? formatCurrency(t.loanIn) : '-'}</td>
      <td style="color:#34D399;">${t.loanOut > 0 ? formatCurrency(t.loanOut) : '-'}</td>
      <td style="font-weight:700; color:#EF4444;">${formatCurrency(curLoan)}</td>
      <td><span style="font-size:0.75rem;">${t.mode}</span></td>
    `;
    tbody.appendChild(tr);
  });
}

function openBonusStatementModal(memId) {
  var m = memberList.find(function(x){ return x.id === memId; });
  if (!m) return;
  document.getElementById("lblBonusStmtTitle").innerText = "Annual Bonus Schedule: " + m.name + " (" + m.id + ")";

  var tbody = document.getElementById("tbodyBonusSchedule");
  if (!tbody) return;
  tbody.innerHTML = "";

  var st = getMemberStats(memId);
  var bonus = calculateAnnualBonusForMember(memId);

  var head = document.getElementById("bonusStmtHeaderStats");
  if (head) {
    head.innerHTML = `
      <div><div class="ledger-stat-lbl">TOTAL CUMULATIVE RD</div><div class="ledger-stat-val" style="color:#38BDF8;">${formatCurrency(st.totalRd)}</div></div>
      <div><div class="ledger-stat-lbl">ANNUAL BONUS ACCRUED</div><div class="ledger-stat-val" style="color:#C084FC;">${formatCurrency(bonus)}</div></div>
    `;
  }

  var months = ["Jan 2026", "Feb 2026", "Mar 2026", "Apr 2026", "May 2026", "Jun 2026", "Jul 2026", "Aug 2026", "Sep 2026", "Oct 2026", "Nov 2026", "Dec 2026"];
  var runningRd = m.rdPaid || 0;
  var monthlyAdd = m.rd || 400;

  months.forEach(function(mn){
    var startBase = runningRd;
    runningRd += monthlyAdd;
    var bMon = Math.round(runningRd * 0.01);

    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${mn}</td>
      <td>${formatCurrency(startBase)}</td>
      <td style="color:#10B981;">+${formatCurrency(monthlyAdd)}</td>
      <td style="font-weight:700;">${formatCurrency(runningRd)}</td>
      <td style="color:#C084FC;">${formatCurrency(bMon)}</td>
      <td style="font-weight:700; color:#C084FC;">${formatCurrency(Math.round(runningRd * 0.12))}</td>
    `;
    tbody.appendChild(tr);
  });

  openModal("modalBonusStatement");
}

function openBonusSetoffModal(memId) {
  var m = memberList.find(function(x){ return x.id === memId; });
  if (!m) return;
  activeBonusMemberId = memId;
  document.getElementById("bonusMemId").value = memId;
  document.getElementById("lblBonusTargetMember").innerText = m.name + " (" + m.id + ")";
  var bonus = calculateAnnualBonusForMember(memId);
  document.getElementById("lblBonusAmount").innerText = formatCurrency(bonus);
  document.getElementById("inpBonusDate").value = formatPureDateClient(new Date());

  var st = getMemberStats(memId);
  document.getElementById("inpBonusAdjLoan").value = Math.min(bonus, st.loanBalance);
  document.getElementById("inpBonusAdjInt").value = 0;
  document.getElementById("inpBonusAdjRd").value = 0;
  document.getElementById("inpBonusAdjPen").value = 0;
  calcBonusNet();
  openModal("modalBonusSetoff");
}

function calcBonusNet() {
  var memId = document.getElementById("bonusMemId").value;
  var bonus = calculateAnnualBonusForMember(memId);
  var aLoan = Math.round(Number(document.getElementById("inpBonusAdjLoan").value)) || 0;
  var aInt = Math.round(Number(document.getElementById("inpBonusAdjInt").value)) || 0;
  var aRd = Math.round(Number(document.getElementById("inpBonusAdjRd").value)) || 0;
  var aPen = Math.round(Number(document.getElementById("inpBonusAdjPen").value)) || 0;

  var netPaid = Math.max(0, bonus - (aLoan + aInt + aRd + aPen));
  document.getElementById("inpBonusNetPaid").value = netPaid;
}

function submitBonusSetoff() {
  var memId = document.getElementById("bonusMemId").value;
  var m = memberList.find(function(x){ return x.id === memId; });
  var bonus = calculateAnnualBonusForMember(memId);

  var sId = "SET-" + formatPureDateClient(new Date()).replace(/-/g, "").substring(2) + "-" + Math.floor(100 + Math.random() * 900);

  var bObj = {
    settlementId: sId,
    date: document.getElementById("inpBonusDate").value || formatPureDateClient(new Date()),
    id: memId,
    name: m ? m.name : "Member",
    totalBonus: bonus,
    adjLoan: Math.round(Number(document.getElementById("inpBonusAdjLoan").value)) || 0,
    adjInterest: Math.round(Number(document.getElementById("inpBonusAdjInt").value)) || 0,
    adjRd: Math.round(Number(document.getElementById("inpBonusAdjRd").value)) || 0,
    adjPenalty: Math.round(Number(document.getElementById("inpBonusAdjPen").value)) || 0,
    netPaid: Math.round(Number(document.getElementById("inpBonusNetPaid").value)) || 0,
    mode: document.getElementById("selBonusMode").value
  };

  bonusList.push(bObj);
  document.getElementById("modalBonusSetoff").style.display = "none";
  recalcAndRenderAll();

  if (typeof google !== "undefined" && google.script && google.script.run) {
    google.script.run.saveBonusSettlementBackend(bObj);
  }
  showNoticeModal("Bonus Set-off posted successfully!");
}

function openBulkModal() {
  var tbody = document.getElementById("tbodyBulkList");
  if (!tbody) return;
  tbody.innerHTML = "";
  document.getElementById("inpBulkDate").value = formatPureDateClient(new Date());

  var activeMems = memberList.filter(function(m){ return m.status === "ACTIVE"; });

  activeMems.forEach(function(m, idx){
    var st = getMemberStats(m.id);
    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td><input type="checkbox" class="chk-bulk-row" data-id="${m.id}" checked onchange="calcBulkGrandTotal()"></td>
      <td>
        <strong style="color:#FBBF24;">${m.name}</strong>
        <div style="font-size:0.7rem; color:#94A3B8;">Loan Due: ${formatCurrency(st.loanBalance)}</div>
      </td>
      <td><input type="number" class="field-ctrl inp-bulk-rd" value="${m.rd}" style="width:70px; text-align:center;" oninput="calcBulkGrandTotal()"></td>
      <td><input type="number" class="field-ctrl inp-bulk-int" value="0" style="width:65px; text-align:center;" oninput="calcBulkGrandTotal()"></td>
      <td><input type="number" class="field-ctrl inp-bulk-repay" value="0" style="width:75px; text-align:center;" oninput="calcBulkGrandTotal()"></td>
      <td><input type="number" class="field-ctrl inp-bulk-pen" value="0" style="width:65px; text-align:center;" oninput="calcBulkGrandTotal()"></td>
      <td><input type="number" class="field-ctrl inp-bulk-waiver" value="0" style="width:65px; text-align:center;" oninput="calcBulkGrandTotal()"></td>
      <td style="font-weight:800; color:#10B981; text-align:center;" class="lbl-bulk-row-total">${formatCurrency(m.rd)}</td>
      <td><select class="field-ctrl sel-bulk-mode" style="width:75px;"><option value="CASH">CASH</option><option value="ONLINE">ONLINE</option></select></td>
      <td><input type="text" class="field-ctrl inp-bulk-narr" placeholder="Remarks" style="width:100px;"></td>
    `;
    tbody.appendChild(tr);
  });

  calcBulkGrandTotal();
  openModal("modalBulk");
}

function calcBulkGrandTotal() {
  var rows = document.querySelectorAll("#tbodyBulkList tr");
  var grand = 0;
  var selCount = 0;

  rows.forEach(function(r){
    var chk = r.querySelector(".chk-bulk-row");
    if (chk && chk.checked) {
      selCount++;
      var rd = Math.round(Number(r.querySelector(".inp-bulk-rd").value)) || 0;
      var intVal = Math.round(Number(r.querySelector(".inp-bulk-int").value)) || 0;
      var repay = Math.round(Number(r.querySelector(".inp-bulk-repay").value)) || 0;
      var pen = Math.round(Number(r.querySelector(".inp-bulk-pen").value)) || 0;
      var waiver = Math.round(Number(r.querySelector(".inp-bulk-waiver").value)) || 0;

      var rowTot = rd + intVal + repay + pen - waiver;
      r.querySelector(".lbl-bulk-row-total").innerText = formatCurrency(rowTot);
      grand += rowTot;
    }
  });

  document.getElementById("lblBulkSelectedCount").innerText = selCount;
  document.getElementById("lblBulkGrandTotal").innerText = formatCurrency(grand);
}

function submitBulkEntries() {
  var rows = document.querySelectorAll("#tbodyBulkList tr");
  var postDate = document.getElementById("inpBulkDate").value || formatPureDateClient(new Date());
  var count = 0;

  rows.forEach(function(r){
    var chk = r.querySelector(".chk-bulk-row");
    if (chk && chk.checked) {
      var memId = chk.getAttribute("data-id");
      var m = memberList.find(function(x){ return x.id === memId; });
      var recNo = "RCP-" + formatPureDateClient(new Date()).replace(/-/g, "").substring(2) + "-" + Math.floor(100 + Math.random() * 900);

      var rd = Math.round(Number(r.querySelector(".inp-bulk-rd").value)) || 0;
      var intVal = Math.round(Number(r.querySelector(".inp-bulk-int").value)) || 0;
      var repay = Math.round(Number(r.querySelector(".inp-bulk-repay").value)) || 0;
      var pen = Math.round(Number(r.querySelector(".inp-bulk-pen").value)) || 0;
      var waiver = Math.round(Number(r.querySelector(".inp-bulk-waiver").value)) || 0;
      var mode = r.querySelector(".sel-bulk-mode").value;
      var narr = r.querySelector(".inp-bulk-narr").value;

      var tot = rd + intVal + repay + pen - waiver;

      if (tot > 0) {
        var pObj = {
          receiptNo: recNo,
          date: postDate,
          id: memId,
          name: m ? m.name : "Member",
          rd: rd,
          interest: intVal,
          penalty: pen,
          loanRepay: repay,
          waiver: waiver,
          total: tot,
          mode: mode,
          narration: narr || "Bulk Monthly Collection",
          type: "BULK"
        };
        paymentList.push(pObj);
        count++;

        if (typeof google !== "undefined" && google.script && google.script.run) {
          google.script.run.savePaymentBackend(pObj);
        }
      }
    }
  });

  document.getElementById("modalBulk").style.display = "none";
  recalcAndRenderAll();
  showNoticeModal("Successfully posted " + count + " bulk receipt entries!");
}

function openFundModal() {
  openModal("modalFund");
}

function openNpaModal() {
  renderNpaModal();
  openModal("modalNpa");
}

function renderNpaModal() {
  var tbody = document.getElementById("tbodyNpaList");
  var tfoot = document.getElementById("tfootNpaTotal");
  if (!tbody) return;
  tbody.innerHTML = "";

  var sumNpa = 0;
  exitList.forEach(function(e){
    if (e.npaLoss > 0) {
      sumNpa += e.npaLoss;
      var tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${formatDateDisplay(e.date)}</td>
        <td style="font-weight:700; color:#EF4444;">${e.exitId}</td>
        <td>${e.name} (${e.id})</td>
        <td style="font-weight:800; color:#EF4444;">${formatCurrency(e.npaLoss)}</td>
        <td><span class="badge-inactive">WRITTEN OFF</span></td>
      `;
      tbody.appendChild(tr);
    }
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="3">TOTAL NPA LOSS</td>
        <td style="color:#EF4444;">${formatCurrency(sumNpa)}</td>
        <td></td>
      </tr>
    `;
  }
}

function renderFundAuditRegisterTable() {
  var tbody = document.getElementById("tbodyFundRegisterList");
  var tfoot = document.getElementById("tfootFundRegisterList");
  if (!tbody) return;
  tbody.innerHTML = "";

  var sumAmt = 0;
  fundList.forEach(function(f){
    sumAmt += (f.amount||0);
    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${formatDateDisplay(f.date)}</td>
      <td style="font-weight:700; color:#38BDF8;">${f.id}</td>
      <td><span class="${f.type === 'INVEST' ? 'badge-active' : 'badge-inactive'}">${f.type}</span></td>
      <td>${f.account}</td>
      <td>${f.entity}</td>
      <td style="font-weight:800; color:${f.type === 'INVEST' ? '#10B981' : '#EF4444'};">${formatCurrency(f.amount)}</td>
      <td>${f.narration}</td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="5">TOTAL FUND TRANSACTIONS</td>
        <td style="color:#38BDF8;">${formatCurrency(sumAmt)}</td>
        <td></td>
      </tr>
    `;
  }
}

function submitFundEntry() {
  var amt = Math.round(Number(document.getElementById("inpFundEntryAmount").value)) || 0;
  var entity = document.getElementById("inpFundEntryEntity").value.trim();
  if (amt <= 0 || !entity) {
    showNoticeModal("Please enter valid Amount and Investor/Entity!");
    return;
  }

  var fObj = {
    id: "FND-" + formatPureDateClient(new Date()).replace(/-/g, "").substring(2) + "-" + Math.floor(100 + Math.random() * 900),
    date: document.getElementById("inpFundEntryDate").value || formatPureDateClient(new Date()),
    type: document.getElementById("inpFundEntryType").value,
    account: document.getElementById("inpFundEntryAccount").value,
    entity: entity,
    amount: amt,
    narration: document.getElementById("inpFundEntryNarration").value
  };

  fundList.push(fObj);
  recalcAndRenderAll();

  if (typeof google !== "undefined" && google.script && google.script.run) {
    google.script.run.saveFundTransactionBackend(fObj);
  }
  showNoticeModal("Fund transaction posted successfully!");
  switchSettingsSubTab(2);
}

function renderProfitLossRegister() {
  var tbody = document.getElementById("tbodyPlBreakup");
  var tfoot = document.getElementById("tfootPlBreakup");
  if (!tbody) return;
  tbody.innerHTML = "";

  var totInt = 0, totPen = 0, totWaiver = 0, totBonusPaid = 0, totBonusPayable = 0;

  paymentList.forEach(function(p){
    totInt += (p.interest||0);
    totPen += (p.penalty||0);
    totWaiver += (p.waiver||0);
  });

  bonusList.forEach(function(b){
    totBonusPaid += (b.totalBonus||0);
  });

  memberList.forEach(function(m){
    if (m.status === "ACTIVE") {
      totBonusPayable += calculateAnnualBonusForMember(m.id);
    }
  });

  var netProfit = (totInt + totPen) - (totWaiver + totBonusPaid);

  document.getElementById("lblPlIntEarned").innerText = formatCurrency(totInt);
  document.getElementById("lblPlPenReceived").innerText = formatCurrency(totPen);
  document.getElementById("lblPlWaiver").innerText = formatCurrency(totWaiver);
  document.getElementById("lblPlBonusPaid").innerText = formatCurrency(totBonusPaid);
  document.getElementById("lblPlBonusPayable").innerText = formatCurrency(totBonusPayable);

  var elNet = document.getElementById("lblPlNetProfit");
  if (elNet) {
    elNet.innerText = (netProfit >= 0 ? "+" : "") + formatCurrency(netProfit);
    elNet.style.color = (netProfit >= 0) ? "#10B981" : "#EF4444";
  }

  var rows = [
    { date: "2026-12-31", cat: "Interest Earned", source: "Loan Borrowers", inc: totInt, exp: 0, rem: "Total Interest Income Received" },
    { date: "2026-12-31", cat: "Penalty Received", source: "Overdue Members", inc: totPen, exp: 0, rem: "Late Fee Charges Collected" },
    { date: "2026-12-31", cat: "Waiver Given", source: "Concessions", inc: 0, exp: totWaiver, rem: "Interest / Penalty Waived" },
    { date: "2026-12-31", cat: "Annual Bonus Paid", source: "RD Savers", inc: 0, exp: totBonusPaid, rem: "Bonus Distributed / Set-off" }
  ];

  rows.forEach(function(r){
    var tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${formatDateDisplay(r.date)}</td>
      <td style="font-weight:700;">${r.cat}</td>
      <td>${r.source}</td>
      <td style="color:#10B981;">${r.inc > 0 ? formatCurrency(r.inc) : '-'}</td>
      <td style="color:#EF4444;">${r.exp > 0 ? formatCurrency(r.exp) : '-'}</td>
      <td>${r.rem}</td>
    `;
    tbody.appendChild(tr);
  });

  if (tfoot) {
    tfoot.innerHTML = `
      <tr class="tfoot-total-row">
        <td colspan="3">NET SOCIETY PROFIT / (LOSS)</td>
        <td style="color:#10B981;">${formatCurrency(totInt + totPen)}</td>
        <td style="color:#EF4444;">${formatCurrency(totWaiver + totBonusPaid)}</td>
        <td style="color:${netProfit >= 0 ? '#10B981' : '#EF4444'}; font-weight:800;">${formatCurrency(netProfit)}</td>
      </tr>
    `;
  }
}

function applyGlobalSettings() {
  globalDueDayStr = document.getElementById("inpGlobalDueDay").value;
  globalInterestRate = Number(document.getElementById("inpGlobalRate").value) || 1.0;
  penaltyStartDateStr = document.getElementById("inpPenaltyStartDate").value;
  skipPenalty = document.getElementById("chkSkipPenalty").checked;

  document.getElementById("modalSettings").style.display = "none";
  recalcAndRenderAll();
  showNoticeModal("Global settings applied successfully!");
}

function formatPureDateClient(d) {
  if (!d) return "2026-01-01";
  if (d instanceof Date) {
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, "0");
    var day = String(d.getDate()).padStart(2, "0");
    return y + "-" + m + "-" + day;
  }
  return String(d).split("T")[0];
}

function printLedgerModal() {
  window.print();
}

function printBonusModal() {
  window.print();
}
</script>`;
}
''')

print("Part3B.gs written")

with open("Code.gs", "w", encoding="utf-8") as f:
    f.write('''/**
 * Master Assembly Code.gs
 */
function getCompleteSoftwareClientScript() {
  return getPart3AClientScript() + "\\n" + getPart3BClientScript();
}
''')

print("Code.gs written")
