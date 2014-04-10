from common import *
from datetime import datetime
import revdns, time


dalmatianPath = "./dalmatian.sh"
wightPath = "java -jar ./wight.jar"
lunaPath = "java -jar ./luna.jar"
expireEntryTime = 96 # Time in hours to expire a file entry saved to the index
refreshHostTime = 5 # Time in hours to rescan a host
minimumNmapTime = 5 # Minimum time in minutes between full nmaps (getIps) 


def getIps():
	(out, err) = executeCommand(dalmatianPath + " scan")
	return out.split()

def getTopFolders(ip):
	(out, err) = executeCommand(dalmatianPath + " listu " + ip)
	shares = out.split("\n")
	# Split \n returns sometimes returns empty strings
	shares.remove("")
	return shares

def mount(ip, share):
	print ("mount " + ip + " " + share)
	(out, err) = executeCommand(dalmatianPath + " mount " + ip + " \"" + share + "\"")

def unmount(ip, share):
	(out, err) = executeCommand(dalmatianPath + " umount " + ip + " \"" + share + "\"")

def callWight():
	print ("Starting WIGHT")
	(out, err) =  executeCommand(wightPath + " -clean " + str(expireEntryTime))
	print(out)
	print ("Exiting WIGHT")

def startLuna():
	executeParallelCommand(lunaPath)


def cycleIps():
	for ip in getIps():
		hostname = revdns.getHostname(ip)

		if hostname in timeScannedHost:
			if ( (datetime.now() - timeScannedHost[hostname]).total_seconds() < refreshHostTime*3600 ) :
				continue

		scanHost(hostname)


def scanHost(hostname):
	timeScannedHost[hostname] = datetime.now()
	shares = getTopFolders(hostname)
	print ("Found " + str(len(shares)) + " top folders at " + hostname)

	for i in xrange(0, len(shares), 4):

		for share in shares[i:i+4]:
			mount(hostname, share)

		callWight()

		for share in shares[i:i+4]:
			unmount(hostname, share)

if __name__ ==  "__main__":
	print("If you want to start the http server, you should also start \'sasha.py\' under client/")
	startLuna()
	# startClient()
	timeScannedHost = {}
	while(True):
		timeLastCycle = datetime.now()

		cycleIps()

		secondsDelta = (datetime.now() - timeLastCycle).total_seconds()
		if ( secondsDelta < minimumNmapTime*60):
			time.sleep(minimumNmapTime*60 - secondsDelta)
