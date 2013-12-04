import re
from common import executeCommand

def reverseIp(ip):
	parts = ip.split(".")
	parts.reverse();
	return r"\.".join(parts)


def hostnameResolved(digoutput):
	match = re.search('ANSWER:\s*(\d)', digoutput);
	if (match is None) or (match.lastindex is None):
		print("Could not find 'ANSWER:' in dig -x output")
		return False
	
	return int(match.group(1)) > 0


def hostnameFromDig(digoutput, ip):
	print("searching for hostname of " + ip),
	match = re.search('^' + reverseIp(ip) + '.*PTR\s+(\S+)', digoutput, flags=re.MULTILINE)

	if (match is None) or (match.lastindex is None):
		print("Could not parse dig -x output")
		return ip

	print("\tfound: " + match.group(1))
	return match.group(1)


def getHostname(ip):

	(digoutput, err) = executeCommand("dig -x " + ip);

	if (not hostnameResolved(digoutput)):
		return ip

	return hostnameFromDig(digoutput, ip)
