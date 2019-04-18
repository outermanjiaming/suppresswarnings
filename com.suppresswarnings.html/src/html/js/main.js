var tWidth = tHeight = 0;
var stopTime = 100;
$(function() {
	tWidth = $(window).width();
	tHeight = 321;
});
$(document).ready(function(e) {
	_initLogoSvg();
	_animate();
});
function _initLogoSvg() {
	//初始化小树
    Snap.load("img/xiaoshu.svg",
    function(svg) {
        this.appendChild(svg.node);
        var Svg = Snap("#xiaoshu");
        Svg.attr({
            width: tWidth,
			height:tHeight,
			viewBox:"0,0,667,323"
        });
		Svg.selectAll("#tudi path,#shugan path").forEach(function(e, index) {
			e.attr({"opacity":0});
		});
		Svg.selectAll("#yezi path").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate(0,0)"});
		});
    },
    document.getElementById("xiaoshuSvg"));
	
	//初始化大树
    Snap.load("img/dashu.svg",
    function(svg) {
        this.appendChild(svg.node);
        var Svg = Snap("#dashu");
        Svg.attr({
            width: tWidth,
			height:tHeight,
			viewBox:"0,0,667,323"
        });
		Svg.selectAll("#yezi path").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate(0,0)"});
		});
		Svg.selectAll("#font").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate("+tWidth/2+" "+tHeight/2+")"});
		});
		Svg.selectAll("#tudi path").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate(0,100)"});
		});
		Svg.selectAll("#yun path,#shugan path").forEach(function(e, index) {
			e.attr({"opacity":0});
		});
    },
    document.getElementById("dashuSvg"));
}
function _animate(){
	Snap.load("img/xiaoshu.svg",
    function(svg) {
		pathMove("xiaoshu","tudi",800);
		setTimeout(function(){
			pathMove("xiaoshu","shugan",1000);
			setTimeout(function(){
				_mianMove("xiaoshu","yezi",500);
			},1000)
		},800);
	});
}

function _mianMove(dom,id,time,stopTime){
	var sTime=(stopTime)?stopTime:80;
	var svg = Snap("#"+dom);
    var snpg = svg.select("#"+id+"").selectAll("path");
	snpg.forEach(function(e, index) {
		 setTimeout(function() {
			 e.animate({"transform":"sclae(1) translate(0,0)"},time,mina.backout);//圆圈圈动画
		 },sTime*index)
	})
	
}
function pathMove(dom,id,time) {
    var svg = Snap("#"+dom);
    var snpg = svg.select("#"+id+"").selectAll("path");
    snpg.forEach(function(e, index) {
        setTimeout(function() {
            var length = e.getTotalLength();
            e.attr({
                opacity: 1,
                strokeDashoffset: length,
                strokeDasharray: length
            });
            e.animate({
                "stroke-dashoffset": 0
            },
            time,"",function(){
				
			});
        },
        50*index)
    })
}