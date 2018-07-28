  var host = "http://suppresswarnings.com/"
  var ids = ["ul","u","ur","mr","dr","d","dl","ml"]
  var index = 0
  var forever = 10000
  var limited = 10000
  var randnum = Math.round(Math.random()*10000)
  var ticket
  function red(){
    $("#"+"svg_"+ids[index%8]).css("stroke","pink");
    if(index<limited) {
      setTimeout("green()", 100);
    } else {
      $("#" + "svg_"+ids[index%8]).css("stroke","#5cb85c");
      $("#"+"svg_ur").css("stroke","pink");
    }
  }
  function green(){
    $("#" + "svg_"+ids[index%8]).css("stroke","#5cb85c");
    if(index<limited) {
      setTimeout("red()", 100);
    } else {
      $("#"+"svg_ur").css("stroke","pink");
    }
    index++;
  }
  function gethtml(uri, container) {
    
    $.ajax({
	  url: uri,
	  data: {
	    random: randnum
	  },
	  success: function( result ) {
	    $( container ).html(result);
	  },
	  error: function( xhr, result, obj ) {
	    $( container ).html( "<span>天哪，不见了</span><br/>" + result);
	  }
	});
  }
  $(document).ready(function(){
    green();
    var main =  $("#main")
    
    $(".navbar-brand").click(function(){
      if(limited <= 0) {
        index = 0
        limited = forever
        green()
      } else {
        limited = 0;
      }
    });
    
    $("li.business").click(function(){
        gethtml("business.html", main)
    });
    
    $("li.user").click(function(){
        gethtml("card.html", main)
        $.ajax({
      	  url: "/wx.http?r=" + Math.random(),
      	  data: {
      		action: "login",
      		random: randnum
      	  },
      	  success: function( result ) {
      	    $( "#qrcodeimg" ).attr("src", result.ticket );
      	  },
      	  error: function( xhr, result, obj ) {
      	    console.log("[lijiaming] err: " + result);
      	  }
      	});
    });
    
    $("li.aboutus").click(function(){
        gethtml("aboutus.html", main)
    });
    
    $("li.raspberrypi").click(function(){
      gethtml("walkthrough/raspberrypi.html", main)
    });
    $("a.navbar-brand").click(function(){
      gethtml("welcome.html", main)
    });
  }); 