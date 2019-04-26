var ItemUiHelper = 
{
	getItem:function(itemData, uname)
	{
		var item =
			'<li class="item">'
				+this.getPoLeft(itemData)
				+this.getPoRight(itemData, uname)
		   +'</li>'
		return item;		
	},
	
	
	
	getPoLeft: function(itemData)
	{
		
		return '<div class="po-left">'
		+'<img src="'+itemData.face+'">'
		+'</div>';		
	},
	
	
	
	getPoRight: function(itemData, uname)
	{
	    var likes = this.getLikes(itemData.likes.entries, uname);
	    var liked = 0;
	    if(likes.indexOf("color:black") != -1) {
	        liked = 1;
	    }
		return '<div class="po-right">'
				+this.getPoHd(itemData, liked)
				+'<div class="triangle"></div>'
				+'<div class="cmt-wrap">'
					+'<div class="likes"><img src="asset/images/l.png"><span id="likes_'+itemData.projectid+'">'+likes+'</span></div>'
					+'<div id="comments_'+itemData.projectid+'" class="cmt-list">'+this.getComments(itemData.comments.entries)+'</div>'
				+'</div>'
			  +'</div>';	
	},
	
	
	
	getPoHd: function(itemData, liked)
	{
	    var like = "";
	    if(liked == 0){
	        like = '<span id="like_'+itemData.projectid+'" onclick="ajaxAddLike(this.id)" style="padding-left:20px;" class="glyphicon glyphicon-heart-empty">点赞</span>';
	    } else {
	        like = '<span id="like_'+itemData.projectid+'" onclick="ajaxAddLike(this.id)" style="padding-left:20px;" class="glyphicon glyphicon-heart">点赞</span>'
	    }
		return '<div class="po-hd">'
				+'<p class="po-name"><span class="data-name">'+itemData.uname+'</span></p>'
				+'<div class="post">'
					+'<p>'+itemData.title+'</p>'
					+'<p>'+this.getImgs(itemData.pictures)+'</p>'
				+'</div>'
				+'<span id="invest_'+itemData.projectid+'" onclick="showinvestMask(this.id)" class="glyphicon glyphicon-usd">投资</span>'
				+'<span id="transmit_'+itemData.projectid+'" onclick="ajaxGetQrcode(this.id)" style="padding-left:30px;" class="glyphicon glyphicon-share">分享</span>'
				+'<span id="comment_'+itemData.projectid+'" onclick="showTextInput(this.id)" style="padding-left:20px;" class="glyphicon glyphicon-comment">留言</span>'
				+like
			+'</div>'
			+'<p class="sponsor" style="font-size: 12px;color: #576b95;border: 1px solid #ddd;border-radius: 3px;padding:2px 3px; background:#eee;"><span>赞助：'+itemData.sponsor+'分</span><span>目标：'+itemData.target+'赞</span><span>投资：'+itemData.invest+'分</span><span>点赞：'+itemData.liked+'个</span></p>';	
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


	
	getLikes: function(likeArr, uname)
	{
		var likes = "";
		if(likeArr == undefined) return likes;
		for(var i=0; i<likeArr.length; ++i)
		{
		    if(uname == likeArr[i].key) {
		        likes += '<font class="myself" style="color:black">' + likeArr[i].key+'，</font>';
		    } else {
		        likes += likeArr[i].key+'，';
		    }
			
		}
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

