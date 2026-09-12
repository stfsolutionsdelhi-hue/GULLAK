function getClientScriptPartA() {
  return `
<script>
// INITIAL AUTHENTICATION & LOGIN LOGIC

window.switchTab = function(tIdx) {
  for (var i = 1; i <= 5; i++) {
    var head = document.getElementById("tabHead" + i);
    var panel = document.getElementById("tabPanel" + i);
    if (head) head.className = (i === tIdx) ? "tab-item active" : "tab-item";
    if (panel) panel.style.display = (i === tIdx) ? "block" : "none";
  }
  if (tIdx === 1 && typeof window.renderMembers === "function") window.renderMembers();
  else if (tIdx === 2 && typeof window.renderPayments === "function") window.renderPayments();
  else if (tIdx === 3 && typeof window.renderLoans === "function") window.renderLoans();
  else if (tIdx === 4 && typeof window.renderBonusTab === "function") window.renderBonusTab();
  else if (tIdx === 5 && typeof window.renderPenaltyTab === "function") window.renderPenaltyTab();
};

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

    var uElem = document.getElementById("inpWinUsername");
    var pElem = document.getElementById("inpWinPassword");
    var uInp = (uElem ? uElem.value : "").trim();
    var pInp = (pElem ? pElem.value : "").trim();
    var errBox = document.getElementById("winLoginError");

    if (!uInp) {
      uInp = "SANISH";
      if (uElem) uElem.value = "SANISH";
    }

    if (!pInp) {
      if (errBox) {
        errBox.innerHTML = "⚠️ Please enter <strong>Password</strong> to continue!";
        errBox.style.display = "block";
      }
      if (pElem) { pElem.style.borderColor = "#EF4444"; pElem.focus(); }
      return false;
    }

    var uUpper = uInp.toUpperCase();
    var pVal = pInp;

    var allUsers = [];
    if (window.initialSheetUsers && Array.isArray(window.initialSheetUsers) && window.initialSheetUsers.length > 0) {
      allUsers = window.initialSheetUsers;
    } else if (window.authorizedUsers && Array.isArray(window.authorizedUsers) && window.authorizedUsers.length > 0) {
      allUsers = window.authorizedUsers;
    }

    var matched = null;
    if (allUsers && allUsers.length > 0) {
      for (var i = 0; i < allUsers.length; i++) {
        var u = allUsers[i];
        var dbUser = String(u.username || "").trim().toUpperCase();
        var dbPass = String(u.password || "").trim();
        if (dbUser === uUpper && (dbPass === pVal || (dbPass === "" && pVal === "12345"))) {
          matched = u;
          break;
        }
      }
    }

    var isMasterPass = false;
    var isUserPassMatch = false;
    if (matched) {
      var mPass = String(matched.password || "").trim();
      if (mPass === pVal || (mPass === "" && pVal === "12345")) {
        isUserPassMatch = true;
      }
    }
    if (isMasterPass || isUserPassMatch) {
      var current = matched || {
        username: uUpper || "SANISH",
        role: (uUpper === "ADMIN" ? "Manager" : "Super Admin"),
        email: "stfsolutionsdelhi@gmail.com"
      };
      window.currentUserSession = current;
      if (errBox) errBox.style.display = "none";
      var overlay = document.getElementById("windowsLoginOverlay");
      if (overlay) {
        overlay.style.display = "none";
        overlay.style.setProperty("display", "none", "important");
      }
      try {
        sessionStorage.removeItem("gullak_v22_session");
        sessionStorage.removeItem("gullak_v21_session");
      } catch(err) {}
      if (typeof window.switchTab === "function") {
        window.switchTab(1);
      }
      try {
        if (typeof window.bootApplication === "function") {
          window.bootApplication();
        }
      } catch(bootErr) {
        console.error("bootApplication error:", bootErr);
      }
      try {
        if (typeof window.refreshAll === "function") {
          window.refreshAll();
        }
      } catch(err) {
        console.error("refreshAll error:", err);
      }
      return false;
    } else {
      var errMsg = "❌ <strong>Invalid Password!</strong><br><small style='color:#CBD5E1;'>Please enter the correct password to continue.</small>";
      if (errBox) {
        errBox.innerHTML = errMsg;
        errBox.style.display = "block";
      }
      if (pElem) {
        pElem.style.borderColor = "#EF4444";
        pElem.value = "";
        pElem.focus();
      }
      return false;
    }
  };

  window.handleLoginKeyPress = function(e) {
  var k = e.key || e.keyCode || e.which;
  if (k === "Enter" || k === 13 || k === "13") {
    if (e.preventDefault) e.preventDefault();
    if (e.stopPropagation) e.stopPropagation();
    window.executeDirectLogin(e);
    return false;
  }
};

window.logoutSession = function() {
  window.currentUserSession = null;
  try {
    sessionStorage.removeItem("gullak_v22_session");
    sessionStorage.removeItem("gullak_v21_session");
    sessionStorage.removeItem("gullak_v21_active_user");
  } catch(e) {}
  var overlay = document.getElementById("windowsLoginOverlay");
  if (overlay) {
    overlay.style.display = "flex";
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
  var DEF_M = [{"id": "MEM010120261", "name": "Afsana Sister Pappu Ji 012025", "mobile": "9773841314", "status": "ACTIVE", "address": "Mohan Garden", "nominee": "Pappu Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120262", "name": "Ajay Kumar Garg Ref Suresh Lala Ji 012025", "mobile": "9873898898", "status": "ACTIVE", "address": "Kakrola", "nominee": "Suresh Lala Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120263", "name": "Amit S/O Sunil (Omwati Aunti Ji ) 102022", "mobile": "8287127921", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Omwati Aunti", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 18000, "opInt": 0, "opPen": 0}, {"id": "MEM010120264", "name": "Arvind Kumar 022022X2", "mobile": "9350743408", "status": "ACTIVE", "address": "Ghaziabad", "nominee": "Rekha Kumari", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 7500, "opInt": 0, "opPen": 0}, {"id": "MEM010120265", "name": "ASHA DEVI REF SUSHIL SO SHILA JI 012025", "mobile": "9311043442", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Sushil", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120266", "name": "Ashish Aswal Ashu Vikas Vihar 022022", "mobile": "9899801307", "status": "ACTIVE", "address": "C-141 Vikas Vihar Kakrola", "nominee": "Sarita Aswal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 9000, "opInt": 0, "opPen": 0}, {"id": "MEM010120267", "name": "Chanchal D/O Anil Padosi 022022", "mobile": "9910216942", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Anil Padosi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 16000, "opInt": 0, "opPen": 0}, {"id": "MEM010120268", "name": "Chanda Devi Ref Shila Devi 022024", "mobile": "8447218816", "status": "ACTIVE", "address": "Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 9200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM010120269", "name": "Deep Lal - Reena Devi 022023", "mobile": "9871869719", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Reena Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 4000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202610", "name": "Deep Lal Electrician 022022", "mobile": "9871869719", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 3000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202611", "name": "DEVENDER SINGH REF RAVI 202501", "mobile": "9456304719", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202612", "name": "Geeta Devi Wo Narender 012025", "mobile": "7042511156", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Narender", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202613", "name": "Hari Ram Ji Vikas Vihar 032022", "mobile": "9650013268", "status": "ACTIVE", "address": "Kakrola", "nominee": "Hari Ram", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202614", "name": "Hirender Kumar - 2 - Neetu 012023", "mobile": "9599356910", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Neetu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15360, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5050, "opInt": 0, "opPen": 0}, {"id": "MEM0101202615", "name": "Hirender Kumar -1- 022022", "mobile": "9599356910", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Hirender", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 17802, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 3030, "opInt": 0, "opPen": 0}, {"id": "MEM0101202616", "name": "Jagdish Mehto X2  022022", "mobile": "7042511481", "status": "ACTIVE", "address": "Jj Colony Bharat Vihar", "nominee": "Jagdish", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202617", "name": "Jagriti Sharma W/O Jugal Kishor 012023", "mobile": "9953111505", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jugal Kishor", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 15000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202618", "name": "JAHANVI SHARMA DO JAGRITI JI 012025", "mobile": "9953111505", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202619", "name": "Jot Singh Ref Ravi 012025", "mobile": "8178738999", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202620", "name": "Jugal Kishor Ji X2 072022", "mobile": "9310732656", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202621", "name": "JYOTI JOSHI JI REF JAGRITI JI 012025", "mobile": "9716124006", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Jagriti Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202622", "name": "Kazim So Mumina Khatoon Ref Pappu 012025", "mobile": "8287493771", "status": "ACTIVE", "address": "Kakrola", "nominee": "Mumina Khatoon", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202623", "name": "KEERTHI R S DO SOMYA MADAM 202501", "mobile": "7827596703", "status": "ACTIVE", "address": "Kakrola", "nominee": "Somya Madam", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202624", "name": "KIRAN DEVI WO SUSHIL KUMAR 202501", "mobile": "7042480937", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sushil Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202625", "name": "Kuwar Pal -1 X2 082022", "mobile": "9871130935", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Kuwar Pal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202626", "name": "Kuwar Pal-2 X2 082022", "mobile": "9871130935", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Kuwar Pal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202627", "name": "Mukesh Sharma Ji X2 022022", "mobile": "8285405743", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Mukesh", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18799.59, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5623, "opInt": 0, "opPen": 0}, {"id": "MEM0101202628", "name": "NANDINI JI 202501", "mobile": "8383071508", "status": "ACTIVE", "address": "SULAHKUL VIHAR", "nominee": "Nandini", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202629", "name": "Narayan Yadav X2 032022", "mobile": "9599959948", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Narayan", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18399.68, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202630", "name": "Narender Babblu Bo Ravi 012025", "mobile": "9354214597", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202631", "name": "Narender Kumar S/O Shila Devi 012023", "mobile": "7042511156", "status": "ACTIVE", "address": "S/O Shila Devi Vikas Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14399.88, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 2000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202632", "name": "Neeraj Renew So Raghuveer Ji 012025", "mobile": "9891811697", "status": "ACTIVE", "address": "Kakrola", "nominee": "Raghuveer Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202633", "name": "Omwati Aunti M/O Anil Kumar 022022", "mobile": "9971157481", "status": "ACTIVE", "address": "C-143 Vikas Vihar Kakrola", "nominee": "Anil Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 17800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202634", "name": "Pappu Carpainter - 1 - 022022", "mobile": "9911563986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Pappu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 13000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202635", "name": "Pappu Carpainter - 2 - Nargis 102022", "mobile": "9911563986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Nargis", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 21000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202636", "name": "Pawan Kumar X2 072022", "mobile": "8368934198", "status": "ACTIVE", "address": "S/O Rakesh Kumar Vikas Vihar", "nominee": "Rakesh Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16799.76, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 19230, "opInt": 0, "opPen": 0}, {"id": "MEM0101202637", "name": "Peter Masih 042022", "mobile": "99990023275", "status": "ACTIVE", "address": "Mohan Garden", "nominee": "Peter", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202638", "name": "Raj Kumar (Colony) Kakrola 062022", "mobile": "8750830986", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 22136, "opInt": 0, "opPen": 0}, {"id": "MEM0101202639", "name": "Raja Ram Ji Ref Deepak 062022", "mobile": "9810812331", "status": "ACTIVE", "address": "Narela", "nominee": "Deepak", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202640", "name": "Ram Bharose Ji Goyla Dairy 022022", "mobile": "9717961768", "status": "ACTIVE", "address": "Goyla Dairy 9717961768 , 0838392003", "nominee": "Ram Bharose", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16200, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202641", "name": "Ravi Garwali 022022", "mobile": "7042085508", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Ravi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 17000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202642", "name": "Sanjay Kumar -1- Ref DeeplaI 022022", "mobile": "9650862110", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202643", "name": "Sanjay Kumar -2-  Sandeep Kr Ref DeeplaI 022023", "mobile": "9650862110", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Sandeep Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202645", "name": "Sanjay Yadav -1 X2 022022", "mobile": "7827004101", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sanjay Yadav", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 23000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202646", "name": "Sanjay Yadav -2- Shubhankar 072023", "mobile": "7827004101", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Shubhankar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 5000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202647", "name": "Santosh Mehto X2 022022", "mobile": "9968062512", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Santosh", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 18800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 17000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202648", "name": "Santosh Mistri Ref DeeplaI 012025", "mobile": "9891703298", "status": "ACTIVE", "address": "Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202649", "name": "Sarika 022022", "mobile": "9718174244", "status": "ACTIVE", "address": "Kakrola", "nominee": "Sarika", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15583.59, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 12000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202650", "name": "Sarita Aswal Wo Ashish 012025", "mobile": "9899801307", "status": "ACTIVE", "address": "Kakrola", "nominee": "Ashish Aswal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 25000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202651", "name": "Shila Devi Ref Omwati Aunti X2 092022", "mobile": "9643588165", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Omwati Aunti", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 16000, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 11000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202652", "name": "Somya Madam Ref Jagriti Sharma 012023", "mobile": "7827596703", "status": "ACTIVE", "address": "Kakrola", "nominee": "Jagriti Sharma", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 14400, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 16000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202653", "name": "Sushil Ji So Sheela Devi 012025", "mobile": "7042480937", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Sheela Devi", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202654", "name": "URUZ KHATMA DO MUMINA REF PAPPU 012025", "mobile": "8287493771", "status": "ACTIVE", "address": "Kakrola", "nominee": "Mumina Khatoon", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 4800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202655", "name": "Viney Electrician Ref Deep Lal 052023", "mobile": "7065708037", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 12800, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 18000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202656", "name": "Vishnu Aggarwal -1 102022", "mobile": "9773557036", "status": "ACTIVE", "address": "Kakrola", "nominee": "Vishnu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 10000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202657", "name": "Vishnu Aggarwal -2 102022", "mobile": "9773557036", "status": "ACTIVE", "address": "Kakrola", "nominee": "Vishnu", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 15600, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 10000, "opInt": 0, "opPen": 0}, {"id": "MEM0101202658", "name": "Parvesh Ansari Ref DeeplaI 010126", "mobile": "9315426875", "status": "ACTIVE", "address": "Kakrola", "nominee": "Deep Lal", "rd": 400, "dateJoined": "2026-01-12", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202659", "name": "Hazrat Ref Parvesh Ansari 010126", "mobile": "9718172262", "status": "ACTIVE", "address": "Dda Flat Janak Puri", "nominee": "Parvesh Ansari", "rd": 400, "dateJoined": "2026-01-12", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202660", "name": "Mintu Devi Ref Chanda Devi 012026", "mobile": "7033953938", "status": "ACTIVE", "address": "Vikas Vihar", "nominee": "Chanda Devi", "rd": 400, "dateJoined": "2026-01-15", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202661", "name": "Mariam R/O Rupam & Shila Devi", "mobile": "8826567542", "status": "ACTIVE", "address": "Bharat Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202662", "name": "Rupam Ref Shila Devi 012026", "mobile": "8130546714", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Shila Devi", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202663", "name": "Surender Rawat 012026", "mobile": "9266782629", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Sumitra Rawat", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202664", "name": "Sumitra Rawat Wo Surender 012026", "mobile": "9266782629", "status": "ACTIVE", "address": "Vikas Vihar Kakrola", "nominee": "Surender Rawat", "rd": 400, "dateJoined": "2026-01-19", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202665", "name": "Priya Sood Ref Raj Kumar 012026", "mobile": "8750830986", "status": "ACTIVE", "address": "House Number B-115 Surya Vihar Binda", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202666", "name": "Raj Kumari Ref Raj Kumar 012026", "mobile": "8750830986", "status": "ACTIVE", "address": "B-75 Bharat Vihar Kakrola 9810424981", "nominee": "Raj Kumar", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202667", "name": "Arvind Kumar Rekha Kumari 012026", "mobile": "9350743408", "status": "ACTIVE", "address": "Gazhiabad", "nominee": "Arvind Kumar", "rd": 400, "dateJoined": "2026-01-31", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}, {"id": "MEM0101202668", "name": "Rakhi Madam Ref Shila Ji 012026", "mobile": "9311633238", "status": "ACTIVE", "address": "Delhi", "nominee": "Shila Ji", "rd": 400, "dateJoined": "2026-01-01", "rdPaid": 0, "dueDay": "15th of every month", "customLimit": 0, "opLoan": 0, "opInt": 0, "opPen": 0}];
  var DEF_P=[];
  var DEF_L=[];

  var members = [];
  var payments = [];
  var loans = [];
  var exitSettlements = [];
  var bonusSettlements = [];
  var fundTransactions = [];

  var initLoaded = false;
  if (typeof window !== "undefined" && window.initialSocietyData && window.initialSocietyData.members && window.initialSocietyData.members.length > 0) {
    members = window.initialSocietyData.members;
    payments = window.initialSocietyData.payments || [];
    loans = window.initialSocietyData.loans || [];
    exitSettlements = window.initialSocietyData.exitSettlements || [];
    bonusSettlements = window.initialSocietyData.bonusSettlements || [];
    fundTransactions = window.initialSocietyData.fundTransactions || [];
    initLoaded = true;
  }

    // Auto-upgrade stale dummy member lists if fewer than 10 members or containing dummy names
  if (members && Array.isArray(members) && (members.length < 10 || (members[0] && members[0].name === "Rahul Kumar"))) {
    console.log("Upgrading stale members array to full 67 real members...");
    members = DEF_M;
    try {
      localStorage.setItem("gullak_v21_m", JSON.stringify(members));
    } catch(e) {}
  }
  if (!initLoaded) {
    try {
      var sM = localStorage.getItem("gullak_v21_m");
      if (sM) members = JSON.parse(sM);
      var sP = localStorage.getItem("gullak_v21_p");
      if (sP) payments = JSON.parse(sP);
      var sL = localStorage.getItem("gullak_v21_l");
      if (sL) loans = JSON.parse(sL);
      var sEx = localStorage.getItem("gullak_v21_ex");
      if (sEx) exitSettlements = JSON.parse(sEx);
      var sB = localStorage.getItem("gullak_v21_b");
      if (sB) bonusSettlements = JSON.parse(sB);
      var sF = localStorage.getItem("gullak_v21_fund");
      if (sF) fundTransactions = JSON.parse(sF);
    } catch(e) {}
  }

  if (!members || members.length === 0) {
    members = JSON.parse(JSON.stringify(DEF_M));
  }
  (members || []).forEach(function(m){
    var rawSt = String(m.status || "ACTIVE").trim().toUpperCase();
    m.status = (rawSt === "INACTIVE" || rawSt === "IN-ACTIVE" || rawSt === "DEACTIVE" || rawSt === "DEACTIVATED") ? "INACTIVE" : "ACTIVE";
  });
  if (!payments) payments = [];
  if (!loans) loans = [];
  if (!exitSettlements) exitSettlements = [];
  if (!bonusSettlements) bonusSettlements = [];
  if (!fundTransactions) fundTransactions = [];

  window.fundTransactions = fundTransactions;
  window.members = members;
  window.payments = payments;
  window.loans = loans;
  window.exitSettlements = exitSettlements;
  window.bonusSettlements = bonusSettlements;
  var globalDefaultRate = 1.0;
  var globalDefaultDue = "15th of every month";
  var pendingBulkData = null;
  var currentActiveLedgerMember = null;

  window.globalSettings = { penaltyStartDate: "2026-10-01", skipPenalty: true };
  try {
    var savedStg = localStorage.getItem("gullak_v21_settings");
    if(savedStg) window.globalSettings = JSON.parse(savedStg);
  } catch(e) {}

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
    // Handle month names like 01-Aug-2026 or 1 Aug 2026
    var mAlpha = s.match(/^(\d{1,2})[-\/\s]([A-Za-z]{3,9})[-\/\s](\d{4})$/);
    if (mAlpha) {
      var monthMap = { jan:1, feb:2, mar:3, apr:4, may:5, jun:6, jul:7, aug:8, sep:9, oct:10, nov:11, dec:12 };
      var mShort = mAlpha[2].substring(0, 3).toLowerCase();
      var moNum = monthMap[mShort] || 1;
      return { yr: parseInt(mAlpha[3], 10), mo: moNum, day: parseInt(mAlpha[1], 10) };
    }
    var dt = new Date(s);
    if (!isNaN(dt.getTime()) && dt.getFullYear() >= 2020 && dt.getFullYear() <= 2100) {
      return { yr: dt.getFullYear(), mo: dt.getMonth() + 1, day: dt.getDate() };
    }
    return { yr: 2026, mo: 1, day: 1 };
  }
  window.parseDateParts = parseDateParts;

  function toIsoDateStr(d){
    if(!d) return "2026-01-01";
    var dp = parseDateParts(d);
    if(!dp || !dp.yr || !dp.mo || !dp.day) return "2026-01-01";
    var y = dp.yr < 2020 || dp.yr > 2100 ? 2026 : dp.yr;
    var m = String(dp.mo).padStart(2, "0");
    var day = String(dp.day).padStart(2, "0");
    return y + "-" + m + "-" + day;
  }
  window.toIsoDateStr = toIsoDateStr;

  function getYearMonthKey(d){
    if(!d) return "2026-01";
    var dp = parseDateParts(d);
    if(!dp || !dp.yr || !dp.mo) return "2026-01";
    var y = dp.yr < 2020 || dp.yr > 2100 ? 2026 : dp.yr;
    var m = String(dp.mo).padStart(2, "0");
    return y + "-" + m;
  }
  window.getYearMonthKey = getYearMonthKey;

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

  function toDisplayDate(d){
    if(!d) return "01/01/2026";
    var dp = parseDateParts(d);
    if(!dp || !dp.yr || !dp.mo || !dp.day) return "01/01/2026";
    var day = String(dp.day).padStart(2, "0");
    var m = String(dp.mo).padStart(2, "0");
    var y = dp.yr < 2020 || dp.yr > 2100 ? 2026 : dp.yr;
    return day + "/" + m + "/" + y;
  }
  window.toDisplayDate = toDisplayDate;

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
    try {
      window.members = members;
      window.payments = payments;
      window.loans = loans;
      window.exitSettlements = exitSettlements;
      window.bonusSettlements = bonusSettlements;
      window.fundTransactions = fundTransactions;
      localStorage.setItem("gullak_v21_m", JSON.stringify(members));
      localStorage.setItem("gullak_v21_p", JSON.stringify(payments));
      localStorage.setItem("gullak_v21_l", JSON.stringify(loans));
      localStorage.setItem("gullak_v21_ex", JSON.stringify(exitSettlements));
      localStorage.setItem("gullak_v21_b", JSON.stringify(bonusSettlements));
      localStorage.setItem("gullak_v21_fund", JSON.stringify(fundTransactions));
      localStorage.setItem("gullak_v21_settings", JSON.stringify(window.globalSettings || { penaltyStartDate: "2026-10-01", skipPenalty: true }));
    } catch(e) {}
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
    var isSkipChecked = true;
    if(window.globalSettings && typeof window.globalSettings.skipPenalty === "boolean"){
      isSkipChecked = window.globalSettings.skipPenalty;
    } else {
      var chkEl = document.getElementById("chkSkipPenalty");
      if(chkEl) isSkipChecked = chkEl.checked;
    }
    if(isSkipChecked) return 0;

    var penStartStr = "2026-10-01";
    if(window.globalSettings && window.globalSettings.penaltyStartDate){
      penStartStr = window.globalSettings.penaltyStartDate;
    } else {
      var psEl = document.getElementById("inpPenaltyStartDate");
      if(psEl && psEl.value) penStartStr = psEl.value;
    }

    var isoPenStart = toIsoDateStr(penStartStr);
    var todayYMD = getTodayYMD();
    if(todayYMD < isoPenStart) return 0;

    var mid = String(m.id || "").trim().toUpperCase();
    var mName = String(m.name || "").trim().toLowerCase();
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
    if(jYr < 2024 || jYr > 2035) jYr = 2026;
    if(jMo < 1 || jMo > 12) jMo = 1;

    var totalRdPaid = cleanNum(m.rdPaid, 0);
    var totalPenPaid = 0;
    var totalWaiver = 0;

    payments.forEach(function(p){
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase();
      var pMName = String(p.name || "").trim().toLowerCase();
      if((pMid && pMid === mid) || (pMName && pMName === mName)){
        totalRdPaid += cleanNum(p.rd, 0);
        totalPenPaid += cleanNum(p.penalty, 0);
        totalWaiver += cleanNum(p.waiver, 0);
      }
    });

    var monthlyRd = cleanRd(m.rd);
    var monthsCovered = Math.floor(totalRdPaid / monthlyRd);
    var totalAccruedPen = cleanNum(m.opPen, 0);

    var pStartParts = parseDateParts(penStartStr);
    var pStartYr = pStartParts.yr || 2026;
    var pStartMo = pStartParts.mo || 10;
    var penaltyStartSerial = pStartYr * 12 + pStartMo;

    var startSerial = jYr * 12 + jMo;
    var currentSerial = curYr * 12 + curMo;
    var monthIdx = 0;

    for(var s = startSerial; s <= currentSerial; s++){
      if(s >= penaltyStartSerial){
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
            var refDt = new Date(pStartYr, pStartMo - 1, 1);
            var calcFromDt = dueDt > refDt ? dueDt : refDt;
            var diffMs = today.getTime() - calcFromDt.getTime();
            var daysLate = Math.floor(diffMs / (1000 * 60 * 60 * 24));
            if(daysLate > 0){
              totalAccruedPen += (daysLate * 10);
            }
          }
        }
      }
      monthIdx++;
    }

    var netPenDue = totalAccruedPen - totalPenPaid - totalWaiver;
    return Math.max(0, netPenDue);
  }

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
      var pMid = String(p.memberId || p.id || "").trim().toUpperCase(); var pMName = String(p.name || "").trim().toLowerCase(); if(((pMid && pMid === mid) || (pMName && pMName === mName)) && cleanNum(p.rd, 0) > 0){
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

  function calculateSocietyLiquidBalances(){
    var cIn = 0, cOut = 0, bIn = 0, bOut = 0;
    // 1. Opening RD from members (considered cash balance unless specified)
    members.forEach(function(m){
      cIn += cleanNum(m.rdPaid || m.opRd, 0);
    });
    // 2. Receipts / Collections
    payments.forEach(function(p){
      var safeMode = String(p.mode||'CASH').toUpperCase().indexOf('ONLINE') >= 0 ? 'BANK' : 'CASH';
      var amt = cleanNum(p.total, 0);
      if(safeMode === 'BANK') bIn += amt; else cIn += amt;
    });
    // 3. Fund Register (Invest/Borrow)
    fundTransactions.forEach(function(f){
      var acc = String(f.account || 'BANK').toUpperCase() === 'CASH' ? 'CASH' : 'BANK';
      var type = String(f.type || 'INVEST').toUpperCase();
      var amt = cleanNum(f.amount, 0);
      if(type === 'INVEST' || type === 'INFLOW'){
        if(acc === 'CASH') cIn += amt; else bIn += amt;
      } else {
        if(acc === 'CASH') cOut += amt; else bOut += amt;
      }
    });
    // 4. Loans Disbursed (Outflow)
    loans.forEach(function(l){
      var safeMode = String(l.mode || 'CASH').toUpperCase().indexOf('ONLINE') >= 0 ? 'BANK' : 'CASH';
      var amt = cleanNum(l.principal, 0);
      if(safeMode === 'BANK') bOut += amt; else cOut += amt;
    });
    // 5. Member Exit Payouts (Outflow)
    exitSettlements.forEach(function(x){
      var amt = cleanNum(x.payout, 0);
      cOut += amt;
    });
    var netCash = cIn - cOut;
    var netBank = bIn - bOut;
    var netTotal = netCash + netBank;
    return { cash: netCash, bank: netBank, total: netTotal, cIn: cIn, cOut: cOut, bIn: bIn, bOut: bOut };
  }
  window.calculateSocietyLiquidBalances = calculateSocietyLiquidBalances;

  function updateKPIs(){
    var activeMems = members.filter(function(m){ return String(m.status).toUpperCase() === 'ACTIVE'; });
    document.getElementById('dispTotalMem').innerText = activeMems.length + ' / ' + members.length;
    var totalRdRecv = 0; members.forEach(function(m){ totalRdRecv += getMemberTotalRd(m); });
    document.getElementById('dispTotalRd').innerText = '₹' + totalRdRecv.toLocaleString('en-IN');
    var totalLoan = 0; loans.forEach(function(l){ if(String(l.status).toUpperCase() === 'ACTIVE') totalLoan += cleanNum(l.outstanding, 0); });
    document.getElementById('dispTotalLoan').innerText = '₹' + totalLoan.toLocaleString('en-IN');
    var estB = 0; members.forEach(function(m){ estB += getMemberBonus(m); });
    document.getElementById('dispTotalBonus').innerText = '₹' + estB.toLocaleString('en-IN');
    
    // Unified Liquid Cash & Bank Register KPI
    var liquid = calculateSocietyLiquidBalances();
    var elFund = document.getElementById('dispTotalFund');
    elFund.innerText = (liquid.total >= 0 ? '+₹' : '-₹') + Math.abs(liquid.total).toLocaleString('en-IN');
    elFund.className = liquid.total >= 0 ? 'kpi-val val-green' : 'kpi-val val-red';
    
    var totalNpa = 0; exitSettlements.forEach(function(e){ totalNpa += cleanNum(e.npaLoss, 0); });
    document.getElementById('dispTotalNpa').innerText = '₹' + totalNpa.toLocaleString('en-IN');
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
