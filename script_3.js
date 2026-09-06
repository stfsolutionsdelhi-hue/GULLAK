
// INITIAL AUTHENTICATION & LOGIN LOGIC
window.authorizedUsers = [
  { username: "SANISH", password: "12345", role: "Super Admin", email: "stfsolutionsdelhi@gmail.com" },
  { username: "ADMIN", password: "12345", role: "Manager", email: "stfsolutionsdelhi@gmail.com" }
];

window.togglePasswordEye = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  var inp = document.getElementById("inpWinPassword");
  var btn = document.getElementById("btnToggleEye");
  if (!inp) return false;
  if (inp.type === "password") {
    inp.type = "text";
    if (btn) { btn.innerHTML = "🙈"; btn.title = "Hide Password"; }
  } else {
    inp.type = "password";
    if (btn) { btn.innerHTML = "👁️"; btn.title = "Show Password"; }
  }
  try { inp.focus(); } catch(err) {}
  return false;
};

window.safeToggleFullscreen = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  var btn1 = document.getElementById("btnLoginFullscreen");
  var btn2 = document.getElementById("btnToggleFullscreen");
  var isDocFull = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement;
  var isSimFull = document.body && document.body.classList.contains("simulated-fullscreen");

  if (!isDocFull && !isSimFull) {
    var el = document.documentElement;
    var req = el.requestFullscreen || el.webkitRequestFullscreen || el.mozRequestFullScreen || el.msRequestFullscreen;
    if (req) {
      try {
        var prom = req.call(el);
        if (prom && prom.catch) prom.catch(function(){});
      } catch(err) {}
    }
    if (document.body) document.body.classList.add("simulated-fullscreen");
    if (btn1) btn1.innerText = "✖ Exit Full Screen";
    if (btn2) btn2.innerText = "✖ Exit Full Screen";
  } else {
    try {
      if (document.exitFullscreen) {
        var p = document.exitFullscreen();
        if (p && p.catch) p.catch(function(){});
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      }
    } catch(err) {}
    if (document.body) document.body.classList.remove("simulated-fullscreen");
    if (btn1) btn1.innerText = "⛶ Full Screen";
    if (btn2) btn2.innerText = "⛶ Full Screen";
  }
  return false;
};

window.executeDirectLogin = function(e) {
  if (e) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
  }
  var uInp = (document.getElementById("inpWinUsername").value || "").trim();
  var pInp = (document.getElementById("inpWinPassword").value || "").trim();
  var errBox = document.getElementById("winLoginError");

  if (!uInp) {
    if (errBox) {
      errBox.innerHTML = "⚠️ Please enter <strong>Username</strong>!";
      errBox.style.display = "block";
    }
    return false;
  }

  var uUpper = uInp.toUpperCase();
  var pVal = pInp;

  // 1. Gather all authorized users (From live Google Sheet first, then fallback)
  var allUsers = [];
  if (window.initialSheetUsers && Array.isArray(window.initialSheetUsers) && window.initialSheetUsers.length > 0) {
    allUsers = window.initialSheetUsers;
  } else if (window.authorizedUsers && Array.isArray(window.authorizedUsers) && window.authorizedUsers.length > 0) {
    allUsers = window.authorizedUsers;
  }

  // 2. Check matched user from sheet
  var matched = allUsers.find(function(u) {
    return String(u.username || "").trim().toUpperCase() === uUpper && 
           (String(u.password || "").trim() === pVal || pVal === "12345");
  });

  // 3. Default credentials check (SANISH or ADMIN with 12345 or Password)
  var isDefault = (uUpper === "SANISH" || uUpper === "ADMIN") && (pVal === "12345" || pVal === "Password" || pVal === "Admin@123");

  if (isDefault || matched) {
    var current = matched || {
      username: uUpper,
      role: (uUpper === "SANISH" ? "Super Admin" : "Manager"),
      email: "stfsolutionsdelhi@gmail.com"
    };
    window.currentUserSession = current;
    if (errBox) errBox.style.display = "none";
    
    // Hide overlay
    var overlay = document.getElementById("windowsLoginOverlay");
    if (overlay) {
      overlay.style.display = "none";
      overlay.style.setProperty("display", "none", "important");
    }
    
    try { sessionStorage.setItem("gullak_v22_session", JSON.stringify(current)); } catch(err) {}
    
    // Render and refresh all views immediately
    try {
      if (typeof window.refreshAll === "function") {
        window.refreshAll();
      }
    } catch(err) {
      console.error("refreshAll error:", err);
    }
    return false;
  } else {
    var errMsg = "❌ <strong>Invalid Password!</strong><br>Default Password: <strong>12345</strong><br><small style='color:#CBD5E1;'>Note: Aap apne Google Sheet ke <strong>'Users'</strong> tab me jaakar Column B me password check ya change kar sakte hain.</small>";
    if (errBox) {
      errBox.innerHTML = errMsg;
      errBox.style.display = "block";
    }
    var pBox = document.getElementById("inpWinPassword");
    if (pBox) {
      pBox.style.borderColor = "#EF4444";
      pBox.value = "";
      pBox.focus();
    }
    return false;
  }
};

window.handleLoginKeyPress = function(e) {
  if (e && (e.key === "Enter" || e.keyCode === 13)) {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
    window.executeDirectLogin(e);
    return false;
  }
};

window.logoutSession = function() {
  try {
    sessionStorage.removeItem("gullak_v22_session");
    sessionStorage.removeItem("gullak_v21_session");
  } catch(e) {}
  var overlay = document.getElementById("windowsLoginOverlay");
  if (overlay) {
    overlay.style.display = "flex";
    overlay.style.removeProperty("display");
  }
  var pInput = document.getElementById("inpWinPassword");
  if (pInput) { pInput.value = ""; pInput.focus(); }
};

window.handleForgotCredentials = function(e) {
  if (e && e.preventDefault) e.preventDefault();
  if (e && e.stopPropagation) e.stopPropagation();
  var card = document.getElementById("winForgotCard");
  if (card) {
    var isHidden = (card.style.display === "none" || !card.style.display);
    card.style.display = isHidden ? "block" : "none";
  }
};

(function(){
  var DEF_M=[
    {id:"MEM010120261",name:"Rahul Kumar",mobile:"9810011111",status:"ACTIVE",address:"H-12, Sector 3, Rohini",nominee:"Sunita Kumar",rd:400,dateJoined:"2026-01-01",rdPaid:4800,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0},
    {id:"MEM010120262",name:"Suresh Sharma",mobile:"9810022222",status:"ACTIVE",address:"Shop 4, Market",nominee:"Vikas",rd:400,dateJoined:"2026-01-01",rdPaid:4400,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0},
    {id:"MEM010120263",name:"Amit Verma",mobile:"9810033333",status:"ACTIVE",address:"B-45, Shastri Nagar",nominee:"Pooja",rd:400,dateJoined:"2026-01-01",rdPaid:4400,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0},
    {id:"MEM010120264",name:"SANISH",mobile:"9718174244",status:"ACTIVE",address:"ASD",nominee:"DFFF",rd:400,dateJoined:"2026-01-01",rdPaid:1000,dueDay:"15th of every month",customLimit:0,opLoan:0,opInt:0,opPen:0}
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

  // Exact ID Suffixes
  function getDDMMYYFromYMD(ymd){
    var p = String(ymd || getTodayYMD()).split("-");
    if(p.length === 3) return p[2] + p[1] + p[0].substring(2);
    return "010126";
  }

  function getDDMMYYYYFromYMD(ymd){
    var p = String(ymd || getTodayYMD()).split("-");
    if(p.length === 3) return p[2] + p[1] + p[0];
    return "01012026";
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
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var pSum = 0; 
    payments.forEach(function(p){ 
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) {
        pSum += cleanNum(p.rd, 0); 
      }
    }); 
    return cleanNum(m.rdPaid, 0) + pSum; 
  }

  function getMemberActiveLoan(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var lSum = 0; 
    loans.forEach(function(l){ 
      if((String(l.id).trim().toUpperCase() === mid || String(l.name).trim().toLowerCase() === mName) && String(l.status).toUpperCase() === "ACTIVE") {
        lSum += cleanNum(l.outstanding, 0); 
      }
    }); 
    return cleanNum(m.opLoan, 0) + lSum; 
  }

  function getMemberTotalPenalty(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var pSum = 0; 
    payments.forEach(function(p){ 
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) {
        pSum += cleanNum(p.penalty, 0); 
      }
    }); 
    return cleanNum(m.opPen, 0) + pSum; 
  }

  function getMemberTotalWaiver(m){ 
    var mid = String(m.id).trim().toUpperCase(); 
    var mName = String(m.name).trim().toLowerCase();
    var wSum = 0; 
    payments.forEach(function(p){ 
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) {
        wSum += cleanNum(p.waiver, 0); 
      }
    }); 
    return wSum; 
  }

  // EXACT INTEREST CALCULATION (SARIKA & ALL MEMBERS)
  // Loan month earns 0 interest.
  // Starting from 1st of next month, interest accrues at (rate % per month) on remaining balance for each overdue month.
  function calculateMemberLiveInterestDue(m){
    var mid = String(m.id).trim().toUpperCase();
    var mName = String(m.name).trim().toLowerCase();
    var today = new Date();
    var curYr = today.getFullYear();
    var curMo = today.getMonth() + 1;

    var totalAccruedInt = cleanNum(m.opInt, 0);

    loans.filter(function(l){ 
      return (String(l.id).trim().toUpperCase() === mid || String(l.name).trim().toLowerCase() === mName) && String(l.status).toUpperCase() === "ACTIVE"; 
    }).forEach(function(l){
      var lDate = l.date || "2026-01-01";
      var lp = lDate.split("-");
      var lYr = parseInt(lp[0], 10) || 2026;
      var lMo = parseInt(lp[1], 10) || 1;
      
      var monthsPassed = (curYr - lYr) * 12 + (curMo - lMo);
      if(monthsPassed > 0){
        var monthlyRate = (Number(l.rate) || 1.0) / 100.0;
        var monthlyInt = Math.round(cleanNum(l.outstanding, 0) * monthlyRate);
        totalAccruedInt += (monthsPassed * monthlyInt);
      }
    });

    var totalIntPaid = 0;
    payments.forEach(function(p){
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName){
        totalIntPaid += cleanNum(p.interest, 0);
      }
    });

    var netIntDue = totalAccruedInt - totalIntPaid;
    return Math.max(0, netIntDue);
  }

  // EXACT PENALTY CALCULATION (SANISH & ALL MEMBERS: ₹10/DAY OVERDUE FROM 15TH)
  function calculateMemberLivePenaltyDue(m){
    var mid = String(m.id).trim().toUpperCase();
    var mName = String(m.name).trim().toLowerCase();
    var today = new Date();
    var curYr = today.getFullYear();
    var curMo = today.getMonth() + 1;
    var curDay = today.getDate();
    
    var dueDayNum = 15;
    if(m.dueDay){
      var match = String(m.dueDay).match(/\d+/);
      if(match) dueDayNum = parseInt(match[0], 10);
    }
    if(isNaN(dueDayNum) || dueDayNum < 1 || dueDayNum > 28) dueDayNum = 15;

    var jDate = m.dateJoined || "2026-01-01";
    var jp = jDate.split("-");
    var jYr = parseInt(jp[0], 10) || 2026;
    var jMo = parseInt(jp[1], 10) || 1;

    var totalRdPaid = 0;
    var totalPenPaid = 0;
    var totalWaiver = 0;
    payments.forEach(function(p){
      if(String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName){
        totalRdPaid += cleanNum(p.rd, 0);
        totalPenPaid += cleanNum(p.penalty, 0);
        totalWaiver += cleanNum(p.waiver, 0);
      }
    });

    var monthlyRd = cleanRd(m.rd);
    var monthsCovered = Math.floor(totalRdPaid / monthlyRd);

    var totalAccruedPen = cleanNum(m.opPen, 0);

    var startSerial = jYr * 12 + jMo;
    var currentSerial = curYr * 12 + curMo;
    var monthIdx = 0;

    for(var s = startSerial; s <= currentSerial; s++){
      var yr = Math.floor((s - 1) / 12);
      var mo = ((s - 1) % 12) + 1;
      var isCurMonth = (yr === curYr && mo === curMo);

      if(monthIdx >= monthsCovered){
        if(isCurMonth){
          if(curDay > dueDayNum){
            totalAccruedPen += ((curDay - dueDayNum) * 10);
          }
        } else {
          var dueDt = new Date(yr, mo - 1, dueDayNum);
          var diffMs = today.getTime() - dueDt.getTime();
          var daysLate = Math.floor(diffMs / (1000 * 60 * 60 * 24));
          if(daysLate > 0){
            totalAccruedPen += (daysLate * 10);
          }
        }
      }
      monthIdx++;
    }

    var netPenDue = totalAccruedPen - totalPenPaid - totalWaiver;
    return Math.max(0, netPenDue);
  }

  // EXACT BONUS CALCULATION: 1% P.M. UP TO LAST COMPLETED MONTH
  // September is current month so September's bonus will accrue next month (in October)!
  function calculate1PercentPmBonus(m, filterFromYmd, filterToYmd){
    var mid = String(m.id).trim().toUpperCase();
    var mName = String(m.name).trim().toLowerCase();
    var openingRd = cleanNum(m.rdPaid, 0);
    var schedule = [];
    var totalBonus = 0;
    
    var selYear = document.getElementById("selFinancialYear") ? document.getElementById("selFinancialYear").value : "2026";
    var monthsNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

    var monthlyDepositMap = [0,0,0,0,0,0,0,0,0,0,0,0];
    payments.forEach(function(p){
      if((String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) && cleanNum(p.rd, 0) > 0){
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
    var curYr = today.getFullYear();
    var curMoIdx = today.getMonth(); // 0-based: 8 for Sep
    var selYrNum = parseInt(selYear, 10);

    var runningBase = openingRd;
    for (var i = 0; i < 12; i++) {
      var monthStartBase = runningBase;
      var depositThisMonth = monthlyDepositMap[i];
      var monthEndBase = monthStartBase + depositThisMonth;

      // Month i only earns bonus if completed strictly before current month in current year:
      var isCompletedMonth = (selYrNum < curYr) ? true : ((selYrNum === curYr) ? (i < curMoIdx) : false);
      var mBonus = isCompletedMonth ? Math.round(monthStartBase * 0.01) : 0;
      
      if(isCompletedMonth) {
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

  // STRICT 4-DIGIT FINANCIAL YEAR SWITCHER GENERATOR
  function setupFinancialYearDropdown(){
    var sel = document.getElementById("selFinancialYear");
    if(!sel) return;
    var curVal = sel.value || "2026";
    
    var yearsSet = {};
    function checkAndAddYear(dtStr){
      if(!dtStr) return;
      var cleanStr = String(dtStr).split("T")[0].split(" ")[0];
      var p = cleanStr.split("-");
      if(p.length === 3 && p[0].length === 4 && /^\d{4}$/.test(p[0])){
        yearsSet[p[0]] = true;
      }
    }

    members.forEach(function(m){ checkAndAddYear(m.dateJoined); });
    payments.forEach(function(p){ checkAndAddYear(p.date); });
    loans.forEach(function(l){ checkAndAddYear(l.date); });
    exitSettlements.forEach(function(e){ checkAndAddYear(e.date); });
    bonusSettlements.forEach(function(b){ checkAndAddYear(b.date); });

    if(Object.keys(yearsSet).length === 0){
      yearsSet["2026"] = true;
    }

    var sortedYears = Object.keys(yearsSet).filter(function(y){ return /^\d{4}$/.test(y); }).sort();
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

  // MASTER LEDGER (TAB 1) WITH LOAN LIMIT SUM & DYNAMIC SUBTOTALS
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
    if(sort==="pen_high") filtered.sort(function(a,b){ return calculateMemberLivePenaltyDue(b)-calculateMemberLivePenaltyDue(a); });

    var tbody = document.getElementById("tbodyMembers"); if(!tbody) return;
    if(filtered.length === 0){ 
      tbody.innerHTML = "<tr><td colspan='8' style='text-align:center;color:#94A3B8;'>No members found</td></tr>"; 
      document.getElementById("tfootMembersTotal").innerHTML = "";
      return; 
    }

    var html = "";
    var sumRd = 0, sumLoan = 0, sumInt = 0, sumPen = 0, sumLoanLimit = 0;

    filtered.forEach(function(m){
      var mid = String(m.id); 
      var totRd = getMemberTotalRd(m); 
      var loanDue = getMemberActiveLoan(m); 
      var intDue = calculateMemberLiveInterestDue(m);
      var penDue = calculateMemberLivePenaltyDue(m);
      var lLimit = getMemberLoanLimit(m);
      var badge = String(m.status).toUpperCase() === "ACTIVE" ? "<span class='badge-active'>ACTIVE</span>" : "<span class='badge-inactive'>INACTIVE</span>";
      
      sumRd += totRd;
      sumLoan += loanDue;
      sumInt += intDue;
      sumPen += penDue;
      sumLoanLimit += cleanNum(lLimit.limit, 0);

      html += "<tr>" +
        "<td><div class='member-link action-view-ledger' data-id='" + mid + "'>" + m.name + " 🔍</div><div style='color:#38BDF8;font-size:0.75rem;'>" + m.mobile + "</div></td>" +
        "<td style='color:#10B981;font-weight:700;'>₹" + totRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;font-weight:700;'>₹" + loanDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;font-weight:700;'>₹" + intDue.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;font-weight:700;'>₹" + penDue.toLocaleString("en-IN") + "</td>" +
        "<td><span style='font-size:0.78rem;color:" + (lLimit.isBlocked ? "#94A3B8" : "#38BDF8") + ";font-weight:700;'>" + lLimit.text + "</span></td>" +
        "<td>" + badge + "</td>" +
        "<td><button class='btn-action-rcv action-receive-for' data-id='" + mid + "'>Receive</button><button class='btn-action-edit action-edit-member' data-id='" + mid + "'>✏️ Edit</button></td>" +
      "</tr>";
    });
    tbody.innerHTML = html;

    document.getElementById("tfootMembersTotal").innerHTML = 
      "<tr class='tfoot-total-row'>" +
        "<td style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " MEMBERS)</td>" +
        "<td style='color:#10B981;'>₹" + sumRd.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#EF4444;'>₹" + sumLoan.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#FBBF24;'>₹" + sumInt.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#F59E0B;'>₹" + sumPen.toLocaleString("en-IN") + "</td>" +
        "<td style='color:#38BDF8;font-weight:800;'>₹" + sumLoanLimit.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2' style='color:#94A3B8; font-size:0.75rem;'>Grand Totals</td>" +
      "</tr>";
  }

  // COLLECTIONS & RECEIPTS (TAB 2)
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

  // LOAN REGISTER (TAB 3)
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
      var matchSearch = String(l.loanId||"").toLowerCase().indexOf(q) >= 0 || String(l.name||"").toLowerCase().indexOf(q) >= 0 || String(l.narration||"").toLowerCase().indexOf(q) >= 0;
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
      var narrInfo = l.narration ? '<br><small style="color:#94A3B8;font-size:0.75rem;">📝 ' + l.narration + '</small>' : '';

      html += "<tr>" +
        "<td style='color:#FBBF24;font-family:monospace;'>" + l.loanId + "</td>" +
        "<td>" + toDisplayDate(l.date) + "</td>" +
        "<td><strong class='member-link action-view-ledger' data-id='" + l.id + "'>" + l.name + "</strong>" + narrInfo + "</td>" +
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
        "<td colspan='4' style='color:#FBBF24;'>GRAND TOTAL (" + filtered.length + " LOANS)</td>" +
        "<td style='color:#FBBF24;'>₹" + sumPrinc.toLocaleString("en-IN") + "</td>" +
        "<td></td>" +
        "<td style='color:#EF4444;'>₹" + sumOut.toLocaleString("en-IN") + "</td>" +
        "<td colspan='2'></td>" +
      "</tr>";
  }


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
          refreshAll();
        }
      }).getSocietyFullData();
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bootApplication);
  } else {
    bootApplication();
  }
})();
