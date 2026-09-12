
const fs = require("fs");

// Build a mock DOM that contains ALL elements present in Part2_Html.gs
const elements = {};

function createMockElement(id, tagName = "div") {
  return {
    id: id || "",
    tagName: tagName.toUpperCase(),
    value: "",
    style: {
      display: "",
      borderColor: "",
      setProperty: function(k, v){ this[k] = v; },
      removeProperty: function(k){ delete this[k]; }
    },
    innerHTML: "",
    innerText: "",
    textContent: "",
    options: [],
    selectedIndex: 0,
    classList: {
      add: function(){},
      remove: function(){},
      contains: function(){ return false; },
      toggle: function(){}
    },
    setAttribute: function(){},
    getAttribute: function(){ return ""; },
    addEventListener: function(){},
    removeEventListener: function(){},
    querySelectorAll: function(){ return []; },
    querySelector: function(){ return null; },
    appendChild: function(child){ return child; },
    removeChild: function(){},
    focus: function(){},
    click: function(){}
  };
}

const document = {
  getElementById: function(id) {
    if (!elements[id]) {
      elements[id] = createMockElement(id);
    }
    return elements[id];
  },
  querySelectorAll: function(sel) {
    return [];
  },
  querySelector: function(sel) {
    return createMockElement();
  },
  createElement: function(tag) {
    return createMockElement("", tag);
  },
  addEventListener: function(){},
  removeEventListener: function(){},
  body: createMockElement("body"),
  documentElement: createMockElement("html")
};

const window = {
  document: document,
  addEventListener: function(){},
  removeEventListener: function(){},
  location: { reload: function(){} },
  sessionStorage: {
    data: {},
    getItem: function(k){ return this.data[k] || null; },
    setItem: function(k, v){ this.data[k] = String(v); },
    removeItem: function(k){ delete this.data[k]; }
  },
  localStorage: {
    data: {},
    getItem: function(k){ return this.data[k] || null; },
    setItem: function(k, v){ this.data[k] = String(v); },
    removeItem: function(k){ delete this.data[k]; }
  },
  navigator: { clipboard: { writeText: () => Promise.resolve() } }
};

global.window = window;
global.document = document;
global.sessionStorage = window.sessionStorage;
global.localStorage = window.localStorage;

console.log("--- Loading test_full_client.js ---");
try {
  require("./test_full_client.js");
  console.log("✅ test_full_client.js loaded without top-level throw!");
} catch(err) {
  console.error("❌ Error loading test_full_client.js:", err);
  process.exit(1);
}

console.log("--- Testing bootApplication ---");
try {
  if (typeof window.bootApplication === "function") {
    window.bootApplication();
    console.log("✅ bootApplication ran successfully!");
  } else {
    console.error("❌ bootApplication is NOT a function on window!");
  }
} catch(err) {
  console.error("❌ bootApplication threw error:", err);
}

console.log("--- Testing executeDirectLogin with EMPTY password ---");
try {
  document.getElementById("inpWinUsername").value = "SANISH";
  document.getElementById("inpWinPassword").value = "";
  let res = window.executeDirectLogin({ preventDefault: ()=>{}, stopPropagation: ()=>{} });
  console.log("Empty password result:", res);
  console.log("Error box display:", document.getElementById("winLoginError").style.display);
  console.log("Error box innerHTML:", document.getElementById("winLoginError").innerHTML);
} catch(err) {
  console.error("❌ executeDirectLogin (empty pass) threw error:", err);
}

console.log("--- Testing executeDirectLogin with WRONG password ---");
try {
  document.getElementById("inpWinUsername").value = "SANISH";
  document.getElementById("inpWinPassword").value = "wrongpass123";
  let res = window.executeDirectLogin({ preventDefault: ()=>{}, stopPropagation: ()=>{} });
  console.log("Wrong password result:", res);
  console.log("Error box display:", document.getElementById("winLoginError").style.display);
  console.log("Error box innerHTML:", document.getElementById("winLoginError").innerHTML);
} catch(err) {
  console.error("❌ executeDirectLogin (wrong pass) threw error:", err);
}

console.log("--- Testing executeDirectLogin with CORRECT password 12345 ---");
try {
  document.getElementById("inpWinUsername").value = "SANISH";
  document.getElementById("inpWinPassword").value = "12345";
  let res = window.executeDirectLogin({ preventDefault: ()=>{}, stopPropagation: ()=>{} });
  console.log("Correct password result:", res);
  console.log("Session user:", window.currentUserSession);
  console.log("Overlay display:", document.getElementById("windowsLoginOverlay").style.display);
} catch(err) {
  console.error("❌ executeDirectLogin (correct pass) threw error:", err);
}
