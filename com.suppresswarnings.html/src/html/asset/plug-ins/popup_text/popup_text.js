function hideActionSheet()
{
	var mask = $('#actionSheet_mask');
	var weuiActionsheet = $('#weui_actionsheet');	
	
	weuiActionsheet.removeClass('weui_actionsheet_toggle');
	mask.removeClass('weui_fade_toggle');
	weuiActionsheet.on('transitionend', function () {
		mask.hide();
	}).on('webkitTransitionEnd', function () {
		mask.hide();
	})
}


function showActionSheet(id)
{
	var mask = $('#actionSheet_mask');
	var weuiActionsheet = $('#weui_actionsheet');
	
	weuiActionsheet.addClass('weui_actionsheet_toggle');
	mask.show().addClass('weui_fade_toggle').click(function () {
		hideActionSheet();
	});
	$('#actionsheet_cancel').click(function () {
		hideActionSheet();
	});
	weuiActionsheet.unbind('transitionend').unbind('webkitTransitionEnd');
	
	$('#weui_actionsheet button')[0].setAttribute("val", id);
}