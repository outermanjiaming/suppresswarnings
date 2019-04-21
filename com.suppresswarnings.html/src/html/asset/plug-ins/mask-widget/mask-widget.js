function showTextInput(id)
{
	var mask = $('#text-input-mask');
	var weuiActionsheet = $('#text-input-content');
	
	weuiActionsheet.addClass('weui_actionsheet_toggle');
	mask.show().addClass('weui_fade_toggle').click(function () {
		hideTextInput();
	});
	$('#actionsheet_cancel').click(function () {
		hideTextInput();
	});
	weuiActionsheet.unbind('transitionend').unbind('webkitTransitionEnd');
	
	$('#text-input-content button')[0].setAttribute("val", id);
}



function hideTextInput()
{
	var mask = $('#text-input-mask');
	var weuiActionsheet = $('#text-input-content');	
	
	weuiActionsheet.removeClass('weui_actionsheet_toggle');
	mask.removeClass('weui_fade_toggle');
	weuiActionsheet.on('transitionend', function () {
		mask.hide();
	}).on('webkitTransitionEnd', function () {
		mask.hide();
	})
}



function showQrcodeMask()
{
	var mask = document.getElementById("qrcode-mask");
	mask.style.display="block"; 
}



function hideQrcodeMask()
{ 
	var mask = document.getElementById("qrcode-mask");
	mask.style.display="none"; 
} 




function showinvestMask(id)
{
	var mask = document.getElementById("invest-mask");
	mask.style.display="block"; 
	var projectid = id.replace("invest_", "");	
	document.getElementById("invest-projectid").value=projectid;
}



function hideinvestMask()
{ 
	var mask = document.getElementById("invest-mask");
	mask.style.display="none"; 
} 



function showGameMask(id)
{
	var mask = document.getElementById("game-mask");
	mask.style.display="block"; 
	document.getElementById("game-projectid").value=id;
}


function hideGameMask()
{ 
	var mask = document.getElementById("game-mask");
	mask.style.display="none"; 
} 