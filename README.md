
# Falcon-yiji


## 自定义Agent监控 Yiji-Falcon-Agent

- Falcon Agent的集成
	- 因所有的监控数据都必须要上报到Falcon Agent，所以为了部署方便和管理，此Agent集成了Falcon Agent，启动时会同时启动自带的Falcon Agent，关闭时也会同时关闭Falcon Agent。
- Agent命令
	- 启动：./bin/agent.sh start
		- 启动日志查看
			- 可通过  
			  tail -f conf/console.log  
			  观察agent的运行情况
	- 关闭：./bin/agent.sh stop
	- 状态：./bin/agent.sh status
- Agent日志
	- Agent运行中的日志分为四种：  
	  1、console（debug）  
	  2、info  
	  3、warn  
	  4、error  
	  每种日志均自动控制日志大小，每到5MB就自动进行日志分割，最多有10个同类文件。既所有的日志文件，最多只会达到200MB，无需担心日志文件过于庞大。
- endpoint命名
	- 见agent.properties的endpoint配置
- metrics命名
	- 若属性文件配置了alias，则为alias
- tag命名
	- tag来区分服务  
	  例如：  
	  **service={value}** - （內建）服务产品名，如tomcat  
	  **service.type={value}** - （內建）监控服务类型，如jmx,database  
	  **agentSignName={value}** - （內建）agent提供的标识字符串，如 allUnVariability（代表该服务agent启动时就不存在）**，**标识服务的占用端口号，服务名等，  
	  **metrics.type={value}** - （內建）监控值类型，如availability，jmxObjectConf，jmxObjectInBuild，httpUrlConf，sqlConf，sqlInBuild，snmpConnomInBuild，snmpPluginInBuild
	- 可用性(availability)会自动打上标签：  
	  **metrics.type=availability,service={value}**
	- 若某个服务有自定义的endPoint（如SNMP V3），则会加上**customerEndPoint=true**的tag
	- Tomcat监控值会打上dir={dirName}的tag，方便同一个物理机启动多个tomcat时，更好的识别具体的tomcat
- agent-plugin
	- Agent所有的监控服务都是插件式开发集成
	- 插件开发
		- JDBC的监控服务，实现JDBCPlugin接口  
		  JMX的监控服务，实现JMXPlugin接口  
		  SNMP的监控服务，实现SNMPV3Plugin接口  
		  探测监控服务，实现DetectPlugin接口
- 监控服务
	- 服务说明
		- JMX监控
			- Agent JMX连接获取方式
				- 与jconsole本地连接获取的相同方式，无需配置jmx连接，只需要指定对应的jmx的serverName即可，支持单机多实例的监控
			- 特殊的metrics说明
				- 若agent在启动时，需要进行监控的服务（对应的work配置为true的）未启动，则将会上报一个名为**allUnVariability**的metrics监控指标，值为0。tag中有metrics的详情（参考tag命名），代表为该服务全部不可用
			- JMX监控属性组成
				- Agent内置的JMX监控属性
					- HeapMemoryUsedRatio - 堆内存使用比例
					- HeapMemoryCommitted - 堆内存已提交的大小
					- NonHeapMemoryCommitted - 非堆内存已提交的大小
					- HeapMemoryFree - 堆内存空闲空间大小
					- HeapMemoryMax - 堆内存最大的空间大小
					- HeapMemoryUsed - 堆内存已使用的空间大小
					- NonHeapMemoryUsed - 非堆内存已使用的空间大小
				- JMX 公共的监控属性自定义配置
					- 定义于conf/jmx/common.properties文件
				- 自定义的监控属性
					- 每个插件自定义的属于自身的监控属性
			- 目前支持的监控组件
				- zookeeper
				- tomcat
				- elasticSearch
				- logstash
				- yijiBoot应用
		- JDBC监控
			- 目前支持的监控组件
				- Oracle
				- Mysql
		- SNMP监控
			- 公共的metrics列表
				- IfHCInOctets
				- IfHCOutOctets
				- IfHCInUcastPkts
				- IfHCOutUcastPkts
				- IfHCInBroadcastPkts
				- IfHCOutBroadcastPkts
				- IfHCInMulticastPkts
				- IfHCOutMulticastPkts
				- IfOperStatus(接口状态，1 up, 2 down, 3 testing, 4 unknown, 5 dormant, 6 notPresent, 7 lowerLayerDown)
				- Ping延时（正常返回延时，超时返回 -1）
			- 交换机（SNMP V3）
				- 说明
					- 监控的设备采集信息和采集逻辑主要参考了Falcon社区的swcollector项目，因swcollector不支持SNMP V3协议。  
						[https://github.com/gaochao1/swcollector](https://github.com/gaochao1/swcollector)
				- 采集的私有metric列表
					- 公共的metrics数据  
					  ****
					- CPU利用率
					- 内存利用率
				- 内存和CPU的目前测试的支持设备
					- Cisco IOS(Version 12)
					- Cisco NX-OS(Version 6)
					- Cisco IOS XR(Version 5)
					- Cisco IOS XE(Version 15)
					- Cisco ASA (Version 9)
					- Ruijie 10G Routing Switch
					- Huawei VRP(Version 8)
					- Huawei VRP(Version 5.20)
					- Huawei VRP(Version 5.120)
					- Huawei VRP(Version 5.130)
					- Huawei VRP(Version 5.70)
					- Juniper JUNOS(Version 10)
					- H3C(Version 5)
					- H3C(Version 5.20)
					- H3C(Version 7)
		- 探测监控
			- HTTP监控
				- 监控Metrics
					- availability
			- Ping监控
				- 监控Metrics
					- availability
					- pingAvgTime
	- 自动发现服务监控说明
		- zookeeper
			- 无需配置，可自动发现
		- tomcat
			- 无需配置，可自动发现
		- elasticSearch
			- 无需配置，可自动发现
		- logstash
			- 无需配置，可自动发现
		- yijiBoot应用
			- 需要配置conf/plugin/yijiBootPlugin.properties配置文件的jmxServerName，然后Agent会自动发现已配置的yijiBoot应用
		- Oracle
			- 需要配置conf/authorization.properties配置中的Oracle连接信息，然后会根据配置的连接信息，进行自动发现Oracle应用，并监控
		- Mysql
			- 需要配置conf/authorization.properties配置中的Mysql连接信息，然后会根据配置的连接信息，进行自动发现Mysql应用，并监控
		- SNMP V3 交换机
			- 需要配置conf/authorization.properties配置中的交换机的SNMP V3的连接信息，然后会根据配置的连接信息，进行自动发现交换机并监控。
		- HTTP监控
			- 只要配置了被探测的地址，就会触发监控服务
		- Ping监控
			- 只要配置了被探测的地址，就会触发监控服务
	- 配置动态生效
		- Agent支持部分配置的动态生效，支持的范围见如下说明
			- authorization.properties文件的改动
				- 若对应插件未启动，则文件修改后，将在Agent下一次的自动服务发现时生效。
				- 若对应的插件已启动，因系统不会重复启动相同的监控服务，故虽然插件配置会生效，但是不会重新启动服务
			- plugin目录下的插件配置文件的改动
				- 若改动的是未启动的监控服务配置（如YijiBoot插件的yijiBootPlugin.properties文件，添加了一个服务名。或更改了插件启动类型等），将在Agent下一次的自动服务发现时生效
				- 若改动的是插件的监控配置（如Tomcat插件的tomcatPlugin.properties文件的服务器监控参数配置），下一次监控扫描就能够生效。
				- 若改动的是插件的自定义配置文件，它的改动将不会触发插件的配置更新事件，不过可以利用改动它的插件配置文件，触发配置更新。
			- 注意
				- 已启动的插件服务，不会因为配置文件的改动而停止服务
				- 插件启动时的配置项（如插件的step，pluginActivateType等）被改动时，若插件已启动，虽然改动的配置插件会实时更新，但由于服务已启动，这些属性已经在启动时固定，所以将不会因为改动而生效
- 监控配置
	- Falcon Agent配置
		- conf/falcon/agent.cfg.json
	- Agent配置
		- conf/agent.properties
	- 授权配置
		- conf/authorization.properties
	- 插件配置
		- conf/plugin目录下
- Q & A
	- 为什么有时候JMX应用启动了，但是Agent总是报无法获取JMX连接
		- 如果JMX应用启动时用的用户（如root或非root）和Agent启动时用的用户身份不一样，可能会出现无法获取JMX连接。目前发现，在ubuntu和Mac系统上，用root身份启动Agent，也出现无法获取用非root身份启动的elasticSearch应用的JMX连接。  
		    
		  **解决方案**：用与应用相同的用户身份再启动一个新的Agent实例，然后进行对该应用的单独监控。需要注意重新制定Agent的监听端口和应用监控的work配置

## 监控自动化

- 监控方案
	- **对于****Falcon****自带的****Agent****监控的值，如****cpu.idle****等，没有任何****tag****信息，用****hostGroups + Templates****的方式进行监控管理**  
	  ****
		- 快速入门 | Open-Falcon  
			[http://book.open-falcon.org/zh/usage/getting-started.html](http://book.open-falcon.org/zh/usage/getting-started.html)
		- Tag和HostGroup | Open-Falcon  
			[http://book.open-falcon.org/zh/philosophy/tags-and-hostgroup.html](http://book.open-falcon.org/zh/philosophy/tags-and-hostgroup.html)
	- **对于自己开发的****Agent****监控的值，可以定义****tag****规则进行上报，用****Expressions****的方式进行监控管理**
- 自动化发布系统部署应用时自动监控
	- 1、自动部署Agent
		- 部署Agent
		- 修改Agent配置
			- tomcat应用：监控tomcat，可自动发现  
			  yiji-boot应用：配置启动类main方法
		- 启动Agent
	- 2、自动配置报警
		- 自动添加应用报警对象（用户、通讯组）
		- 自动配置Expression监控表达式，匹配对应的通讯组
- 应用自动化部署，监控自动进行监控停止和恢复
	- 操作：  
	  应用停止时，暂停对应的Expression监控表达式  
	  应用启动后，开启对应的Expression监控表达式  
	  应用删除后，删除对应的Expression监控表达式，用户，通讯组
		- 直接操作数据库，hbs每分钟从DB中load各种数据，处理后放到内存里，静待agent、judge的请求
