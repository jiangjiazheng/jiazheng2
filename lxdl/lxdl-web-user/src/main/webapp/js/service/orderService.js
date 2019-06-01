//我的订单服务层
app.service('orderService', function ($http) {
    //订单列表
    this.findOrderList = function () {
        return $http.get('order/findOrderList.do');
    }
});