#!/usr/bin/env bash

if [ "x${JAVA_HOME}" == "x" ]; then
    echo "JAVA_HOME is not valid"
    exit 1
fi

JAVA="${JAVA_HOME}/bin/java"

if [ ! -f "$JAVA" ]
then
        echo Invalid Java Home detected at ${JAVA_HOME}
        exit 1
fi

FINDNAME=$0
while [ -h $FINDNAME ] ; do FINDNAME=`ls -ld $FINDNAME | awk '{print $NF}'` ; done
RUNDIR=`echo $FINDNAME | sed -e 's@/[^/]*$@@'`
unset FINDNAME

# cd to top level agent home
if test -d $RUNDIR; then
  cd $RUNDIR/..
else
  cd ..
fi

agentHome=`pwd`

liblist=`ls ${agentHome}/lib/`
for lib in $liblist
do
 agent_classpath="${agent_classpath}:${agentHome}/lib/${lib}"
done
agent_class=com.yiji.falcon.agent.Agent

client_cmd="${JAVA} \
	-server -Xms64m -Xmx128m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:SurvivorRatio=4 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m \
	-XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseParNewGC -XX:MaxTenuringThreshold=5 -XX:+CMSClassUnloadingEnabled \
	-XX:+TieredCompilation -XX:+ExplicitGCInvokesConcurrent -XX:AutoBoxCacheMax=20000 \
	-verbosegc  -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:${agentHome}/logs/gc.log \
	-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${agentHome}/logs/oom-${START_DATE}.hprof \
	-Dagent.conf.path=${agentHome}/conf/agent.properties \
	-Dauthorization.conf.path=${agentHome}/conf/authorization.properties \
	-Dagent.quartz.conf.path=${agentHome}/conf/quartz.properties \
	-Dagent.log4j.conf.path=${agentHome}/conf/log4j.properties \
	-Dagent.jmx.metrics.common.path=${agentHome}/conf/jmx/common.properties \
	-Dagent.plugin.conf.dir=${agentHome}/conf/plugin \
	-Dagent.falcon.dir=${agentHome}/falcon \
	-Dagent.falcon.conf.dir=${agentHome}/conf/falcon \
	-cp ${agent_classpath} ${agent_class} $1
"

case $1 in
start)
	nohup $client_cmd > /dev/null 2>&1 &
;;
stop)
	$client_cmd
;;
status)
	$client_cmd
;;
*)
    echo "Syntax: program < start | stop | status >"
esac

