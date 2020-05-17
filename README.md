## 一款Android Studio插件，帮助你快速添加Google依赖库和查询历史版本
>### 插件还在等待jetbrains审核😭还没通过(可能因为疫情原因，未能及时处理)，所以现在暂时只能通过[本地安装](https://github.com/wuyr/GoogleLibraryVersionQuerier#安装)，抱歉！
### 特性：
#### 1. 编辑build.gradle时，会有代码提示：

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/1.gif)

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/2.gif)

<br/>

#### 2. 光标定位到目标类库所在行，右键可快速查看和替换某个版本：

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/3.gif) ![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/4.gif)

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/5.png)

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/6.png)

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/7.png)

<br/>

### 使用技巧:
3.0版本支持了Maven、Jcenter仓库搜索，如果不加一些条件过滤的话，在编辑gradle文件时的代码提示可能会有很多名字相似的库，像这样：

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/9.png)

遇到这种情况可以在前面加包名信息，格式如下：

**group关键词(包名)**:**artifact关键词(仓库名)**

示例：

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/10.png)

可以看到过滤了group之后的搜索结果精准了很多。

<br/>

### 安装：
**在线安装：**

插件还在等待jetbrains审核😭还没通过(可能因为疫情原因，未能及时处理)，所以现在暂时只能通过本地安装，抱歉！
~~*Android Studio -> Settings -> Plugins -> Marketplace*：<br/>搜索：***Google Library Version Querier***即可~~：

![preview](https://github.com/wuyr/GoogleLibraryVersionQuerier/raw/master/previews/8.png)

**本地安装：**

到 [releases](https://github.com/wuyr/GoogleLibraryVersionQuerier/releases) 里下载最新版的zip，解压后把里面的 *GoogleLibraryVersionQuerier.zip* 拖进Android Studio中并重启。

<br/>

### 数据来源：[wanandroid.com](https://wanandroid.com/maven_pom/index) ， [maven.aliyun.com](https://maven.aliyun.com/mvn/view)
### 感谢[鸿神](https://www.wanandroid.com/)提供技术支持

<br/>

### 更新日志:

 - **3.0** 支持Maven、Jcenter仓库搜索，支持所有.gradle文件。

 - **2.1** 修复请求多次接口Bug。 

 - **2.0** 加入编辑build.gradle文件时的代码提示和支持搜索历史版本。

 - **1.0** 完成搜索和替换Google官方依赖库最新版本。
     
