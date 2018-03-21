  var ids = ["ul","u","ur","mr","dr","d","dl","ml"]
  var index = 0
  var forever = 10000
  function red(){
    $("#"+"svg_"+ids[index%8]).css("stroke","pink");
    if(index<forever) {
      setTimeout("green()", 100);
    } else {
      $("#" + "svg_"+ids[index%8]).css("stroke","#5cb85c");
    }
  }
  function green(){
    $("#" + "svg_"+ids[index%8]).css("stroke","#5cb85c");
    if(index<forever) {
      setTimeout("red()", 100);
    }
    index++;
  }
  
  $(document).ready(function(){
    green();
  }); 