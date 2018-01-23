var index = 0;
var current;
const userLogin = "user.http?action=Login";
const userRegsiter = "user.http?action=Regsiter";
const userInvite = "user.http?action=Invite";
const userInvited = "user.http?action=Invited";
const interaction = "user.http?action=Interaction"
var server = {
	protocol : 'http://',
	host : '139.199.104.224/',
	uri : 'interaction.http',
	ajax : function(obj, args) {
		var r = Math.random();
		$.ajax(this.protocol + this.host + this.uri + "&" + args)
		.done(function(data, textStatus, jqXHR) {
			$("#msg").append( "<li style='list-style-type:none;'><a href='"+server.protocol + server.host+"'> Always </a>" + data + "</li>");
			$("#msg").scrollTop($("#msg")[0].scrollHeight);
		})
		.fail(function() {
			console.log("error" + r);
		})
		.always(function() {
			var cmdtxt = $('#cmdinput').val();
			if(current == cmdtxt) {
				$('#cmdinput').val('');
			}
			$("#msg").append( "<li style='list-style-type:none;'><a href='"+server.protocol + server.host+"'> Always </a>" + window.token + "</li>");
			$("#msg").scrollTop($("#msg")[0].scrollHeight);
		});
	},
	alert : function() {
		alert('server alert ' + this.protocol + this.host + this.uri)
	},
	action : function(value) {
		this.uri = value;
	},
	set : function(value) {
		window.token = value;
		sessionStorage.token = value;
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
$(document).ready(function() {
	$("#cmdbtn").click(function() {
		current = $('#cmdinput').val();
		$("#msg").append( "<li style='list-style-type:none;'> "+ index +"  " + current + "</li>");
		$(this).addClass('btn-success');
		cmd();
	});

	$("#btnlogin").click(function() {
		$("#panelLogin").slideUp();
	});
});