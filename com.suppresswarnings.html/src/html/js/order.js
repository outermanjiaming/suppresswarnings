function toggleordergoods(obj) {
	var goods = $(obj).parent().siblings(".order_goods")[0]
	$(goods).slideToggle()
}
function addorder(order) {
	var myorders = $("#myorders")
	var orderinfo = '<div class="order_info">' +
    	
    	'订单号：<span>' + order.orderid + '</span><br/>'+
    	'收货人：<span>' + order.username + '</span><br/>'+
    	'时间：<span>' + order.time + '</span><br/>'+
    	'状态：<span>' + order.state + '</span><button>立即支付</button><br/>'+
    	'详情：<button class="toggleordergoods" onclick="toggleordergoods(this)">展开</button><span>' + order.detail + '</span><br/>'+
    	'</div>'
    var ordergoods = '<div class="order_goods" style="display: none;">' + 
    	'<ul>'
	var carts = order.carts
	var size = carts.length
	for(var k=0;k<size;k++) {
		var cart = carts[k]
		var goods = cart.goods
		var goodsli = '<li>' +
				        '<div class="inner">' +
				              '<div class="item_img">' +
				                '<a href="/detail.html?code='+ticket+'&state='+state+'&goodsid=' +goods.goodsid+ '">' +
				                   '<img src="'+goods.image+'" title="' +goods.title+ '">'+
				                '</a>' +
				              '</div>' +
				              '<a class="smalla" href="/detail.html?code='+ticket+'&state='+state+'&goodsid=' +goods.goodsid+ '">' + goods.title+ '</a>' +
				              '<div class="price">' +
				                '<span>' + goods.price+ '</span>' +
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
        index()
      } else {
        var orderlist = JSON.parse(result)
        var length = orderlist.length
        for (var k = 0; k < length; k++) {
        	var order = orderlist[k]
        	addorder(order)
        }
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] order list err: " + result)
      index()
    }
})