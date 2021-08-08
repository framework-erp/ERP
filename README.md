一个简化java应用开发的框架。 **（目前只支持JAVA8）** 

通过 “聚合、仓库、过程” 3个概念，隔离业务逻辑和技术实现细节，使开发者专注于产品业务本身。

### 一个简单的例子：

	@Process
	public Order completeOrder(String orderId) {
		Order order = orderRepository.findByIdForUpdate(orderId);
		order.setState("compleated");
		return order;
	}


