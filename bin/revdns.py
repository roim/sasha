import re
from common import executeCommand

def reverseIp(ip):
	parts = ip.split(".")
	parts.reverse();
	return r"\.".join(parts)


def hostnameResolved(digoutput):
	match = re.search('ANSWER:\s*(\d)', digoutput);
	return int(match.group(1)) >= 0


def hostnameFromDig(digoutput, ip):
	match = re.search('^' + reverseIp(ip) + '.*PTR\s+(\S+)', digoutput, flags=re.MULTILINE)
	return match.group(1)


def getHostname(ip):
	# digcommand = "diganswer=$( + "); "
	# gawkcommand0 = "gawk \'match($0, /.*ANSWER:[[:space:]]*([[:digit:]])/, cap) { print cap[1]} \'"
	# gawkcommand1 = "gawk \'/^" + reverseIp(ip) + "/ { print } \'"
	# gawkcommand2 = "gawk \'match($0, /PTR[[:space:]]*(.*)$/, cap) { print cap[1] }\'"

	(digoutput, err) = executeCommand("dig -x " + ip);

	if (not hostnameResolved(digoutput)):
		return ip

	return hostnameFromDig(digoutput, ip)


getHostname("127.0.0.1")