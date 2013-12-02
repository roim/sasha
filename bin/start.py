import subprocess

def executeCommand(command):
	proc = subprocess.Popen([command], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
	return proc.communicate()

def getIps():
	(out, err) = executeCommand("./dalmatian.sh scan")

	return out.split()

def getTopFolders(ip):
	(out, err) = executeCommand("./dalmatian.sh listu " + ip)
	
	return out.split()

def mount(ip, share):
	print ("mount " + ip + " " + share)
	(out, err) = executeCommand("./dalmatian.sh mount " + ip + " " + share)

def unmount(ip, share):
	(out, err) = executeCommand("./dalmatian.sh umount " + ip + " " + share)

def callWight():
	(out, err) =  executeCommand("./wight.jar")
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