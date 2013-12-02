import subprocess


dalmatianPath = "./dalmatian.sh"
wightPath = "./wight.jar"
expireEntryTime = 96 # Time in hours to expire a file entry saved to the index


def executeCommand(command):
	proc = subprocess.Popen([command], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
	return proc.communicate()

def getIps():
	(out, err) = executeCommand(dalmatianPath + " scan")

	return out.split()

def getTopFolders(ip):
	(out, err) = executeCommand(dalmatianPath + " listu " + ip)
	
	return out.split()

def mount(ip, share):
	print ("mount " + ip + " " + share)
	(out, err) = executeCommand(dalmatianPath + " mount " + ip + " " + share)

def unmount(ip, share):
	(out, err) = executeCommand(dalmatianPath + " umount " + ip + " " + share)

def callWight():
	(out, err) =  executeCommand(wightPath + " -clean 96")
	print (out)

# "./dalmatian.sh list 192.168.78.84

ipcubo = "192.168.78.84"
shares = getTopFolders('192.168.78.84')

for i in xrange(0, len(shares), 4):
	for share in shares[i:i+4] :
		mount(ipcubo, share)

	callWight()

	for share in shares[i:i+4] :
		unmount(ipcubo, share)