
// IMMEDIATE LOGIN CONTROLLER (V22 PRO)
(function(){
  window.togglePasswordEye = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
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
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
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

  window.handleForgotCredentials = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();
    var card = document.getElementById("winForgotCard");
    if (!card) return false;
    var isHidden = (card.style.display === "none" || !card.style.display);
    card.style.display = isHidden ? "block" : "none";
    return false;
  };

  window.handleLoginKeyPress = function(e) {
    if (e && (e.key === "Enter" || e.keyCode === 13)) {
      if (e.preventDefault) e.preventDefault();
      if (e.stopPropagation) e.stopPropagation();
      window.executeDirectLogin(e);
      return false;
    }
  };

  window.executeDirectLogin = function(e) {
    if (e && e.preventDefault) e.preventDefault();
    if (e && e.stopPropagation) e.stopPropagation();

    var uInp = (document.getElementById("inpWinUsername") ? document.getElementById("inpWinUsername").value : "").trim();
    var pInp = (document.getElementById("inpWinPassword") ? document.getElementById("inpWinPassword").value : "").trim();
    var errBox = document.getElementById("winLoginError");

    if (!uInp) {
      if (errBox) {
        errBox.innerHTML = "⚠️ Please enter <strong>Username</strong>!";
        errBox.style.display = "block";
      }
      return false;
    }
    if (!pInp) {
      if (errBox) {
        errBox.innerHTML = "⚠️ Please enter <strong>Password</strong>!";
        errBox.style.display = "block";
      }
      var pBox0 = document.getElementById("inpWinPassword");
      if (pBox0) { pBox0.style.borderColor = "#EF4444"; pBox0.focus(); }
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

    // Match against sheet users (Case insensitive username, exact password match)
    var matched = allUsers.find(function(u) {
      var dbUser = String(u.username || "").trim().toUpperCase();
      var dbPass = String(u.password || "").trim();
      return dbUser === uUpper && (dbPass === pVal || (dbPass === "" && pVal === "12345"));
    });

    // Default system credentials: SANISH or ADMIN with 12345 or Password
    var isDefault = (uUpper === "SANISH" || uUpper === "ADMIN") && (pVal === "12345" || pVal === "Password");

    if (isDefault || matched) {
      var current = matched || {
        username: uUpper,
        role: (uUpper === "SANISH" ? "Super Admin" : "Manager"),
        email: "stfsolutionsdelhi@gmail.com"
      };
      window.currentUserSession = current;
      if (errBox) errBox.style.display = "none";

      var overlay = document.getElementById("windowsLoginOverlay");
      if (overlay) {
        overlay.style.display = "none";
        overlay.style.setProperty("display", "none", "important");
      }

      try { sessionStorage.setItem("gullak_v22_session", JSON.stringify(current)); } catch(err) {}

      try {
        if (typeof window.refreshAll === "function") {
          window.refreshAll();
        }
      } catch(err) {
        console.error("refreshAll error:", err);
      }
      return false;
    } else {
      var errMsg = "❌ <strong>Invalid Password!</strong><br><small style='color:#CBD5E1;'>Default Password: <strong>12345</strong><br>Note: Aap apne Google Sheet ke <strong>'Users'</strong> tab me jaakar Column B me password check ya update kar sakte hain.</small>";
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

  function bindLoginListeners() {
    var btnLog = document.getElementById("btnWinLogin");
    if (btnLog) btnLog.onclick = window.executeDirectLogin;

    var btnEye = document.getElementById("btnToggleEye");
    if (btnEye) {
      btnEye.onclick = window.togglePasswordEye;
      btnEye.ontouchstart = window.togglePasswordEye;
    }

    var btnFull = document.getElementById("btnLoginFullscreen");
    if (btnFull) btnFull.onclick = window.safeToggleFullscreen;

    var forgotLink = document.querySelector(".win-forgot-link");
    if (forgotLink) forgotLink.onclick = window.handleForgotCredentials;

    var pBox = document.getElementById("inpWinPassword");
    if (pBox) pBox.onkeydown = window.handleLoginKeyPress;

    var uBox = document.getElementById("inpWinUsername");
    if (uBox) uBox.onkeydown = window.handleLoginKeyPress;
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bindLoginListeners);
  } else {
    bindLoginListeners();
  }
})();
