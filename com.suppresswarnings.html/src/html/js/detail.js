var rate = 0.01
function addtocart(obj) {
	showDiv()
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
	    	closeDiv()
	      if("fail" == result) {
	    	  console.log('fail to addgoodstocart: ' + result)
	      } else {
	    	  console.log('great addgoodstocart')
	    	  $.tipsBox({
	              obj: $("#gotocart"),
	              str: "+1",
	              callback: function () {
	              }
	          });
	          niceIn($("#gotocart"));
	      }
	    },
	    error: function( xhr, result, obj ) {
	    	closeDiv()
	      console.log("[lijiaming] addgoodstocart err: " + result)
	    }
	})
}
function loadDetail(){
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
	        daigou()
	      } else {
	        var goods = JSON.parse(result)
	        var cent = parseFloat(goods.pricecent)
			var price = rate * cent
			price = price.toFixed(2)
			$("#image").attr("src", goods.image)
			$("#goodstitle").text(goods.title)
			$("#goodsprice").text("¥" + price)
	        $("#totalprice").text("¥" + price)
	        $("#addtocart").data("goodsid", goods.goodsid)
	        $("#addtocart").data("agentid", ticket)
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
	      daigou()
	    }
	})
}
loadDetail()
