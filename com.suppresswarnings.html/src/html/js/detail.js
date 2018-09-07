
function loadCarts(){
	jQuery.ajax({
	    url: "/wx.http?r=" + Math.random(),
	    data: {
		    action : "daigou",
		    todo : "goodsdetail",
		    random : randnum,
		    ticket : ticket,
		    state : state,
		    goodsid : goodsid
	    },
	    success: function( result ) {
	      if("fail" == result) {
	        console.log('fail to detail: ' + result)
	        index()
	      } else {
	        var goods = JSON.parse(result)
	        var cent = parseFloat(goods.pricecent)
			var price = rate * cent
			price = price.toFixed(2)
			$("#image").attr("src", goods.image)
			$("#goodstitle").text(goods.title)
			$("#goodsprice").text("¥" + price)
	        $("#totalprice").text("¥" + price)
	        var goodsimage = goods.listimages
	        var arr = goodsimage.split(",")
	        var length = arr.length
	        for(var k=0;k<length;k++) {
	        	var str = arr[k]
	        	$('<div><img class="desc" src="' +str+ '"></div>').appendTo($("#goodsimages"))
	        }
	        
	      }
	    },
	    error: function( xhr, result, obj ) {
	      console.log("[lijiaming] detail err: " + result)
	      index()
	    }
	})
}
loadCarts()
