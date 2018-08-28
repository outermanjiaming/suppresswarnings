
function replyquiz(obj) {
	var quizId = $(obj).data("quizId")
	var reply = $("#replyquiz").val()
	
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
    		$("#replyquiz").val("提交失败，稍后重试")
    	} else {
    		$("#replyquiz").val("提交成功，可以继续提交")
    	}
    	
    },
    error: function( xhr, result, obj ) {
    	$("#replyquiz").val("服务端错误，请按要求输入重试")
    }
  })
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
        $("#business").append("<div class='form-group text-center'><input id='replyquiz' type='text' class='input btn-xs' placeholder='请按要求输入回复内容' size='35'><button type='button' class='btn btn-xs' data-quizId='"+state+"' onclick='replyquiz(this)'>回复</button></div>")
        $("#business").append("<input id='userimg' type='text' class='sr-only hidden' value='"+collect.userimg+"'/>")
        $("#business").append("<input id='username' type='text' class='sr-only hidden' value='"+collect.username+"'/>")
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      $('#inviteTitle').text('!素朴网联')
      oauth2()
    }
})
