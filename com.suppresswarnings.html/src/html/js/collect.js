function aomnewreply(myimg, reply, replyId) {
	var li = "<li class='boder_v1'><img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='"+myimg+"'><span>"+reply+"</span><div class='form-group text-center'><button type='button' class='btn btn-xs' onclick='changesame(this)'>同义句</button> - <button type='button' class='btn btn-xs' onclick='changereply(this)'>回复</button><div class='sendreplydiv hidden' style='padding-top:15px;'><input type='text' class='input btn-xs sendreplyinput' placeholder='请输入回复内容' size='30'><button type='button' data-replyid='"+replyId+"' data-which='replysame' class='btn btn-xs sendreplybtn' onclick='sendreply(this)'>发送</button></div></div></li>"
	$("#business").append(li)
}
function aomchangesame(obj) {
	var sendreplydiv = $(obj).siblings(".sendreplydiv")[0]
	$(sendreplydiv).removeClass("hidden")
	var sendreplybtn = $(sendreplydiv).children(".sendreplybtn")[0]
	$(sendreplybtn).data("which", "same")
}
function aomchangereply(obj) {
	var sendreplydiv = $(obj).siblings(".sendreplydiv")[0]
	$(sendreplydiv).removeClass("hidden")
	var sendreplybtn = $(sendreplydiv).children(".sendreplybtn")[0]
	$(sendreplybtn).data("which", "reply")
}
function aomsendreply(obj) {
	var replyId = $(obj).data("replyid")
	var which = $(obj).data("which")
	console.log(replyId + " & " + which)
	var sendreplyinput = $(obj).siblings(".sendreplyinput")[0]
	var reply = $(sendreplyinput).val()
	alert(reply)
	$(sendreplyinput).val("")
}
function replyone(myimg, reply) {
	$("#clientreply").append("<div><img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='"+myimg+"'/><span class='collectreply'>" + reply + "</span></div>");
}
function replyquiz(obj) {
	var quizId = $(obj).data("quizid")
	var reply = $("#replyquiz").val()
	var myimg = $("#userimg").val()
	var myname= $("#username").val()
	jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "replyquiz",
	    random : randnum,
	    ticket : ticket,
	    state : state,
	    reply : reply
    },
    success: function( result ) {
    	if("fail" == result) {
    		$("#replyquiz").attr("placeholder","提交失败，稍后重试")
    	} else {
    		$("#replyquiz").attr("placeholder","提交成功，可以继续提交")
    		replyone(myimg, reply)
    	}
    	$("#replyquiz").val("")
    	$("#replyquiz").focus()
    },
    error: function( xhr, result, obj ) {
    	$("#replyquiz").attr("placeholder","服务端错误，请按要求输入重试")
    }
  })
}
var replySize
var replyCount
var replyArray
var timeOut
function similarreply(obj) {
	var replyId = $(obj).data("replyid")
	var which = $(obj).data("which")
	console.log(replyId + " & " + which)
	var similarreplyinput = $(obj).siblings(".similarreplyinput")[0]
	var similarrepliesdiv = $(obj).siblings(".similarrepliesdiv")[0]
	var reply = $(similarreplyinput).val()
	var myimg = $("#userimg").val()
	var myname= $("#username").val()
	jQuery.ajax({
	    url: "/wx.http?r=" + Math.random(),
	    data: {
		    action : "replysimilar",
		    random : randnum,
		    ticket : ticket,
		    state : state,
		    replyid : replyId,
		    similar : reply
	    },
	    success: function( result ) {
	    	if("fail" == result) {
	    		$(similarreplyinput).attr("placeholder","提交失败，稍后重试")
	    	} else {
	    		$(similarreplyinput).attr("placeholder","提交成功，可以继续提交")
	    		similarreplyone(similarrepliesdiv, myimg, result, reply)
	    	}
	    	$(similarreplyinput).val("")
	    	$(similarreplyinput).focus()
	    },
	    error: function( xhr, result, obj ) {
	    	$(similarreplyinput).attr("placeholder","服务端错误，请按要求输入重试")
	    }
	  })
}
function similarreplyone(div, myimg, replyid, reply) {
	$(div).append("<div><img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='"+myimg+"'/><span class='similarreplyspan' id='"+replyid+"'>" + reply + "</span></div>");
}
function similarreplyauto() {
	if(replyCount >= replySize) {
		clearInterval(timeOut)
	} else {
		var reply = replyArray[replyCount]
		replyCount = replyCount + 1
		var one = $("<div class='form-group similarreplydiv'><div class='form-group similarreplyreply'>" + replyCount + ". " + reply.reply+"</div><div class='form-group similarrepliesdiv'></div><input type='text' class='input btn-xs similarreplyinput' placeholder='请输入同义句' size='30'><button type='button' data-replyid='"+reply.replyid+"' data-which='similarreply' class='btn btn-xs similarreplybtn' onclick='similarreply(this)'>发送</button></div>")
		$("#similarreply").append(one)
		var div = one.children(".similarrepliesdiv")[0]
		jQuery.ajax({
		    url: "/wx.http?r=" + Math.random(),
		    data: {
			    action : "similarreplies",
			    random : randnum,
			    ticket : ticket,
			    state : state,
			    replyid : reply.replyid
		    },
		    success: function( result ) {
		      if("fail" == result) {
		    	  console.log("fail to load similar replies")
		      } else {
		    	  var items = JSON.parse(result)
		    	  var itemsize = items.length
		    	  for(var k=0;k < itemsize;k++) {
		    		  $(div).append("<div>   |-" + items[k] + "</div>");
		    	  }
		      }
		    },
		    error: function( xhr, result, obj ) {
		      console.log("[lijiaming] similar replies err: " + result)
		    }
		})
	}
}
jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "collect",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to access_token: ' + result)
        $('#inviteTitle').text('素朴网联!')
        oauth2()
      } else {
        $('#inviteTitle').text('素朴网联')
        
        var collect = JSON.parse(result)
        var arr = collect.array
        var length = arr.length
        for (var k = 0; k < length; k++) {
        	$("#crewimg").append("<img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='" + arr[k] + "'/>");
        }
        
        var myimg = collect.userimg
        var myname = collect.username
        $("#business > .boder_v1").append("<div class='form-group text-center'><input id='replyquiz' type='text' class='input btn-xs' data-quizid='"+state+"' placeholder='请按要求输入回复内容' size='30'><button id='replyquizbtn' type='button' class='btn btn-xs' data-quizid='"+state+"' onclick='replyquiz(this)'>回复</button></div>")
        $("#business").append("<input id='userimg' type='text' class='sr-only hidden' value='"+collect.userimg+"'/>")
        $("#business").append("<input id='username' type='text' class='sr-only hidden' value='"+collect.username+"'/>")
        $("#replyquiz").on('keypress', function(e) {
			var keycode = e.keyCode;
			if(keycode == '13') {
				$("#replyquizbtn").click()
			}
		});
        $("#business").append("<li class='boder_v1'><div id='clientreply' class='form-group client-reply'><strong>你输入的数据显示在这里：</strong></div></li>")
        $("#business").append("<li class='boder_v1'><div id='similarreply' class='form-group similar-reply'><strong>请写出每一句的同义句：</strong></div></li>")
        replyArray = collect.replyinfo
        replySize = replyArray.length
        replyCount = 0
        timeOut = setInterval("similarreplyauto()",250)
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      $('#inviteTitle').text('!素朴网联')
      oauth2()
    }
})
