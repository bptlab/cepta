import re
import sys
if len(sys.argv) < 2:
    print("No regex")
    sys.exit(1)
regex = sys.argv[1]
# print("Regex is: ", regex)
matches = []
for line in sys.stdin:
    # print("Checking line: ", line)
    line_matches = re.finditer("(?=(" + regex + "))", line)
    matches += [match.group(1) for match in line_matches]
if len(matches) < 1:
    print("No matches")
    sys.exit(1)
print(matches[-1])