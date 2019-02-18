(function(exports) {
	var KeyBoard = function(input,n,options) {
		var body = document.getElementsByTagName('body')[0];
		var DIV_ID = options && options.divId || '__w_l_h_v_c_z_e_r_o_divid';
		if (document.getElementById(DIV_ID)) {
			body.removeChild(document.getElementById(DIV_ID));
		}
		this.input = input;
		this.el = document.createElement('div');
		var self = this;
		var zIndex = options && options.zIndex || 1000;
		var width = options && options.width || '100%';
		var height = options && options.height || '200px';
		var paddingTop = options && options.paddingTop || '5px';
		var paddingBottom = options && options.paddingBottom || '5px';
		var fontSize = options && options.fontSize || '15px';
		var border = options && options.borderTop || 'solid 1px #ccc;';
		var backgroundColor = options && options.backgroundColor || '#dddddd';
		var TABLE_ID = options && options.table_id || 'table_0909099';
		var mobile = typeof orientation !== 'undefined';
		this.el.id = DIV_ID;
		this.el.style.position = 'fixed';
		this.el.style.left = 0;
		this.el.style.right = 0;
		//this.el.style.bottom = 0;
		this.el.style.zIndex = zIndex;
		this.el.style.width = width;
		this.el.style.height = height;
		this.el.style.borderTop = border;
		this.el.style.backgroundColor = backgroundColor;
		//样式
		var cssStr = '<style type="text/css">';
		// 用css控制键盘是否显示
		cssStr += '.nfs-keyboard-header{height:50px;background-color:#eee;font-size: 18px;color: #888;}';
		cssStr += '.nfs-keyboard-body{height:200px;background-color:#eee;}';
		cssStr += '.opacityOut{display:none}';
		cssStr += '.a-bounceinB{bottom:0;border:0;-webkit-animation:bounceinB 0.3s ease-in backwards;-moz-animation:bounceinB 0.3s ease-in backwards;-ms-animation:bounceinB 0.3s ease-in backwards;animation:bounceinB 0.3s ease-in backwards;}';
		cssStr += '.a-bounceoutB{bottom:-250px;border:0;padding:0;-webkit-animation:bounceoutB 0.3s ease-out backwards;-moz-animation:bounceoutB 0.3s ease-out backwards;-ms-animation:bounceoutB 0.3s ease-out backwards;animation:bounceoutB 0.3s ease-out backwards;}';
		cssStr += '@-webkit-keyframes bounceoutB{0%{opacity:1;-webkit-transform:translateY(-250px);}100%{opacity:0;-webkit-transform:translateY(0);}}';
		cssStr += '@-moz-keyframes bounceoutB{0%{opacity:1;-webkit-transform:translateY(-250px);}100%{opacity:0;-webkit-transform:translateY(0);}}';
		cssStr += '@-ms-keyframes bounceoutB{0%{opacity:1;-webkit-transform:translateY(-250px);}100%{opacity:0;-webkit-transform:translateY(0);}}';
		cssStr += '@keyframes bounceoutB{0%{opacity:1;-webkit-transform:translateY(-250px);}100%{opacity:0;-webkit-transform:translateY(0);}}';
		cssStr += '@-webkit-keyframes bounceinB{0%{opacity:0;-webkit-transform:translateY(250px);}100%{opacity:1;-webkit-transform:translateY(0);}}';
		cssStr += '@-moz-keyframes bounceinB{0%{opacity:0;-webkit-transform:translateY(250px);}100%{opacity:1;-webkit-transform:translateY(0);}}';
		cssStr += '@-ms-keyframes bounceinB{0%{opacity:0;-webkit-transform:translateY(250px);}100%{opacity:1;-webkit-transform:translateY(0);}}';
		cssStr += '@keyframes bounceinB{0%{opacity:0;-webkit-transform:translateY(250px);}100%{opacity:1;-webkit-transform:translateY(0);}}';
		//table样式
		cssStr += '#' + TABLE_ID + '{font-size:18px;text-align:center;width:100%;height:200px;border-top:1px solid #CECDCE;background-color:#FFF;}';
		cssStr += '#' + TABLE_ID + ' td{width:33%;border:1px solid #ddd;border-right:0;border-top:0;}';
		if (!mobile) {
			cssStr += '#' + TABLE_ID + ' td:hover{background-color:#1FB9FF;color:#FFF;}';
		}
		cssStr += '</style>';
		//background
		var back = '<div id="background" style="position: fixed;z-index:-1001;top: 0;right: 0;bottom: 0;left: 0;background-color: rgba(0,0,0,.3);font-size:0;">123</div>'
		//nfs-keyboard-header
		var header = '<div class="nfs-keyboard-header">';
		//Button
		var btnCompleteStr = '<div style="width:15%;height:32px;background-color:#1FB9FF;';
		btnCompleteStr += 'float:right;margin-right:2%;text-align:center;color:#fff;';
		btnCompleteStr += 'line-height:32px;font-size: 14px;border-radius:3px;margin-top:8px;;cursor:pointer;">完成</div>';
		//Button
		var btnCancelStr = '<div style="width:15%;height:32px;background-color:#D3D9DF;';
		btnCancelStr += 'float:left;margin-left:2%;text-align:center;color:#000;';
		btnCancelStr += 'line-height:32px;font-size: 14px;border-radius:3px;margin-top:8px;cursor:pointer;">取消</div>';
		//input
		var inputStr = '<input type="text" id="_input" value="" readonly="readonly" placeholder="" unselectable="on" onfocus="this.blur()"';
		inputStr += ' style="width:50%;height:28px;float:left;margin-left:8%;';
		inputStr += 'text-align:right;color:#000;border: 1px solid #ccc;';
		inputStr += 'line-height:28px;margin-top:10px;font-size:18px;"><div style="clear:both"></div></div>';
		//nfs-keyboard-body
		//table
		var tableStr = '<div class="nfs-keyboard-body">';
		tableStr += '	<table id="' + TABLE_ID + '" border="0" cellspacing="0" cellpadding="0">';
		tableStr += ' 	<tr><td>1</td><td>2</td><td>3</td></tr>';
		tableStr += ' 	<tr><td>4</td><td>5</td><td>6</td></tr>';
		tableStr += ' 	<tr><td>7</td><td>8</td><td>9</td></tr>';
		tableStr += '	 	<tr><td style="background-color:#D3D9DF;">.</td><td>0</td>';
		tableStr += '	 			<td style="background-color:#D3D9DF;">删除</td></tr>';
		tableStr += '</table>';
		tableStr += '</div>';
		//html的渲染
		this.el.innerHTML = cssStr + header + tableStr;
		
		//控制输入框中的格式
		function clearNoNum(obj) {
			obj.value = obj.value.replace(/[^\d.]/g, ""); //清除“数字”和“.”以外的字符
			obj.value = obj.value.replace(/^\./g, ""); //验证第一个字符是数字而不是.
			obj.value = obj.value.replace(/\.{2,}/g, "."); //只保留第一个. 清除多余的.
			obj.value = obj.value.replace(".", "$#$").replace(/\./g, "").replace("$#$", ".");
			if(obj.value.indexOf(".") < 0 && obj.value !=""){//以上已经过滤，此处控制的是如果没有小数点，首位不能为类似于 01、02的金额
	        	obj.value= parseFloat(obj.value); 
	   		}
			if(obj.value.indexOf(".") >= 0){//判断是否有小数点
				if(obj.value.split(".")[1].length > n){//控制只能输入小数点后2位
					obj.value = obj.value.substr(0, obj.value.length - 1);
				}
			}
		}
	
		function addEvent(e) {
			var ev = e || window.event;
			var clickEl = ev.element || ev.target;
			var value = clickEl.textContent || clickEl.innerText;
			if (clickEl.tagName.toLocaleLowerCase() === 'td' && value !== "删除") {
				if (input) {
					if (clickEl.tagName.toLocaleLowerCase() === 'td' && value === ".") {
						if(n <= 0){
							input.value = input.value;
						}else{
							input.value += value;
							clearNoNum(input);
						}
					}else{
						input.value += value;
						clearNoNum(input);
					}
				}
			} else if (clickEl.tagName.toLocaleLowerCase() === 'td' && value === "删除") {
				var num = input.value;
				if (num) {
					var newNum = num.substr(0, num.length - 1);
					input.value = newNum;
				}
			}
			input.fireEvent("onchange");
		}
		
		if (mobile) {
			this.el.ontouchstart = addEvent;
		} else {
			this.el.onclick = addEvent;
		}
		body.appendChild(this.el);
		this.el.classList = 'a-bounceinB';
	}
	exports.KeyBoard = KeyBoard;
})(window);