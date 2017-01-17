#!/bin/sh

function run(){
 length=${#period}
 unit=${period:`expr $length - 1`:$length}
 value=${period:0:`expr $length - 1`}
 case $unit in
  s) echo "[cspec] unit is m, $value"
     ;;
  m) value=`expr $value \* 60`
     echo "[cspec] unit is m, $value"
     ;;
  h) value=`expr $value \* 60 \* 60`
     echo "[cspec] unit is h, $value"
     ;;
  *) value=${period}
     echo "[cspec] unit is none, $value"
     ;;
 esac
 while true
 do
  # move es data to sftp server
  backup/bin/backup.sh -c sen_cspec_cluster -n 10.1.3.10:9300 -i sentiment -t technology,country_classify,doc_classify,dictionary -bp $localPath'/backup'
  # export es data
  import/bin/import.sh -p import -c sen_cspec_cluster -n 10.1.3.10:9300 -tc java -r /opt/cspec/sftp/es/data -cmd o
  sleep $value
 done
}

period=$1

echo "$period"

base="$( cd "$( dirname "${BASH_SOURCE[0]}" )/" && pwd )"
localPath=$base'/data'
echo "$localPath"
run
