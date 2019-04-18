var ItemUiHelper = 
{
	getItem:function(itemData)
	{
		var item =
			'<li class="item">'
				+this.getPoLeft(itemData)
				+this.getPoRight(itemData)
		   +'</li>'
		return item;		
	},
	
	
	
	getPoLeft: function(itemData)
	{
		
		return '<div class="po-left">'
		+'<img src="'+itemData.face+'">'
		+'</div>';		
	},
	
	
	
	getPoRight: function(itemData)
	{
		return '<div class="po-right">'
				+this.getPoHd(itemData)
				+'<div class="triangle"></div>'
				+'<div class="cmt-wrap">'
					+'<div class="likes"><img src="asset/images/l.png"><span id="likes_'+itemData.projectid+'">'+this.getLikes(itemData.likes.entries)+'</span></div>'
					+'<div id="comments_'+itemData.projectid+'" class="cmt-list">'+this.getComments(itemData.comments.entries)+'</div>'
				+'</div>'
			  +'</div>';	
	},
	
	
	
	getPoHd: function(itemData)
	{
		return '<div class="po-hd">'
				+'<p class="po-name"><span class="data-name">'+itemData.uname+'</span></p>'
				+'<div class="post">'
					+'<p>'+itemData.title+'</p>'
					+'<p>'+this.getImgs(itemData.pictures)+'</p>'
				+'</div>'
				+'<p class="time"><span id="invest_'+itemData.projectid+'" onclick="showinvestMask(this.id)" class="c-icon glyphicon glyphicon-usd">投资</span></p>'
				+'<span id="transmit_'+itemData.projectid+'" onclick="ajaxGetQrcode(this.id)" class="c-icon glyphicon glyphicon-share">分享</span>'
				+'<span id="comment_'+itemData.projectid+'" onclick="showTextInput(this.id)" class="c-icon glyphicon glyphicon-comment">留言</span>'
				+'<span id="like_'+itemData.projectid+'" onclick="ajaxAddLike(this.id)" class="c-icon glyphicon glyphicon-heart-empty">点赞</span>'
			+'</div>';	
	},
	

	
	getImgs: function(imgUrls)
	{
		var imgs = "";
		var imgUrlArr = new Array();
		imgUrlArr = imgUrls.split(",");
		for(var i=0; i<imgUrlArr.length; ++i)
		{
			imgs += '<img class="list-img" src="'+imgUrlArr[i]+'" style="height: 80px;">';
		}
		return imgs;
	},


	
	getTime: function(addtime)
	{
		return "刚刚";
	},


	
	getLikes: function(likeArr)
	{
		var likes = "";
		if(likeArr == undefined) return likes;
		for(var i=0; i<likeArr.length; ++i)
		{
			likes += likeArr[i].key+'，';
		}
		likes = likes.substr(0,likes.length-1);
		likes += "...";
		return likes;
	},
	
	
	
	getComments: function(conmentsArr)
	{
		var comments = "";
		if(conmentsArr == undefined) return comments;
		for(var i=0; i<conmentsArr.length; ++i)
		{
			comments += '<p><span>'+conmentsArr[i].key+'</span>：'+conmentsArr[i].value+'</p>';
		}
		return comments;
	}
};

