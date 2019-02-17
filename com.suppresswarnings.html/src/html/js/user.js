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
        $("#ownerimg").attr("src", user.ownerimg)
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
        mydatas.append("<div class='mydata'>"+size+"</div>")
      }
    },error: function( xhr, result, obj ) {
      console.log("[lijiaming] user err: " + result)
      $('#inviteTitle').text('!个人中心')
    }
  })
