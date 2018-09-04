function updateCartnum(inputnum, cartid, newnum){
	jQuery.ajax({
	    url: "/wx.http?r=" + Math.random(),
	    data: {
		    action : "daigou",
		    todo : "updatecartnum",
		    random : randnum,
		    ticket : ticket,
		    state : state,
		    cartid : cartid,
		    newnum : newnum
	    },
	    success: function( result ) {
	      if("fail" == result) {
	            console.log('fail to updatecartnum: ' + result)
	      } else {
	    	  $(inputnum).val(result)
	      }
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[lijiaming] updatecartnum err: " + result)
	    }
	})
}
function subnum(obj) {
	var cartid = $(obj).parent().data("cartid")
	var inputnum = $(obj).siblings(".input-num")[0]
	var num = $(inputnum).val()
	var newnum = parseInt(num) - 1
	updateCartnum(inputnum, cartid, newnum)
}

function updatenum(obj) {
	var cartid = $(obj).parent().data("cartid")
	var num = $(obj).val()
	var newnum = parseInt(num)
	updateCartnum(obj, cartid, newnum)
}

function addnum(obj) {
	var cartid = $(obj).parent().data("cartid")
	var inputnum = $(obj).siblings(".input-num")[0]
	var num = $(inputnum).val()
	var newnum = parseInt(num) + 1
	updateCartnum(inputnum, cartid, newnum)
}
function order(obj) {
	var username = $("#username").val()
	if(username.length < 1) {
		$("#username").focus()
		return
	}
	var mobile = $("#mobile").val()
	if(mobile.length < 11) {
		$("#mobile").focus()
		return
	}
	
	var address = $("#address").val()
	if(address.length < 10) {
		$("#address").focus()
		return
	}
	var comment = $("#comment").val()

	jQuery.ajax({
	    url: "/wx.http?r=" + Math.random(),
	    data: {
		    action : "daigou",
		    todo : "makeanorder",
		    random : randnum,
		    ticket : ticket,
		    state : state,
		    username : username,
		    mobile : mobile,
		    address : address,
		    comment : comment
	    },
	    success: function( result ) {
	      if("fail" == result) {
	        console.log('fail to access_token: ' + result)
	      } else {
	    	  console.log('great make an order')
	    	  console.log('prepay ok' + result)
	          
	          var prepay = JSON.parse(result)
	          
	          console.log('prepay.package = ' + prepay.package)
	          WeixinJSBridge.invoke(
	                'getBrandWCPayRequest', {
	                   "appId": prepay.appId,
	                   "timeStamp":prepay.timeStamp,
	                   "nonceStr":prepay.nonceStr,
	                   "package":prepay.package,
	                   "signType":prepay.signType,
	                   "paySign":prepay.paySign
	                },
	                function(res){
	                  console.log('res.err_msg'+res.err_msg)
	                  if(res.err_msg == "get_brand_wcpay_request:ok" ){
	                      console.log('finish success')
	                      window.location.href = '/order.html?code=' + ticket + '&state=' + state
	                  }
	          })
	      }
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[lijiaming] order err: " + result)
	    }
	})
}
function addone(cart, goods, price) {
	var div = '<div class="inner">' +
        '<div class="item_img">' + 
    '<a href="/detail.html?goodsid=' +goods.goodsid+ '">' + 
         '<img src="' +goods.image+ '" title="' +goods.title+ '">' + 
    '</a>' + 
   '</div>' +
   '<div class="goods_desc">' +
     '<dl>' +
       '<dt><a href="/detail.html?goodsid=' +goods.goodsid+ '">' +goods.title+ '</a></dt>' +
     '</dl>' +
     '<div class="price"><span>¥' +price+ '</span><em class="goods_numx">x' +cart.count+ '</em> </div>' +
   '</div>' +
   '<div class="num">' +
     '<div class="qiehuan">' +
       '<div class="xm-input-number"  data-cartid="' +cart.cartid+ '" >' + 
         '<a href="javascript:;" onclick="subnum(this)" class="input-sub"></a>' +
         '<input type="text" onkeydown="if(event.keyCode == 13) event.returnValue = false" value="' + cart.count + '" class="input-num" onchange="updatenum(this);">' +
         '<a href="javascript:;" onclick="addnum(this)" class="input-add"></a>' + 
        '</div>' +
     '</div>' +
     '<div class="delete">' +
       '<a href="javascript:;" onclick="removeitem(this);"> 删除 </a>' + 
     '</div>'+
   '</div>'+
 '</div>'
    $(".item-list").append(div)
}

jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "daigou",
	    todo : "mycarts",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to access_token: ' + result)
        index()
      } else {
        var cartslist = JSON.parse(result)
        var length = cartslist.length
        var sum = 0
        var count = 0
        var rate = 0.01
        for (var k = 0; k < length; k++) {
        	var cart = cartslist[k]
        	var goods = cart.goods
        	var cent = parseFloat(goods.pricecent)
        	var cnt = parseInt(cart.count)
        	var total = cent * cnt
        	sum = sum + total
        	count = count + cnt
        	var price = rate * cent
        	addone(cart, goods, price)
        }
        var totalprice = rate * parseFloat(sum)
        $("#totalprice").text("¥" + totalprice)
        $("#goodscount").text(count)
        $("#goodstype").text(length)
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      index()
    }
})
