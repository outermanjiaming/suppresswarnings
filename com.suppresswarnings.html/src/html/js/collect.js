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
        
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      $('#inviteTitle').text('!素朴网联')
      oauth2()
    }
  })
function hideme(obj) {
	obj.hide()
}
function addon(){
	 $("#business").append("<li class='boder_v1 addon' onclick='hideme(this)'><span>addon</span></li>")
}
$(document).ready(function(){
  $(".replyinput").click(function(){
    $("p").slideToggle();
  });
  $(".boder_v1").click(function(){
	  $("#business").append("<li class='boder_v1' onclick='addon()'><span>test</span></li>")
  })
});