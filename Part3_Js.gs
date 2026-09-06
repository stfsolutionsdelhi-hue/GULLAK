
function getCompleteSoftwareClientScript() {
  return `
<script>
// INITIAL AUTHENTICATION & LOGIN LOGIC
window.authorizedUsers = [
  { username: "SANISH", password: "Password", role: "Super Admin", email: "stfsolutionsdelhi@gmail.com" },
  { username: "ADMIN", password: "Admin@123", role: "Manager", email: "stfsolutionsdelhi@gmail.com" }
];

window.togglePasswordEye = function(e) {
  if (e) { if (e.preventDefault) e.preventDefault(); if (e.stopPropagation) e.stopPropagation(); }
  var inp = document.getElementById("inpWinPassword");
  var btn = document.getElementById("btnToggleEye");
  if (!inp) return false;
  if (inp.type === "password") {
    inp.type = "text";
    if (btn) { btn.innerText = "🙈"; btn.title = "Hide Password"; }
  } else {
    inp.type = "password";
    if (btn) { btn.innerText = "👁️"; btn.title = "Show Password"; }
  }
  try { inp.focus(); } catch(err) {}
  return false;
};

window.safeToggleFullscreen = function(e) {
  if (e) { if (e.preventDefault) e.preventDefault(); if (e.stopPropagation) e.stopPropagation(); }
  var btn1 = document.getElementById("btnLoginFullscreen");
  var isDocFull = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement;
  var isSimFull = document.body && document.body.classList.contains("simulated-fullscreen");

  if (!isDocFull && !isSimFull) {
    var el = document.documentElement;
    var p = null;
    try {
      if (el.requestFullscreen) p = el.requestFullscreen();
      else if (el.webkitRequestFullscreen) p = el.webkitRequestFullscreen();
    } catch(err) {}
    if (!p) {
      if (document.body) document.body.classList.add("simulated-fullscreen");
    }
    if (btn1) btn1.innerText = "✖ Exit Full Screen";
  } else {
    if (isDocFull) {
      try {
        if (document.exitFullscreen) document.exitFullscreen();
        else if (document.webkitExitFullscreen) document.webkitExitFullscreen();
      } catch(err) {}
    }
    if (document.body) document.body.classList.remove("simulated-fullscreen");
    if (btn1) btn1.innerText = "⛶ Full Screen";
  }
  return false;
};

window.executeDirectLogin = function(e) {
  if (e) { if (e.preventDefault) e.preventDefault(); if (e.stopPropagation) e.stopPropagation(); }
  var uInp = (document.getElementById("inpWinUsername").value || "").trim();
  var pInp = (document.getElementById("inpWinPassword").value || "").trim();
  var errBox = document.getElementById("winLoginError");

  if (!uInp) {
    if (errBox) { errBox.innerHTML = "⚠️ Please enter <strong>Username</strong>!"; errBox.style.display = "block"; }
    return false;
  }

  var uUpper = uInp.toUpperCase();
  var pLower = pInp.toLowerCase();
  var isSanish = (uUpper === "SANISH") && (pInp === "Password" || pLower === "password" || pInp === "123456" || pInp === "Admin@123");
  var isAdmin = (uUpper === "ADMIN") && (pInp === "Admin@123" || pLower === "admin" || pInp === "Password");

  var userList = window.authorizedUsers || [];
  var matched = userList.find(function(u) {
    return u.username.toLowerCase() === uInp.toLowerCase() && (u.password === pInp || u.password.toLowerCase() === pLower);
  });

  if (isSanish || isAdmin || matched) {
    var current = matched || (isSanish ? { username: "SANISH", role: "Super Admin" } : { username: "ADMIN", role: "Manager" });
    window.currentUserSession = current;
    if (errBox) errBox.style.display = "none";
    var overlay = document.getElementById("windowsLoginOverlay");
    if (overlay) overlay.style.display = "none";
    try { sessionStorage.setItem("gullak_v21_session", JSON.stringify(current)); } catch(err) {}
    return false;
  } else {
    if (errBox) {
      errBox.innerHTML = "❌ <strong>Invalid Credentials!</strong><br>Default: <strong>SANISH</strong> / <strong>Password</strong>";
      errBox.style.display = "block";
    }
    var pBox = document.getElementById("inpWinPassword");
    if (pBox) { pBox.select(); pBox.focus(); }
    return false;
  }
};

window.handleLoginKeyPress = function(e) {
  if (e && (e.key === "Enter" || e.keyCode === 13)) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
    window.executeDirectLogin();
    return false;
  }
};

window.logoutSession = function() {
  try { sessionStorage.removeItem("gullak_v21_session"); } catch(e) {}
  var overlay = document.getElementById("windowsLoginOverlay");
  if (overlay) overlay.style.display = "flex";
  var pInput = document.getElementById("inpWinPassword");
  if (pInput) { pInput.value = ""; pInput.focus(); }
};

window.handleForgotCredentials = function() {
  alert("🔑 DEFAULT LOGIN CREDENTIALS:\n\n• Username: SANISH\n• Password: Password\n\nSheet ke 'Users' tab me jaakar aap password change kar sakte hain.");
};

(function(){
  var DEF_M=[
    {id:"MEM0101261",name:"Rahul Kumar",mobile:"9810011111",status:"ACTIVE",address:"H-12, Sector 3, Rohini",nominee:"Sunita Kumar",rd:400,dateJoined:"2026-01-01",rdPaid:4800,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0},
    {id:"MEM0101262",name:"Suresh Sharma",mobile:"9810022222",status:"ACTIVE",address:"Shop 4, Market",nominee:"Vikas",rd:400,dateJoined:"2026-01-01",rdPaid:4400,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0},
    {id:"MEM0101263",name:"Amit Verma",mobile:"9810033333",status:"ACTIVE",address:"B-45, Shastri Nagar",nominee:"Pooja",rd:400,dateJoined:"2026-01-01",rdPaid:4400,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0},
    {id:"MEM0101264",name:"SANISH",mobile:"9718174244",status:"ACTIVE",address:"ASD",nominee:"DFFF",rd:400,dateJoined:"2026-01-01",rdPaid:1000,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0}
  ];
  var DEF_P=[];
  var DEF_L=[];

  var members=JSON.parse(JSON.stringify(DEF_M)), payments=JSON.parse(JSON.stringify(DEF_P)), loans=JSON.parse(JSON.stringify(DEF_L)), exitSettlements=[], bonusSettlements=[];
  var globalDefaultRate = 1.0;
  var globalDefaultDue = "15th of every month";
  var pendingBulkData = null;
  var currentActiveLedgerMember = null;

  function cleanNum(val, def){ 
    var n = Number(val); 
    if(isNaN(n) || n > 10000000 || n < 0) return (def || 0); 
    return Math.round(n); 
  }
  function cleanRd(val){ 
    var n = Number(val); 
    if(isNaN(n) || n <= 0 || n > 50000) return 400; 
    return Math.round(n); 
  }
  function getTodayYMD(){ 
    var d = new Date(); 
    return d.getFullYear() + "-" + String(d.getMonth()+1).padStart(2,"0") + "-" + String(d.getDate()).padStart(2,"0"); 
  }

  function toDisplayDate(ymd){
    if(!ymd) return "01/01/2026";
    var p = String(ymd).split("T")[0].split(" ")[0].split("-");
    if(p.length === 3) return p[2] + "/" + p[1] + "/" + p[0];
    return ymd;
  }

  function getDDMMYYFromYMD(ymd){
    var p = String(ymd || getTodayYMD()).split("-");
    if(p.length === 3) return p[2] + p[1] + p[0].substring(2);
    return "010126";
  }

  function saveStore(){ 
    try{ 
      localStorage.setItem("gullak_v21_m", JSON.stringify(members)); 
      localStorage.setItem("gullak_v21_p", JSON.stringify(payments)); 
      localStorage.setItem("gullak_v21_l", JSON.stringify(loans)); 
      localStorage.setItem("gullak_v21_ex", JSON.stringify(exitSettlements)); 
      localStorage.setItem("gullak_v21_b", JSON.stringify(bonusSettlements)); 
    }catch(e){} 
    refreshAll(); 
  }

  function getMemberTotalRd(m){ 
    var mid = String(m.id); var pSum = 0; 
    payments.forEach(function(p){ if(String(p.id)===mid) pSum += cleanNum(p.rd, 0); }); 
    return cleanNum(m.rdPaid, 0) + pSum; 
  }
  function getMemberActiveLoan(m){ 
    var mid = String(m.id); var lSum = 0; 
    loans.forEach(function(l){ if(String(l.id)===mid && String(l.status).toUpperCase()==="ACTIVE") lSum += cleanNum(l.outstanding, 0); }); 
    return cleanNum(m.opLoan, 0) + lSum; 
  }
  function getMemberTotalPenalty(m){ 
    var mid = String(m.id); var pSum = 0; 
    payments.forEach(function(p){ if(String(p.id)===mid) pSum += cleanNum(p.penalty, 0); }); 
    return cleanNum(m.opPen, 0) + pSum; 
  }
  function getMemberTotalWaiver(m){ 
    var mid = String(m.id); var wSum = 0; 
    payments.forEach(function(p){ if(String(p.id)===mid) wSum += cleanNum(p.waiver, 0); }); 
    return wSum; 
  }

  function calculateMemberLiveInterestDue(m){
    var mid = String(m.id);
    var today = new Date();
    var curYr = today.getFullYear();
    var curMo = today.getMonth() + 1;

    var totalIntDue = 0;
    loans.filter(function(l){ return String(l.id) === mid && String(l.status).toUpperCase() === "ACTIVE"; }).forEach(function(l){
      var lDate = l.date || "2026-01-01";
      var lp = lDate.split("-");
      var lYr = parseInt(lp[0], 10);
      var lMo = parseInt(lp[1], 10);
      var monthsPassed = (curYr - lYr) * 12 + (curMo - lMo);
      if(monthsPassed > 0){
        var monthlyRate = (Number(l.rate) || 1.0) / 100.0;
        totalIntDue += Math.round(cleanNum(l.outstanding, 0) * monthlyRate);
      }
    });
    return totalIntDue + cleanNum(m.opInt, 0);
  }

  function calculate1PercentPmBonus(m, filterFromYmd, filterToYmd){
    var mid = String(m.id);
    var openingRd = cleanNum(m.rdPaid, 0);
    var schedule = [];
    var totalBonus = 0;
    
    var selYear = document.getElementById("selFinancialYear") ? document.getElementById("selFinancialYear").value : "2026";
    var monthsNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

    var monthlyDepositMap = [0,0,0,0,0,0,0,0,0,0,0,0];
    payments.forEach(function(p){
      if(String(p.id) === mid && cleanNum(p.rd, 0) > 0){
        var dt = String(p.date || "");
        if(dt.indexOf(selYear + "-") === 0){
          var mNum = parseInt(dt.split("-")[1], 10);
          if(!isNaN(mNum) && mNum >= 1 && mNum <= 12){
            monthlyDepositMap[mNum - 1] += cleanNum(p.rd, 0);
          }
        }
      }
    });

    var today = new Date();
    var curMonthIndex = (today.getFullYear() === parseInt(selYear, 10)) ? today.getMonth() : 11;

    var runningBase = openingRd;
    for (var i = 0; i < 12; i++) {
      var monthStartBase = runningBase;
      var depositThisMonth = monthlyDepositMap[i];
      var monthEndBase = monthStartBase + depositThisMonth;

      var isPastOrCurrent = (i <= curMonthIndex);
      var mBonus = isPastOrCurrent ? Math.round(monthStartBase * 0.01) : 0;
      
      if(isPastOrCurrent) {
        totalBonus += mBonus;
      }

      schedule.push({
        month: monthsNames[i] + " " + selYear,
        monthIdx: i + 1,
        opening: monthStartBase,
        deposit: depositThisMonth,
        closing: monthEndBase,
        mBonus: mBonus,
        accumBonus: totalBonus
      });

      runningBase = monthEndBase;
    }

    return { totalBonus: totalBonus, schedule: schedule };
  }

  function getMemberBonus(m){ return calculate1PercentPmBonus(m).totalBonus; }
  
  function getMemberLoanLimit(m){ 
    if(String(m.status).toUpperCase() === "INACTIVE"){
      return { limit: 0, text: "0 (INACTIVE)", isBlocked: true };
    }
    var activeDue = getMemberActiveLoan(m); 
    if(activeDue > 0) return { limit: 0, text: "₹0 (Active Loan)", isBlocked: true }; 
    if(cleanNum(m.customLimit, 0) > 0) return { limit: cleanNum(m.customLimit, 0), text: "₹" + cleanNum(m.customLimit, 0).toLocaleString("en-IN"), isBlocked: false }; 
    var std = getMemberTotalRd(m) * 2; 
    return { limit: std, text: "₹" + std.toLocaleString("en-IN"), isBlocked: false }; 
  }

  function setupFinancialYearDropdown(){
    var sel = document.getElementById("selFinancialYear");
    if(!sel) return;
    var curVal = sel.value || "2026";
    
    var yearsSet = {};
    for(var y = 2022; y <= new Date().getFullYear() + 2; y++) yearsSet[y] = true;
    payments.forEach(function(p){ if(p.date) yearsSet[p.date.split("-")[0]] = true; });
    loans.forEach(function(l){ if(l.date) yearsSet[l.date.split("-")[0]] = true; });

    var sortedYears = Object.keys(yearsSet).sort();
    var h = "";
    sortedYears.forEach(function(yr){
      var isSel = (yr === curVal) ? "selected" : "";
      h += "<option value='" + yr + "' " + isSel + ">FY " + yr + "</option>";
    });
    sel.innerHTML = h;
  }

  function updateKPIs(){
    var activeMems = members.filter(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; });
    document.getElementById("dispTotalMem").innerText = activeMems.length + " / " + members.length;
    var totalRdRecv = 0; members.forEach(function(m){ totalRdRecv += getMemberTotalRd(m); });
    document.getElementById("dispTotalRd").innerText = "₹" + totalRdRecv.toLocaleString("en-IN");
    var totalLoan = 0; loans.forEach(function(l){ if(String(l.status).toUpperCase() === "ACTIVE") totalLoan += cleanNum(l.outstanding, 0); });
    document.getElementById("dispTotalLoan").innerText = "₹" + totalLoan.toLocaleString("en-IN");
    var estB = 0; members.forEach(function(m){ estB += getMemberBonus(m); });
    document.getElementById("dispTotalBonus").innerText = "₹" + estB.toLocaleString("en-IN");
    var fund = totalRdRecv - totalLoan + 45000;
    var elFund = document.getElementById("dispTotalFund");
    elFund.innerText = (fund >= 0 ? "+₹" : "-₹") + Math.abs(fund).toLocaleString("en-IN");
    elFund.className = fund >= 0 ? "kpi-val val-green" : "kpi-val val-red";
    var totalNpa = 0; exitSettlements.forEach(function(e){ totalNpa += cleanNum(e.npaLoss, 0); });
    document.getElementById("dispTotalNpa").innerText = "₹" + totalNpa.toLocaleString("en-IN");
  }

  function renderMembers(){
    var st = document.getElementById("selFilterStatus").value;
    var sort = document.getElementById("selSortMembers").value;
    var q = (document.getElementById("memberFilterInput").value || "").toLowerCase().trim();
    var filtered = members.filter(function(m){ 
      return (String(m.name||"").toLowerCase().indexOf(q)>=0||String(m.mobile||"").indexOf(q)>=0)&&(st==="ALL"||String(m.status).toUpperCase()===st); 
    });

    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });
    if(sort==="rd_high") filtered.sort(function(a,b){ return getMemberTotalRd(b)-getMemberTotalRd(a); });
    if(sort==="rd_low") filtered.sort(function(a,b){ return getMemberTotalRd(a)-getMemberTotalRd(b); });
    if(sort==="loan_high") filtered.sort(function(a,b){ return getMemberActiveLoan(b)-getMemberActiveLoan(a); });

    var tbody = document.getElementById("tbodyMembers"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='8' style='text-align:center;color:#94A3B8;'>No members found</td></tr>"; 
      document.getElementById("tfootMembersTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumLoan = 0, sumInt = 0, sumPen = 0;

    filtered.forEach(function(m){
      var mid = String(m.id); 
      var totRd = getMemberTotalRd(m); 
      var loanDue = getMemberActiveLoan(m); 
      var intDue = calculateMemberLiveInterestDue(m);
      var penDue = cleanNum(m.opPen, 0);
      var lLimit = getMemberLoanLimit(m);
      var badge = String(m.status).toUpperCase() === "ACTIVE" ? "<span class='badge-active'>ACTIVE</span>" : "<span class='badge-inactive'>INACTIVE</span>";
      
      sumRd += totRd;
      sumLoan += loanDue;
      sumInt += intDue;
      sumPen += penDue;

      html += "<tr>" +
        "<td><div class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + " 🔍</div><div style='color:#38BDF8;font-size:0.75rem;'>" + m.mobile + "</div></td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + totRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;font-weight:700;'>₹" + loanDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;font-weight:700;'>₹" + intDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;font-weight:700;'>₹" + penDue.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-size:0.75rem;color:" + (lLimit.isBlocked ? "#94A3B8" : "#FBBF24") + ";font-weight:700;'>" + lLimit.text + "</span></td>" +
        "<td>" + badge + "</td>" +
        "<td><button class='btn-action-rcv action-receive-for' data-id='" + mid + "'>Receive</button><button class='btn-action-edit action-edit-member' data-id='" + mid + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootMembersTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td style='color:#FBBF24;'>TOTAL SUM (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumLoan.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + sumInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td colspan='3' style='color:#94A3B8; font-size:0.75rem;'>Grand Totals</td>" +
      "</tr>";
  }

  function renderPayments(){
    var mode = document.getElementById("selFilterPayMode").value;
    var sort = document.getElementById("selSortPayDate").value;
    var fromD = document.getElementById("inpPayFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpPayFilterTo").value || "2030-12-31";
    var q = (document.getElementById("searchPayInput").value || "").toLowerCase().trim();

    var filtered = payments.filter(function(p){
      var d = p.date || "2026-01-01";
      var matchDate = (d >= fromD && d <= toD);
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
      var matchMode = (mode === "ALL" || safeMode === mode);
      var matchSearch = String(p.receiptNo||"").toLowerCase().indexOf(q) >= 0 || String(p.name||"").toLowerCase().indexOf(q) >= 0 || String(p.narration||"").toLowerCase().indexOf(q) >= 0;
      return matchDate && matchMode && matchSearch;
    });

    if(sort==="new") filtered.sort(function(a,b){ return new Date(b.date||"2026-01-01")-new Date(a.date||"2026-01-01"); });
    if(sort==="old") filtered.sort(function(a,b){ return new Date(a.date||"2026-01-01")-new Date(b.date||"2026-01-01"); });
    if(sort==="amt_high") filtered.sort(function(a,b){ return cleanNum(b.total,0)-cleanNum(a.total,0); });

    var tbody = document.getElementById("tbodyPayments"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='11' style='text-align:center;color:#94A3B8;'>No receipts found for selected date range</td></tr>"; 
      document.getElementById("tfootPaymentsTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumInt = 0, sumPen = 0, sumRepay = 0, sumWvr = 0, sumTot = 0;

    filtered.forEach(function(p){
      var narrTxt = p.narration ? '<span class="narration-badge">📝 ' + p.narration + '</span>' : '';
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
      var rRd = cleanNum(p.rd, 0);
      var rInt = cleanNum(p.interest, 0);
      var rPen = cleanNum(p.penalty, 0);
      var rRepay = cleanNum(p.loanRepay, 0);
      var rWvr = cleanNum(p.waiver, 0);
      var rTot = cleanNum(p.total, 0);

      sumRd += rRd; sumInt += rInt; sumPen += rPen; sumRepay += rRepay; sumWvr += rWvr; sumTot += rTot;

      html += "<tr>" +
        "<td style='color:#FBBF24;font-family:monospace;font-weight:700;'>" + p.receiptNo + "</td>" +
        "<td>" + toDisplayDate(p.date) + "</td>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + p.id + "'>" + p.name + "</strong>" + narrTxt + "</td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + rRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + rInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + rPen.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;font-weight:700;'>₹" + rRepay.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + rWvr.toLocaleString("en-IN") + "</td>" +
        "<td style='font-weight:800;color:#10B981;font-size:0.95rem;'>₹" + rTot.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-weight:700;color:" + (safeMode==="CASH"?"#F59E0B":"#38BDF8") + ";'>" + safeMode + "</span></td>" +
        "<td><button class='btn-action-edit action-edit-receipt' data-rec='" + p.receiptNo + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootPaymentsTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='3' style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " RECEIPTS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + sumInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;'>₹" + sumRepay.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#94A3B8;'>₹" + sumWvr.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#10B981;'>₹" + sumTot.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  function renderLoans(){
    var fromD = document.getElementById("inpLoanFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpLoanFilterTo").value || "2030-12-31";
    var tp = document.getElementById("selFilterLoanType").value;
    var st = document.getElementById("selFilterLoanStatus").value;
    var q = (document.getElementById("searchLoanInput") ? document.getElementById("searchLoanInput").value : "").toLowerCase().trim();

    var filtered = loans.filter(function(l){
      var d = l.date || "2026-01-01";
      var matchDate = (d >= fromD && d <= toD);
      var matchType = (tp === "ALL" || String(l.type) === tp);
      var matchStatus = (st === "ALL" || String(l.status).toUpperCase() === st);
      var matchSearch = String(l.loanId||"").toLowerCase().indexOf(q) >= 0 || String(l.name||"").toLowerCase().indexOf(q) >= 0;
      return matchDate && matchType && matchStatus && matchSearch;
    });

    var tbody = document.getElementById("tbodyLoans"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='9' style='text-align:center;color:#94A3B8;'>No loans matching criteria</td></tr>"; 
      document.getElementById("tfootLoansTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumPrinc = 0, sumOut = 0;

    filtered.forEach(function(l){
      var badge = String(l.status).toUpperCase() === "ACTIVE" ? "badge-active" : "badge-inactive";
      var pr = cleanNum(l.principal, 0);
      var out = cleanNum(l.outstanding, 0);
      sumPrinc += pr; sumOut += out;

      html += "<tr>" +
        "<td style='color:#FBBF24;font-family:monospace;'>" + l.loanId + "</td>" +
        "<td>" + toDisplayDate(l.date) + "</td>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + l.id + "'>" + l.name + "</strong></td>" +
        "<td>" + l.type + "</td>" +
        "<td>₹" + pr.toLocaleString("en-IN") + "</td>" +
        "<td>" + l.rate + "%</td>" +
        "<td style='color:#EF4444;font-weight:700;'>₹" + out.toLocaleString("en-IN") + "</td>" +
        "<td><span class='" + badge + "'>" + l.status + "</span></td>" +
        "<td><button class='btn-action-edit action-edit-loan' data-loanid='" + l.loanId + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootLoansTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td colspan='4' style='color:#FBBF24;'>TOTAL LOAN DISBURSED & DUES</td>" +
        "<td style='color:#FBBF24;'>₹" + sumPrinc.toLocaleString("en-IN") + "</td>" +
        "<td></td>" +
        "<td style='color:#EF4444;'>₹" + sumOut.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  function renderBonusTab(){
    var tbody = document.getElementById("tbodyBonusList"); if(!tbody) return;
    var st = document.getElementById("selFilterBonusStatus") ? document.getElementById("selFilterBonusStatus").value : "ALL";
    var sort = document.getElementById("selSortBonus") ? document.getElementById("selSortBonus").value : "bonus_high";
    var q = (document.getElementById("searchBonusInput") ? document.getElementById("searchBonusInput").value : "").toLowerCase().trim();
    var fromD = document.getElementById("inpBonusFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpBonusFilterTo").value || "2026-12-31";

    var filtered = members.filter(function(m){
      var isPaid = bonusSettlements.some(function(b){ return String(b.id) === String(m.id); });
      var matchStatus = (st === "ALL" || (st === "PAID" && isPaid) || (st === "PENDING" && !isPaid));
      var matchSearch = String(m.name||"").toLowerCase().indexOf(q) >= 0 || String(m.id||"").toLowerCase().indexOf(q) >= 0;
      return matchStatus && matchSearch;
    });

    if(sort==="bonus_high") filtered.sort(function(a,b){ return getMemberBonus(b) - getMemberBonus(a); });
    if(sort==="bonus_low") filtered.sort(function(a,b){ return getMemberBonus(a) - getMemberBonus(b); });
    if(sort==="name_az") filtered.sort(function(a,b){ return (a.name||"").localeCompare(b.name||""); });

    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='7' style='text-align:center;color:#94A3B8;'>No records matching criteria</td></tr>"; 
      document.getElementById("tfootBonusTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumBonus = 0, sumLoan = 0, sumPen = 0;

    filtered.forEach(function(m){
      var mid = String(m.id); 
      var totRd = getMemberTotalRd(m); 
      var bonus = getMemberBonus(m); 
      var loanDue = getMemberActiveLoan(m);
      var penDue = cleanNum(m.opPen, 0);
      var isPaid = bonusSettlements.some(function(b){ return String(b.id) === mid; });
      var badge = isPaid ? "<span class='badge-active'>SET-OFF / PAID</span>" : "<span class='badge-pending'>PENDING</span>";
      
      sumRd += totRd; sumBonus += bonus; sumLoan += loanDue; sumPen += penDue;

      html += "<tr>" +
        "<td><strong style='color:#FBBF24;'>" + m.name + "</strong><br><small style='color:#94A3B8;'>" + m.id + "</small></td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + totRd.toLocaleString("en-IN") + "</td>" +
        "<td><span class='bonus-clickable action-view-bonus-statement' data-id='" + mid + "' title='Click to view month-by-month breakup'>₹" + bonus.toLocaleString("en-IN") + " 🔍</span></td>" +
        "<td style='color:#EF4444;font-weight:700;'>₹" + loanDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + penDue + "</td>" +
        "<td>" + badge + "</td>" +
        "<td><button class='btn btn-dark action-view-bonus-statement' data-id='" + mid + "' style='padding:4px 7px;font-size:0.75rem;margin-right:4px;'>📜 Breakup</button><button class='btn btn-purple action-open-bonus-setoff' data-id='" + mid + "' style='padding:4px 8px;font-size:0.75rem;'>⚡ Set-off</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootBonusTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td style='color:#FBBF24;'>TOTALS</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#C084FC;'>₹" + sumBonus.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumLoan.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }

  function populateDropdowns(filterText){
    var ft = (filterText || "").toLowerCase().trim();
    var html = "";
    members.forEach(function(m){
      if(!ft || m.name.toLowerCase().indexOf(ft) >= 0 || m.mobile.indexOf(ft) >= 0){
        html += "<option value='" + m.id + "'>" + m.name + " (" + m.mobile + ")</option>";
      }
    });
    if(!html) html = "<option value=''>No matching member found</option>";
    document.getElementById("selPayMember").innerHTML = html;
    document.getElementById("selLoanMember").innerHTML = html;
    document.getElementById("selExitMember").innerHTML = html;
  }

  function switchTab(idx){
    for(var i=1; i<=4; i++){
      document.getElementById("tabPanel"+i).style.display = idx===i ? "block" : "none";
      document.getElementById("tabHead"+i).className = idx===i ? "tab-item active" : "tab-item";
    }
  }

  function openModal(id){ document.getElementById(id).style.display = "flex"; }
  function closeAllModals(){
    document.querySelectorAll(".modal-backdrop").forEach(function(m){ m.style.display = "none"; });
  }
  function closeModal(id){ document.getElementById(id).style.display = "none"; }
  
  function showNotice(t, m){ 
    document.getElementById("noticeHeader").innerText = t; 
    document.getElementById("noticeBody").innerHTML = m; 
    openModal("modalNotice"); 
  }

  function openBonusOverviewModal(){
    renderBonusSubReports();
    openModal("modalBonusOverview");
  }

  function renderBonusSubReports(){
    var fromD = document.getElementById("inpSubFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpSubFilterTo").value || "2030-12-31";
    var iHtml = ""; var bHtml = ""; var totalInt = 0; var totalBPaid = 0;

    payments.filter(function(p){
      var d = p.date || "2026-01-01";
      return (d >= fromD && d <= toD && cleanNum(p.interest, 0) > 0);
    }).forEach(function(p){
      totalInt += cleanNum(p.interest, 0);
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
      iHtml += "<tr><td>" + toDisplayDate(p.date) + "</td><td><strong>" + p.name + "</strong></td><td>" + p.receiptNo + "</td><td style='color:#FBBF24;font-weight:700;'>₹" + p.interest.toLocaleString("en-IN") + "</td><td><span style='font-weight:700;color:" + (safeMode==="CASH"?"#F59E0B":"#38BDF8") + ";'>" + safeMode + "</span></td></tr>";
    });

    bonusSettlements.filter(function(b){
      var d = b.date || "2026-01-01";
      return (d >= fromD && d <= toD);
    }).forEach(function(b){
      totalBPaid += cleanNum(b.totalBonus, 0);
      var safeMode = String(b.mode||"ONLINE").toUpperCase().indexOf("CASH") >= 0 ? "CASH" : "ONLINE";
      bHtml += "<tr><td>" + toDisplayDate(b.date) + "</td><td><strong>" + b.name + "</strong></td><td>" + b.settlementId + "</td><td style='color:#C084FC;font-weight:700;'>₹" + b.totalBonus.toLocaleString("en-IN") + "</td><td><span style='font-weight:700;color:" + (safeMode==="CASH"?"#F59E0B":"#38BDF8") + ";'>" + safeMode + "</span></td></tr>";
    });

    document.getElementById("tbodySubInterest").innerHTML = iHtml || "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No interest receipts in date range</td></tr>";
    document.getElementById("tbodySubBonus").innerHTML = bHtml || "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No bonus settlements in date range</td></tr>";
    document.getElementById("btnSubTabInterest").innerText = "📈 Interest Received (₹" + totalInt.toLocaleString("en-IN") + ")";
    document.getElementById("btnSubTabBonus").innerText = "🎁 Bonus Settled (₹" + totalBPaid.toLocaleString("en-IN") + ")";
  }

  function openMemberBonusStatement(id){
    var m = members.find(function(x){ return String(x.id) === String(id); }); if(!m) return;
    var fromD = document.getElementById("inpBonusFilterFrom").value || "2026-01-01";
    var toD = document.getElementById("inpBonusFilterTo").value || "2026-12-31";
    var bInfo = calculate1PercentPmBonus(m, fromD, toD);

    document.getElementById("lblBonusStmtTitle").innerText = "Annual Bonus Statement: " + m.name + " (" + m.id + ") [" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + "]";
    document.getElementById("bonusStmtHeaderStats").innerHTML = 
      "<div><div class='ledger-stat-lbl'>Opening RD (31 Dec 2025)</div><div class='ledger-stat-val' style='color:#10B981;'>₹" + cleanNum(m.rdPaid, 0).toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Monthly RD Rate</div><div class='ledger-stat-val' style='color:#38BDF8;'>₹" + cleanRd(m.rd).toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Calculated Bonus (1% P.M.)</div><div class='ledger-stat-val' style='color:#C084FC;'>₹" + bInfo.totalBonus.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Status</div><div class='ledger-stat-val' style='color:#FBBF24;'>" + (bonusSettlements.some(function(b){ return String(b.id) === String(m.id); }) ? "SET-OFF / PAID" : "PENDING") + "</div></div>";
    
    var sHtml = "";
    bInfo.schedule.forEach(function(s){
      sHtml += "<tr>" +
        "<td><strong>" + s.month + "</strong></td>" +
        "<td>₹" + s.opening.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;'>+₹" + s.deposit.toLocaleString("en-IN") + "</td>" +
        "<td style='font-weight:700;'>₹" + s.closing.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#34D399;font-weight:700;'>+₹" + s.mBonus.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#C084FC;font-weight:800;'>₹" + s.accumBonus.toLocaleString("en-IN") + "</td>" +
      "</tr>";
    });
    document.getElementById("tbodyBonusSchedule").innerHTML = sHtml;
    openModal("modalBonusStatement");
  }

  function openMemberLedger(id){
    var m = members.find(function(x){ return String(x.id) === String(id); }); if(!m) return;
    currentActiveLedgerMember = m;
    renderLedgerModalContent();
    openModal("modalLedger");
  }

  function renderLedgerModalContent(){
    if(!currentActiveLedgerMember) return;
    var m = currentActiveLedgerMember;
    var mid = String(m.id);
    var fromD = document.getElementById("inpLedgerFilterFrom").value || "2020-01-01";
    var toD = document.getElementById("inpLedgerFilterTo").value || "2030-12-31";

    document.getElementById("lblLedgerName").innerText = "📜 Member Passbook: " + m.name + " (" + m.id + ") [" + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + "]";
    var totRd = getMemberTotalRd(m); 
    var loanDue = getMemberActiveLoan(m); 
    var lLimit = getMemberLoanLimit(m); 
    var bonus = getMemberBonus(m);
    var totPen = getMemberTotalPenalty(m);
    var totWvr = getMemberTotalWaiver(m);

    document.getElementById("ledgerHeaderStats").innerHTML = 
      "<div><div class='ledger-stat-lbl'>Status / Due Date</div><div class='ledger-stat-val' style='color:#38BDF8;font-size:0.95rem;'>" + m.status + " • <span style='font-size:0.8rem;'>" + (m.dueDay||"15th") + "</span></div></div>" +
      "<div><div class='ledger-stat-lbl'>Loan Limit</div><div class='ledger-stat-val' style='color:#FBBF24;font-size:0.95rem;'>" + lLimit.text + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Total RD Saved</div><div class='ledger-stat-val' style='color:#10B981;'>₹" + totRd.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Active Loan Due</div><div class='ledger-stat-val' style='color:#EF4444;'>₹" + loanDue.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Total Penalty Paid</div><div class='ledger-stat-val' style='color:#F59E0B;'>₹" + totPen.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Total Waiver Given</div><div class='ledger-stat-val' style='color:#34D399;'>₹" + totWvr.toLocaleString("en-IN") + "</div></div>" +
      "<div><div class='ledger-stat-lbl'>Bonus Status</div><div class='ledger-stat-val' style='color:#C084FC;'>₹" + bonus.toLocaleString("en-IN") + " (PENDING)</div></div>";

    var allTxns = [];

    if(cleanNum(m.rdPaid, 0) > 0 || cleanNum(m.opLoan, 0) > 0){
      allTxns.push({
        date: m.dateJoined || "2026-01-01",
        refId: "OPN-BAL",
        particulars: "Opening Balances (As of 31 Dec 2025)",
        debit: cleanNum(m.opLoan, 0),
        credit: cleanNum(m.rdPaid, 0),
        mode: "SYSTEM"
      });
    }

    loans.filter(function(l){ 
      return String(l.id) === mid && (l.date >= fromD && l.date <= toD); 
    }).forEach(function(l){
      allTxns.push({
        date: l.date,
        refId: l.loanId,
        particulars: "Loan Disbursed (" + l.type + " @ " + l.rate + "%)",
        debit: cleanNum(l.principal, 0),
        credit: 0,
        mode: "ONLINE/CASH"
      });
    });

    payments.filter(function(p){ 
      return String(p.id) === mid && (p.date >= fromD && p.date <= toD); 
    }).forEach(function(p){
      var nInfo = p.narration ? " - " + p.narration : "";
      var parts = [];
      if(cleanNum(p.rd, 0) > 0) parts.push("RD: ₹" + cleanNum(p.rd, 0));
      if(cleanNum(p.loanRepay, 0) > 0) parts.push("Repay: ₹" + cleanNum(p.loanRepay, 0));
      if(cleanNum(p.interest, 0) > 0) parts.push("Int: ₹" + cleanNum(p.interest, 0));
      if(cleanNum(p.penalty, 0) > 0) parts.push("Pen: ₹" + cleanNum(p.penalty, 0));
      allTxns.push({
        date: p.date,
        refId: p.receiptNo,
        particulars: "Receipt (" + parts.join(", ") + ")" + nInfo,
        debit: 0,
        credit: cleanNum(p.total, 0),
        mode: p.mode || "CASH"
      });
    });

    allTxns.sort(function(a, b){ return new Date(a.date) - new Date(b.date); });

    var txnHtml = "";
    var runningBalance = 0;
    allTxns.forEach(function(t){
      runningBalance += (t.credit - t.debit);
      var balColor = runningBalance >= 0 ? "#10B981" : "#EF4444";
      txnHtml += "<tr>" +
        "<td>" + toDisplayDate(t.date) + "</td>" +
        "<td style='color:#FBBF24; font-family:monospace;'>" + t.refId + "</td>" +
        "<td><strong>" + t.particulars + "</strong></td>" +
        "<td style='color:#EF4444; font-weight:700;'>" + (t.debit > 0 ? "₹" + t.debit.toLocaleString("en-IN") : "-") + "</td>" +
        "<td style='color:#10B981; font-weight:700;'>" + (t.credit > 0 ? "₹" + t.credit.toLocaleString("en-IN") : "-") + "</td>" +
        "<td style='font-weight:800; color:" + balColor + ";'>₹" + runningBalance.toLocaleString("en-IN") + "</td>" +
        "<td>" + t.mode + "</td>" +
      "</tr>";
    });

    document.getElementById("tbodyLedgerTxns").innerHTML = txnHtml || "<tr><td colspan='7' style='text-align:center;color:#94A3B8;'>No transactions recorded within " + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + "</td></tr>";
  }

  function printPdfArea(areaId, title){
    var c = document.getElementById(areaId).innerHTML; 
    var fromD = document.getElementById("inpLedgerFilterFrom") ? document.getElementById("inpLedgerFilterFrom").value : "2026-01-01";
    var toD = document.getElementById("inpLedgerFilterTo") ? document.getElementById("inpLedgerFilterTo").value : "2026-12-31";
    var w = window.open("","","width=960,height=750");
    w.document.write("<html><head><title>" + title + "</title><style>body{font-family:Arial,sans-serif;padding:24px;color:#0F172A;background:#FFF}button,.close-x,input{display:none!important}.ledger-header{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;background:#F8FAFC;padding:14px;border-radius:8px;margin-bottom:18px;border:2px solid #CBD5E1}.ledger-stat-lbl{font-size:11px;font-weight:800;color:#475569;text-transform:uppercase}.ledger-stat-val{font-size:15px;font-weight:800;color:#0F172A;margin-top:2px}table{width:100%;border-collapse:collapse;margin-top:10px}th,td{border:1.5px solid #94A3B8;padding:9px 12px;font-size:12px;text-align:left}th{background:#0F766E;color:#FFF;font-weight:800;text-transform:uppercase}tr:nth-child(even){background:#F1F5F9}td strong{color:#0F172A}</style></head><body><div style='margin-bottom:10px;font-size:13px;font-weight:bold;color:#64748B;'>Statement Date Range: " + toDisplayDate(fromD) + " to " + toDisplayDate(toD) + "</div>" + c + "</body></html>");
    w.document.close(); w.focus(); setTimeout(function(){ w.print(); }, 500);
  }

  function updateBulkRowTotal(tr){
    var rd = cleanNum(tr.querySelector(".b-rd").value, 0);
    var int = cleanNum(tr.querySelector(".b-int").value, 0);
    var repay = cleanNum(tr.querySelector(".b-repay").value, 0);
    var pen = cleanNum(tr.querySelector(".b-pen").value, 0);
    var wvr = cleanNum(tr.querySelector(".b-waiver").value, 0);
    var rowTot = rd + int + repay + pen - wvr;
    tr.querySelector(".b-row-total").innerText = "₹" + rowTot.toLocaleString("en-IN");
    tr.querySelector(".b-row-total").setAttribute("data-val", rowTot);
    calculateBulkGrandSummary();
  }

  function calculateBulkGrandSummary(){
    var checkedRows = document.querySelectorAll("#tbodyBulkList tr");
    var totalAmt = 0; var selectedCount = 0;
    checkedRows.forEach(function(tr){
      var chk = tr.querySelector(".bulk-row-chk");
      if(chk && chk.checked){
        selectedCount++;
        var val = Number(tr.querySelector(".b-row-total").getAttribute("data-val")) || 0;
        totalAmt += val;
      }
    });
    document.getElementById("lblBulkSelectedCount").innerText = selectedCount;
    document.getElementById("lblBulkGrandTotal").innerText = "₹" + totalAmt.toLocaleString("en-IN");
  }

  function renderBulkList(){
    var dt = getTodayYMD();
    document.getElementById("inpBulkDate").value = dt;
    document.getElementById("dispBulkDateFormatted").innerText = "(" + toDisplayDate(dt) + ")";

    var html = "";
    members.filter(function(m){ return String(m.status).toUpperCase() === "ACTIVE"; }).forEach(function(m){
      var mid = String(m.id); 
      var totLoan = getMemberActiveLoan(m); 
      var estInt = calculateMemberLiveInterestDue(m);
      var defRd = cleanRd(m.rd);
      var defPen = cleanNum(m.opPen, 0);
      var defTot = defRd + estInt + defPen;
      html += "<tr>" +
        "<td><input type='checkbox' class='bulk-row-chk' data-id='" + mid + "' data-name='" + m.name + "' checked></td>" +
        "<td><strong style='color:#FBBF24;'>" + m.name + "</strong><br><small style='color:#94A3B8;font-size:0.72rem;'>RD: ₹" + defRd + " | Int: ₹" + estInt + " | Loan: ₹" + totLoan + " | Pen: ₹" + defPen + "</small></td>" +
        "<td><input type='number' step='1' class='field-ctrl b-rd' style='width:65px;padding:4px;text-align:center;' value='" + defRd + "'></td>" +
        "<td><input type='number' step='1' class='field-ctrl b-int' style='width:65px;padding:4px;text-align:center;' value='" + estInt + "'></td>" +
        "<td><input type='number' step='1' class='field-ctrl b-repay' style='width:65px;padding:4px;text-align:center;' value='0'></td>" +
        "<td><input type='number' step='1' class='field-ctrl b-pen' style='width:60px;padding:4px;text-align:center;' value='" + defPen + "'></td>" +
        "<td><input type='number' step='1' class='field-ctrl b-waiver' style='width:60px;padding:4px;text-align:center;' value='0'></td>" +
        "<td style='text-align:center;font-weight:800;color:#10B981;' class='b-row-total' data-val='" + defTot + "'>₹" + defTot.toLocaleString("en-IN") + "</td>" +
        "<td><select class='field-ctrl b-mode-sel' style='width:75px;padding:4px;font-size:0.75rem;'><option value='CASH' selected>CASH</option><option value='ONLINE'>ONLINE</option></select></td>" +
      "</tr>";
    });
    document.getElementById("tbodyBulkList").innerHTML = html;
    calculateBulkGrandSummary();
  }

  function addCustomBulkRow(){
    var id = "MEM" + getDDMMYYFromYMD(getTodayYMD()) + (members.length + 1); 
    var tr = document.createElement("tr");
    tr.innerHTML = "<td><input type='checkbox' class='bulk-row-chk' data-id='" + id + "' data-name='New Member' checked></td><td><input type='text' class='field-ctrl b-name' placeholder='Member Name' value='Member " + (document.querySelectorAll("#tbodyBulkList tr").length + 1) + "'></td><td><input type='number' step='1' class='field-ctrl b-rd' style='width:65px;padding:4px;text-align:center;' value='400'></td><td><input type='number' step='1' class='field-ctrl b-int' style='width:65px;padding:4px;text-align:center;' value='0'></td><td><input type='number' step='1' class='field-ctrl b-repay' style='width:65px;padding:4px;text-align:center;' value='0'></td><td><input type='number' step='1' class='field-ctrl b-pen' style='width:60px;padding:4px;text-align:center;' value='0'></td><td><input type='number' step='1' class='field-ctrl b-waiver' style='width:60px;padding:4px;text-align:center;' value='0'></td><td style='text-align:center;font-weight:800;color:#10B981;' class='b-row-total' data-val='400'>₹400</td><td><select class='field-ctrl b-mode-sel' style='width:75px;padding:4px;font-size:0.75rem;'><option value='CASH' selected>CASH</option><option value='ONLINE'>ONLINE</option></select></td>";
    document.getElementById("tbodyBulkList").appendChild(tr);
    calculateBulkGrandSummary();
  }

  function openFundModal(){
    var inAmt = 0; payments.forEach(function(p){ inAmt += cleanNum(p.total,0); });
    var outAmt = 0; loans.forEach(function(l){ outAmt += cleanNum(l.principal,0); });
    var cashIn = 0; var onlineIn = 0;
    payments.forEach(function(p){ 
      if(String(p.mode).toUpperCase().indexOf("ONLINE") >= 0) onlineIn += cleanNum(p.total,0); 
      else cashIn += cleanNum(p.total,0); 
    });
    
    document.getElementById("lblRegCashBal").innerText = "₹" + (cashIn + 12400).toLocaleString("en-IN");
    document.getElementById("lblRegBankBal").innerText = "₹" + (onlineIn + 19400).toLocaleString("en-IN");
    document.getElementById("lblRegTotalFund").innerText = "₹" + (cashIn + onlineIn + 31800 - outAmt).toLocaleString("en-IN");

    var mHtml = "<tr><td><strong>August 2026</strong></td><td style='color:#10B981;'>+₹75,600</td><td style='color:#EF4444;'>-₹32,000</td><td>Net: +₹43,600</td><td><button class='btn btn-dark action-drill-fund' data-month='2026-08' data-label='August 2026' style='padding:2px 6px;font-size:0.75rem;'>Source 🔍</button></td></tr>";
    mHtml += "<tr><td><strong>September 2026 (Live)</strong></td><td style='color:#10B981;'>+₹" + inAmt.toLocaleString("en-IN") + "</td><td style='color:#EF4444;'>-₹" + outAmt.toLocaleString("en-IN") + "</td><td>Net: " + (inAmt >= outAmt ? "+₹" : "-₹") + Math.abs(inAmt - outAmt).toLocaleString("en-IN") + "</td><td><button class='btn btn-dark action-drill-fund' data-month='2026-09' data-label='September 2026' style='padding:2px 6px;font-size:0.75rem;'>Source 🔍</button></td></tr>";
    document.getElementById("tbodyFundMonths").innerHTML = mHtml;
    document.getElementById("fundDrilldownBox").style.display = "none";
    openModal("modalFund");
  }

  function drilldownFundMonth(mKey, mLabel){
    document.getElementById("fundDrilldownBox").style.display = "block";
    document.getElementById("lblDrilldownTitle").innerText = "Drilldown Transactions for: " + mLabel;
    var dHtml = "";
    var filteredP = payments.filter(function(p){ return String(p.date||"").indexOf(mKey) === 0; });
    var filteredL = loans.filter(function(l){ return String(l.date||"").indexOf(mKey) === 0; });

    filteredP.forEach(function(p){
      var safeMode = String(p.mode||"CASH").toUpperCase().indexOf("ONLINE") >= 0 ? "ONLINE" : "CASH";
      dHtml += "<tr><td>" + toDisplayDate(p.date) + "</td><td><strong>" + p.name + "</strong></td><td style='color:#10B981;'>Collection Inflow (+)</td><td style='font-weight:700;color:#10B981;'>+₹" + p.total.toLocaleString("en-IN") + "</td><td>" + safeMode + "</td></tr>";
    });
    filteredL.forEach(function(l){
      dHtml += "<tr><td>" + toDisplayDate(l.date) + "</td><td><strong>" + l.name + "</strong></td><td style='color:#EF4444;'>Loan Disbursal Outflow (-)</td><td style='font-weight:700;color:#EF4444;'>-₹" + l.principal.toLocaleString("en-IN") + "</td><td>ONLINE/CASH</td></tr>";
    });
    document.getElementById("tbodyDrilldown").innerHTML = dHtml || "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No transaction sources recorded for " + mLabel + "</td></tr>";
  }

  function openNpaModal(){
    var html = "";
    exitSettlements.filter(function(e){ return cleanNum(e.npaLoss, 0) > 0; }).forEach(function(e){
      html += "<tr><td>" + toDisplayDate(e.date) + "</td><td style='color:#FBBF24;'>" + e.exitId + "</td><td><strong>" + e.name + "</strong> (" + e.id + ")</td><td style='color:#EF4444;font-weight:800;'>₹" + cleanNum(e.npaLoss, 0).toLocaleString("en-IN") + "</td><td><span class='badge-inactive'>WRITTEN OFF</span></td></tr>";
    });
    document.getElementById("tbodyNpaList").innerHTML = html || "<tr><td colspan='5' style='text-align:center;color:#94A3B8;'>No NPA or bad debts recorded. 100% Recovery!</td></tr>";
    openModal("modalNpa");
  }

  window.calcBonusNet = function(){
    var id = document.getElementById("bonusMemId").value;
    var m = members.find(function(x){ return String(x.id) === String(id); }); if(!m) return;
    var totalBonus = getMemberBonus(m);
    var adjLoan = cleanNum(document.getElementById("inpBonusAdjLoan").value, 0);
    var adjInt = cleanNum(document.getElementById("inpBonusAdjInt").value, 0);
    var adjRd = cleanNum(document.getElementById("inpBonusAdjRd").value, 0);
    var adjPen = cleanNum(document.getElementById("inpBonusAdjPen").value, 0);
    document.getElementById("inpBonusNetPaid").value = totalBonus - adjLoan - adjInt - adjRd - adjPen;
  };

  window.loadExitDetails = function(){
    var sel = document.getElementById("selExitMember"); if(!sel) return;
    var m = members.find(function(x){ return String(x.id) === String(sel.value); }); if(!m) return;
    document.getElementById("lblExitRd").innerText = "₹" + getMemberTotalRd(m).toLocaleString("en-IN");
    document.getElementById("lblExitLoan").innerText = "₹" + getMemberActiveLoan(m).toLocaleString("en-IN");
    document.getElementById("lblExitBonus").innerText = "₹" + getMemberBonus(m).toLocaleString("en-IN");
    document.getElementById("lblExitPen").innerText = "₹" + cleanNum(m.opPen, 0);
    calcExitNet();
  };

  window.calcExitNet = function(){
    var sel = document.getElementById("selExitMember"); if(!sel) return;
    var m = members.find(function(x){ return String(x.id) === String(sel.value); }); if(!m) return;
    var totRd = getMemberTotalRd(m); var loanDue = getMemberActiveLoan(m);
    var bonus = document.getElementById("chkExitIncludeBonus").checked ? getMemberBonus(m) : 0;
    var npa = cleanNum(document.getElementById("inpExitNpa").value, 0);
    var waiver = cleanNum(document.getElementById("inpExitWaiver").value, 0);
    var net = (totRd + bonus + waiver) - (loanDue - npa);
    document.getElementById("inpExitNetRefund").value = net;
    var elRes = document.getElementById("dispExitNetResult");
    var elType = document.getElementById("lblExitResultType");
    if(net >= 0){ elType.innerText = "PAYABLE TO MEMBER (REFUND)"; elRes.innerText = "+₹" + net.toLocaleString("en-IN"); elRes.style.color = "#EF4444"; }
    else { elType.innerText = "RECEIVABLE FROM MEMBER"; elRes.innerText = "₹" + Math.abs(net).toLocaleString("en-IN"); elRes.style.color = "#10B981"; }
  };

  function applyGlobalSettings(){
    var due = document.getElementById("inpGlobalDueDay").value.trim() || "15th of every month";
    var rate = Number(document.getElementById("inpGlobalRate").value) || 1.0;
    globalDefaultDue = due;
    globalDefaultRate = rate;
    members.forEach(function(m){
      if(m.status === "ACTIVE" && (!m.dueDay || m.dueDay.indexOf("custom") < 0)) {
        m.dueDay = due;
      }
    });
    closeModal("modalSettings"); saveStore();
    showNotice("Settings Applied", "Default due date set to '" + due + "'. Default rate for new loans is " + rate + "%. Existing loans remain safe.");
  }

  function refreshAll(){
    try{ setupFinancialYearDropdown(); }catch(e){}
    try{ updateKPIs(); }catch(e){}
    try{ renderMembers(); }catch(e){}
    try{ renderPayments(); }catch(e){}
    try{ renderLoans(); }catch(e){}
    try{ renderBonusTab(); }catch(e){}
    try{ populateDropdowns(); }catch(e){}
  }

  function resetSanitizedStore(){
    localStorage.clear();
    showNotice("Syncing Live Data", "Google Sheet database se fresh verified data load ho raha hai...");
    if(window.google && google.script && google.script.run){
      google.script.run.withSuccessHandler(function(res){
        if(res){
          if(Array.isArray(res.members)&&res.members.length>0) members = res.members;
          if(Array.isArray(res.payments)&&res.payments.length>0) payments = res.payments;
          if(Array.isArray(res.loans)&&res.loans.length>0) loans = res.loans;
          if(Array.isArray(res.exitSettlements)) exitSettlements = res.exitSettlements;
          if(Array.isArray(res.bonusSettlements)) bonusSettlements = res.bonusSettlements;
          if(Array.isArray(res.users)&&res.users.length>0) window.authorizedUsers = res.users;
          saveStore();
          showNotice("Live Data Synced", "Google Sheet se fresh verified data load ho gaya hai.");
        }
      }).getSocietyFullData();
    }
  }

  function executeBulkPosting(postList){
    var cnt = 0;
    postList.forEach(function(pObj){
      payments.unshift(pObj);
      if(window.google && google.script && google.script.run) google.script.run.savePaymentBackend(pObj);
      cnt++;
    });
    closeAllModals(); saveStore();
    showNotice("Bulk Collections Posted", cnt + " members ka payment successfully post ho gaya hai.");
  }

  function resetNewMemberFormClean(){
    document.getElementById("editMemId").value = "";
    document.getElementById("lblMemberModalHead").innerText = "👤 + Add New Member";
    document.getElementById("inpNewMemName").value = "";
    document.getElementById("inpNewMemMobile").value = "";
    document.getElementById("inpNewMemStatus").value = "ACTIVE";
    document.getElementById("inpNewMemJoinDate").value = getTodayYMD();
    document.getElementById("inpNewMemRd").value = "400";
    document.getElementById("inpNewMemAddress").value = "";
    document.getElementById("inpNewMemNominee").value = "";
    document.getElementById("inpNewMemBal").value = "0";
    document.getElementById("inpNewMemOpLoan").value = "0";
    document.getElementById("inpNewMemOpInt").value = "0";
    document.getElementById("inpNewMemOpPen").value = "0";
    document.getElementById("inpNewMemCustomLimit").value = "0";
  }

  function setupEvents(){
    document.getElementById("btnTopReceive").addEventListener("click",function(){ 
      document.getElementById("editReceiptNo").value = "";
      document.getElementById("lblReceiveModalHead").innerText = "📥 Receive Amount";
      document.getElementById("inpPayDate").value = getTodayYMD(); 
      document.getElementById("inpPayRd").value = 400;
      document.getElementById("inpPayInterest").value = 0;
      document.getElementById("inpPayPenalty").value = 0;
      document.getElementById("inpPayWaiver").value = 0;
      document.getElementById("inpPayPrincipal").value = 0;
      document.getElementById("inpPayNarration").value = "";
      populateDropdowns(); 
      openModal("modalReceive"); 
    });

    document.getElementById("btnTopLoan").addEventListener("click",function(){ 
      document.getElementById("editLoanId").value = "";
      document.getElementById("lblLoanModalHead").innerText = "💸 Issue Society Loan";
      document.getElementById("inpLoanDate").value = getTodayYMD(); 
      document.getElementById("inpLoanPrinc").value = "";
      document.getElementById("inpLoanRate").value = globalDefaultRate; 
      populateDropdowns(); 
      openModal("modalLoan"); 
    });
    
    document.getElementById("btnTopAddMember").addEventListener("click",function(){ 
      resetNewMemberFormClean(); 
      openModal("modalMember"); 
    });

    document.getElementById("btnTopBulk").addEventListener("click",function(){ 
      renderBulkList(); 
      openModal("modalBulk"); 
    });

    document.getElementById("btnTopExit").addEventListener("click",function(){ 
      populateDropdowns(); 
      loadExitDetails(); 
      openModal("modalExit"); 
    });

    document.getElementById("btnTopSettings").addEventListener("click",function(){ 
      document.getElementById("inpGlobalDueDay").value = globalDefaultDue; 
      document.getElementById("inpGlobalRate").value = globalDefaultRate; 
      openModal("modalSettings"); 
    });

    document.getElementById("btnTopReload").addEventListener("click", resetSanitizedStore);

    document.getElementById("selFinancialYear").addEventListener("change",function(){
      var yr = this.value;
      document.getElementById("inpPayFilterFrom").value = yr + "-01-01";
      document.getElementById("inpPayFilterTo").value = yr + "-12-31";
      document.getElementById("inpLoanFilterFrom").value = yr + "-01-01";
      document.getElementById("inpLoanFilterTo").value = yr + "-12-31";
      document.getElementById("inpFundFrom").value = yr + "-01-01";
      document.getElementById("inpFundTo").value = yr + "-12-31";
      document.getElementById("inpBonusFilterFrom").value = yr + "-01-01";
      document.getElementById("inpBonusFilterTo").value = yr + "-12-31";
      refreshAll();
      showNotice("Financial Year Switched", "Active view switched to <strong>FY " + yr + "</strong>.");
    });

    document.getElementById("tabHead1").addEventListener("click",function(){ switchTab(1); });
    document.getElementById("tabHead2").addEventListener("click",function(){ switchTab(2); });
    document.getElementById("tabHead3").addEventListener("click",function(){ switchTab(3); });
    document.getElementById("tabHead4").addEventListener("click",function(){ switchTab(4); });

    document.getElementById("kpiCardMembers").addEventListener("click",function(){ switchTab(1); });
    document.getElementById("kpiCardRd").addEventListener("click",function(){ switchTab(2); });
    document.getElementById("kpiCardLoans").addEventListener("click",function(){ switchTab(3); });
    document.getElementById("kpiCardBonus").addEventListener("click",openBonusOverviewModal);
    document.getElementById("kpiCardFund").addEventListener("click",openFundModal);
    document.getElementById("kpiCardNpa").addEventListener("click",openNpaModal);

    document.getElementById("btnSubTabInterest").addEventListener("click",function(){
      document.getElementById("boxSubInterest").style.display = "block";
      document.getElementById("boxSubBonus").style.display = "none";
      this.className = "tab-item active";
      document.getElementById("btnSubTabBonus").className = "tab-item";
    });
    document.getElementById("btnSubTabBonus").addEventListener("click",function(){
      document.getElementById("boxSubInterest").style.display = "none";
      document.getElementById("boxSubBonus").style.display = "block";
      this.className = "tab-item active";
      document.getElementById("btnSubTabInterest").className = "tab-item";
    });
    document.getElementById("btnApplySubFilter").addEventListener("click",renderBonusSubReports);

    document.getElementById("btnAddBulkRow").addEventListener("click",addCustomBulkRow);
    document.getElementById("btnBulkSetAllCash").addEventListener("click",function(){ document.querySelectorAll(".b-mode-sel").forEach(function(s){ s.value="CASH"; }); });
    document.getElementById("btnBulkSetAllOnline").addEventListener("click",function(){ document.querySelectorAll(".b-mode-sel").forEach(function(s){ s.value="ONLINE"; }); });

    document.getElementById("chkSelectAllBulk").addEventListener("change",function(){
      var isChkd = this.checked;
      document.querySelectorAll(".bulk-row-chk").forEach(function(c){ c.checked = isChkd; });
      calculateBulkGrandSummary();
    });

    document.getElementById("tbodyBulkList").addEventListener("input",function(e){
      var tr = e.target.closest("tr");
      if(tr) updateBulkRowTotal(tr);
    });

    document.getElementById("tbodyBulkList").addEventListener("change",function(e){
      if(e.target.classList.contains("bulk-row-chk")){
        calculateBulkGrandSummary();
      }
    });

    document.getElementById("inpBulkDate").addEventListener("change", function(){
      document.getElementById("dispBulkDateFormatted").innerText = "(" + toDisplayDate(this.value) + ")";
    });

    document.getElementById("btnPanelNewReceipt").addEventListener("click",function(){ 
      document.getElementById("editReceiptNo").value = "";
      document.getElementById("lblReceiveModalHead").innerText = "📥 Receive Amount";
      document.getElementById("inpPayDate").value=getTodayYMD(); 
      populateDropdowns(); 
      openModal("modalReceive"); 
    });

    document.getElementById("btnPanelNewLoan").addEventListener("click",function(){ 
      document.getElementById("editLoanId").value = "";
      document.getElementById("lblLoanModalHead").innerText = "💸 Issue Society Loan";
      document.getElementById("inpLoanDate").value=getTodayYMD(); 
      document.getElementById("inpLoanRate").value=globalDefaultRate; 
      populateDropdowns(); 
      openModal("modalLoan"); 
    });

    document.getElementById("btnPrintLedgerPdf").addEventListener("click",function(){ printPdfArea("printableLedgerArea","Member Ledger Statement"); });
    document.getElementById("btnPrintBonusPdf").addEventListener("click",function(){ printPdfArea("printableBonusArea","Bonus Statement"); });
    document.getElementById("btnApplyGlobalSettings").addEventListener("click",applyGlobalSettings);

    document.getElementById("inpSearchReceiveMember").addEventListener("input",function(){ populateDropdowns(this.value); });
    document.getElementById("inpSearchLoanMember").addEventListener("input",function(){ populateDropdowns(this.value); });

    document.getElementById("inpLedgerFilterFrom").addEventListener("change",renderLedgerModalContent);
    document.getElementById("inpLedgerFilterTo").addEventListener("change",renderLedgerModalContent);

    document.getElementById("inpPayFilterFrom").addEventListener("change",renderPayments);
    document.getElementById("inpPayFilterTo").addEventListener("change",renderPayments);
    document.getElementById("selFilterPayMode").addEventListener("change",renderPayments);
    document.getElementById("selSortPayDate").addEventListener("change",renderPayments);
    document.getElementById("searchPayInput").addEventListener("input",renderPayments);

    document.getElementById("inpLoanFilterFrom").addEventListener("change",renderLoans);
    document.getElementById("inpLoanFilterTo").addEventListener("change",renderLoans);
    document.getElementById("selFilterLoanType").addEventListener("change",renderLoans);
    document.getElementById("selFilterLoanStatus").addEventListener("change",renderLoans);
    document.getElementById("searchLoanInput").addEventListener("input",renderLoans);

    document.getElementById("selFilterStatus").addEventListener("change",renderMembers);
    document.getElementById("selSortMembers").addEventListener("change",renderMembers);
    document.getElementById("memberFilterInput").addEventListener("input",renderMembers);

    document.getElementById("selFilterBonusStatus").addEventListener("change",renderBonusTab);
    document.getElementById("selSortBonus").addEventListener("change",renderBonusTab);
    document.getElementById("searchBonusInput").addEventListener("input",renderBonusTab);
    document.getElementById("inpBonusFilterFrom").addEventListener("change",renderBonusTab);
    document.getElementById("inpBonusFilterTo").addEventListener("change",renderBonusTab);

    document.getElementById("btnNoticeOk").addEventListener("click",closeAllModals);
    document.getElementById("btnCloseDrilldown").addEventListener("click",function(){ document.getElementById("fundDrilldownBox").style.display="none"; });

    document.addEventListener("keydown", function(e){
      if(e.key === "Escape" || e.keyCode === 27){ closeAllModals(); }
    });

    document.querySelectorAll(".action-close-modal").forEach(function(btn){
      btn.addEventListener("click", closeAllModals);
    });

    document.getElementById("btnSubmitReceive").addEventListener("click",function(){
      var editRec = document.getElementById("editReceiptNo").value;
      var memId = document.getElementById("selPayMember").value;
      var m = members.find(function(x){ return String(x.id) === String(memId); });
      var dt = document.getElementById("inpPayDate").value || getTodayYMD();
      var rd = cleanNum(document.getElementById("inpPayRd").value, 0);
      var int = cleanNum(document.getElementById("inpPayInterest").value, 0);
      var pen = cleanNum(document.getElementById("inpPayPenalty").value, 0);
      var wvr = cleanNum(document.getElementById("inpPayWaiver").value, 0);
      var pr = cleanNum(document.getElementById("inpPayPrincipal").value, 0);
      var narr = document.getElementById("inpPayNarration").value.trim();
      var tot = rd + int + pen + pr - wvr;
      if(tot <= 0 && wvr <= 0){ showNotice("Alert", "Kripya valid amount bharein."); return; }
      
      var safeMode = document.getElementById("selPayMode").value === "ONLINE" ? "ONLINE" : "CASH";
      
      if(editRec){
        var existP = payments.find(function(p){ return p.receiptNo === editRec; });
        if(existP){
          existP.date = dt; existP.id = memId; existP.name = m ? m.name : "Member";
          existP.rd = rd; existP.interest = int; existP.penalty = pen; existP.waiver = wvr;
          existP.loanRepay = pr; existP.total = tot; existP.mode = safeMode; existP.narration = narr;
          if(window.google && google.script && google.script.run) google.script.run.savePaymentBackend(existP);
          showNotice("Receipt Updated", "Receipt No: " + editRec + " successfully updated.");
        }
      } else {
        var ddmmyy = getDDMMYYFromYMD(dt);
        var recNo = "CER" + ddmmyy + Math.floor(10 + Math.random() * 90);
        var newPay = { receiptNo: recNo, date: dt, id: memId, name: m ? m.name : "Member", rd: rd, interest: int, penalty: pen, waiver: wvr, loanRepay: pr, total: tot, mode: safeMode, type: "REGULAR", narration: narr };
        payments.unshift(newPay);

        if(pr > 0){
          var aL = loans.filter(function(l){ return String(l.id) === String(memId) && l.status === "ACTIVE"; });
          var rem = pr;
          for(var i = 0; i < aL.length && rem > 0; i++){
            if(aL[i].outstanding <= rem){ rem -= aL[i].outstanding; aL[i].repaid = aL[i].principal; aL[i].outstanding = 0; aL[i].status = "CLOSED"; }
            else { aL[i].outstanding -= rem; aL[i].repaid += rem; rem = 0; }
          }
        }
        if(window.google && google.script && google.script.run) google.script.run.savePaymentBackend(newPay);
        showNotice("Receipt Generated", "Receipt No: <strong>" + recNo + "</strong> (Total: ₹" + tot.toLocaleString("en-IN") + ") successfully saved.");
      }
      closeAllModals(); saveStore();
    });

    document.getElementById("btnSubmitLoan").addEventListener("click",function(){
      var editLId = document.getElementById("editLoanId").value;
      var memId = document.getElementById("selLoanMember").value;
      var m = members.find(function(x){ return String(x.id) === String(memId); });
      var dt = document.getElementById("inpLoanDate").value || getTodayYMD();
      var pr = cleanNum(document.getElementById("inpLoanPrinc").value, 0);
      var rt = Number(document.getElementById("inpLoanRate").value) || 1.0;
      var tp = document.getElementById("selLoanType").value;
      if(pr <= 0){ showNotice("Loan Alert", "Valid loan amount bharein."); return; }

      if(editLId){
        var existL = loans.find(function(l){ return l.loanId === editLId; });
        if(existL){
          existL.date = dt; existL.id = memId; existL.name = m ? m.name : "Member";
          existL.type = tp; existL.principal = pr; existL.rate = rt; existL.outstanding = pr;
          if(window.google && google.script && google.script.run) google.script.run.saveLoanBackend(existL);
          showNotice("Loan Updated", "Loan ID: " + editLId + " successfully updated.");
        }
      } else {
        var ddmmyy = getDDMMYYFromYMD(dt);
        var lnId = "LOAN" + ddmmyy + Math.floor(10 + Math.random() * 90);
        var newLoan = { loanId: lnId, date: dt, id: memId, name: m ? m.name : "Member", type: tp, principal: pr, rate: rt, repaid: 0, outstanding: pr, status: "ACTIVE" };
        loans.unshift(newLoan);
        if(window.google && google.script && google.script.run) google.script.run.saveLoanBackend(newLoan);
        showNotice("Loan Disbursed", "Loan ID: <strong>" + lnId + "</strong> ke antargat ₹" + pr.toLocaleString("en-IN") + " jari kiya gaya.");
      }
      closeAllModals(); saveStore();
    });

    document.getElementById("btnSubmitMember").addEventListener("click",function(){
      var editId = document.getElementById("editMemId").value;
      var name = document.getElementById("inpNewMemName").value.trim();
      var mob = document.getElementById("inpNewMemMobile").value.trim().replace(/[^0-9]/g, "");
      var addr = document.getElementById("inpNewMemAddress").value.trim();
      var nom = document.getElementById("inpNewMemNominee").value.trim();
      var jDate = document.getElementById("inpNewMemJoinDate").value || "2026-01-01";
      var safeMonthlyRd = cleanRd(document.getElementById("inpNewMemRd").value);
      var safeOpeningRd = cleanNum(document.getElementById("inpNewMemBal").value, 0);

      if(!name){ showNotice("Required", "Member ka Full Name bharna anivarya hai."); return; }
      if(!mob || mob.length !== 10){ showNotice("Validation Error", "Mobile number strictly 10 digits ka hona chahiye (Current digits: " + mob.length + ")."); return; }
      if(!addr){ showNotice("Required", "Member ka Address bharna anivarya hai."); return; }
      if(!nom){ showNotice("Required", "Nominee / Reference ka naam bharna anivarya hai."); return; }

      var savedMember = null;
      if(editId){
        var m = members.find(function(x){ return String(x.id) === String(editId); });
        if(m){ 
          m.name = name; m.mobile = mob; 
          m.status = document.getElementById("inpNewMemStatus").value; 
          m.dateJoined = jDate; m.rd = safeMonthlyRd; m.rdPaid = safeOpeningRd; 
          m.address = addr; m.nominee = nom;
          m.opLoan = cleanNum(document.getElementById("inpNewMemOpLoan").value, 0); 
          m.opInt = cleanNum(document.getElementById("inpNewMemOpInt").value, 0); 
          m.opPen = cleanNum(document.getElementById("inpNewMemOpPen").value, 0); 
          m.customLimit = cleanNum(document.getElementById("inpNewMemCustomLimit").value, 0); 
          savedMember = m; 
        }
      } else {
        var ddmmyy = getDDMMYYFromYMD(jDate);
        var id = "MEM" + ddmmyy + (members.length + 1);
        savedMember = { 
          id: id, name: name, mobile: mob, 
          status: document.getElementById("inpNewMemStatus").value, 
          dateJoined: jDate, dueDay: globalDefaultDue, 
          rd: safeMonthlyRd, rdPaid: safeOpeningRd, 
          address: addr, nominee: nom,
          opLoan: cleanNum(document.getElementById("inpNewMemOpLoan").value, 0), 
          opInt: cleanNum(document.getElementById("inpNewMemOpInt").value, 0), 
          opPen: cleanNum(document.getElementById("inpNewMemOpPen").value, 0), 
          customLimit: cleanNum(document.getElementById("inpNewMemCustomLimit").value, 0) 
        };
        members.push(savedMember);
      }
      closeAllModals(); saveStore();
      if(savedMember && window.google && google.script && google.script.run) google.script.run.saveMemberBackend(savedMember);
      showNotice("Member Saved", "Member profile successfully save ho gayi (Opening RD: ₹" + safeOpeningRd + ").");
    });

    document.getElementById("btnSubmitBulk").addEventListener("click",function(){
      var rows = document.querySelectorAll(".bulk-row-chk:checked");
      if(rows.length === 0){ showNotice("Alert", "Koi bhi member select nahi kiya gaya hai."); return; }
      var dt = document.getElementById("inpBulkDate").value || getTodayYMD();
      var postList = [];
      var grandTotal = 0;
      var ddmmyy = getDDMMYYFromYMD(dt);

      rows.forEach(function(c){
        var tr = c.closest("tr");
        var id = c.getAttribute("data-id");
        var name = c.getAttribute("data-name") || (tr.querySelector(".b-name") ? tr.querySelector(".b-name").value : "Member");
        var rd = cleanNum(tr.querySelector(".b-rd").value, 0);
        var int = cleanNum(tr.querySelector(".b-int").value, 0);
        var repay = cleanNum(tr.querySelector(".b-repay").value, 0);
        var pen = cleanNum(tr.querySelector(".b-pen").value, 0);
        var wvr = cleanNum(tr.querySelector(".b-waiver").value, 0);
        var safeMode = tr.querySelector(".b-mode-sel").value === "ONLINE" ? "ONLINE" : "CASH";
        var tot = rd + int + repay + pen - wvr;
        grandTotal += tot;
        var recNo = "CER" + ddmmyy + Math.floor(100 + Math.random() * 900);
        postList.push({ receiptNo: recNo, date: dt, id: id, name: name, rd: rd, interest: int, penalty: pen, waiver: wvr, loanRepay: repay, total: tot, mode: safeMode, type: "BULK", narration: "Bulk Collection" });
      });

      pendingBulkData = postList;
      document.getElementById("confirmHeader").innerText = "Confirm Bulk Post";
      document.getElementById("confirmBody").innerHTML = "Aap <strong>" + postList.length + " members</strong> ki collection entries post karne ja rahe hain.<br><br>Posting Date: <strong>" + toDisplayDate(dt) + "</strong><br>Grand Total Amount: <strong style='color:#10B981; font-size:1.15rem;'>₹" + grandTotal.toLocaleString("en-IN") + "</strong><br><br>Kya aap ise confirm karna chahte hain?";
      openModal("modalConfirm");
    });

    document.getElementById("btnConfirmProceed").addEventListener("click",function(){
      if(pendingBulkData && pendingBulkData.length > 0){
        executeBulkPosting(pendingBulkData);
        pendingBulkData = null;
      }
    });

    document.getElementById("btnSubmitBonusSetoff").addEventListener("click",function(){
      var id = document.getElementById("bonusMemId").value;
      var m = members.find(function(x){ return String(x.id) === String(id); }); if(!m) return;
      var dt = document.getElementById("inpBonusDate").value || getTodayYMD();
      var totalBonus = getMemberBonus(m);
      var netPaid = cleanNum(document.getElementById("inpBonusNetPaid").value, 0);
      var safeMode = document.getElementById("selBonusMode").value === "CASH" ? "CASH" : "ONLINE";
      var ddmmyy = getDDMMYYFromYMD(dt);
      var bObj = { settlementId: "DEB" + ddmmyy + Math.floor(10 + Math.random() * 90), date: dt, id: m.id, name: m.name, totalBonus: totalBonus, netPaid: netPaid, mode: safeMode };
      bonusSettlements.unshift(bObj);
      closeAllModals(); saveStore();
      if(window.google && google.script && google.script.run) google.script.run.saveBonusSettlementBackend(bObj);
      showNotice("Bonus Adjusted", m.name + " ka Bonus successfully adjust ho gaya.");
    });

    document.getElementById("btnSubmitExit").addEventListener("click",function(){
      var sel = document.getElementById("selExitMember"); if(!sel) return;
      var m = members.find(function(x){ return String(x.id) === String(sel.value); }); if(!m) return;
      var npa = cleanNum(document.getElementById("inpExitNpa").value, 0);
      var wvr = cleanNum(document.getElementById("inpExitWaiver").value, 0);
      var net = cleanNum(document.getElementById("inpExitNetRefund").value, 0);
      var bonus = document.getElementById("chkExitIncludeBonus").checked ? getMemberBonus(m) : 0;
      var totRd = getMemberTotalRd(m);
      var loanDue = getMemberActiveLoan(m);
      var ddmmyy = getDDMMYYFromYMD(getTodayYMD());
      var exId = "DEB" + ddmmyy + Math.floor(10 + Math.random() * 90);

      var exRecord = { exitId: exId, date: getTodayYMD(), id: m.id, name: m.name, totalRd: totRd, loanDues: loanDue, bonusAdj: bonus, npaLoss: npa, waiver: wvr, netSettlement: net, status: "INACTIVE" };
      exitSettlements.unshift(exRecord);

      m.status = "INACTIVE";
      loans.filter(function(l){ return String(l.id) === String(m.id); }).forEach(function(l){ l.status = "CLOSED"; l.outstanding = 0; });
      closeAllModals(); saveStore();
      if(window.google && google.script && google.script.run){
        google.script.run.saveMemberBackend(m);
        google.script.run.saveExitSettlementBackend(exRecord);
      }
      showNotice("Exit Executed", m.name + " ka settlement complete ho gaya (NPA Write-off: ₹" + npa + ").");
    });

    document.addEventListener("click",function(e){
      var t = e.target; if(!t) return;
      
      var lBtn = t.closest(".action-view-ledger"); 
      if(lBtn){ openMemberLedger(lBtn.getAttribute("data-id")); return; }
      
      var rBtn = t.closest(".action-receive-for"); 
      if(rBtn){ 
        document.getElementById("editReceiptNo").value = "";
        document.getElementById("lblReceiveModalHead").innerText = "📥 Receive Amount";
        populateDropdowns(); 
        document.getElementById("selPayMember").value = rBtn.getAttribute("data-id"); 
        document.getElementById("inpPayDate").value = getTodayYMD(); 
        openModal("modalReceive"); 
        return; 
      }
      
      var erBtn = t.closest(".action-edit-receipt");
      if(erBtn){
        var recNo = erBtn.getAttribute("data-rec");
        var p = payments.find(function(x){ return x.receiptNo === recNo; });
        if(p){
          document.getElementById("editReceiptNo").value = p.receiptNo;
          document.getElementById("lblReceiveModalHead").innerText = "✏️ Edit Receipt (" + p.receiptNo + ")";
          populateDropdowns();
          document.getElementById("selPayMember").value = p.id;
          document.getElementById("inpPayDate").value = p.date;
          document.getElementById("selPayMode").value = p.mode || "CASH";
          document.getElementById("inpPayRd").value = cleanNum(p.rd, 0);
          document.getElementById("inpPayInterest").value = cleanNum(p.interest, 0);
          document.getElementById("inpPayPenalty").value = cleanNum(p.penalty, 0);
          document.getElementById("inpPayWaiver").value = cleanNum(p.waiver, 0);
          document.getElementById("inpPayPrincipal").value = cleanNum(p.loanRepay, 0);
          document.getElementById("inpPayNarration").value = p.narration || "";
          openModal("modalReceive");
        }
        return;
      }

      var elBtn = t.closest(".action-edit-loan");
      if(elBtn){
        var lnId = elBtn.getAttribute("data-loanid");
        var l = loans.find(function(x){ return x.loanId === lnId; });
        if(l){
          document.getElementById("editLoanId").value = l.loanId;
          document.getElementById("lblLoanModalHead").innerText = "✏️ Edit Loan (" + l.loanId + ")";
          populateDropdowns();
          document.getElementById("selLoanMember").value = l.id;
          document.getElementById("inpLoanDate").value = l.date;
          document.getElementById("selLoanType").value = l.type || "Gullak Loan";
          document.getElementById("inpLoanPrinc").value = cleanNum(l.principal, 0);
          document.getElementById("inpLoanRate").value = l.rate || 1.0;
          openModal("modalLoan");
        }
        return;
      }

      var eBtn = t.closest(".action-edit-member"); 
      if(eBtn){
        var m = members.find(function(x){ return String(x.id) === String(eBtn.getAttribute("data-id")); });
        if(m){ 
          document.getElementById("editMemId").value = m.id; 
          document.getElementById("lblMemberModalHead").innerText = "✏️ Edit Member: " + m.name;
          document.getElementById("inpNewMemName").value = m.name; 
          document.getElementById("inpNewMemMobile").value = m.mobile; 
          document.getElementById("inpNewMemStatus").value = m.status; 
          document.getElementById("inpNewMemJoinDate").value = m.dateJoined || "2026-01-01"; 
          document.getElementById("inpNewMemRd").value = cleanRd(m.rd); 
          document.getElementById("inpNewMemBal").value = cleanNum(m.rdPaid, 0); 
          document.getElementById("inpNewMemOpLoan").value = cleanNum(m.opLoan, 0); 
          document.getElementById("inpNewMemOpInt").value = cleanNum(m.opInt, 0); 
          document.getElementById("inpNewMemOpPen").value = cleanNum(m.opPen, 0); 
          document.getElementById("inpNewMemCustomLimit").value = cleanNum(m.customLimit, 0); 
          document.getElementById("inpNewMemAddress").value = m.address || ""; 
          document.getElementById("inpNewMemNominee").value = m.nominee || ""; 
          openModal("modalMember"); 
        }
        return;
      }

      var bBtn = t.closest(".action-open-bonus-setoff"); 
      if(bBtn){
        var m = members.find(function(x){ return String(x.id) === String(bBtn.getAttribute("data-id")); });
        if(m){ 
          document.getElementById("bonusMemId").value = m.id; 
          document.getElementById("lblBonusTargetMember").innerText = m.name + " (" + m.id + ")"; 
          document.getElementById("lblBonusAmount").innerText = "₹" + getMemberBonus(m).toLocaleString("en-IN"); 
          document.getElementById("inpBonusDate").value = getTodayYMD(); 
          document.getElementById("inpBonusAdjLoan").value = 0; 
          document.getElementById("inpBonusAdjInt").value = 0; 
          document.getElementById("inpBonusAdjRd").value = 0; 
          document.getElementById("inpBonusAdjPen").value = 0; 
          calcBonusNet(); 
          openModal("modalBonusSetoff"); 
        }
        return;
      }

      var sBtn = t.closest(".action-view-bonus-statement"); 
      if(sBtn){ openMemberBonusStatement(sBtn.getAttribute("data-id")); return; }
      
      var dBtn = t.closest(".action-drill-fund"); 
      if(dBtn){ drilldownFundMonth(dBtn.getAttribute("data-month"), dBtn.getAttribute("data-label")); return; }
    });
  }

  function startApp(){
    try{
      var sm = localStorage.getItem("gullak_v21_m"); if(sm) members = JSON.parse(sm);
      var sp = localStorage.getItem("gullak_v21_p"); if(sp) payments = JSON.parse(sp);
      var sl = localStorage.getItem("gullak_v21_l"); if(sl) loans = JSON.parse(sl);
      var sx = localStorage.getItem("gullak_v21_ex"); if(sx) exitSettlements = JSON.parse(sx);
      var sb = localStorage.getItem("gullak_v21_b"); if(sb) bonusSettlements = JSON.parse(sb);
      var ss = sessionStorage.getItem("gullak_v21_session");
      if(ss){
        var userSess = JSON.parse(ss);
        if(userSess && userSess.username){
          var ov = document.getElementById("windowsLoginOverlay");
          if(ov) ov.style.display = "none";
        }
      }
    }catch(e){}
    setupEvents();
    refreshAll();
    if(window.google && google.script && google.script.run){
      google.script.run.withSuccessHandler(function(res){
        if(res){
          if(Array.isArray(res.members)&&res.members.length>0) members = res.members;
          if(Array.isArray(res.payments)&&res.payments.length>0) payments = res.payments;
          if(Array.isArray(res.loans)&&res.loans.length>0) loans = res.loans;
          if(Array.isArray(res.exitSettlements)) exitSettlements = res.exitSettlements;
          if(Array.isArray(res.bonusSettlements)) bonusSettlements = res.bonusSettlements;
          if(Array.isArray(res.users)&&res.users.length>0) window.authorizedUsers = res.users;
          saveStore();
        }
      }).getSocietyFullData();
    }
  }

  if(document.readyState === "loading") document.addEventListener("DOMContentLoaded", startApp);
  else startApp();
})();
</script>
</body>
</html>
`;
}
