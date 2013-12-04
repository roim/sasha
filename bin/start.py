import logging
from common import *
from datetime import datetime
import revdns, time


dalmatianPath = "./dalmatian.sh"
wightPath = "./wight.jar"
lunaPath = "./luna.jar"
expireEntryTime = 96 # Time in hours to expire a file entry saved to the index
refreshHostTime = 5 # Time in hours to rescan a host
minimumNmapTime = 5 # Minimum time in minutes between full nmaps (getIps) 




def getIps():
	scancmd = dalmatianPath + " scan"
	(out, err) = executeCommand(scancmd)
	return out.split()

def getTopFolders(ip):
	listucmd = dalmatianPath + " listu " + ip
	(out, err) = executeCommand(listucmd)
	return out.split()

def mount(ip, share):
	mountcmd = dalmatianPath + " mount " + ip + " " + share
	(out, err) = executeCommand(mountcmd)

def unmount(ip, share):
	umountcmd = dalmatianPath + " umount " + ip + " " + share
	(out, err) = executeCommand(umountcmd)

def callWight():
	logging.info("Calling wight")
	(out, err) =  executeCommand(wightPath + " -clean " + str(expireEntryTime))
	logging.debug("out: " + out)
	logging.debug("err: " + err)
	logging.info("Finished wight")

def startLuna():
	executeParallelCommand(lunaPath)
	logging.info("Started luna")


def cycleIps():
	logging.info("Starting a full cycle through all ips")
	for ip in getIps():
		hostname = revdns.getHostname(ip)

		if hostname in timeScannedHost:
			if ( (datetime.now() - timeScannedHost[hostname]).total_seconds() < refreshHostTime*3600 ) :
				continue

		scanHost(hostname)


def scanHost(hostname):
	timeScannedHost[hostname] = datetime.now()
	shares = getTopFolders(hostname)

	logging.info("Found " + str(len(shares)) + " shares at " + hostname)

	if (len(shares) == 0):
		return

	logging.info("Starting mounting and indexing")

	for i in xrange(0, len(shares), 4):

		for share in shares[i:i+4]:
			mount(hostname, share)

		logging.debug("")
		callWight()

		for share in shares[i:i+4]:
			unmount(hostname, share)

	logging.info("Finished indexing all shares in " + hostname)

if __name__ ==  "__main__":
	print("If you want to start the http server, you should also start \'sasha.py\' under client/")
	logging.basicConfig(level=logging.DEBUG)
	startLuna()
	# startClient()
	timeScannedHost = {}
	while(True):
		timeLastCycle = datetime.now()

		cycleIps()

		secondsDelta = (datetime.now() - timeLastCycle).total_seconds()
		if ( secondsDelta < minimumNmapTime*60):
			time.sleep(minimumNmapTime*60 - secondsDelta)
