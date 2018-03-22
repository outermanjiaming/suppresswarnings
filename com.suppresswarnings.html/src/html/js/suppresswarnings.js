  var host = "http://suppresswarnings.com/"
  var ids = ["ul","u","ur","mr","dr","d","dl","ml"]
  var index = 0
  var forever = 10000
  var limited = 10000
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
    }
    index++;
  }
  function gethtml(uri, container) {
    
    $.ajax({
	  url: uri,
	  data: {
	    zipcode: 97201
	  },
	  success: function( result ) {
	    $( container ).html( "<pre>" + result + "</pre>" );
	  }
	});
  }
  $(document).ready(function(){
    green();
    
    $(".navbar-brand").click(function(){
      if(limited <= 0) {
        index = 0
        limited = forever
        green()
      } else {
        limited = 0;
      }
    });
  }); 