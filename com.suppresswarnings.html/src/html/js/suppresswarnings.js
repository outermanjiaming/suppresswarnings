var index = 0;
var cmd = function() {
	index ++;
	var r = Math.random();
	var jqxhr = $.ajax("index.html")
	.done(function() {
		alert("success");
	})
	.fail(function() {
		console.log("error" + r);
	})
	.always(function() {
		$("#msg").append( "<li id=" + r + "> "+ index +" Successful Request!" + window.token + "</li>");
		$("#msg").scrollTop($("#msg")[0].scrollHeight);
	});
}

var server = {
	protocol : 'http://',
	host : '139.199.104.224/',
	aler : function() {
		alert('server alert ' + this.host)
	},
	token : function() {
		if (sessionStorage.token) {
			window.token = sessionStorage.token;
		} else {
			var value = 'T' + Math.random();
			window.token = value;
			sessionStorage.token = value;
		}
	}
}
server.token();
$(document).ajaxSuccess(function(event, request, settings) {
	$("#msg").append("<p>Successful Request!</p>");
});
$(document).ready(function() {
	$("button").click(function() {
		$(this).addClass('btn-success');
		cmd();
	});
});