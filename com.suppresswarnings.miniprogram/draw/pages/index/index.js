// pages/canvas/canvas.js
Page({
  data: {
    times : 1,
    whoami : "wx.request.result"
  },
  canvasFrame(){
    wx.navigateTo({
      url: '/pages/canvas/canvas'
    })
  },
  clickName() {
    var that = this;
    wx.request({
      url: 'https://suppresswarnings.com/wx.http?action=miniprogram',
      data :{
        t: that.data.times
      },
      success: function (res) {
        console.log(res.data)
        that.setData({ whoami: res.data });
      },
      fail: function (res) {
        console.log(res.data)
        that.setData({ whoami: that.data.whoami + '-fail-' + res });
      },
      complete: function (res) {
        console.log(res.data)
        that.data.times ++;
        that.setData({ whoami: that.data.whoami + '-complete-' + that.data.times});
      }
    })
    
  },
  onLoad: function (options) {
    var that = this;
    that.sys();
    console.log("hello world");
    const ctx = wx.createCanvasContext('canvas');

    this.lines(ctx, 320, 320, 20, 20);
    ctx.stroke();

    ctx.setFillStyle('red');
    ctx.fillRect(20, 120, 50, 50);
    
    ctx.setFillStyle('green');
    ctx.fillRect(80, 120, 150, 50);

    ctx.setFillStyle('blue');
    ctx.fillRect(240, 120, 30, 50);

    ctx.setFillStyle('yellow');
    ctx.fillRect(20, 180, 250, 50);

    ctx.setFillStyle('pink');
    ctx.fillRect(280, 120, 20, 110);

    ctx.draw();

  },
  lines: function(ctx, xmax, ymax,dx, dy) {
    var startx = 0;
    var starty = 0;
    while(true) {
      startx = 0;
      starty += dy;
      if (starty > ymax) {
        break;
      }
      ctx.setStrokeStyle("black");
      ctx.setLineWidth(1);
      ctx.moveTo(startx, starty);
      ctx.lineTo(xmax, starty);
    }

    while (true) {
      starty = 0;
      startx += dx;
      if (startx > xmax) {
        break;
      }
      ctx.setStrokeStyle("black");
      ctx.setLineWidth(1);
      ctx.moveTo(startx, starty);
      ctx.lineTo(startx, ymax);
    }
    startx += dx;
    if (startx > xmax) {


    }
  },
  sys: function () {
    var that = this;
    wx.getSystemInfo({
      success: function (res) {
        that.setData({
          windowW: res.windowWidth,
          windowH: res.windowHeight
        })
      },
    })
  }
});