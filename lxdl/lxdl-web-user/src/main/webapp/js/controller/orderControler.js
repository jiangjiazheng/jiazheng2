//订单控制层
app.controller('orderController', function ($scope, orderService) {
    //查询购物车列表
    $scope.findOrderList = function () {
        orderService.findOrderList().success(
            function (response) {
                $scope.orderVOList = response;
            }
        );
    }
});