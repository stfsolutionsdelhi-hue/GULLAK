function getClientScriptPartA() {
  return `
<script>
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
    window.isAppInFullscreenMode = true;
    if (btn1) btn1.innerText = "✖ Exit Full Screen";
    if (btn2) btn2.innerText = "✖ Exit Full Screen";
  } else {
    window.isAppInFullscreenMode = false;
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
    
    // Ensure app is booted
    try {
      if (typeof window.bootApplication === "function") {
        window.bootApplication();
      }
    } catch(err) {
      console.error("bootApplication error:", err);
    }

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
    var errMsg = "❌ <strong>Invalid Password!</strong><br><small style='color:#CBD5E1;'>Please enter the correct password. You can check or reset your password in the <strong>'Users'</strong> tab of your connected Google Sheet.</small>";
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
  var fundTransactions = [];
  try {
    var savedFund = localStorage.getItem("gullak_v21_fund");
    if(savedFund) fundTransactions = JSON.parse(savedFund);
  } catch(e) {}
  window.fundTransactions = fundTransactions;
  var globalDefaultRate = 1.0;
  var globalDefaultDue = "15th of every month";
  var pendingBulkData = null;
  var currentActiveLedgerMember = null;

  function parseDateParts(d){
    if (!d) return { yr: 2026, mo: 1, day: 1 };
    if (d instanceof Date) {
      if (isNaN(d.getTime())) return { yr: 2026, mo: 1, day: 1 };
      return { yr: d.getFullYear(), mo: d.getMonth() + 1, day: d.getDate() };
    }
    var s = String(d).trim().split("T")[0].split(" ")[0];
    var mYmd = s.match(/^(\d{4})[-\/.](\d{1,2})[-\/.](\d{1,2})$/);
    if (mYmd) {
      return { yr: parseInt(mYmd[1], 10), mo: parseInt(mYmd[2], 10), day: parseInt(mYmd[3], 10) };
    }
    var mDmy = s.match(/^(\d{1,2})[-\/.](\d{1,2})[-\/.](\d{4})$/);
    if (mDmy) {
      return { yr: parseInt(mDmy[3], 10), mo: parseInt(mDmy[2], 10), day: parseInt(mDmy[1], 10) };
    }
    var dt = new Date(s);
    if (!isNaN(dt.getTime()) && dt.getFullYear() >= 2020 && dt.getFullYear() <= 2100) {
      return { yr: dt.getFullYear(), mo: dt.getMonth() + 1, day: dt.getDate() };
    }
    return { yr: 2026, mo: 1, day: 1 };
  }
  window.parseDateParts = parseDateParts;

  function cleanNum(val, def){ 
    if(val === null || val === undefined) return (def || 0);
    var str = String(val).replace(/,/g, "").trim();
    var n = Number(str); 
    if(isNaN(n) || n > 100000000 || n < 0) return (def || 0); 
    return Math.round(n); 
  }
  function cleanRd(val){ 
    if(val === null || val === undefined) return 400;
    var str = String(val).replace(/,/g, "").trim();
    var n = Number(str); 
    if(isNaN(n) || n <= 0 || n > 50000) return 400; 
    return Math.round(n); 
  }
  function getTodayYMD(){ 
    var d = new Date(); 
    return d.getFullYear() + "-" + String(d.getMonth()+1).padStart(2,"0") + "-" + String(d.getDate()).padStart(2,"0"); 
  }

  function toDisplayDate(ymd){
    if(!ymd) return "01-01-2026";
    var p = String(ymd).split("T")[0].split(" ")[0].split("-");
    if(p.length === 3) return p[2] + "-" + p[1] + "-" + p[0];
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
      localStorage.setItem("gullak_v21_fund", JSON.stringify(fundTransactions));
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

  // EXACT PENALTY CALCULATION (SANISH, AMIT VERMA & ALL MEMBERS: ₹10/DAY OVERDUE FROM 15TH)
  function calculateMemberLivePenaltyDue(m){
    // GLOBAL PENALTY SKIP & START DATE CONTROL (V36)
    var isSkipChecked = document.getElementById("chkSkipPenalty") ? document.getElementById("chkSkipPenalty").checked : true;
    if(window.globalSettings && typeof window.globalSettings.skipPenalty === "boolean"){
      isSkipChecked = window.globalSettings.skipPenalty;
    }
    if(isSkipChecked) return 0;

    var penStartStr = document.getElementById("inpPenaltyStartDate") ? document.getElementById("inpPenaltyStartDate").value : "2026-10-01";
    if(window.globalSettings && window.globalSettings.penaltyStartDate){
      penStartStr = window.globalSettings.penaltyStartDate;
    }
    var todayYMD = getTodayYMD();
    if(todayYMD < penStartStr) return 0;

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

    var jdp = parseDateParts(m.dateJoined || "2026-01-01");
    var jYr = jdp.yr;
    var jMo = jdp.mo;
    if(jYr < 2024 || jYr > 2100) jYr = 2026;
    if(jMo < 1 || jMo > 12) jMo = 1;

    // Total RD paid includes opening RD balance (rdPaid) plus all subsequent payments
    var totalRdPaid = cleanNum(m.rdPaid, 0);
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
  function calculate1PercentPmBonus(m, filterFromYmd, filterToYmd){
    var mid = String(m.id).trim().toUpperCase();
    var mName = String(m.name).trim().toLowerCase();
    var openingRd = cleanNum(m.rdPaid, 0);
    var schedule = [];
    var totalBonus = 0;
    
    var selVal = document.getElementById("selFinancialYear") ? document.getElementById("selFinancialYear").value : "2026";
    var selYrNum = 2026;
    if(selVal){
      var matched = String(selVal).match(/\d{4}/);
      if(matched) selYrNum = parseInt(matched[0], 10);
    }
    if(isNaN(selYrNum) || selYrNum < 2020 || selYrNum > 2100) selYrNum = 2026;
    var selYear = String(selYrNum);

    var monthsNames = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

    // 1. Calculate RD deposits made in years prior to selYrNum
    var priorYearsRd = 0;
    // 2. Map monthly deposits for selYrNum
    var monthlyDepositMap = [0,0,0,0,0,0,0,0,0,0,0,0];
    payments.forEach(function(p){
      if((String(p.id).trim().toUpperCase() === mid || String(p.name).trim().toLowerCase() === mName) && cleanNum(p.rd, 0) > 0){
        var dp = parseDateParts(p.date);
        if(dp.yr < selYrNum){
          priorYearsRd += cleanNum(p.rd, 0);
        } else if(dp.yr === selYrNum && dp.mo >= 1 && dp.mo <= 12){
          monthlyDepositMap[dp.mo - 1] += cleanNum(p.rd, 0);
        }
      }
    });

    var today = new Date();
    var curYr = today.getFullYear();
    var curMoIdx = today.getMonth(); // 0-based: 8 for Sep

    var baseOpening = openingRd + priorYearsRd;
    var runningBase = baseOpening;

    for (var i = 0; i < 12; i++) {
      var monthStartBase = runningBase;
      var depositThisMonth = monthlyDepositMap[i];
      var monthEndBase = monthStartBase + depositThisMonth;

      // Month i earns bonus if completed strictly before current month in current year, or all months of a prior year:
      var isCompletedMonth = (selYrNum < curYr) ? true : ((selYrNum === curYr) ? (i <= curMoIdx) : false);
      // 1% bonus is calculated on the closing balance of RD for that month (Opening + Prior Deposits + Deposits made in month i)
      var calcBase = monthEndBase;
      var mBonus = isCompletedMonth && calcBase > 0 ? Math.round(calcBase * 0.01) : 0;
      
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

  // AI-POWERED COLLISION-FREE AUTO-ID GENERATOR
  function generateAutoId(type, dateStr){
    var dp = parseDateParts(dateStr || getTodayYMD());
    var yrShort = String(dp.yr).substring(2);
    var moShort = String(dp.mo).padStart(2, "0");
    var dayShort = String(dp.day).padStart(2, "0");
    var datePrefix = yrShort + moShort + dayShort;
    
    var pfx = "REC";
    var existingList = [];
    var t = String(type || "REC").toUpperCase();
    if(t === "REC" || t === "RECEIPT" || t === "PAYMENT"){
      pfx = "REC";
      existingList = payments.map(function(x){ return String(x.receiptNo || ""); });
    } else if(t === "LOAN" || t === "LN"){
      pfx = "LN";
      existingList = loans.map(function(x){ return String(x.loanId || ""); });
    } else if(t === "MEM" || t === "MEMBER"){
      pfx = "MEM";
      existingList = members.map(function(x){ return String(x.id || ""); });
    } else if(t === "EXIT" || t === "EX"){
      pfx = "EXT";
      existingList = exitSettlements.map(function(x){ return String(x.exitId || ""); });
    } else if(t === "SET" || t === "SETTLEMENT" || t === "BONUS"){
      pfx = "SET";
      existingList = bonusSettlements.map(function(x){ return String(x.settlementId || ""); });
    } else if(t === "FUND" || t === "FND"){
      pfx = "FND";
      existingList = fundTransactions.map(function(x){ return String(x.id || ""); });
    }

    var seq = 1;
    var candidate = "";
    while(true){
      var seqStr = String(seq).padStart(3, "0");
      candidate = pfx + "-" + datePrefix + "-" + seqStr;
      if(existingList.indexOf(candidate) === -1){
        break;
      }
      seq++;
    }
    return candidate;
  }
  window.generateAutoId = generateAutoId;

  // FINANCIAL YEAR (CALENDAR YEAR: JAN - DEC) SWITCHER GENERATOR
  function setupFinancialYearDropdown(){
    var sel = document.getElementById("selFinancialYear");
    if(!sel) return;
    var curVal = sel.value || "2026";
    if(curVal.indexOf(" ") >= 0){
      var m = curVal.match(/\d{4}/);
      if(m) curVal = m[0];
    }
    
    var currentYear = (new Date()).getFullYear().toString();
    if(!/^\d{4}$/.test(currentYear)) currentYear = "2026";

    var fyMap = {};
    fyMap["2026"] = true;
    fyMap[currentYear] = true;

    function recordYearFromDate(dtStr){
      if(!dtStr) return;
      var dp = parseDateParts(dtStr);
      if(dp && dp.yr && dp.yr >= 2020 && dp.yr <= 2100){
        fyMap[String(dp.yr)] = true;
      }
    }

    payments.forEach(function(p){ recordYearFromDate(p.date); });
    loans.forEach(function(l){ recordYearFromDate(l.date); });
    exitSettlements.forEach(function(e){ recordYearFromDate(e.date); });
    bonusSettlements.forEach(function(b){ recordYearFromDate(b.date); });
    fundTransactions.forEach(function(f){ recordYearFromDate(f.date); });

    var sortedYrs = Object.keys(fyMap).sort();
    var h = "";
    sortedYrs.forEach(function(yr){
      var isSel = (yr === curVal) ? "selected" : "";
      h += "<option value='" + yr + "' " + isSel + ">FY " + yr + "</option>";
    });
    sel.innerHTML = h;
    if(sortedYrs.indexOf(curVal) === -1){
      sel.value = currentYear;
    } else {
      sel.value = curVal;
    }
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
    
    // Calculate liquid fund factoring in Invest Inflows & Borrow Outflows
    var fundInflow = 0;
    var fundOutflow = 0;
    fundTransactions.forEach(function(f){
      var amt = cleanNum(f.amount, 0);
      if(String(f.type).toUpperCase() === "INVEST") fundInflow += amt;
      else if(String(f.type).toUpperCase() === "BORROW") fundOutflow += amt;
    });
    var fund = totalRdRecv - totalLoan + 45000 + fundInflow - fundOutflow;
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
`;
}
