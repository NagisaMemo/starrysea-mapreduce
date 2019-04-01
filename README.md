# starrysea-mapreduce

## 简介

MapReduce 是一个可应用于大规模数据集的并行运算的软件架构。Map（映射）函数用来把一组键值对映射成一组新的键值对（在这里指存储为文件），Reduce（归纳）函数指的是对一个列表的元素进行适当的合并（在这里指对文件进行分析）。有关 MapReduce 的更多信息，请参阅[维基百科](https://zh.wikipedia.org/wiki/MapReduce)。<br>
这是 StarrySea 针对 QQ 群聊天记录的数据分析推出的基于 Java 的 MapReduce 框架。可以实现按发言日期、发言人等的发言记录分类及统计功能。您也可以对其进行改造以适用其他类型的数据。

## 快速入门

### 支持的格式

目前支持分析**群聊**记录。每条记录看上去应该是这样子的：<br>
>2017-07-20 9:50:56 小明(10000)<br>
这是一条消息。<br>
<br>

这是使用 QQ 号或手机登录的用户的发言。或者是<br>
>2017-07-19 21:47:19 小红\<someone@example.com><br>
这也是一条消息。<br>
<br>

这是使用邮箱登录的用户的发言。

### 将该框架添加到您的项目中

可以使用 Gradle 将其添加到您的项目中。<br>
先在 build.gradle 中添加 JitPack 仓库：
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
再添加依赖支持：
```groovy
dependencies {
	implementation 'com.github.StarSeaProject:starrysea-mapreduce:1.4.3'
}
```
1.4.3 为撰写此自述文件时的最新版本。您需要确认当前最新版本号并将上述内容中的版本号手动更改为最新。之后重新执行 Gradle Build。

### 在类中导入该框架
添加该语句：
```java
import top.starrysea.mapreduce.*;
```
或者使用您的 IDE 的“自动导入”功能。

## 使用说明

此处指导您进行简单的 MapReduce 应用。

### 创建 Mapper

创建一个继承自 `Mapper` 的类。为了方便之后的操作，请添加 `@Component` 注解。
您需要重写2个方法：`map` 和 `outputFileSubType`。<br>
<br>
`map` 方法的写法如下：
```java
@Override
protected MapReduceContext map(SingleMessage singleMessage, MapReduceContext context) {
	return context.write(/*该部分为分组方式*/, singleMessage);
}
```
`context.write` 方法的第一个参数为一字符串，可以称之为分组方式。按照指定的方式去将大文件分成许多小文件。如 `singleMessage.getYear() + "-" + singleMessage.getMonth() + "-" + singleMessage.getDay()` 将按照 "yyyy-MM-dd" 的方式分离每日的记录，而 `singleMessage.getId()` 可以按照发言人分割记录。<br>
`singleMessage` 为一条发言记录的类 `SingleMessage` 的对象，包含该条发言蕴含的基本信息。
<br>
`outputFileSubType` 方法写法如下：
```java
@Override
protected String outputFileSubType() {
	return "byWhat"/*此处需返回一个字符串*/;
}
```
该方法需返回一个字符串，该字符串将作为本次 map 结果存储的目录的名称。

### 创建 Reducer

对于简单计数型的分析，我们提供了 `LongReducer` 类，您需要创建一个继承自它的类。同样加上 `@Component` 注解。<br>
您需要重写2个方法：`reduce` 和 `reduceFinish`。<br>
<br>
`reduce` 方法的主要功能为对刚才 map 的文件进行分析。<br>
以下示例可对一个 map 后的文件进行发言记录的计数，稍后将用一个 `Map<String, Long>` 来存储该结果，使用文件名作为其键名。
```java
@Override
protected ReduceResult<Long> reduce(File path) {
	long count = 0;
	try (Stream<String> line = Files.lines(path.toPath())) {
		count = line.count();
	} catch (IOException e) {
		logger.error(e.getMessage(), e);
	}
	String group = path.getName();
	date = group.substring(0, group.lastIndexOf('.'));
	return ReduceResult.of(group, count);
}
```
由于 map 后一行为一个记录，只计算行数即可得到记录数。返回值 `ReduceResult.of(group, count)` 相当于一个将数据存入`Map<String, Long>`中的操作。<br>
<br>
`reduceFinish` 方法中可以获取到 map 结果并进行下一步操作。`reduceResult` 即为所有 reduce 结果的 `Map<String, Long>`。
```java
@Override
protected void reduceFinish(Map<String, Long> reduceResult, MapReduceContext context) {
	/*进行后续操作*/
	}
```
是将其存入数据库？还是保存到文件中？抑或是直接在控制台输出？接下来的操作由您决定。<br>

### 统计复读

您可能会有这种需求，查看哪些内容启动了群内的复读机。因此我们定义了 `MapLongReducer` 类用于存储消息内容和复读次数。此时结果Map中的值类型不是 `Long`，而是`Map<String, Long>`。<br>
统计复读的示例会稍显复杂,首先需要导入记录，同时筛掉掉图片和表情:
```java
Map<String, Long> result = new HashMap<>();
try (Stream<String> line = Files.lines(path.toPath())) {
	result = line.map(SingleMessage::stringToMessage).filter(singleMessage -> !singleMessage.getBody().equals("[图片]") || !singleMessage.getBody().equals("[表情]")).map(SingleMessage::getBody).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
} catch (IOException e) {
	logger.error(e.getMessage(), e);
}
```
`SingleMessage` 类还提供了将记录反转为对象的方法，需要在这里使用。此处的`result` 为每条记录的发言内容和发言次数。<br>
接下来筛去只有一条的记录:
```java
Map<String, Long> theResult = new HashMap<>();
for (Map.Entry<String, Long> entry : result.entrySet()) {
	if (entry.getValue() != 1L) {
		theResult.put(entry.getKey(), entry.getValue());
	}
}
```
您得到了 `theResult`，里面包含了重复2次及以上的信息。<br>
按照和 `LongReducer` 同样的方法进行分组:
```java
String group = path.getName();
	date = group.substring(0, group.lastIndexOf('.'));
	return ReduceResult.of(group, theResult);
```

### 对 Mapper 和 Reducer 进行配置

新建一个类并使用 `@Configuration` 注解该类，以及在该类中使用 `@Autowired` 注解添加所有的 mapper 和 reducer。<br>
您还需要一个对其进行配置的方法，示例如下。
```java
@Bean
public StarryseaMapReduceManager getStarryseaMapReduceManager() {
	StarryseaMapReduceManager s = new StarryseaMapReduceManager(StarryseaMapReduceConfiguration.of().input(inputPath).output(outputPath));
	s.register(myMapper, myReducer1, myReducer2);
	s.run();
	return s;
	}
```
`inputPath` 和 `outputPath` 为两个字符串，分别对应 mapper 的文件输入和输出目录，需要您来设置，请设为**绝对路径**。<br>
`register`方法用于注册对应的 mapper 和 reducer，使该 mapper 输出的文件可以被后面的 reducer 使用。一次只能注册一个 mapper，但是可以注册多个 reducer。<br>
您也可以不注册 reducer，只用 mapper 分割文件。

### 测试

在输入文件夹中放入**合法的**聊天记录文件，您会看到处理好的文件放入输出目录中，并自动进行 reduce 及进一步的操作。<br>
**注意： 放入其他文件可能会导致出现错误的结果。**

## 架构设计

### 分析过程

可以使用正则表达式来提取每条聊天记录的开头，而每个开头中间的便是正文信息。据此可以抽象出一个类，包含开头和正文。开头包含了诸如发言时间和发言人之类的信息，可以进一步提取这些信息出来。这些操作放在了 `Mapper` 类中，抽离出来的类即为 `SingleMessage`。

### Mapper

`Mapper` 根据之前设定的输入和输出目录创建文件夹，并使用 `WatchService` 监听输入文件夹，有文件动作之后会对该文件进行分析，获得 `SingleMessage` 对象，并将它们输出至输出文件夹。

### Reducer

`Reducer` 负责的操作为统计每一个文件的发言计数并将它们归纳起来进行下一步操作。归纳依据为分组，即使用指南中的`ReduceResult.of(group, count)`。相同的组会将结果相加，最终得出 reduce 结果。<br>
对于 `MapLongReducer`，相同的组会将结果合并。

### Context

`MapReduceContext` 管理 map 文件的写入操作和一些常量的定义操作。在 map 过程中同样使用了分组的思维。即使用指南中的 `context.write` 方法。组相同的记录会被放在一个文件中。

### ReduceResult

该类用于存储单个文件 reduce 的结果，我们为其添加了泛型支持，从而兼容其他的类。

### SingleMessage

该类用于存储单个记录。可以从其中获取一条记录中的基本信息，同时也提供了一个静态方法将 map 后的记录转换为该类的对象。

### 配置和初始化

在 `StarryseaMapReduceConfiguration` 类中提供了线程池的配置信息，通过修改它可以改变该框架在您的机器上的工作效率，**但也有可能导致灾难性后果**。<br>
`StarryseaMapReduceManager` 类中会对线程池等进行初始化等操作，以及含有注册 mapper 和 reducer 的操作。

### 改造

您也可以用此框架分析其他类型的信息，需要将框架中和聊天记录相关的地方进行修改。例如您需要更改 `Mapper` 类中的 `split` 和 `execStr` 方法，您还需要为新的数据类型在 bo 包中建立一个新的类，以及在 reducer 包中建立用于分析其他数据类型的 reducer。

## 参与贡献

您可以通过以下方式参与开发进程：

* fork 该项目至您的仓库，将其克隆至您的计算机
* 做出修改并提交
* 在 [JitPack](https://jitpack.io/) 搜索**您账号下的该仓库**，例如 `yourname/Starrysea-mapreduce`
* 点击 Commits 选项卡，选取最新提交，并按照提示重新部署
* 运行测试
* 建立 Pull request

## 许可

(待补充)