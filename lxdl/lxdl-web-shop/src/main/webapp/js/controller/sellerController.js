//控制层
app.controller('sellerController', function ($scope, $controller, $http, sellerService) {

    $controller('baseController', {$scope: $scope});//继承





    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        sellerService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        sellerService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        sellerService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = sellerService.update($scope.entity); //修改
        } else {
            serviceObject = sellerService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.flag) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }

    $scope.add = function () {
        sellerService.add($scope.entity).success(
            function (response) {
                if (response.flag) {
                    // 重新查询
                    // $scope.reloadList();//重新加载
                    location.href = "shoplogin.html";
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        sellerService.dele($scope.selectIds).success(
            function (response) {
                if (response.flag) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };


    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        sellerService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //正则验证
 /*   function regexPwd() {
        var str = "/^[a-zA-z]\w{2-17}$/";
        var reg = new RegExp(str);

    }*/


    $scope.checkPwd = function () {
        $scope.result = '';
        if (!($scope.pass.oldPassword && $scope.pass.newPassword && $scope.pass.fixPassword)) {
            $scope.result = "请填写完整";
            return false;
        } else if ($scope.pass.newPassword != $scope.pass.fixPassword) {
            $scope.result = "两次密码不一致";
            return false;
        } else if ($scope.pass.oldPassword == $scope.pass.newPassword) {
            $scope.result = "新旧密码不能一致";
            return false;
        }
        return true;
    };

    $scope.pass = {};
    $scope.modifyPassword = function () {
        var b = $scope.checkPwd();
        if (!b) {
            alert($scope.result);
            return;
        }
        sellerService.updatePwd($scope.pass).success(function (response) {
            if (response.flag) {
                location.href = "/logout";
            } else {
                alert(response.message);
            }
        });

        // alert("jinlaile");
    };

	$scope.findAll=function(){
		sellerService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}

	//分页
	$scope.findPage=function(page,rows){
		sellerService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	//商家信息回显
    $scope.findOne=function(){
        sellerService.findOne().success(
            function(response){
                $scope.seller= response;
            }
        );
    }

    //商家信息修改
    $scope.updateSeller=function(){
        sellerService.updateSeller($scope.seller).success(
            function(response){
               if (response.flag){
                   // $scope.reloadList();// 重新加载列表
				   location.href="/admin/seller.html";
			   } else{
               	alert("修改失败");
			   }
            }
        );
    }

	//保存
	$scope.save=function(){
		var serviceObject;//服务层对象
		if($scope.entity.id!=null){//如果有ID
			serviceObject=sellerService.update( $scope.entity ); //修改
		}else{
			serviceObject=sellerService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.flag){
					//重新查询
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}
		);
	}

	$scope.add = function(){
		sellerService.add( $scope.entity  ).success(
			function(response){
				if(response.flag){
					// 重新查询
		        	// $scope.reloadList();//重新加载
					location.href="shoplogin.html";
				}else{
					alert(response.message);
				}
			}
		);
	}


	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		sellerService.dele( $scope.selectIds ).success(
			function(response){
				if(response.flag){
					$scope.reloadList();//刷新列表
					$scope.selectIds = [];
				}
			}
		);
	}

	$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		sellerService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

});
