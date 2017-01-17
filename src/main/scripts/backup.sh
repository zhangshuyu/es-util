#!/bin/sh -

home="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

function classpath() {
    arr=("$( ls $1 )")
    first="1"
    cp=""
    for jar in $arr; do
        if [[ "$first" = "1" ]]; then
            first="0"
            cp="$1/$jar"
        else
            cp="$cp:$1/$jar"
        fi
    done;
    echo $cp;
}
main=com.hansight.es.AppDelete
cp="$( classpath "$home/lib" )"
echo "home is $home"
conf="$home/conf"
logs="$home/logs"
mkdir -p $conf $logs

nohup java -Xmx1G -Xms1G  -cp $conf:$cp $main $@ &
