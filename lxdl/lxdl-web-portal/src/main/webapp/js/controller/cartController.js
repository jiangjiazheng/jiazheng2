//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = {totalNum: 0, totalMoney: 0};   //cartService.sum($scope.cartList);
            }
        );
    }

    //数量加减
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.flag) {//如果成功
                    $scope.findCartList();//刷新列表
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //获取当前用户的地址列表
    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == '1') {
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }

            }
        );
    }

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }

    //判断某地址对象是不是当前选择的地址
    $scope.isSeletedAddress = function (address) {
        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order = {paymentType: '1'};//订单对象

    //选择支付类型
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    //保存订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//联系人

        cartService.submitOrder($scope.order).success(
            function (response) {
                //alert(response.message);
                if (response.flag) {
                    //页面跳转
                    if ($scope.order.paymentType == '1') {//如果是微信支付，跳转到支付页面
                        location.href = "pay.html";
                    } else {//如果货到付款，跳转到提示页面
                        location.href = "paysuccess.html";
                    }

                } else {
                    alert(response.message);	//也可以跳转到提示页面
                }

            }
        );
    }
    /** --------------------自定义------------------------ */
    // 定义一个数组装选中的ItemId
    $scope.selectItemIds = [];
    // 购物车复选框选中
    $scope.updateSelectItemIds = function ($event, item) {
        //判断是否选中
        if ($event.target.checked) {
            $scope.selectItemIds.push(item.itemId);

            $scope.totalValue.totalNum += item.num;       //累加数量
            $scope.totalValue.totalMoney += item.totalFee;//累加金额
        } else {
            //取消:将id从数组中移除
            var index = $scope.selectItemIds.indexOf(item.itemId);
            $scope.selectItemIds.splice(index, 1);

            $scope.totalValue.totalNum -= item.num;       //累减数量
            $scope.totalValue.totalMoney -= item.totalFee;//累减金额
        }
    }
    //点解结算判断是否登录
    $scope.judgingLogin = function () {
        cartService.judgingLogin().success(
            function (response) {
                if (response.flag) {
                    //登录了处理选中的购物项
                    $scope.handleCart()
                } else {
                    //未登录弹出友好信息提示
                    alert(response.message);
                    //跳转到登录页面
                    location.href = "login.html";
                }
            })
    }

    //将选中的购物项即数组传到后端处理，存到redis中
    $scope.handleCart = function () {
        cartService.handleCart($scope.selectItemIds).success(
            function () {
                location.href = 'getOrderInfo.html'
            })
    }

    //结算取出选中的购物项
    $scope.getCheckedCart = function () {
        cartService.getCheckedCart().success(function (response) {
            $scope.cartList = response;
            $scope.totalValue = cartService.sum($scope.cartList);
        })
    };

    // 定义一个数组:
    // $scope.selectIds = [];
    // // 更新复选框：
    // $scope.updateSelection = function($event,id){
    //     // 复选框选中
    //     if($event.target.checked){
    //         // 向数组中添加元素
    //         $scope.selectIds.push(id);
    //     }else{
    //         // 从数组中移除
    //         var idx = $scope.selectIds.indexOf(id);
    //         $scope.selectIds.splice(idx,1);
    //     }
    //
    // }
    $scope.selall = function ($event, cartList) {
        //如果被选中 则为true
        if ($event.target.checked) {
            $scope.checkId = true;
            //console.log(cartList);

            for (var i = 0; i < cartList.length; i++) {
                var cart = cartList[i];
                for (var j = 0; cart.orderItemList.length; j++) {

                    if ($scope.selectItemIds.indexOf(cart.orderItemList[j].itemId) >= 0) {//判断数组中是否重复存在
                        continue;
                    } else {
                        $scope.selectItemIds.push(cart.orderItemList[j].itemId);
                        $scope.totalValue.totalNum += cart.orderItemList[j].num;       //累加数量
                        $scope.totalValue.totalMoney += cart.orderItemList[j].totalFee;//累加金额
                    }
                }
            }
            console.log($scope.selectItemIds);
        }
        else {
            $scope.checkId = false;
            $scope.selectItemIds = [];
            $scope.totalValue = {totalNum: 0, totalMoney: 0};
        }

    // 新增收货地址
    $scope.saveAddress = function () {
        cartService.saveAddress($scope.entity).success(function (response) {
           if (response.flag) {
               alert(response.message);
               // 新增成功， 重新获取当前用户的地址列表
               location.href = 'getOrderInfo.html';
           } else {
               alert(response.message);
           }
        });
    }

    $scope.setAlia = function (temp) {
        var alia;
        if (temp == 1){
            alia = document.getElementById('aliaOne').innerText;

        } else if(temp == 2) {
            alia = document.getElementById('aliaTwo').innerText;

        } else if(temp == 3) {
            alia = document.getElementById('aliaThree').innerText;
        }
        $scope.alia = alia;

    }


    }
});