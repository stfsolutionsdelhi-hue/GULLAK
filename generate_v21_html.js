const fs = require('fs');

// We will construct Part 2 (HTML/CSS) and Part 3 (JS) so that each part is concise and well under token limits.
// Let's create:
// 1. Part2_HtmlCss.gs (containing function getV21HtmlPart() and function getV21CssPart())
// 2. Part3_ClientJs.gs (containing function getV21JsPart() and function getCompleteSoftwareHtml())
// All combined together into Code.gs as well.

const userAttachedHtml = fs.readFileSync("Code.gs", "utf8"); // let's see if we have previous
