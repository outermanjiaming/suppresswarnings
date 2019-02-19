$.ajax({
	url : "/wx.http?r=" + Math.random(),
	data : {
		action : "payment",
		random : randnum,
		ticket : ticket,
		state : state
	},
	success : function(result) {
		if ("fail" == result) {
			console.log('fail to access_token: ' + result)
		} else {
			console.log('great lijiaming')
			
			var goods = JSON.parse(result)
			
			var input = $("#amount")
			$("#reason").text(goods.reason)
			$("#what").text(goods.what)
			$("#userimg").attr("src", goods.userimg)
			if(goods.type == "Auth") {
				input.hide()
				var price = 0.01 * parseFloat(goods.pricecent);
				price = price.toFixed(2)
				$("#amount").val(total)
				$("#money").text(price)
			} else {
				input.show()
				input.val("");
				input.focus()
				new KeyBoard(document.getElementById('amount'), 2);
			}
			
			closeDiv()
		}
	},
	error : function(xhr, result, obj) {
		console.log("[lijiaming] check payment err: " + result)
	}
})