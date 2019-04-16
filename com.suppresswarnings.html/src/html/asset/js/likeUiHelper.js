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
					+'<div id="comments_'+itemData.projectid+'" class="cmt-list">'+this.getComments()+'</div>'
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
				+'<p class="time"><img id="invest_'+itemData.projectid+'" onclick="ajaxAddInvest(this.id)" class="c-icon" src="asset/images/c.png" style="padding-left:10px"></p>'
				+'<img id="transmit_'+itemData.projectid+'" onclick="ajaxAddTransmit(this.id)" class="c-icon" src="asset/images/c.png" style="padding-left:10px">'
				+'<img id="comment_'+itemData.projectid+'" onclick="showActionSheet(this.id)" class="c-icon" src="asset/images/c.png" style="padding-left:10px">'
				+'<img id="like_'+itemData.projectid+'" onclick="ajaxAddLike(this.id)" class="c-icon" src="asset/images/c.png" style="padding-left:10px">'
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
		for(var i=0; i<likeArr.length; ++i)
		{
			likes += likeArr[i].key+'，';
		}
		likes = likes.substr(0,likes.length-1);
		likes += "...";
		return likes;
	},
	
	
	
	getComments: function()
	{
		var comments = '<p><span class="data-name">万虎科技~贾素杰</span>：</span>澳洲大堡礁，这边刚好是夏季，挺适合避寒的。</p><p><span class="data-name">万虎科技~贾素杰</span>：</span>澳洲大堡礁，这边刚好是夏季，挺适合避寒的。</p>';
		return comments;
	}
};

