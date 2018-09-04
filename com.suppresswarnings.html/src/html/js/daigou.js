function addtocart(obj) {
	var goodsid = $(obj).data("goodsid")
	var agentid = $(obj).data("agentid")
	jQuery.ajax({
	    url: "/wx.http?r=" + Math.random(),
	    data: {
		    action : "daigou",
		    todo : "addgoodstocart",
		    random : randnum,
		    ticket : ticket,
		    goodsid: goodsid,
		    state : agentid
	    },
	    success: function( result ) {
	      if("fail" == result) {
	    	  console.log('fail to addgoodstocart: ' + result)
	      } else {
	    	  console.log('great addgoodstocart')
	      }
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[lijiaming] addgoodstocart err: " + result)
	    }
	})
}
function addone(goods) {
	var rate = 0.01
	var price = rate * parseFloat(goods.pricecent)
	var li = '<li>' +
   '<div class="item">' +
    '<div class="goods_images">' +
      '<a href="/detail.html?agentid='+state+'&code='+ticket+'&goodsid='+goods.goodsid+'">' +
        '<img src="' + goods.image + '" style="width:100%;" alt="'+goods.title+'">' +
      '</a>' +
    '</div>' +
    '<dl>' +
          '<dt><a href="/detail.html?agentid=who&code=what&goodsid=which">'+goods.title+'</a></dt>' +
	  	  '<dd style="font-size:12px;">'+goods.extra+'</dd>' + 
	      '<dd><i>￥'+price+'</i></dd>' + 
	      '<dd><a class="rbtn mini-addcart" href="javascript:;" onclick="addtocart(this)" data-agentid="'+state+'" data-goodsid="' +goods.goodsid+ '"> 加入购物车 </a></dd>' +
    '</dl>' +
  '</div>' +
'</li>'
  $(".single_item").append(li)
}

jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "daigou",
	    todo : "index",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to access_token: ' + result)
        oauth2()
      } else {
        var goodslist = JSON.parse(result)
        var length = goodslist.length
        for (var k = 0; k < length; k++) {
        	var goods = goodslist[k]
        	addone(goods)
        }
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      oauth2()
    }
})
