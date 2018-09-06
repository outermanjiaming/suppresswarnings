



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
      index()
    }
})