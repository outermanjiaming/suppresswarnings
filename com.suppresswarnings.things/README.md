##  SuppressWarnings.Things 
* 如果需要更高级的功能，可以私聊我

## 让你通过公众号控制自己写的程序
* 应用场景1: 通过命令查看自己的服务器运行状态
* 应用场景2: 查看我的树莓派当前的运行情况
* 应用场景3: 通过命令控制硬件设备：开灯，关灯，打开电视
* 应用场景N: 通过命令控制任何程序

## 直接Get Started

```xml
<dependency>
  <groupId>com.suppresswarnings</groupId>
  <artifactId>com.suppresswarnings.things</artifactId>
  <version>1.0.5</version>
</dependency>
```

* 增加zxing的依赖 

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.3.0</version>
</dependency>
```

* 增加QRCodeUtil
用法：在Things中增加showQRCode方法，根据情况使用相应的Util静态方法

----

在你的项目中的pom.xml加入这个依赖，然后开始写代码。

### Example如下

```
import com.suppresswarnings.things.SuppressWarnings;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;

@SuppressWarnings("测试Things是否可以")
public class Example implements Things {

    public static void main(String[] args) {
        ThingsManager.connect(new Example());
    }

    @SuppressWarnings("测试一下")
    public String test(String input) {
        System.out.println("输入： " + input);

        return SUCCESS;
    }

    @Override
    public String description() {
        return "测试Things是否可用";
    }

    @Override
    public String exception(String error) {
        System.err.println("Error: " + error);


        return "error";
    }
}
```

### 运行main方法，会弹出二维码，扫码就可以使用了
`在公众号「素朴网联」输入对应的命令就可以直达程序`


## 解说
* 程序入口main方法，`ThingsManager.connect(new Example());`
* 实现Things接口，` implements Things`
* 使用注解com.suppresswarnings.things.SuppressWarnings，`@SuppressWarnings("测试一下")`，
* 1. 其中value是String类型，
* 1. 如果注解在方法上就是命令（方法是固定格式：`public String test(String input)）
* 1. 如果注解在其他地方就是普通说明（后期可能会对类上面的注解进行解析）
* 1. 返回值包括Things.INTERACTIVE，Things.SUCCESS，Things.FAIL，Things.ERROR，其中Things.INTERACTIVE表示接下来的输入会直接进入该方法内。