
function addone(cart) {
	var goods = cart.goods
	var rate = 0.01
	var price = rate * parseFloat(goods.pricecent)
	var div = '<div class="inner">' +
        '<div class="item_img">' + 
    '<a href="/detail.html?goodsid=' +goods.goodsid+ '">' + 
         '<img src="' +goods.image+ '" title="' +goods.title+ '">' + 
    '</a>' + 
   '</div>' +
   '<div class="goods_desc">' +
     '<dl>' +
       '<dt><a href="/detail.html?goodsid=' +goods.goodsid+ '">' +goods.title+ '</a></dt>' +
     '</dl>' +
     '<div class="price"><span>¥' +price+ '</span><em class="goods_numx">x' +cart.count+ '</em> </div>' +
   '</div>' +
   '<div class="num">' +
     '<div class="qiehuan">' +
       '<div class="xm-input-number"  data-cartid="' +cart.cartid+ '" >' + 
         '<a href="javascript:;" onclick="subnum(this)" class="input-sub active"></a>' +
         '<input type="text" onkeydown="if(event.keyCode == 13) event.returnValue = false" value="1" class="input-num" onchange="updatenum(this);">' +
         '<a href="javascript:;" onclick="addnum(this)" class="input-add"></a>' + 
        '</div>' +
     '</div>' +
     '<div class="delete">' +
       '<a href="javascript:;" onclick="removeitem(this);"> 删除 </a>' + 
     '</div>'+
   '</div>'+
 '</div>'
    $(".item-list").append(div)
}

jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "daigou",
	    todo : "mycarts",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to access_token: ' + result)
        index()
      } else {
        var cartslist = JSON.parse(result)
        var length = cartslist.length
        for (var k = 0; k < length; k++) {
        	var cart = cartslist[k]
        	addone(cart)
        }
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      index()
    }
})
