#!/bin/sh

# A dalmatian is a dog
# But this Dalmatian is a wrapper to unix utilities that find devices connected to a local network,
#   list shares on a device, mont and unmount them.

SELF="$0"
MountPath="./scan"

findMachines(){ # Print IPs of online PCs in given subnet (format: IP/submask length)
    sudo nmap -sn "$1" --privileged | awk 'match($0, /192.([0-9]*\.)+[0-9]*/) {print substr($0, RSTART, RLENGTH)}'
}

list(){         # List Samba Shares in a node
    smbclient -gL \\"$1" --no-pass 2>/dev/null | awk -F\| '/Disk/ && $3=="" {print $2}';
}

listFormated(){ # List Samba Shares in a node in a fashion way
    list "$1" | for FOLDER in $(cat /dev/stdin) ; do echo "$MountPath"/smb/"$1"/$FOLDER/; done
}

mountShare(){   # Mount Samba share in folder
    mkdir --parents "$MountPath"/smb:/"$1"/"$2"
    sudo mount -t cifs \\\\"$1"\\"$2" "$MountPath"/smb:/"$1"/"$2" -o ro,guest
}
umountShare(){  # Unmount Samba share in folder
    sudo umount "$MountPath"/smb:/"$1"/"$2"
    rmdir --ignore-fail-on-non-empty --parents "$MountPath"/smb:/"$1"/"$2"
}

case "$1" in
    install)
        PACKAGES="cifs-utils samba smbclient nmap"
        echo "Dalmatian script is about to install the packages \" "$PACKAGES" \" if not already installed in the system."
        for i in "$PACKAGES"
            do sudo apt-get install $i;
        done
        ;;
    scan)
        findMachines 192.168.72.0/21        
        ;;
    list)  
        listFormated "$2"       
        ;;
    listu)
        list "$2"
        ;;
    mount)
        mountShare "$2" "$3"
        ;;
    umount)
        umountShare "$2" "$3"
        ;;
    *)
        echo "Usage : $SELF [install|scan|list|listu|mount|umount] [list computer]"
        exit 1
        ;;
esac    
