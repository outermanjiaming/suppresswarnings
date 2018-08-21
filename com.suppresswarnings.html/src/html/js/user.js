jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "user",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to user: ' + result)
        $('#inviteTitle').text('个人中心!')
      } else {
        $('#inviteTitle').text('个人中心')
        
        var user = JSON.parse(result)
        $("#ownerimg").html("<img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='" + user.ownerimg + "'/>")
        $("#ownername").html(user.ownername)
        var arr = user.array
        var length = arr.length
        var crewimg = $("#crewimg")
        for (var k = 0; k < length; k++) {
        	crewimg.append("<img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='" + arr[k] + "'/>");
        }
        
        var datas = user.datas
        var size = datas.length
        var mydatas = $("#mydatas")
        for (var k = 0; k < size; k++) {
        	var map = datas[k]
        	var quizId = map.quizId
        	var quiz   = map.quiz
        	var quizState = map.quizState
        	var clazz = "btn-info"
        	var btn = "可出售"
        	if(quizState == 1) {
        		clazz = "btn-primary"
        		btn = "正在出售"
        	} else if(quizState == 2) {
        		clazz = "btn-warning"
        		btn = "已售"
        	}
        	mydatas.append("<div class='mydata' id='" + quizId + "' data-id='" + quizId + "'><button class='btn btn-xs " + clazz + "'>" + btn + "</button><a target='_black' href='/" + quizId + ".html'>" + quiz + "</a></div>");
        }
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] user err: " + result)
      $('#inviteTitle').text('!个人中心')
    }
  })