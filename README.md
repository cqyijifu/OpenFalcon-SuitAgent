# OpenFalcon-SuitAgent


### 升级日志

---

**BY：2016年09月23日**

- 交换机监控：
	- 交换机监控添加接口采集配置，防止出现因接口采集数据量过大出现的采集时间过长，导致数据上报问题
    - 接口数据上报类型改为COUNTER	
    - 优化交换机采集的配置格式
    - 优化SNMP采集性能

---

**BY：2016年09月19日**

- Oracle监控：改进优化Oracle的表空间统计
- Docker监控：
    - 优化自动发现`Docker`服务逻辑
    - 只支持本机`Docker`服务监控，配置文件无须配置地址
    - 摒弃`Remote API`的方式，采用`cAdvisor` + `docker command`方式，资源占用率更小，更加稳定。（只支持Linux系统，配置`cAdvisor`启动的监听端口在`dockerPlugin.properties`）
    - 自动探测监控本机的`cAdvisor`服务（`cAdvisor`以`Docker`容器的方式启动，[详情点击](https://github.com/google/cadvisor)），若本机已启动`cAdvisor`服务，则直接连接已启动的`cAdvisor`服务，否则会启动内置的`cAdvisor`服务
    - 不再要求被监控的`Docker`开启`Remote API`功能
- 其他细节优化

---

### 版本说明

本系统版本划分如下

- alpha：内部测试版（不建议使用于生产环境）
- beta：公开测试版（不建议使用于生产环境）
- final：最终正式版（可用于生产环境）

当前版本请查看pom.xml信息。

### 使用之前

- 此系统是和[OpenFalcon](http://book.open-falcon.org/zh/index.html)监控系统一起使用,是为了更方便的进行运维监控。若不了解,可以先点击链接去OpenFalcon的官方社区进行了解。
- 目前此系统仅支持类Unix系统下使用，不支持Windows系统

### 什么是SuitAgent

这是一个获取各种系统的监控数据的Agent。
其中内置了OpenFalcon的社区组件:`FalconAgent`,因所有的监控数据都必须要上报到`FalconAgent`，所以为了部署方便和管理，`SuitAgent`集成了`FalconAgent`，若`agent.falcon.push.url`配置为本地地址,则启动时会同时启动自带的`FalconAgent`，关闭时也会同时关闭`FalconAgent`。

### 为什么有这个项目

OpenFalcon监控系统，是由一系列的组件构成。对于操作系统的监控，官方有FalconAgent，但是对于其他系统的监控，
都是使用脚本或者其他方式进行单独采集，然后将数据上报给FalconAgent系统。
公司为了运维监控的方便和自动化，便有了这个项目，将系统的采集，集中在一个系统中，并且尽可能的采集便捷。

### SuitAgent特点

- 自动探测部署机上的服务,自动监控
- 监控配置动态生效,无需重启
- 能够动态发现部署机上新启动的服务
- 支持`Mock`接口功能,有自动化运维的公司,可利用此特性进行监控自动化开发
- 监控服务全部插件化实现，可自定义开发自己需求的监控服务的插件，只需要开发采集逻辑和插件运行配置。`SuitAgent`可自动发现插件，并根据配置自动运行，无需关心插件的启动。

### 目前支持的监控服务

- Docker（包括Docker内的应用存活情况监控）
- ElasticSearch
- Http
- Logstash
- Mysql
- 交换机
- Ntp
- Oracle
- Ping
- Standalone Jar（Java通过java -jar xxx.jar运行的服务）
- Tcp
- Tomcat
- Zookeeper

### SuitAgent编译

构建工具:maven
Java版本：jdk 1.8

选择对应的版本进行编译:

	- linux-64 : mvn clean package -Plinux-64 -Dmaven.test.skip=true
	- linux64-noJar : mvn clean package -Plinux64-noJar -Dmaven.test.skip=true
	- osx-noJar : mvn clean package -Posx-noJar -Dmaven.test.skip=true

编译后，在target目录下可看到编译后的压缩包，解压缩，改改配置，即可使用

或者直接下载编译后的文件，地址：

	https://yunpan.cn/cMTHpgsQ3e5DV （提取码：9f74）

版本说明:

- linux-64 : linux-64位自带Jre运行环境。不需要目标系统安装java
- osx-noJar : 苹果系统。需要安装java环境
- linux64-noJar : linux-64位系统。需要目标系统安装个java环境


修改相关配置后,直接启动`SuitAgent`即可

*建议：推荐编译使用，因目前`SuitAgent`尚在完善中，代码和配置难免会有所变动。网盘中的文件不能保证随时都是最新的。*

#### SuitAgent配置

- FalconAgent配置文件(配置官方社区的FalconAgent)
	- {SuitAgentHome}/conf/falcon/agent.cfg.json
- SuitAgent配置文件
	- {SuitAgentHome}/conf/agent.properties
- 监控服务授权配置
	- {SuitAgentHome}/conf/authorization.properties
- 监控服务插件配置
	- {SuitAgentHome}/conf/plugin目录下

#### SuitAgent相关命令

- 启动：`./bin/agent.sh start` (使用root用户)
	- 启动日志查看
		- 可通过`tail -f conf/console.log`  
		  观察`SuitAgent`的运行情况
- 关闭：`./bin/agent.sh stop`
- 状态：`/bin/agent.sh status`

### SuitAgent日志

- SuitAgent运行中的日志分为四种：  
  1、console（debug）  
  2、info  
  3、warn  
  4、error  
  每种日志均自动控制日志大小，每到5MB就自动进行日志分割，最多有10个同类文件。既所有的日志文件，最多只会达到200MB，无需担心日志文件过于庞大。

### 监控数据上报
	
#### 公有的Tag
	
	跟OpenFalcon官方社区的思想一致,FalconAgent采集的系统监控信息(如内存,CPU等等一百多种)没有任何tag信息
	其他的业务系统的监控,都会打上tag。`SuitAgent`采集的系统，基本都是业务系统（非Linux操作系统监控信息），所以都会有对应的Tag
	
- tag来区分服务

  例如：  
  
	`service`={value} ：（內建）服务产品名，如tomcat
	
	`service.type`={value} ：（內建）监控服务类型，如jmx,database
	
	`agentSignName`={value} ：（內建）agent提供的标识字符串，如 allUnVariability（代表该服务SuitAgent启动时就不存在），标识服务的占用端口号，服务名等。
	
	`metrics.type`={value} ：（內建）监控值类型，如availability，jmxObjectConf，jmxObjectInBuild，httpUrlConf，sqlConf，sqlInBuild，snmpConnomInBuild，snmpPluginInBuild

- 可用性(`availability`)会自动打上标签： 
 
  `metrics.type`=availability,`service`={value}
  
- 若某个服务有自定义的endPoint（如SNMP V3），则会加上`customerEndPoint=true`的tag

- `Tomcat`监控值会打上`dir={dirName}`的tag，方便同一个物理机启动多个tomcat时，更好的识别具体的tomcat


### SuitAgent Plugin 说明

SuitAgent所有的监控服务都是插件式开发集成

#### 如何自定义插件开发

`SuitAgent`支持的插件一共以下几种：

- JDBC的监控服务，实现`com.yiji.falcon.agent.plugins.JDBCPlugin`接口  
- JMX的监控服务，实现`com.yiji.falcon.agent.plugins.JMXPlugin`接口  
- SNMP V3的监控服务，实现`com.yiji.falcon.agent.plugins.SNMPV3Plugin`接口  
- 探测监控服务，实现`com.yiji.falcon.agent.plugins.DetectPlugin`接口

若要开发自己的监控服务，想好自己的监控服务是哪种类型，参照`SuitAgent`目前已实现的插件结构，
在包`com.yiji.falcon.agent.plugins.plugin`下建立自己的插件目录，将插件类放在该目录中，
然后根据规则，在`src/main/resources_ext/conf/plugin/`目录下建立自己的插件配置文件即可。

### SuitAgent目前集成的监控服务

#### JMX监控服务

##### 特殊的metrics说明
	- 若SuitAgent在启动时，需要进行监控的服务（对应的work配置为true的）未启动，则将会上报一个名为`allUnVariability`的metrics监控指标，值为`0`。tag中有metrics的详情（参考tag命名），代表为该服务全部不可用

##### JMX监控属性组成

JMX监控的属性，由以下三部分组成

- SuitAgent内置的JMX监控属性
	
		- `HeapMemoryUsedRatio` - 堆内存使用比例
		- `HeapMemoryCommitted` - 堆内存已提交的大小
		- `NonHeapMemoryCommitted` - 非堆内存已提交的大小
		- `HeapMemoryFree` - 堆内存空闲空间大小
		- `HeapMemoryMax` - 堆内存最大的空间大小
		- `HeapMemoryUsed` - 堆内存已使用的空间大小
		- `NonHeapMemoryUsed` - 非堆内存已使用的空间大小
	
- JMX 公共的监控属性自定义配置
	- 定义于`conf/jmx/common.properties`文件
- 自定义的监控属性
	- 每个插件自定义的属于自身的监控属性
		
##### Java应用的停机处理说明

正常情况下，若Java应用停机，则它的JMX连接将会不可用，此时，`SuitAgent`将会上报该应用不可用的监控报告，并且，在每一次重新获取监控值时，都会尝试重新连接此应用。

若该应用是被下线了，就是废弃了，那么岂不是会永远上报不可用状态？所以，`SuitAgent`有一个处理机制，在`SuitAgent`启动时，它会记录每一个Java应用的应用路径，如果该应用被发现停机了，它会检查该路径还是否存在有效，如果路径无效，`SuitAgent`将会清除此下线应用的监控信息，就不会上报不可用了。
	
##### 目前支持的监控组件

- zookeeper
- tomcat
- elasticSearch
- logstash
- standaloneJar（单独Jar包运行的应用，如SpringBoot)
	
#### JDBC监控的服务

##### 目前支持的监控组件

		- Oracle
		- Mysql

#### SNMP监控服务

##### 公共的metrics列表

每个接口的Metrics：
	
	- if.HCInBroadcastPkts
	- if.HCInMulticastPkts
	- if.HCInOctets
	- if.HCInUcastPkts
	- if.HCOutBroadcastPkts
	- if.HCOutMulticastPkts
	- if.getIfHCOutUcastPkts
	- if.HCOutOctets
	- if.OperStatus : (接口状态，1 up, 2 down, 3 testing, 4 unknown, 5 dormant, 6 notPresent, 7 lowerLayerDown)

`ping` Metrics：

	- pingAvgTime : Ping延时（正常返回延时，超时返回 -1）
		
##### 交换机（SNMP V3）

说明
	
监控的设备采集信息和采集逻辑主要参考了Falcon社区的swcollector项目，因swcollector不支持SNMP V3协议。  
		[https://github.com/gaochao1/swcollector](https://github.com/gaochao1/swcollector)
		
采集的私有metric列表
	
		- 公共的metrics数据
		- CPU利用率
		- 内存利用率
		
内存和CPU的目前测试的支持设备
	
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

#### 探测监控服务

- HTTP监控

	监控Metrics: 
	
		- availability
		- response.code : 响应状态码
		- response.time : 响应时间 毫秒
		
- Ping监控

	监控Metrics
	
		- availability
		- pingAvgTime : ping的平均延时（当前为每次ping5次，取绝对值）
		- pingSuccessRatio : ping的成功次数占比，如ping了`5次`，只成功返回`4次`，则为`0.8`
			
- TCP（Socket）监控

	监控Metrics : 
	
		- availability
		- response.time : 响应时间 毫秒
	
- Yiji NTP 监控

	监控Metrics
	
		- availability
			- `0`：NTP监控失败（如ntpdate命令执行失败）
			- `1`：NTP监控成功
		- ntpOffset :  ntpdate命令解析的offset值
			
- Docker 监控

	监控Metrics
	
		- availability
			- `0`：Docker daemon 探测失败
			- `1`：Docker daemon 探测成功
		- availability-container
			- 说明：在SuitAgent第一次运行Docker插件时，会将第一次检测到的容器名称保存到内存缓存中，在以后的每一次监控时，会上报内存缓存中的容器的可用性状态
			- `0`：容器已停止运行
			- `1`：容器正在运行
		- availability.container.app
			- 说明：在SuitAgent第一次运行Docker插件时，会将第一次检测到的容器内的进程情况进行缓存到内存中，在以后的每一次监控时，会重新获取容器内的进程情况，若与第一次一致，则为可用
			- `0`：容器内应用运行不正常
			- `1`：容器内应用正常运行
			- `-1`：容器内的应用状态数据获取失败
		- has_cpu : 是否有CPU使用权限
		- total.cpu.usage.rate : CPU总使用率百分比
		- user.cpu.usage.rate : CPU用户级别使用率百分比
		- system.cpu.usage.rate : CPU系统级别使用率百分比
		- has_memory : 是否有内存使用权限
		- mem.size.usage : 当前已使用的内存大小（单位兆）
		- mem.size.cache : 当前已缓存的内存大小（单位兆）
		- mem.usage.rate : 内存已使用与容器总内存百分比
		- mem.cache.rate : 内存缓存与容器总内存的百分比
		- has_network : 是否有网络使用权限
		- net.if.in.bytes : 网络IO流入字节数
		- net.if.in.packets : 网络IO流入包数
		- net.if.in.dropped : 网络IO流入丢弃数
		- net.if.in.errors : 网络IO流入出错数
		- net.if.out.bytes : 网络IO流出字节数
		- net.if.out.packets : 网络IO流出包数
		- net.if.out.dropped : 网络IO流出丢弃数
		- net.if.out.errors : 网络IO流出出错数

### 自动发现功能说明

- zookeeper

	无需配置，可自动发现
	
- tomcat

	无需配置，可自动发现
	
- elasticSearch

	无需配置，可自动发现
	
- logstash

	无需配置，可自动发现
	
- standalone应用(java -jar 方式的Jar包运行)

	需要配置`conf/plugin/standaloneJarPlugin.properties`配置文件的`jmxServerName`或`jmxServerDir`，
	然后SuitAgent会自动发现已配置的standalone应用
	
- Oracle

	需要配置`conf/authorization.properties`配置中的Oracle连接信息，然后会根据配置的连接信息，进行自动发现Oracle应用，并监控
	
- Mysql

	需要配置`conf/authorization.properties`配置中的Mysql连接信息，然后会根据配置的连接信息，进行自动发现Mysql应用，并监控
	
- SNMP V3 交换机

	需要配置`conf/authorization.properties`配置中的交换机的SNMP V3的连接信息，然后会根据配置的连接信息，进行自动发现交换机并监控。
	
- HTTP监控

	只要配置了被探测的地址，就会触发监控服务
	
- Ping监控

	只要配置了被探测的地址，就会触发监控服务
	
- TCP监控

	只要配置了被探测的地址，就会触发监控服务
	
- NTP监控

	只要配置了NTP服务器地址，就会触发监控服务
	
- Docker监控

	无需配置地址，自动探测本机的`Docker`服务：若发现有命令`/usr/bin/docker`存在，就会启动`Docker`监控插件。并自动连接本机的`cAdvisor`服务或启动内置的`vAdvisor`服务

### SuitAgent 动态配置

`SuitAgent`支持部分配置的动态生效，支持的范围见如下说明

`authorization.properties`文件的改动

- 若对应插件未启动，则文件修改后，将在SuitAgent下一次的自动服务发现时生效。
- 若对应的插件已启动，因系统不会重复启动相同的监控服务，故虽然插件配置会生效，但是不会重新启动服务
	
`plugin`目录下的插件配置文件的改动

- 若改动的是未启动的监控服务配置（如`StandaloneJarPlugin`插件的`standaloneJarPlugin.properties`文件，添加了一个服务名。或更改了插件启动类型等），将在`SuitAgent`下一次的自动服务发现时生效
- 若改动的是插件的监控配置（如`Tomcat`插件的`tomcatPlugin.properties`文件的服务器监控参数配置），下一次监控扫描就能够生效。
- 若改动的是插件的自定义配置文件，它的改动将不会触发插件的配置更新事件，不过可以利用改动它的插件配置文件，触发配置更新。
	
注意

- 已启动的插件服务，不会因为配置文件的改动而停止服务
- 插件启动时的配置项（如插件的`step`，`pluginActivateType`等）被改动时，若插件已启动，虽然改动的配置插件会实时更新，但由于服务已启动，这些属性已经在启动时固定，所以将不会因为改动而生效


### SuitAgent mock 接口

接口说明

`SuitAgent`提供可用性(`availability`)的`mock`服务。

`mock`服务具有有效性,有效性时长,通过配置 `agent.properties` 文件的 `agent.mock.valid.time` 配置项

有效的`mock`,将会让`mock`的目标服务即使已经停止运行,也会上报一个可用的监控数据,并且带上 `mock=true` 的 `tag`

若`mock`的时间超时,既无效,则会上报目标服务不可用的监控数据,并且带上 `mock=timeout-{time}` 的 `tag` 。其中`{time}`是停机时间

接口使用

- http://ip:port/mock/list

	- 查看`SuitAgent`当前所有的mock配置，返回json格式的数据,示例:
		
```
{
	"jmx": {
	  "service": "tomcat",
	  "isTimeout": false,
	  "shutdownTime": 0
	}
}
```
        
	
- http://ip:port/mock/add/`serviceType`/`service`

	- 添加一个mock配置
	
- http://ip:port/mock/remove/`serviceType`/`service`

	- 删除一个mock配置
		
参数说明

- `serviceType` : 对应于监控值`tag`中的`service.type`属性	

- `service` : 对应于监控值`tag`中的`service`或`agentSignName`属性


