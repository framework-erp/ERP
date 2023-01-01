一个简化java应用开发的框架。 **（目前只支持JAVA8）**

通过 “聚合、仓库、过程” 3个概念，隔离业务逻辑和技术实现细节，使开发者专注于产品业务本身。

### 一个简单的例子：

```
	@Process
	public Order completeOrder(String orderId) {
		Order order = orderRepository.findByIdForUpdate(orderId);//从仓库取出order
		if ("ongoing".equals(order.getState())) {
			order.setState("compleated");//改变他的状态
			return order;//返回改变后的order
		}
		return null;
	}
```

这里我们首先从订单仓库取出了一个订单（聚合），随后改变了他的状态，变成“已完成”，最后返回了这个“已完成”的订单。在这过程我们不关心查询和保存这些和数据库打交道的事情，我们也不关心 “并发改变订单状态所带来的问题”
这样的复杂技术细节，需要做的仅仅是给方法加上  **@Process** 注解，ARP就会为你照顾一切技术细节。

需要说明的是，这里的订单仓库 “orderRepository” 也不需要花费多少心思去开发，ARP有一个内置的内存仓库
“erp.repository.CommonMemRepository”，另外也提供了一个仓库的mongodb实现，在这个工程：[https://gitee.com/zhengchengdong/erp-repository-spring-data-mongodb](https://gitee.com/zhengchengdong/arp-repository-spring-data-mongodb)

### HelloWorld：

1. maven 依赖

```
<dependency>
  <groupId>io.gitee.zhengchengdong</groupId>
  <artifactId>ARP</artifactId>
  <version>1.2.0</version>
</dependency>
```



 