
const jsdom = require("jsdom");
const { JSDOM } = jsdom;
const fs = require("fs");

const html = fs.readFileSync("Part2_Html.gs", "utf-8");
const tick1 = html.indexOf("`");
const tick2 = html.lastIndexOf("`");
const rawHtml = html.substring(tick1 + 1, tick2);

const dom = new JSDOM(rawHtml, { runScripts: "dangerously" });
const window = dom.window;
const document = window.document;
global.window = window;
global.document = document;
global.localStorage = {
  store: {},
  getItem: function(k){ return this.store[k] || null; },
  setItem: function(k, v){ this.store[k] = v; },
  removeItem: function(k){ delete this.store[k]; }
};
global.sessionStorage = global.localStorage;

try {
  eval(fs.readFileSync("combined_full.js", "utf-8"));
  console.log("Evaluation successful!");
  console.log("Members count:", window.members.length);
  console.log("Total members disp:", document.getElementById("dispTotalMem").innerText);
  console.log("Total RD disp:", document.getElementById("dispTotalRd").innerText);
  console.log("Table rows rendered:", document.querySelectorAll("#tbodyMembers tr").length);
} catch(err) {
  console.error("SIMULATION RUNTIME ERROR:", err);
}
