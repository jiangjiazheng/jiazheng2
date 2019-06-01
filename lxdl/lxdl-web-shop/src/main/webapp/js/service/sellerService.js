//服务层
app.service('sellerService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../seller/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../seller/findPage.do?page='+page+'&rows='+rows);
	}
	//查询商家信息
	this.findOne=function(){
		return $http.get('../seller/findOne.do');
	}
	// 修改商家资料
	this.updateSeller=function (entity) {
		return $http.post('../seller/updateSeller.do',entity );
    }

	//增加 
	this.add=function(entity){
		return  $http.post('../seller/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../seller/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../seller/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../seller/search.do?page='+page+"&rows="+rows, searchEntity);
	}
	// 修改密码
    this.updatePwd = function (pass) {
        return $http.post('../seller/modifyPassword.do', pass);
    };
});
