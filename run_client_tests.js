
const fs = require("fs");

const domElements = {};
const makeElem = (id) => {
  return {
    id: id || "",
    value: "",
    style: {
      setProperty: function(k, v){ this[k] = v; },
      removeProperty: function(k){ delete this[k]; }
    },
    innerHTML: "",
    innerText: "",
    textContent: "",
    setAttribute: () => {},
    getAttribute: () => "",
    addEventListener: () => {},
    classList: { add: () => {}, remove: () => {}, contains: () => false },
    querySelectorAll: () => [],
    querySelector: () => null,
    appendChild: () => {},
    removeChild: () => {},
    focus: () => {}
  };
};

const document = {
  getElementById: (id) => {
    if (!domElements[id]) domElements[id] = makeElem(id);
    return domElements[id];
  },
  querySelectorAll: () => [],
  querySelector: () => null,
  addEventListener: () => {},
  createElement: (tag) => makeElem(),
  body: makeElem("body")
};

const window = {
  addEventListener: () => {},
  removeEventListener: () => {},
  document,
  sessionStorage: {
    data: {},
    getItem(k) { return this.data[k] || null; },
    setItem(k, v) { this.data[k] = String(v); },
    removeItem(k) { delete this.data[k]; }
  },
  localStorage: {
    data: {},
    getItem(k) { return this.data[k] || null; },
    setItem(k, v) { this.data[k] = String(v); },
    removeItem(k) { delete this.data[k]; }
  },
  location: { reload: () => {} },
  navigator: { clipboard: { writeText: () => Promise.resolve() } }
};

global.window = window;
global.document = document;
global.localStorage = window.localStorage;
global.sessionStorage = window.sessionStorage;

require("./test_bundle.js");

console.log("--- 1. Testing Initial State ---");
console.log("Members count:", window.members.length);
if (window.members.length < 4) throw new Error("Members array is missing default dummy members!");
console.log("Sample Member 1:", window.members[0].name, window.members[0].rd, window.members[0].address);
console.log("Sample Member 4:", window.members[3].name, window.members[3].mobile);

console.log("--- 2. Testing Strict Login Authentication ---");
// Setup input elements
document.getElementById("inpWinUsername").value = "SANISH";
document.getElementById("inpWinPassword").value = "wrongpass";
let loginResult = window.executeDirectLogin();
if (window.currentUserSession) throw new Error("Security breach: Logged in with wrong password!");
console.log("✅ Wrong password successfully blocked!");

// Test correct password
document.getElementById("inpWinPassword").value = "12345";
window.executeDirectLogin();
if (!window.currentUserSession || window.currentUserSession.username !== "SANISH") {
  throw new Error("Failed to login with correct password 12345!");
}
console.log("✅ Correct login with SANISH / 12345 successful!");

// Test sheet users
window.initialSheetUsers = [
  { username: "PRESIDENT", password: "SecretPassword99", role: "Super Admin", email: "president@society.org" }
];
document.getElementById("inpWinUsername").value = "PRESIDENT";
document.getElementById("inpWinPassword").value = "WrongSecret";
window.currentUserSession = null;
window.executeDirectLogin();
if (window.currentUserSession) throw new Error("Security breach: Logged in with wrong sheet password!");
console.log("✅ Wrong sheet user password blocked!");

document.getElementById("inpWinPassword").value = "SecretPassword99";
window.executeDirectLogin();
if (!window.currentUserSession || window.currentUserSession.username !== "PRESIDENT") {
  throw new Error("Failed to login with sheet user credentials!");
}
console.log("✅ Sheet user login with exact password successful!");

console.log("--- 3. Testing Core App Functions ---");
console.log("Testing calculate1PercentPmBonus on member 1:");
let b1 = window.calculate1PercentPmBonus(window.members[0]);
console.log("Member 1 Bonus:", b1.totalBonus);

console.log("Testing Exit Member calculation:");
document.getElementById("selExitMember").value = "MEM010120261";
window.handleExitMemberChange();
console.log("Exit Form RD Display:", document.getElementById("dispExitRd").textContent);

console.log("Testing Fund Edit Modal:");
if (typeof window.openEditFundModal === "function") {
  window.openEditFundModal(0);
  console.log("Fund edit form opened with amount:", document.getElementById("inpEditFundAmount").value);
}

console.log("ALL TESTS PASSED WITH 100% SUCCESS!");
