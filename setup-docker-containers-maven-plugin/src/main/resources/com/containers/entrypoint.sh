#!/bin/sh

SLEPT=0
DEFAULT_RETRY_INTERVAL=5
DEFAULT_TIMEOUT=600

check_if_service_ready(){
	while keep_trying; do
		echo "Checking if ${1}:${2} is ready . . ."
		if nc -z $1 $2; then
  			echo "Ready"
  			break
  		else
  			sleep $RETRY_INTERVAL
  			SLEPT=$((RETRY_INTERVAL+SLEPT))
  		fi
	done
}

keep_trying(){
	if [ "$TIMEOUT" -gt "$SLEPT" ]; then
		return 0
	else
		echo "Exceeded timeout"
		exit 1
	fi
}

if [ -z "$TIMEOUT" ]; then
	TIMEOUT=$DEFAULT_TIMEOUT
	echo "Timeout not specified. Defaulting to ${TIMEOUT} seconds."
else
    echo "Timeout set to ${TIMEOUT} seconds."
fi

if [ -z "$RETRY_INTERVAL" ]; then
	RETRY_INTERVAL=$DEFAULT_RETRY_INTERVAL
	echo "Retry interval not specified. Defaulting to ${RETRY_INTERVAL} seconds."
else
    echo "Retry interval set to ${RETRY_INTERVAL} seconds."
fi

for var in "$@" ; do
	host=${var%:*}
	port=${var#*:}
	check_if_service_ready $host $port
done