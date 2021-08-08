一个简化java应用开发的框架。 **（目前只支持JAVA8）** 

通过 “聚合、仓库、过程” 3个概念，隔离业务逻辑和技术实现细节，使开发者专注于产品业务本身。

### 一个简单的例子：

	@Process
	public Order completeOrder(String orderId) {
		Order order = orderRepository.findByIdForUpdate(orderId);//从仓库取出order
		order.setState("compleated");//改变他的状态
		return order;//返回改变后的order
	}


