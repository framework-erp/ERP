一个简化java应用开发的框架。

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
这样的复杂技术细节，需要做的仅仅是给方法加上  **@Process** 注解，ERP就会为你照顾一切技术细节。

值得一提的是，这里的订单仓库 “orderRepository” 不需要花心思去开发，ERP有一个内置的内存仓库
“erp.repository.impl.mem.MemRepository”，此外也提供了仓库的[mongodb实现](https://github.com/framework-erp/ERP-mongodb.git)

### HelloWorld：
TODO



 
