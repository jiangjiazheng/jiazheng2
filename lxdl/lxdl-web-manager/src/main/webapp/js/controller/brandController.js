// 该模块特有的
// 定义控制器
// 依赖注入service
app.controller('brandController', function ($http, $scope, $controller, brandService) {
    /**
     * 继承baseController,{$scope:$scope} :继承baseController中的域对象
     */
    $controller('baseController', {$scope: $scope});

    /**
     * 查询所有
     */
    $scope.getAll = function () {
        //向服务器发起请求
        brandService.getAll().success(function (result) {
            $scope.list = result;
        })
    };

    /**
     * 定义分页条件查询
     * 在查询框中定义 ng-click=angularjs的点击事件.
     * 且查询中使用 ng-model="searchEntity.name"来绑定数据.
     * searchEntity来封装这些查询参数.
     */
    //searchEntity 需要定义才能使用 {}为对象 []数组
    $scope.searchEntity = {};
    $scope.search = function (curPage, pageSize) {
        //向服务器端发送请求,使用post传输,放在请求体中的数据用searchEntity绑定了传送过去.
        $http.post('/brand/searchByPage.do?curPage=' + curPage
            + '&pageSize=' + pageSize, $scope.searchEntity).success(function (result) {

            //响应
            $scope.paginationConf.totalItems = result.total;
            $scope.list = result.rows;
        })
    };

    /**
     * 添加品牌
     * 且在添加的品牌的input标签中使用 ng-model绑定数据和entity封装
     * 这里entity不需要定义?因为在form表单中.   疑问?还是需要去了解下
     *  -->经过了解:
     *          1.ng-model绑定数据,就会初始化对象entity
     *          2.但是,首先search这个方法页面加载的时候就调用了这个方法,但是页面没有加载完,导致searchEntity
     *          没有被初始化,故需要在应用程序中定义.然而entity的查询,是定义的方法,页面加载完后,entity就初始化了.
     *          不需要再应用程序中去定义.
     *
     *
     * 添加更新的方法...
     */
    $scope.saveAndUpdate = function () {
        //定义httpUrl请求地址.
        var httpUrl = null;
        //判断entity中id是否存在
        if ($scope.entity.id != null) {
            //发送更新请求
            httpUrl = $http.post('/brand/update.do', $scope.entity);
        } else {
            //发送添加请求
            httpUrl = $http.post('/brand/save.do', $scope.entity);
        }

        httpUrl.success(function (result) {
            //成功:刷新页面
            if (result.flag) {
                alert(result.message);
                $scope.reloadList();
            } else {
                //失败:提示保存失败
                alert(result.message);
            }
        })
    };

    /**
     * 根据id获取品牌信息
     */
    $scope.getById = function (id) {
        $http.get('/brand/getById.do?id=' + id).success(function (result) {
            $scope.entity = result;
        })
    };

    /**
     * 删除品牌
     * 传递参数:selectIds数组 复选框选中的品牌删除
     */
    $scope.remove = function () {
        $http.get('/brand/remove.do?ids=' + $scope.selectIds).success(function (result) {
            // 成功：刷新页面
            if (result.flag) {
                alert(result.message);
                $scope.reloadList();
                // 需要清空数组的数据(代表删除成功后,需要把之前的数组id清空)
                $scope.selectIds = [];
            } else {
                // 失败：提示失败
                alert(result.message);
            }
        });
    };


});