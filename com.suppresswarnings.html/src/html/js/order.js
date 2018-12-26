function toggleordergoods(obj) {
	var goods = $(obj).parent().siblings(".order_goods")[0]
	$(goods).slideToggle()
}

function timestampToTime(timestamp) {
    var date = new Date(Number(timestamp));
    var Y = date.getFullYear() + '-';
    var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
    var D = date.getDate() + ' ';
    var h = date.getHours() + ':';
    var m = date.getMinutes() + ':';
    var s = date.getSeconds();
    return Y+M+D+h+m+s;
}

function addorder(order) {
	var myorders = $("#myorders")
	var orderinfo = '<div class="order_info">' +
    	
    	'订单号：<span>' + order.orderid + '</span><br/>'+
    	'收货人：<span>' + order.username + '</span><br/>'+
    	'地址：<span>' + order.address + '</span><br/>'+
    	'手机：<span>' + order.mobile + '</span><br/>'+
    	'备注：<span>' + (order.comment == undefined ? "无" : order.comment) + '</span><br/>'+
    	'时间：<span>' + timestampToTime(order.time) + '</span><br/>'
    if(order.state == "Paid") {
    	orderinfo = orderinfo +'状态：<span style="color:green;">已支付</span><br/>'
    } else if(order.state == "Closed"){
    	orderinfo = orderinfo +'状态：<span>已关闭</span><br/>'
    } else if(order.state == "Paying"){
    	orderinfo = orderinfo +'状态：<span>正在支付</span><br/>'
    } else if(order.state == "Create"){
    	orderinfo = orderinfo +'状态：<span>未支付</span><br/>'
    } else {
    	orderinfo = orderinfo +'状态：<span>' + order.state + '</span><br/>'
    }
	orderinfo = orderinfo + '</div>'
    var ordergoods = '<div class="order_goods"><ul>'
    var rate = 0.01
	var carts = order.carts
	var size = carts.length
	for(var k=0;k<size;k++) {
		var cart = carts[k]
		var goods = cart.goods
		var cent = parseFloat(cart.actualpricecent)
    	var price = rate * cent
    	price = price.toFixed(2)
		var goodsli = '<li>' +
				        '<div class="inner">' +
				              '<div class="item_img">' +
				                '<a href="/detail.html?code='+ticket+'&state='+state+'&goodsid=' +goods.goodsid+ '">' +
				                   '<img src="'+goods.image+'" title="' +goods.title+ '">'+
				                '</a>' +
				              '</div>' +
				              '<a class="smalla" href="/detail.html?code='+ticket+'&state='+state+'&goodsid=' +goods.goodsid+ '">' + goods.title+ '</a>' +
				              '<div class="price">' +
				                '<span>' + price + '</span>' +
				                '<em>'+ cart.count + '件</em>' +
				              '</div>' +
				            '</div>' +
				        '</li>'
		ordergoods = ordergoods + goodsli
	}
	ordergoods = ordergoods + '</ul></div>'
	$('<li class="dashedli"><div>' + orderinfo + ordergoods + '</div></li>').appendTo(myorders)
}

jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "daigou",
	    todo : "myorders",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to access_token: ' + result)
        daigou(state)
      } else {
    	  showDiv()
        var orderlist = JSON.parse(result)
        var length = orderlist.length
        for (var k = 0; k < length; k++) {
        	var order = orderlist[k]
        	addorder(order)
        }
    	  $("#myorders").append('<div style="width:100%;height:111px;"><img src="/loading.gif" style="width:100%;heigth:30px"></div>')
        closeDiv()
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] order list err: " + result)
      daigou(state)
    }
})