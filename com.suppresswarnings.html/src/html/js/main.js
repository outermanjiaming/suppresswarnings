var stopTime = 100;
$(document).ready(function(e) {
	_initLogoSvg();
	_animate();
	
	$("#like").click(function(){
	    console.log(12345);
	});
});
function _initLogoSvg() {
	//初始化小树
    Snap.load("img/xiaoshu.svg",
    function(svg) {
        this.appendChild(svg.node);
        var Svg = Snap("#xiaoshu");
        Svg.attr({
            width: "100%",
			height: "300",
			viewBox:"0,0,650,400"
        });
		Svg.selectAll("#tudi path,#shugan path").forEach(function(e, index) {
			e.attr({"opacity":0});
		});
		Svg.selectAll("#yezi path").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate(0,0)"});
		});
		Svg.selectAll("#liketext").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate(0,0)"});
		});
		Svg.selectAll("#githubtext").forEach(function(e, index) {
			e.attr({"transform":"sclae(0) translate(200,0)"});
		});
    },
    document.getElementById("xiaoshuSvg"));
    
    
    Snap.load("img/logo.svg",
    function(svg) {
        this.appendChild(svg.node);
    },
    document.getElementById("logo"));
}
function _animate(){
	Snap.load("img/xiaoshu.svg",
    function(svg) {
		pathMove("xiaoshu","tudi",800);
		setTimeout(function(){
			pathMove("xiaoshu","shugan",1000);
			setTimeout(function(){
				_mianMove("xiaoshu","yezi",500);
				setTimeout(function(){
				  _textMove();
				},800);
			},1000)
		},800);
	});
}
function _textMove(){
	var svg = Snap("#xiaoshu");
    var like = svg.select("#liketext");
	like.animate({"transform":"sclae(1) translate(200,180)"},1000,mina.backout);
	
	var github = svg.select("#githubtext");
	github.animate({"transform":"sclae(1) translate(100,180)"},1000,mina.backout);
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