function isPositiveNum(val){
    if(val === "" || val ==null){
        return false;
    }
    if(!isNaN(val)){
    	if(val <= 0) {
    		return false;
    	}
        return true;
    } else{
        return false;
    }
}
function removeitem(obj) {
	var cartid = $(obj).data("cartid")
	jQuery.ajax({
	    url: "/wx.http?r=" + Math.random(),
	    data: {
		    action : "daigou",
		    todo : "removecart",
		    random : randnum,
		    ticket : ticket,
		    state : state,
		    cartid : cartid
	    },
	    success: function( result ) {
	      if("fail" == result) {
	    	  console.log('fail to remove item: ' + result)
	      } else {
	    	  $(obj).parent().parent().parent().remove()
	    	  loadCarts()
	      }
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[lijiaming] remove item err: " + result)
	    }
	})
}
function updateCartnum(inputnum, cartid, newnum){
	if(!isPositiveNum(newnum)) {
		return false;
	}
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
	    	  loadCarts()
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
	var div = '<div class="inner" data-goodscount="'+cart.count+'" data-goodsprice="'+goods.pricecent+'">' +
        '<div class="item_img">' + 
    '<a href="/detail.html?goodsid=' +goods.goodsid+ '&state='+state+'&code='+ticket+'">' + 
         '<img src="' +goods.image+ '" title="' +goods.title+ '">' + 
    '</a>' + 
   '</div>' +
   '<div class="goods_desc">' +
     '<dl>' +
       '<dt><a href="/detail.html?goodsid=' +goods.goodsid+ '&state='+state+'&code='+ticket+'">' +goods.title+ '</a></dt>' +
     '</dl>' +
     '<div class="price"><span>¥' +price+ '</span></div>' +
   '</div>' +
   '<div class="num">' +
     '<div class="qiehuan">' +
       '<div class="xm-input-number" data-cartid="' +cart.cartid+ '">' + 
         '<a href="javascript:;" onclick="subnum(this)" class="input-sub"></a>' +
         '<input type="text" onkeydown="if(event.keyCode == 13) event.returnValue = false" value="' + cart.count + '" class="input-num" onchange="updatenum(this);">' +
         '<a href="javascript:;" onclick="addnum(this)" class="input-add"></a>' + 
        '</div>' +
     '</div>' +
     '<div class="delete">' +
       '<a href="javascript:;" data-cartid="' +cart.cartid+ '" onclick="removeitem(this);"> 删除 </a>' + 
     '</div>'+
   '</div>'+
 '</div>'
    $(".item-list").append(div)
}
function loadCarts(){
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
	        console.log('fail to cart: ' + result)
	        daigou()
	      } else {
	        var cartslist = JSON.parse(result)
	         $(".item-list").empty()
	        
	        var length = cartslist.length
	        if(length == 0) {
	        	$(".folw_shopmain").html('<div id="emptycart" style="padding:50px 0;text-align: center;line-height: 40px;color: #999;font-size: 18px;margin-top: 20px;"><img src="/empty_cart.png" width="100" height="95"> <br>购物车为什么空空的呢？</div><div class="qb_gap" style="width:60%; margin:0 auto;"><a href="/daigou.html" style="text-align: center;text-decoration: none;line-height: 45px;height: 45px;font-size: 15px;display: block;width: 100%;color: #ffffff!important;-webkit-border-radius: .3em;-moz-border-radius: .3em;-ms-border-radius: .3em;-o-border-radius: .3em;border-radius: .3em;background-color: #DD2726;">马上买买买!</a></div>')
	        	$(".flow_bottom").hide()
	        	return
	        }
	        $(".flow_bottom").show()
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
	        	price = price.toFixed(2)
	        	addone(cart, goods, price)
	        }
	        
	        var totalprice = rate * sum
	        totalprice = totalprice.toFixed(2)
	        $("#totalprice").text("¥" + totalprice)
	        $("#goodscount").text(count)
	        $("#goodstype").text(length)
	      }
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[lijiaming] cart err: " + result)
	      daigou()
	    }
	})
}
loadCarts()
