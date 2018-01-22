var cmd = function() {
	var r = Math.random();
	var jqxhr = $.ajax( "example.php" )
	  .done(function() {
	    alert( "success" );
	  })
	  .fail(function() {
	    console.log( "error" + r );
	  })
	  .always(function() {
	    $("#msg").append("<li id="+r+"> Successful Request!</li>");
	  });
}

$(document).ajaxSuccess(function(event, request, settings) {
	$("#msg").append("<p>Successful Request!</p>");
});
$(document).ready(function() {
	$("button").click(function() {
		$(this).addClass('btn-success');
		cmd();
	});
});