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
			if(goods.type == "Auth" || goods.type == "Software" || goods.type == "Invest") {
				input.hide()
				rate = 1
				var total = parseFloat(goods.pricecent);
				var price = 0.01 * total
				price = price.toFixed(2)
				$("#amount").val(total)
				$("#money").text(price)
			} else {
				input.show()
				input.val("");
				input.focus()
				rate = parseFloat(goods.pricecent);
				new KeyBoard(document.getElementById('amount'), 2);
				changeval(input)
			}
			
			closeDiv()
		}
	},
	error : function(xhr, result, obj) {
		console.log("[lijiaming] check payment err: " + result)
	}
})
