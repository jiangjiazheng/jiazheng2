// 公共操作
// 定义控制器
// 让模块特有的操作继承公共的Controller操作
app.controller('baseController', function ($scope) {

    // 定义分页的属性
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,		// 当前页码
        totalItems: 0,		// 总条数
        itemsPerPage: 5,	// 每页显示的条数
        perPageOptions: [5, 10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList(); // 重新加载，分页查询
        }
    };

    /**
     * 无查询条件的分页查询
     * 定义分页查询的方法：属性：传递：当前页码、每页显示的条数  响应：结果集、总条数
     */
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //searchEntity 需要定义才能使用 {}为对象 []数组
    $scope.searchEntity = {};

    /**
     * 根据定义获得复选框中id
     * 复选框操作:
     */
    // 定义一个数组:
    $scope.selectIds = [];
    // 更新复选框：
    $scope.updateSelectIds = function ($event, id) {
        //判断是否选中
        if ($event.target.checked) {
            //选中,将id加入到数组
            $scope.selectIds.push(id);
            //console.log($scope.selectIds);
        } else {
            //取消:将id从数组中移除
            var index = $scope.selectIds.indexOf(id);
            /**
             * array.splice(index,howmany,item1,.....,itemX)
             * index 必需。规定从何处添加/删除元素。该参数是开始插入和（或）删除的数组元素的下标，必须是数字。
             * howmany 必需。规定应该删除多少元素。必须是数字，但可以是 "0"。如果未规定此参数，则删除从 index 开始到原数组结尾的所有元素。
             * item1, ..., itemX 可选。要添加到数组的新元素
             */
            $scope.selectIds.splice(index, 1);
        }
    };

    /**
     * 尝试下全选 selall($event,list);
     *
     * 点击它将所有的checkbox置为true或false然后将所有的id放进 $scope.selectIds = [];
     *
     * 循环中的checkbox中的函数updateSelectIds($event,entity.id);
     *      -- 作用是更新selectIds[]数组中的id，
     * 你如果选择或取消某条数据的话，相应的selectIds[]数组就会取消或者增加.
     *
     * ng-checked='绑定的参数hg-model'...
     *
     */
    $scope.selall = function ($event, data) {

        //如果被选中 则为true
        if ($event.target.checked) {
            $scope.checkId = true;
            console.log(data);

            //for (var key in data) {
            //console.log(key);
            //if ($scope.selectIds.indexOf(data[key].id) >= 0) {//判断数组中是否重复存在
            //    continue;
            //} else {
            //    $scope.selectIds.push(data[key].id);
            //}
            //}

            for (var i = 0; i < data.length; i++) {
                if ($scope.selectIds.indexOf(data[i].id) >= 0) {//判断数组中是否重复存在
                    continue;
                } else {
                    $scope.selectIds.push(data[i].id);
                }
            }
            //console.log($scope.selectIds);
        }
        else {
            $scope.checkId = false;
            $scope.selectIds = [];
        }

    };


    // 定义方法：获取JSON字符串中的某个key对应值的集合
    $scope.jsonToString = function (jsonStr, key) {
        // 将字符串转成JSOn:
        var jsonObj = JSON.parse(jsonStr);

        var value = "";
        for (var i = 0; i < jsonObj.length; i++) {

            if (i > 0) {
                value += ",";
            }

            value += jsonObj[i][key];
        }
        return value;
    }

});