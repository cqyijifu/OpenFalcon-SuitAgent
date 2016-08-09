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

agent_classpath="${agentHome}/lib/falcon-agent-1.0.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/commons-logging-1.2.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/commons-lang-2.6.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/json-20140107.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/log4j-1.2.12.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/quartz-2.2.1.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/slf4j-api-1.7.5.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/slf4j-log4j12-1.7.5.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/tools.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/ojdbc6-11.2.0.3.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/jyaml-1.3.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/fastjson-1.2.11.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/guava-18.0.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/snmp4j-2.5.0.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/mysql-connector-java-5.1.38.jar"
agent_classpath="${agent_classpath}:${agentHome}/lib/http-request-6.0.jar"
agent_class=com.yiji.falcon.agent.Agent

client_cmd="${JAVA} \
	-server -Xms128m -Xmx128m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=4 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m \
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

