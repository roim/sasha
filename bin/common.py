import subprocess, logging


def logCmd(cmd):
	logging.debug("Running \'" + cmd + "\'")

def executeCommand(command):
	proc = subprocess.Popen([command], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
	logCmd(command)
	return proc.communicate()

def executeParallelCommand(command):
	proc = subprocess.Popen([command], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
	return proc

