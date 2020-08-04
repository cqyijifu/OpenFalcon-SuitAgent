
# 本仓库已停止维护，新仓库地址：[https://github.com/DevopsJK/SuitAgent](https://github.com/DevopsJK/SuitAgent)

---
## 下载地址：[SuitAgent](https://github.com/DevopsJK/SuitAgent/releases/tag/16.2)

## OpenFalcon-SuitAgent

## 注意，此版本的suitAgent上报到plus版本的agent可能会出现很多不可用的监控指标数据

新版的Falcon-Plus已经放出，为适应新版的平台，并进行相关tag的优化（12.x版本的tag值，上报给plus版本的agent，会有一些问题，所以tag值会变化。），由于tag的改动，对已有的监控采集指标有很大的混淆影响，顾此地址上的版本不再更新（13.0之前的版本），此版本的升级包也不再更新（因为不再维护原来的更新包的库，并且13.0版本以后的更新包的默认路径以及局域网升级工具的更新包下载地址均已改变）。
### 适用于plus版本的位置：[https://github.com/DevopsJK/SuitAgent](https://github.com/DevopsJK/SuitAgent)

如果现在正在使用12.X版本的，并且大规模应用的，因tag值会变化，酌情选择是否更新到13.x版本。

tag变动：
删除`metrics.type`标签
`service.type`改为`serviceType`

### [升级日志](https://github.com/DevopsJK/SuitAgent/wiki/updateLog)
### [文档](https://github.com/DevopsJK/SuitAgent/wiki)
### [局域网升级工具](https://github.com/DevopsJK/SuitAgentUpdateTool)
