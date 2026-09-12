# Complete Builder and Verifier for Version 38 (Clean Master)
import os, sys, json, re

print("Starting V38 Clean Master builder...")

# Read the original V38 components from /tmp/v38
with open("/tmp/v38/Part1_Server.gs", "r", encoding="utf-8") as f:
    part1_server = f.read()

with open("/tmp/v38/Part2_Html.gs", "r", encoding="utf-8") as f:
    part2_html = f.read()

with open("/tmp/v38/Part3A.gs", "r", encoding="utf-8") as f:
    part3a_js = f.read()

with open("/tmp/v38/Part3B.gs", "r", encoding="utf-8") as f:
    part3b_js = f.read()

print(f"Loaded: Part1={len(part1_server)}, Part2={len(part2_html)}, Part3A={len(part3a_js)}, Part3B={len(part3b_js)}")
