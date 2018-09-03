
function addone() {
	var li = '<li>' +
   '<div class="item">' +
    '<div class="goods_images">' +
      '<a href="/detail.html?agentid=who&code=what&goodsid=which">' +
        '<img src="http://xinniuguoji.com/images/goods/20180802/eed7ca5854e0df17.png" style="width:100%;" alt="测试动态">' +
      '</a>' +
    '</div>' +
    '<dl>' +
          '<dt><a href="/detail.html?agentid=who&code=what&goodsid=which">测试动态</a></dt>' +
	  	  '<dd style="font-size:12px;">[新西兰仓]</dd>' + 
	      '<dd><i>￥350.63</i></dd>' + 
	      '<dd><a class="rbtn mini-addcart" href="javascript:;" data-agentid="who" data-goodsid="which"> 加入购物车 </a></dd>' +
    '</dl>' +
  '</div>' +
'</li>'
  $(".single_item").append(li)
}

addone()