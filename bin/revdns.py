import re
from common import executeCommand

def reverseIp(ip):
	parts = ip.split(".")
	parts.reverse();
	return r"\.".join(parts)


def hostnameResolved(digoutput):
	match = re.search('ANSWER:\s*(\d)', digoutput);
	return int(match.group(1)) > 0


def hostnameFromDig(digoutput, ip):
	print("searching for hostname of " + ip),
	match = re.search('^' + reverseIp(ip) + '.*PTR\s+(\S+)', digoutput, flags=re.MULTILINE)
	print("\tfound: " + match.group(1))
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

a = """; <<>> DiG 9.8.1-P1 <<>> -x 192.168.72.5
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NXDOMAIN, id: 35864
;; flags: qr aa rd ra; QUERY: 1, ANSWER: 0, AUTHORITY: 1, ADDITIONAL: 0

;; QUESTION SECTION:
;5.72.168.192.in-addr.arpa.	IN	PTR

;; AUTHORITY SECTION:
72.168.192.in-addr.arpa. 38400	IN	SOA	dns.server. redecasd.rede. 2802773367 10800 3600 604800 38400

;; Query time: 0 msec
;; SERVER: 192.168.72.3#53(192.168.72.3)
;; WHEN: Wed Dec  4 02:03:12 2013
;; MSG SIZE  rcvd: 102"""

