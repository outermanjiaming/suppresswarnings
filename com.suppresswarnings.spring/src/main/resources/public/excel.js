

function download(index, obj) {
	console.log("download this obj")
	openemail()
	var myform = $(obj).parent()
	var email = $("#email").val()
	if(email.length < 1) return
	var tmpInput=$("<input type='hidden' name='email' value='"+email+"'/>");
    myform.append(tmpInput);
    
    var idxInput=$("<input type='hidden' name='index' value='"+index+"'/>");
    myform.append(idxInput);
    myform.submit();
}

function list(index, obj){
	console.log("lijiaming say list all")
	var txt = '<div class="row"><hr><div class="columns"><h3>'+
	(index + 1) + '. <span>' + obj.name+
	'</span></h3></div><div class="columns"><p class="desc">'+
	obj.desc+'</p></div> <div class="columns"><form action="/download?id='+
	index +
	'"><button type="submit" style="display:none;" name="index" id="index" value="'+index+'"></button><button type="button" onclick="download('+index+', this)" class="downloadbtn"> Download</button></form></div></div>'
	
	$("#downloads").append(txt)
}

function terms(index, obj){
	console.log("lijiaming say list all")
	var txt = '<div class="row"><hr><div class="columns"><h3><span>' + 
	obj.name+
	'</span></h3></div><div class="columns"><p class="desc">'+
	obj.desc+'</p></div> <div class="columns"></div></div>'
	
	$("#downloads").append(txt)
}


$.ajax({
    url: "listFiles",
    type: "POST",
    data: {},
    contentType: false,
    processData: false,
    success: function (data) {
    	var a = data.length
    	var i =0
    	for(;i<a-1;i++) {
    		var b = data[i]
    		list(i, b)
    	}
    	terms(i, data[i])
    },
    error: function () {
        alert("failed！");
    }
});