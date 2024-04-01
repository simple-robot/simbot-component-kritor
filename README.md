<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
<picture>
  <source media="(prefers-color-scheme: dark)" srcset=".simbot/logo-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset=".simbot/logo.svg">
  <img alt="simbot logo" src=".simbot/logo.svg" width="260" />
</picture>
<h2>
    ~ Simple Robot ~ <br/> <small>Kritor Component</small>
</h2>
<a href="https://github.com/simple-robot/simbot-component-kritor/releases/latest"><img alt="release" src="https://img.shields.io/github/v/release/simple-robot/simbot-component-kritor" /></a>
<a href="https://repo1.maven.org/maven2/love/forte/simbot/component/simbot-component-kritor-api/" target="_blank">
  <img alt="release" src="https://img.shields.io/maven-central/v/love.forte.simbot.component/simbot-component-kritor-api" /></a>
   <hr>
   <img alt="stars" src="https://img.shields.io/github/stars/simple-robot/simbot-component-kritor" />
   <img alt="forks" src="https://img.shields.io/github/forks/simple-robot/simbot-component-kritor" />
   <img alt="watchers" src="https://img.shields.io/github/watchers/simple-robot/simbot-component-kritor" />
   <img alt="repo size" src="https://img.shields.io/github/repo-size/simple-robot/simbot-component-kritor" />
   <img alt="issues" src="https://img.shields.io/github/issues-closed/simple-robot/simbot-component-kritor?color=green" />
   <img alt="last commit" src="https://img.shields.io/github/last-commit/simple-robot/simbot-component-kritor" />
   <a href="./COPYING"><img alt="copying" src="https://img.shields.io/github/license/simple-robot/simbot-component-kritor" /></a>

</div>


Simple Robot Kritor 组件是一个将
[Kritor](https://github.com/KarinJS/kritor)
协议在
[Simple Robot](http://github.com/simple-robot/simpler-robot) 标准API下实现的组件库，
并由此提供simbot中的各项能力。

> [!caution]
> WIP now.

## 使用

## 文档

## Examples

使用simbot核心库

```Kotlin
val application = launchSimpleApplication {
    useKritor() // 安装使用Kritor组件库
}

application.kritorBots {
    // 注册bot并启动
    retister(account = "", ticket = "") {
        // config...
    }.also { it.start() }
}

// 注册事件处理器
application.listeners {
    // 处理事件 ChatGroupMessage
    // 这是simbot API定义的泛用类型
    process<ChatGroupMessageEvent> {
        // ...
    }

    // 指定处理 Kritor 组件内定义的各事件类型
    process<KritorEvent> {
        // ...
    }
}
```

使用Spring Boot starter

配置信息:

```json
{
  "component": "simbot.kritor",
  "auth": {
    "account": "",
    "ticket": "",
    "channel": {
        "type": "address",
        "name": "localhost",
        "port": 8080
    }
  },
  "config": {
  }
}
```

```Kotlin
@SpringBootApplication
@EnableSimbot // 启动simbot
class MainApp

// 默认配置下，simbot会自动扫描并加载配置的bot信息，
// 并默认地自动启动它们

fun main(args: Array<String>) {
    runApplication<MainApp>(*args)
}
```
