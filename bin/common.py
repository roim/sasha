import subprocess

def executeCommand(command):
	proc = subprocess.Popen([command], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
	return proc.communicate()